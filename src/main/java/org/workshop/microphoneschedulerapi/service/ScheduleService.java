package org.workshop.microphoneschedulerapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.workshop.microphoneschedulerapi.domain.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final SceneService sceneService;

    @Autowired
    public ScheduleService(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    /**
     * Generates optimized microphone schedule for a play
     * @param playTitle play identifier
     * @param micPool list of available microphones (ordered)
     * @return map sceneId -> (actor -> microphone)
     */
    public Map<Integer, Map<Actor, Microphone>> generateSchedule(String playTitle, List<Microphone> micPool) {
        List<Scene> allScenes = sceneService.getAllScenes(playTitle);
        Map<Actor, Microphone> actorCurrentMicMap = new HashMap<>();
        Map<Integer, Map<Actor, Microphone>> schedule = new LinkedHashMap<>();

        for (int i = 0; i < allScenes.size(); i++) {
            Scene scene = allScenes.get(i);
            List<Actor> actorsInScene = getActorsInScene(scene);
            Map<Actor, Microphone> sceneMicAssignment = new HashMap<>();

            for (Actor actor : actorsInScene) {
                Microphone bestMic = chooseBestMic(actor, scene, i, allScenes, actorCurrentMicMap, micPool, sceneMicAssignment, actorsInScene);
                sceneMicAssignment.put(actor, bestMic);

                // Update actor info
                if (bestMic != null && !bestMic.equals(actor.getCurrentMic())) {
                    actor.setMicSwitches(actor.getMicSwitches() + 1);
                }
                actor.setCurrentMic(bestMic);
                actorCurrentMicMap.put(actor, bestMic);
            }

            schedule.put(scene.getSceneId(), sceneMicAssignment);
        }

        return schedule;
    }

    // ---------------- Core Algorithm ----------------

    private Microphone chooseBestMic(Actor actor,
                                     Scene scene,
                                     int currentIndex,
                                     List<Scene> allScenes,
                                     Map<Actor, Microphone> actorCurrentMicMap,
                                     List<Microphone> micPool,
                                     Map<Actor, Microphone> sceneMicAssignment,
                                     List<Actor> actorsInScene) {

        // used mics already assigned in this scene (so far)
        Set<Microphone> usedMics = new HashSet<>(sceneMicAssignment.values());

        // available pool mics not yet used in this scene
        List<Microphone> availableMics = micPool == null
                ? Collections.emptyList()
                : micPool.stream().filter(m -> !usedMics.contains(m)).collect(Collectors.toList());

        // If actor already has mic, prefer keeping it (rule #1)
        if (actorAlreadyHasMic(actor) && micPool != null && micPool.contains(actor.getCurrentMic())) {
            return actor.getCurrentMic();
        }

        Microphone bestMic = null;
        int bestScore = Integer.MIN_VALUE;

        // If there are available mics, evaluate them
        for (Microphone mic : availableMics) {
            int score = 0;
            // Rule scores (tunable)
            score += actorAlreadyHasMic(actor) ? 100 : 0;                                   // keep current mic
            score += unusedMicAvailable(actorCurrentMicMap, mic) ? 50 : 0;                   // unused mic
            score += actorWasInactiveInPreviousScene(actor, allScenes, currentIndex) ? 20 : 0;
            score += actorWithFewestScenes(actor, allScenes) ? 15 : 0;
            score += actorWithMostTimeUntilNextScene(actor, allScenes, currentIndex) ? 10 : 0;
            score += actorChangingRoleNext(actor, allScenes, currentIndex) ? 5 : 0;
            score += actorWithLeastMicSwitches(actor, actorsInScene) ? 5 : 0;

            // Look-ahead: reduce score when this mic leads to conflicts in next N scenes
            score -= countConflictsInNextScenes(actor, mic, allScenes, currentIndex, actorCurrentMicMap, 2);

            if (score > bestScore) {
                bestScore = score;
                bestMic = mic;
            }
        }

        // If no available mic from pool, try to steal according to rules (take from someone not in this scene but has mic, etc.)
        if (bestMic == null) {
            // try to pick a mic currently assigned to some actor (not in this scene) using prioritization
            for (Map.Entry<Actor, Microphone> entry : actorCurrentMicMap.entrySet()) {
                Actor other = entry.getKey();
                Microphone mic = entry.getValue();
                if (mic == null) continue;
                // skip if mic already used in this scene
                if (sceneMicAssignment.containsValue(mic)) continue;

                int score = 0;
                // prefer stealing from actors inactive in previous scene
                score += actorWasInactiveInPreviousScene(other, allScenes, currentIndex) ? 10 : 0;
                // prefer stealing from actor with few scenes
                score += actorWithFewestScenes(other, allScenes) ? 8 : 0;
                // prefer from actor with many scenes until next (so they have buffer)
                score += actorWithMostTimeUntilNextScene(other, allScenes, currentIndex) ? 6 : 0;
                // prefer from actor with least mic switches to avoid overloading a single actor
                score += actorWithLeastMicSwitches(other, actorsInScene) ? 4 : 0;

                // choose candidate mic to take
                if (score > bestScore) {
                    bestScore = score;
                    bestMic = mic;
                }
            }
        }

        // Final fallback: any mic from pool or actorCurrentMicMap
        if (bestMic == null) {
            if (micPool != null && !micPool.isEmpty()) return micPool.get(0);
            // else any mic assigned in actorCurrentMicMap
            return actorCurrentMicMap.values().stream().findFirst().orElse(null);
        }

        return bestMic;
    }

    // ---------------- Rule Functions ----------------

    private boolean actorAlreadyHasMic(Actor actor) {
        return actor != null && actor.getCurrentMic() != null;
    }

    private boolean unusedMicAvailable(Map<Actor, Microphone> actorCurrentMicMap, Microphone mic) {
        if (mic == null) return false;
        return actorCurrentMicMap == null || !actorCurrentMicMap.containsValue(mic);
    }

    private boolean actorWasInactiveInPreviousScene(Actor actor, List<Scene> allScenes, int currentIndex) {
        if (currentIndex <= 0) return true; // no previous scene -> treat as inactive
        Scene prev = allScenes.get(currentIndex - 1);
        return getActorsInScene(prev).stream().noneMatch(a -> a.equals(actor));
    }

    /**
     * Count how many scenes the actor appears in (scans allScenes).
     */
    private int countScenesForActor(Actor actor, List<Scene> allScenes) {
        if (actor == null) return 0;
        int count = 0;
        for (Scene s : allScenes) {
            boolean appears = s.getScene_characters().stream()
                    .map(Scene_character::getPersonage)
                    .map(Personage::getActor)
                    .anyMatch(a -> a.equals(actor));
            if (appears) count++;
        }
        return count;
    }

    private boolean actorWithFewestScenes(Actor actor, List<Scene> allScenes) {
        List<Actor> uniqueActors = allScenes.stream()
                .flatMap(s -> s.getScene_characters().stream()
                        .map(Scene_character::getPersonage)
                        .map(Personage::getActor))
                .distinct()
                .collect(Collectors.toList());

        int actorCount = countScenesForActor(actor, allScenes);
        int min = uniqueActors.stream()
                .mapToInt(a -> countScenesForActor(a, allScenes))
                .min().orElse(actorCount);

        return actorCount <= min;
    }

    private boolean actorWithMostTimeUntilNextScene(Actor actor, List<Scene> allScenes, int currentIndex) {
        int nextDistance = Integer.MAX_VALUE;
        for (int i = currentIndex + 1; i < allScenes.size(); i++) {
            Scene s = allScenes.get(i);
            boolean appears = s.getScene_characters().stream()
                    .map(Scene_character::getPersonage)
                    .map(Personage::getActor)
                    .anyMatch(a -> a.equals(actor));
            if (appears) {
                nextDistance = i - currentIndex;
                break;
            }
        }
        // configurable: prefer when nextDistance is larger (>=2 as example)
        return nextDistance >= 2;
    }

    private boolean actorChangingRoleNext(Actor actor, List<Scene> allScenes, int currentIndex) {
        Personage current = currentCharacter(actor, allScenes.get(currentIndex));
        if (current == null) return false;

        for (int i = currentIndex + 1; i < allScenes.size(); i++) {
            for (Scene_character sc : allScenes.get(i).getScene_characters()) {
                Personage nextP = sc.getPersonage();
                Actor nextActor = nextP.getActor();
                if (actor.equals(nextActor) && !Objects.equals(nextP, current)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean actorWithLeastMicSwitches(Actor actor, List<Actor> contextActors) {
        if (contextActors == null || contextActors.isEmpty()) return true;
        int actorSwitches = actor.getMicSwitches();
        int min = contextActors.stream().mapToInt(Actor::getMicSwitches).min().orElse(actorSwitches);
        return actorSwitches <= min;
    }

    private int countConflictsInNextScenes(Actor actor, Microphone mic, List<Scene> allScenes,
                                           int currentIndex, Map<Actor, Microphone> actorCurrentMicMap, int lookAhead) {
        if (mic == null) return 0;
        int conflicts = 0;
        for (int i = 1; i <= lookAhead; i++) {
            int idx = currentIndex + i;
            if (idx >= allScenes.size()) break;
            Scene next = allScenes.get(idx);
            List<Actor> nextActors = getActorsInScene(next);
            for (Actor nextActor : nextActors) {
                Microphone assigned = actorCurrentMicMap.get(nextActor);
                if (assigned != null && assigned.equals(mic)) conflicts++;
            }
        }
        return conflicts;
    }

    // ---------------- Helper Functions ----------------

    private Personage currentCharacter(Actor actor, Scene scene) {
        return scene.getScene_characters().stream()
                .map(Scene_character::getPersonage)
                .filter(p -> p.getActor().equals(actor))
                .findFirst()
                .orElse(null);
    }

    private List<Actor> getActorsInScene(Scene scene) {
        return scene.getScene_characters().stream()
                .map(Scene_character::getPersonage)
                .map(Personage::getActor)
                .collect(Collectors.toList());
    }

    /**
     * Returns minimum number of mics required for the play
     */
    public int calculateMinimumMics(String playTitle) {
        List<Scene> allScenes = sceneService.getAllScenes(playTitle);
        return allScenes.stream()
                .mapToInt(s -> getActorsInScene(s).size())
                .max()
                .orElse(0);
    }
}
