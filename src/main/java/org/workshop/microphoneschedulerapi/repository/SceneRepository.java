package org.workshop.microphoneschedulerapi.repository;

import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.workshop.microphoneschedulerapi.domain.entity.*;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface  SceneRepository extends JpaRepository<Scene, Integer> {

    Optional<List<Scene>> findScenesByPlay(Play play);

    // Find all scenes belonging to a specific Play
    List<Scene> findAllByPlay(Play play);

    /*
    @Query(value="select u.characters from Scene u where u.sceneId = :sceneId")
    List<Personage> getCharactersBySceneId(int sceneId);
     */

    void deleteAllByPlay(Play play);

    @Query(value="select s.sceneId from Scene s where s.play.playName = :playName")
    List<Integer> findSceneIdsByPlayName(@NonNull String playName);

    //@Query(value="select s from Scene where s.sceneId = :sceneId")
    Scene findSceneBySceneId(int sceneId);

    @Modifying
    @Query("UPDATE Scene s SET s.sceneName = :newSceneName, s.actNumber = :newAct, s.sceneNumber = :newSceneNumber WHERE s.sceneId = :id")
    void updateScene(int id, String newSceneName, int newAct, int newSceneNumber);



    // Additional queries for scheduling rules


    // Get all actors in a given scene
    @Query("""
       SELECT p.actor
       FROM Scene_character sc
       JOIN sc.personage p
       WHERE sc.scene.sceneId = :sceneId
       """)
    List<Actor> findActorsBySceneId(@Param("sceneId") int sceneId);


    // Get all scenes for a given actor
    @Query("""
       SELECT sc.scene
       FROM Scene_character sc
       WHERE sc.personage.actor.actorId = :actorId
       """)
    List<Scene> findScenesByActorId(@Param("actorId") int actorId);

    // Get next scenes for an actor (look-ahead)
    @Query("""
       SELECT sc.scene
       FROM Scene_character sc
       WHERE sc.personage.actor.actorId = :actorId
         AND sc.scene.sceneId > :currentSceneId
       ORDER BY sc.scene.sceneId ASC
       """)
    List<Scene> findNextScenesForActor(@Param("actorId") int actorId,
                                       @Param("currentSceneId") int currentSceneId);

    // Count the number of scenes an actor participates in
    @Query("""
       SELECT COUNT(sc)
       FROM Scene_character sc
       WHERE sc.personage.actor.actorId = :actorId
       """)
    int countScenesForActor(@Param("actorId") int actorId);

    // Get Scene_character for an actor in a specific scene
    @Query("""
       SELECT sc
       FROM Scene_character sc
       WHERE sc.personage.actor.actorId = :actorId
         AND sc.scene.sceneId = :sceneId
       """)
    Scene_character findSceneCharacterForActorInScene(@Param("actorId") int actorId,
                                                      @Param("sceneId") int sceneId);



}


