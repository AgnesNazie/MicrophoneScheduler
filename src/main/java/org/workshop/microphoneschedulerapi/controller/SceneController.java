package org.workshop.microphoneschedulerapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.microphoneschedulerapi.domain.dto.SceneDTO;
import org.workshop.microphoneschedulerapi.domain.entity.Actor;
import org.workshop.microphoneschedulerapi.domain.entity.Microphone;
import org.workshop.microphoneschedulerapi.domain.entity.Scene;
import org.workshop.microphoneschedulerapi.service.SceneService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "", allowPrivateNetwork = "")
@RequestMapping("/api/v1/scene")
@RestController
public class SceneController {

    private final SceneService sceneService;

    @Autowired
    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    // ---------------------- CRUD for Scene ----------------------



    @PostMapping("/create")
    public ResponseEntity<Scene> createScene(@RequestBody SceneDTO form) {
        try {
            Scene created = sceneService.createScene(form);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Scene>> getAllScenes() {
        try {
            List<Scene> scenes = sceneService.getAllScenes();
            return ResponseEntity.ok(scenes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{sceneId}")
    public ResponseEntity<Scene> getSceneById(@PathVariable int sceneId) {
        try {
            Scene scene = sceneService.getSceneById(sceneId);
            return ResponseEntity.ok(scene);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{sceneId}")
    public ResponseEntity<Void> updateScene(@PathVariable int sceneId, @RequestBody SceneDTO form) {
        try {
            sceneService.updateScene(sceneId, form);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/{sceneId}")
    public ResponseEntity<Void> deleteScene(@PathVariable int sceneId) {
        try {
            sceneService.deleteScene(sceneId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ---------------------- Queries ----------------------

    @GetMapping("/byPlay/{playName}")
    public ResponseEntity<List<Scene>> getScenesByPlay(@PathVariable String playName) {
        try {
            List<Scene> scenes = sceneService.getScenesByPlay(playName);
            return ResponseEntity.ok(scenes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{sceneId}/actors")
    public ResponseEntity<List<Actor>> getActorsInScene(@PathVariable int sceneId) {
        try {
            List<Actor> actors = sceneService.getActorsInScene(sceneId);
            return ResponseEntity.ok(actors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{sceneId}/microphones")
    public ResponseEntity<List<Microphone>> getMicrophonesInScene(@PathVariable int sceneId) {
        try {
            List<Microphone> microphones = sceneService.getMicrophonesInScene(sceneId);
            return ResponseEntity.ok(microphones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/nextScenes/{actorId}/{currentSceneId}")
    public ResponseEntity<List<Scene>> getNextScenesForActor(
            @PathVariable int actorId,
            @PathVariable int currentSceneId) {
        try {
            List<Scene> nextScenes = sceneService.getNextScenesForActor(actorId, currentSceneId);
            return ResponseEntity.ok(nextScenes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ---------------------- Scene-Character Management ----------------------

    @PutMapping("/{sceneId}/addPersonage/{personageId}")
    public ResponseEntity<Void> addPersonageToScene(
            @PathVariable int sceneId,
            @PathVariable int personageId) {
        try {
            sceneService.addPersonageToScene(sceneId, personageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("already in scene")) {
                return ResponseEntity.status(409).build(); // conflict
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{sceneId}/removePersonage/{personageId}")
    public ResponseEntity<Void> removePersonageFromScene(
            @PathVariable int sceneId,
            @PathVariable int personageId) {
        try {
            sceneService.removePersonageFromScene(sceneId, personageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/assignMicrophone/{sceneCharacterId}/{microphoneId}")
    public ResponseEntity<Void> assignMicrophoneToSceneCharacter(
            @PathVariable Long sceneCharacterId,
            @PathVariable int microphoneId) {
        try {
            sceneService.assignMicrophone(sceneCharacterId, microphoneId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/removeMicrophone/{sceneCharacterId}")
    public ResponseEntity<Void> removeMicrophoneFromSceneCharacter(@PathVariable Long sceneCharacterId) {
        try {
            sceneService.removeMicrophone(sceneCharacterId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


