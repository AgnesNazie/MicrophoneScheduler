package org.workshop.microphoneschedulerapi.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.workshop.microphoneschedulerapi.domain.entity.*;

import java.util.List;

@Repository
@Transactional
public interface PersonageRepository extends JpaRepository<Personage, Integer> {

    // Get all personages in a specific scene
    List<Personage> findByScene(Scene scene);

    // Get a personage by actor assigned
    List<Personage> findByActor(Actor actor);

    // Get a personage by microphone assigned
    List<Personage> findByMicrophone(Microphone microphone);

    // Optional: Find a personage by scene and actor
    Personage findBySceneAndActor(Scene scene, Actor actor);

    // Optional: Find a personage by scene and microphone
    Personage findBySceneAndMicrophone(Scene scene, Microphone microphone);

    // Optional: Find all personages not assigned to any scene
    List<Personage> findBySceneIsNull();

    // Optional: Find all personages in a play (if you have a scene -> play relationship)
    List<Personage> findByScene_Play_PlayName(String playName);

}
