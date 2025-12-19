package org.workshop.microphoneschedulerapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.workshop.microphoneschedulerapi.domain.entity.Personage;
import org.workshop.microphoneschedulerapi.domain.entity.Play;
import org.workshop.microphoneschedulerapi.domain.entity.Scene;
import org.workshop.microphoneschedulerapi.domain.entity.Scene_character;

import java.util.List;

@Repository
public interface Scene_characterRepository extends JpaRepository<Scene_character, Long> {

    //Scene_character findScene_characterByscene_id(int sceneId);

    List<Scene_character> findScene_charactersByPersonage(Personage personage);

    boolean existsScene_charactersByPersonage(Personage personage);

    //  count scenes for the table
    int countByPersonage_PersonageId(int personageId);


    //@Query(value="select s.scene_character_id from Scene_character s where s.scene.play.playName = :playName")
    //List<Scene_character> findAllScene_charactersByPlay(Play play);

    //@Query(value="select s.scene_character_id from Scene_character s where s.scene.play.playName = :playName")
    //List<Integer> findAllScene_charactersIdByPlay(Play play);

}
