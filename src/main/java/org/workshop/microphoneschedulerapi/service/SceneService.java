package org.workshop.microphoneschedulerapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.workshop.microphoneschedulerapi.domain.dto.SceneDTO;
import org.workshop.microphoneschedulerapi.domain.entity.*;
import org.workshop.microphoneschedulerapi.repository.*;

import java.util.List;
import java.util.Objects;

@Service
public class SceneService {

    private SceneRepository sceneRepository;
    private PlayRepository playRepository;
    private PersonageRepository personageRepository;
    private final ActorRepository actorRepository;       // new
    private final MicrophoneRepository microphoneRepository;
    private final Scene_characterRepository scene_characterRepository;// new

    @Autowired
    public SceneService(SceneRepository sceneRepository, PlayRepository playRepository, PersonageRepository personageRepository,ActorRepository actorRepository,
                        MicrophoneRepository microphoneRepository,
                        Scene_characterRepository scene_characterRepository ) {
        this.sceneRepository = sceneRepository;
        this.playRepository = playRepository;
        this.personageRepository = personageRepository;
        this.actorRepository = actorRepository;
        this.microphoneRepository = microphoneRepository;
        this.scene_characterRepository = scene_characterRepository;
    }


    // ---------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------

    public Scene createScene(SceneDTO sceneDTO) {
        // 1. Fetch the Play using its name (String key)
        Play play = playRepository.findById(sceneDTO.getPlayName())
                .orElseThrow(() -> new RuntimeException("Play not found: " + sceneDTO.getPlayName()));

        // 2. Create a new Scene and set fields
        Scene scene = Scene.builder()
                .sceneName(sceneDTO.getSceneName())
                .actNumber(sceneDTO.getActNumber())
                .sceneNumber(sceneDTO.getSceneNumber())
                .play(play) // associate the Play
                .build();

        // 3. Save and return the Scene
        return sceneRepository.save(scene);
    }


    public List<Scene> getAllScenes() {
        return sceneRepository.findAll();
    }

    public List<Scene> getAllScenes(String playTitle) {
        Play play = playRepository.findById(playTitle)
                .orElseThrow(() -> new RuntimeException("Play not found"));
        return sceneRepository.findScenesByPlay(play)
                .orElseThrow(() -> new RuntimeException("No scenes found for this play"));
    }

    public Scene getSceneById(int sceneId) {
        return sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("Scene not found"));
    }

    public void updateScene(int sceneId, SceneDTO form) {
        Scene scene = getSceneById(sceneId);

        scene.setSceneName(form.getSceneName());
        scene.setActNumber(form.getActNumber());
        scene.setSceneNumber(form.getSceneNumber());

        sceneRepository.save(scene);
    }

    public void deleteScene(int sceneId) {
        Scene scene = getSceneById(sceneId);
        sceneRepository.delete(scene);
    }

    // ---------------------------------------------------------
    // Queries
    // ---------------------------------------------------------

    public List<Scene> getScenesByPlay(String playName) {
        Play play = playRepository.findById(playName)
                .orElseThrow(() -> new RuntimeException("Play not found"));

        return sceneRepository.findScenesByPlay(play)
                .orElseThrow(() -> new RuntimeException("No scenes found"));
    }

    public List<Actor> getActorsInScene(int sceneId) {
        return sceneRepository.findActorsBySceneId(sceneId);
    }

    public List<Microphone> getMicrophonesInScene(int sceneId) {
        Scene scene = getSceneById(sceneId);
        List<Personage> chars = personageRepository.findByScene(scene);

        return chars.stream()
                .map(Personage::getMicrophone)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Scene> getNextScenesForActor(int actorId, int currentSceneId) {
        return sceneRepository.findNextScenesForActor(actorId, currentSceneId);
    }

    // ---------------------------------------------------------
    // Scene–Character Management
    // ---------------------------------------------------------

    public void addPersonageToScene(int sceneId, int personageId) {
        Scene scene = getSceneById(sceneId);
        Personage personage = personageRepository.findById(personageId)
                .orElseThrow(() -> new RuntimeException("Personage not found"));

        Scene_character sc = new Scene_character();
        sc.setScene(scene);
        sc.setPersonage(personage);

        scene_characterRepository.save(sc);
    }

    @Transactional
    public void removePersonageFromScene(int sceneId, int personageId) {

        Scene scene = getSceneById(sceneId);

        Scene_character sc = scene.getScene_characters()
                .stream()
                .filter(x -> x.getPersonage().getPersonageId() == personageId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Personage not in this scene"));

        scene_characterRepository.delete(sc);
    }
    // ---------------------------------------------------------
    // Scene–Character Microphone Assignment
    // ---------------------------------------------------------

    public void assignMicrophone(Long sceneCharacterId, int microphoneId) {

        Scene_character sc = scene_characterRepository.findById(sceneCharacterId)
                .orElseThrow(() -> new RuntimeException("SceneCharacter not found" +  + sceneCharacterId));

        Microphone microphone = microphoneRepository.findById(microphoneId)
                .orElseThrow(() -> new RuntimeException("Microphone not found" + microphoneId));

        sc.setMicrophone(microphone);
        scene_characterRepository.save(sc);
    }

    public void removeMicrophone(Long sceneCharacterId) {
       Scene_character sc  = scene_characterRepository.findById(sceneCharacterId)
                .orElseThrow(() -> new RuntimeException("SceneCharacter not found" + sceneCharacterId));

        sc.setMicrophone(null);
        scene_characterRepository.save(sc);
    }
}