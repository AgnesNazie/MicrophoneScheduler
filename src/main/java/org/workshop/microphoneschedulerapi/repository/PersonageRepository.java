package org.workshop.microphoneschedulerapi.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.workshop.microphoneschedulerapi.domain.entity.*;

import java.util.List;

@Repository
@Transactional
public interface PersonageRepository extends JpaRepository<Personage, Integer> {

    // Get all personages in a specific scene
    @Query("SELECT sc.personage FROM Scene_character sc WHERE sc.scene = :scene")
    List<Personage> findByScene(@Param("scene") Scene scene);

    // Get a personage by actor assigned
    List<Personage> findByActor(Actor actor);

    // Get a personage by microphone assigned
    List<Personage> findByMicrophone(Microphone microphone);


    // Optional: Find a personage by scene and actor
    @Query("SELECT sc.personage FROM Scene_character sc WHERE sc.scene = :scene AND sc.personage.actor = :actor")
    Personage findBySceneAndActor(@Param("scene") Scene scene, @Param("actor") Actor actor);

    // Optional: Find a personage by scene and microphone (Scene_character microphone)
    @Query("SELECT sc.personage FROM Scene_character sc WHERE sc.scene = :scene AND sc.microphone = :microphone")
    Personage findBySceneAndMicrophone(@Param("scene") Scene scene, @Param("microphone") Microphone microphone);

    // Optional: Find all personages not assigned to any scene
    @Query("SELECT p FROM Personage p WHERE p.scene_characters IS EMPTY")
    List<Personage> findBySceneIsNull();

    // Optional: Find all personages in a play (via scene -> play relationship)
    @Query("SELECT sc.personage FROM Scene_character sc WHERE sc.scene.play.playName = :playName")
    List<Personage> findByScene_Play_PlayName(@Param("playName") String playName);

}
