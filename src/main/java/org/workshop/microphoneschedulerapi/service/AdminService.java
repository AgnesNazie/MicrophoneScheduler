package org.workshop.microphoneschedulerapi.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.workshop.microphoneschedulerapi.domain.dto.*;
import org.workshop.microphoneschedulerapi.domain.entity.*;
import org.workshop.microphoneschedulerapi.domain.model.PersonageCustom;
import org.workshop.microphoneschedulerapi.domain.model.ScenePersonageMicrophoneCustom;
import org.workshop.microphoneschedulerapi.domain.model.UserRole;
import org.workshop.microphoneschedulerapi.repository.*;

import javax.lang.model.element.Element;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private PlayRepository playRepository;
    private PersonageRepository personageRepository;
    private SceneRepository sceneRepository;
    private ActorRepository actorRepository;
    private Scene_characterRepository scene_characterRepository;
    private UserRepository userRepository;
    private MicrophoneRepository microphoneRepository;


    @Autowired
    public AdminService(PlayRepository playRepository, PersonageRepository personageRepository,
                        SceneRepository sceneRepository, ActorRepository actorRepository,
                        Scene_characterRepository scene_characterRepository, UserRepository userRepository,
                        MicrophoneRepository microphoneRepository) {
        this.playRepository = playRepository;
        this.personageRepository = personageRepository;
        this.sceneRepository = sceneRepository;
        this.actorRepository = actorRepository;
        this.scene_characterRepository = scene_characterRepository;
        this.userRepository = userRepository;
        this.microphoneRepository = microphoneRepository;
    }

    public List<Play> getAllPlays() {
        return playRepository.findAll();
    }

    public Play createPlay(Play play) {
        return playRepository.save(play);
    }


    /**
     * This method will delete play and all related scenes and scene_characters Character (personages) and
     * microphones used in play will still be in database in case they are used in other production.
     * @param playName
     */
    @Transactional
    public void deletePlay(String playName) {
        Play play = playRepository.getReferenceById(playName);
        List<Scene> scenesToBeRemoved = sceneRepository.findAllByPlay(play);
        for (Scene scene : scenesToBeRemoved) {
            // Remove all Scene_character entries for this scene
            scene.getScene_characters().clear();  // orphanRemoval = true will delete from DB automatically
        }

        // Delete all scenes
        sceneRepository.deleteAllByPlay(play);
        // Finally delete the play
        playRepository.delete(play);
    }

    public void updatePlay(String playName, LocalDate date, String description) {
        playRepository.updatePlay(playName, date, description);
    }

    public SceneCustomListDTO getAllScenesInPlay(String playName) {
        List<Scene> scenes = sceneRepository.findAllByPlay(playRepository.getReferenceById(playName));
        scenes.sort(Comparator
                .comparing(Scene::getActNumber)
                .thenComparing(Scene::getSceneNumber));

        SceneCustomListDTO sceneCustomListDTO = new SceneCustomListDTO();
        List<Scene> scenesInPlay = new ArrayList<>();
        for (Scene scene : scenes) {
            Scene newScene = Scene.builder()
                    .sceneId(scene.getSceneId())
                    .sceneName(scene.getSceneName())
                    .actNumber(scene.getActNumber())
                    .sceneNumber(scene.getSceneNumber())
                    .build();
            scenesInPlay.add(newScene);
        }
        sceneCustomListDTO.setScenes(scenesInPlay);
        return sceneCustomListDTO;
    }

    /*
    public void createScene(CreateSceneDTOForm form) {
        Scene newScene = Scene.builder()
                .play(playRepository.getReferenceById(form.playName()))
                .actNumber(form.actNumber())
                .sceneName(form.sceneName())
                .sceneNumber(form.sceneNumber())
                .build();
        sceneRepository.save(newScene);
    }
    */

    public void updateScene(UpdateSceneDTOForm form) throws Exception {

        Play play = sceneRepository.findSceneBySceneId(form.sceneId()).getPlay();
        List<Scene> scenes = sceneRepository.findAllByPlay(play);
        for(Scene scene : scenes) {
            if(scene.getSceneNumber() == form.sceneNumber() && scene.getActNumber() == form.actNumber()) {
                throw new Exception("Scene already exists");
            }
        }

        sceneRepository.updateScene(form.sceneId(), form.sceneName(), form.actNumber(), form.sceneNumber());
    }

    public void deleteScene(int sceneId) {
        List<Scene_character> sceneCharacters = scene_characterRepository.findAll();
        for (Scene_character scene_character : sceneCharacters) {
            if(scene_character.getScene().getSceneId() == sceneId) {
                scene_characterRepository.delete(scene_character);
            }
        }
        sceneRepository.deleteById(sceneId);
    }

    @Transactional
    public void addPersonageToScene(int sceneId, int personageId) throws Exception {
        Scene scene = sceneRepository.findSceneBySceneId(sceneId);
        Personage character = personageRepository.findById(personageId).orElseThrow(()
                -> new Exception("Personage not found"));

        // Check if character is used in other productions
        Play currentPlay = scene.getPlay();
        for (Scene_character sc : character.getScene_characters()) {
            Scene otherScene = sc.getScene();
            if (otherScene != null && !otherScene.getPlay().getPlayName().equals(currentPlay.getPlayName())) {
                throw new Exception("Character used in other production already");
            }
            // It's fine if the scene is from the same play
        }


        for(Scene_character scene_character :scene.getScene_characters() ) {

            // Prevent the same character from being added twice in the same scene
            if(scene_character.getPersonage().getPersonageId() == personageId) {
                throw new Exception("Character already in scene");
            }

            // Prevent the same actor from being in two roles in the same scene
            if(scene_character.getPersonage().getActor() != null && character.getActor() != null) {
                if(scene_character.getPersonage().getActor().getActorId() == character.getActor().getActorId()) {
                    throw new Exception("Actor already in scene");
                }
            }
        }

        // Create and add new Scene_character

        Scene_character scene_character = Scene_character.builder()
                .scene(scene)
                .personage(personageRepository.findById(personageId).orElseThrow())
                .build();

        scene.getScene_characters().add(scene_character);
        scene.setScene_characters(scene.getScene_characters());
        scene_characterRepository.save(scene_character);

        /*
        Personage personage = personageRepository.findById(personageId).orElseThrow();
        List<Scene_character> sceneCharacters2 = personage.getScene_characters();

        Scene_character scene_character2 = Scene_character.builder()
                .scene(scene)
                .personage(personageRepository.findById(personageId).orElseThrow())
                //.personage(personageRepository.getReferenceById(personageId))
                .build();

        sceneCharacters2.add(scene_character2);
        personage.setScene_characters(sceneCharacters2);
         */

    }

    @Transactional
    public void removePersonageFromScene(int sceneId, int personageId) {
        List<Scene_character> scene_characters = scene_characterRepository.findScene_charactersByPersonage(personageRepository.getReferenceById(personageId));
        for (Scene_character scene_character : scene_characters) {
            if(scene_character.getScene().getSceneId() == sceneId) {
                scene_characterRepository.delete(scene_character);
            }
        }
    }

    /*
    @Transactional
    public void assignActorToPersonage(int actorId, int personageId) {
        Personage personage = personageRepository.findById(personageId).orElseThrow();
        try {
            personage.setActor(actorRepository.findById(actorId).orElseThrow());
        } catch (Exception e) {
            personage.setActor(null);
        }
    }
    */

    public void createPersonage(CreatePersonageDTOForm form) {
        Personage newPersonage = Personage.builder()
                .personageName(form.personageName())
                .build();
        personageRepository.save(newPersonage);
    }

    @Transactional
    public void editPersonage(EditPersonageDTO form) {
        Personage personage = personageRepository.findById(form.personageId()).orElseThrow();
        personage.setPersonageName(form.personageName());
        personage.setActor(actorRepository.getReferenceById(form.actorId()));

        personageRepository.save(personage);
    }

    @Transactional
    public void deletePersonage(int personageId) {
        Personage personage = personageRepository.findById(personageId).orElseThrow();
        scene_characterRepository.findScene_charactersByPersonage(personage).forEach(scene_character -> {
            scene_characterRepository.delete(scene_character);
        });
        personageRepository.findById(personageId).orElseThrow().setScene_characters(null);
        personageRepository.findById(personageId).orElseThrow().setActor(null);
        Personage personage2 = personageRepository.findById(personageId).orElseThrow();

        personageRepository.delete(personage2);
    }

    public PersonageInDbCustomDTO getAllPersonages() {
        PersonageInDbCustomDTO personageInDbCustomDTO = new PersonageInDbCustomDTO();
        List<PersonageCustom> customList = new ArrayList<>();
        List<Personage> personages = personageRepository.findAll();
        for (Personage personage : personages) {

            // ALWAYS fetch from repository (not from entity)
            List<Scene_character> scRows =
                    scene_characterRepository.findScene_charactersByPersonage(personage);

            // Sort scenes correctly (Act -> Scene)
            scRows.sort(Comparator
                    .comparing((Scene_character sc) -> sc.getScene().getActNumber())
                    .thenComparing(sc -> sc.getScene().getSceneNumber()));

            // Build unique scene list
            List<Scene> customScenes = new ArrayList<>();
            Set<Integer> seen = new HashSet<>();

            for (Scene_character scene_character : scRows) {
                if (scene_character.getScene() == null) continue;
                int sid = scene_character.getScene().getSceneId();
                if (seen.add(sid)) {
                    Scene customScene = Scene.builder()
                            .sceneId(scene_character.getScene().getSceneId())
                            .actNumber(scene_character.getScene().getActNumber())
                            .sceneNumber(scene_character.getScene().getSceneNumber())
                            .sceneName(scene_character.getScene().getSceneName())
                            .build();
                    customScenes.add(customScene);
                }
            }

            // Get play name if personage appears in at least one scene
            String playName = scRows.isEmpty()
                    ? null
                    : scRows.get(0).getScene().getPlay().getPlayName();

            PersonageCustom custom = PersonageCustom.builder()
                    .personageId(personage.getPersonageId())
                    .personageName(personage.getPersonageName())
                    .actorName(personage.getActor() == null
                            ? null
                            : personage.getActor().getUser().getUsername())
                    .playName(playName)
                    .scenes(customScenes)
                    .build();

            customList.add(custom);
        }

        personageInDbCustomDTO.setPersonages(customList);
        return personageInDbCustomDTO;


    }

    /*
    public PersonageInDbCustomDTO getAllPersonagesInPlay(String playName) {
        PersonageInDbCustomDTO personageInDbCustomDTO = new PersonageInDbCustomDTO();
        List<PersonageCustom> customList = new ArrayList<>();
        PersonageInDbCustomDTO allPersonagesinDb = getAllPersonages();

        for (PersonageCustom personageCustom : allPersonagesinDb.getPersonages()) {
            if (personageCustom.getPlayName() != null && personageCustom.getPlayName().equals(playName)) {
                customList.add(personageCustom);
            }
        }
        personageInDbCustomDTO.setPersonages(customList);

        return personageInDbCustomDTO;
    }
    */

    /*
    public PersonageInDbCustomDTO getAllPersonagesInScene(int sceneId) {
        PersonageInDbCustomDTO personageInDbCustomDTO = new PersonageInDbCustomDTO();
        List<PersonageCustom> customList = new ArrayList<>();
        PersonageInDbCustomDTO allPersonagesinDb = getAllPersonages();

        for (PersonageCustom personageCustom : allPersonagesinDb.getPersonages()) {
            for (Scene scene : personageCustom.getScenes()) {
                if (scene.getSceneName() != null && scene.getSceneId() == sceneId) {
                    customList.add(personageCustom);
                }
            }
        }
        personageInDbCustomDTO.setPersonages(customList);

        return personageInDbCustomDTO;
    }
    */

    public MicrophoneScheduleSuggestedDTO suggestMicrophoneSchedule(String playName) {

        List<Scene> allScenesInPlay = sceneRepository.findAllByPlay(playRepository.getReferenceById(playName));

        List<Scene_character> scene_charactersInPlay = new ArrayList<>();
        for (Scene scene : allScenesInPlay) {
            for (Scene_character scene_character : scene.getScene_characters()) {
                if(scene_character.getPersonage() != null){
                    scene_charactersInPlay.add(scene_character);
                }
            }
        }

        List<ScenePersonageMicrophoneCustom> scenePersonageMicrophoneCustoms = new ArrayList<>();

        List<Integer> personageIds = scene_charactersInPlay.stream().map(p -> p.getPersonage().getPersonageId()).toList();
        List<Integer> distictPersonges = personageIds.stream().distinct().toList();
        int maxNeededMicrophonesInPlay = distictPersonges.size(); //When testing with larger Play's if this is exceeded all actors should have their own microphone.

        List<Integer> actNumbers = scene_charactersInPlay.stream().map(s -> s.getScene().getActNumber()).toList();
        List<Integer> distinctActNumbers = actNumbers.stream().distinct().toList();

        List<ScenePersonageMicrophoneCustom> customList = new ArrayList<>();
        int id= 1;

        for (Scene_character scene_character : scene_charactersInPlay) {
            if(scene_character.getPersonage().getActor() != null){
                ScenePersonageMicrophoneCustom scenePersonageMicrophoneCustom = ScenePersonageMicrophoneCustom.builder()
                        .id(id)
                        .scenePersonageId(scene_character.getScene_character_id())
                        .userId(scene_character.getPersonage().getActor().getUser().getUserId())
                        .userName(scene_character.getPersonage().getActor().getUser().getUsername())
                        .sceneId(scene_character.getScene().getSceneId())
                        .sceneName(scene_character.getScene().getSceneName())
                        .actNumber(scene_character.getScene().getActNumber())
                        .personageName(scene_character.getPersonage().getPersonageName())
                        .build();
                customList.add(scenePersonageMicrophoneCustom);
                id++;
            }
            else{
                ScenePersonageMicrophoneCustom scenePersonageMicrophoneCustom = ScenePersonageMicrophoneCustom.builder()
                        .id(id)
                        .scenePersonageId(scene_character.getScene_character_id())
                        .userId(null)
                        .userName(null)
                        .sceneId(scene_character.getScene().getSceneId())
                        .sceneName(scene_character.getScene().getSceneName())
                        .actNumber(scene_character.getScene().getActNumber())
                        .personageName(scene_character.getPersonage().getPersonageName())
                        .build();
                customList.add(scenePersonageMicrophoneCustom);
                id++;
            }

        }
        customList.sort(Comparator.comparing(ScenePersonageMicrophoneCustom::getActNumber));

        //List<Microphone> suggestedMicrophones = new ArrayList<>();
        MicrophoneScheduleSuggestedDTO microphoneScheduleSuggestedDTO = new MicrophoneScheduleSuggestedDTO();
        int counter = 0;
        int sceneNumber = 1;
        Dictionary<String, Integer> userMicrophones = new Hashtable<>();
        int microphoneCounter = 1;
        boolean newAct = true;
        int actCounter = 1;
        int numScenesPrevious = 0;
        boolean found = false;
        int foundIndex = 0;

        // Rework

        List<Integer> acts = new ArrayList<>();
        for(ScenePersonageMicrophoneCustom scenePersonageMicrophoneCustom : customList) {
            acts.add(scenePersonageMicrophoneCustom.getActNumber());
        }
        int numberOfActs = acts.stream().distinct().toList().size();

        for(int act = 1; act <= numberOfActs; act++) {
            List<Integer> char_scenes = new ArrayList<>();
            for (ScenePersonageMicrophoneCustom scenePersonageMicrophoneCustom1 : customList) {
                if (scenePersonageMicrophoneCustom1.getActNumber() == actCounter) {
                    char_scenes.add(scenePersonageMicrophoneCustom1.getId());
                }
            }
            //Number of scenes in act in loop
            int numberOfChar_scenes = char_scenes.size(); //char_scenes.stream().distinct().toList().size();

            for (int scene = 1; scene <= numberOfChar_scenes; scene++) {
                if (microphoneCounter == 1) {
                    customList.get(counter).setMicrophoneId(microphoneCounter);
                    found = true;
                    microphoneCounter++;
                    counter++;
                } else {
                    if(numScenesPrevious != 0){
                        for(int i = 1; i < scene; i++){
                            if(!found){
                                if(customList.get(counter - i).getPersonageName().equals(customList.get(counter).getPersonageName())){
                                    found = true;
                                    customList.get(counter).setMicrophoneId(customList.get(counter - i).getMicrophoneId());
                                    counter++;
                                    break;
                                }
                            }

                            if(!found){
                                for(int j = 1; j < numScenesPrevious; j++){
                                    if(customList.get(counter - scene - numScenesPrevious + j).getPersonageName().equals(customList.get(counter).getPersonageName())){
                                        found = true;
                                        customList.get(counter).setMicrophoneId(customList.get(counter - scene - numScenesPrevious + j).getMicrophoneId());
                                        counter++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    else{
                        for(int i = 1; i < scene; i++) {
                            if (customList.get(counter - i).getPersonageName().equals(customList.get(counter).getPersonageName())) {
                                found = true;
                                customList.get(counter).setMicrophoneId(customList.get(counter - i).getMicrophoneId());
                                counter++;
                                break;
                            }
                        }
                    }
                }
                if(!found){
                    customList.get(counter).setMicrophoneId(microphoneCounter);
                    microphoneCounter++;
                    counter++;
                }
                found = false;
            }
                numScenesPrevious = numberOfChar_scenes;
                microphoneCounter = 1;
                actCounter++;


        }

        //for (ScenePersonageMicrophoneCustom scenePersonageMicrophoneCustom : customList) {
        //    List<ScenePersonageMicrophoneCustom> scenePersonageMicrophonesScenes = new ArrayList<>();
        //    for(ScenePersonageMicrophoneCustom scenePersonageMicrophoneCustom1 : customList) {
        //        if(scenePersonageMicrophoneCustom1.getActNumber() == actCounter){
        //            scenePersonageMicrophonesScenes.add(scenePersonageMicrophoneCustom1);
        //        }
        //    }
        //    int numberOfScenes = scenePersonageMicrophonesScenes.size();
         //   if(newAct && counter == 0){
         //       customList.get(counter).setMicrophoneId(microphoneCounter);
         //       microphoneCounter++;
         //       counter++;
         //       sceneNumber++;
         //       newAct = false;
         //   }
         //   else if()
         //   if(sceneNumber == numberOfScenes) {
         //       newAct = true;
         //       sceneNumber = 1;
          //  }


            /* //Redoing this

            if (scenePersonageMicrophoneCustom.getActNumber() == 1 && counter == 0) { //Very first element
                customList.get(counter).setMicrophoneId(microphoneCounter);
                userMicrophones.put(scenePersonageMicrophoneCustom.getUserName(), microphoneCounter);
                microphoneCounter++;
                counter++;
            } else {
                String currentUser = customList.get(counter).getUserName();
                int currentScene = customList.get(counter).getSceneNumber();
                int currentAct = customList.get(counter).getActNumber();
                sceneNumber = 1;
                while (true) {
                    if (currentAct == 1 && currentScene == 1) {
                        customList.get(counter).setMicrophoneId(customList.get(counter).getMicrophoneId());
                        userMicrophones.put(scenePersonageMicrophoneCustom.getUserName(), microphoneCounter);
                        microphoneCounter++;
                        counter++;
                        break;
                    } else {
                        int indexOfUser = 0;
                        //Checking if user was same as in previous scenes
                        int finalCounter = counter;
                        int amountScenes = customList.stream().filter(sc ->
                                        sc.getSceneNumber() == customList.get(finalCounter - 1).getSceneNumber() &&
                                                sc.getActNumber() == customList.get(finalCounter - 1).getActNumber())
                                .collect(Collectors.toList()).size();
                        boolean sameUser = false;
                        for (int i = 0; i < amountScenes; i++) {
                            if (currentUser.equals(customList.get(counter - i - 1).getUserName())) {
                                sameUser = true;
                                indexOfUser = counter - i - 1;
                            }
                        }
                        if (sameUser) {
                            customList.get(counter).setMicrophoneId(customList.get(indexOfUser).getMicrophoneId());
                            userMicrophones.put(scenePersonageMicrophoneCustom.getUserName(), indexOfUser);
                            counter++;
                            break;
                        } else {
                            customList.get(counter).setMicrophoneId(microphoneCounter);
                            userMicrophones.put(scenePersonageMicrophoneCustom.getUserName(), counter);
                            microphoneCounter++;
                            counter++;
                            break;
                        }
                    }
                }
                sceneNumber++;
            }

             */
        //}

        microphoneScheduleSuggestedDTO.setMicrophoneList(customList);
        return microphoneScheduleSuggestedDTO;
    }

    public List<UsersAndRolesDTO> getUsersAndRoles() {
        List<User> users = userRepository.findAll();
        List<UsersAndRolesDTO> list = new ArrayList<>();
        for (User user : users) {
            UsersAndRolesDTO usersAndRolesDTO = UsersAndRolesDTO.builder()
                    .userId(user.getUserId())
                    .userName(user.getUsername())
                    .role(user.getUserRole())
                    .build();
            list.add(usersAndRolesDTO);
        }

        return list;
    }

    @Transactional
    public void setUserRole(long userId, UserRole userRole) {
        User user = userRepository.getReferenceById(userId);
        user.setUserRole(userRole);
        userRepository.save(user);
    }

    public void deleteUser(long userId) {
        User user = userRepository.getReferenceById(userId);
        userRepository.delete(user);
    }

    public List<ActorsConnectedToUsersDTO> getActorsConnectedToUsers() {
        List<Actor> actors = actorRepository.findAll();
        List<ActorsConnectedToUsersDTO> actorsConnectedToUsersDTO = new ArrayList<>();
        for (Actor actor : actors) {
            ActorsConnectedToUsersDTO actorUserCustom = ActorsConnectedToUsersDTO.builder()
                    .actorId(actor.getActorId())
                    .userId(actor.getUser().getUserId())
                    .userName(actor.getUser().getUsername())
                    .build();
            actorsConnectedToUsersDTO.add(actorUserCustom);
        }
        return actorsConnectedToUsersDTO;
    }

    public List<CustomScenePersonageDTO> getCustomScenePersonages(String playName) {
        List<Scene> scenes = sceneRepository.findAllByPlay(playRepository.getReferenceById(playName));
        scenes.sort(Comparator
                .comparing(Scene::getActNumber)
                .thenComparing(Scene::getSceneNumber));

        List<CustomScenePersonageDTO> customScenePersonageDTOs = new ArrayList<>();
        for (Scene scene : scenes) {
            List<Personage> personages = new ArrayList<>();
            for (Scene_character scene_character : scene.getScene_characters()) {
                if(scene_character.getPersonage() != null) {
                    Personage personage = Personage.builder()
                            .personageId(scene_character.getPersonage().getPersonageId())
                            .personageName(scene_character.getPersonage().getPersonageName())
                            .actor(scene_character.getPersonage().getActor())
                            .build();
                    personages.add(personage);
                }

            }

            CustomScenePersonageDTO customScenePersonageDTO = CustomScenePersonageDTO.builder()
                    .sceneId(scene.getSceneId())
                    .sceneName(scene.getSceneName())
                    .sceneNumber(scene.getSceneNumber())
                    .actNumber(scene.getActNumber())
                    .personages(personages)
                    .build();
            customScenePersonageDTOs.add(customScenePersonageDTO);
        }

        return customScenePersonageDTOs;
    }

    public void addAct(String playName, int act, int scenes){
        Play play = playRepository.getReferenceById(playName);

        // Count how many scenes already exist in THIS act
        int existingScenesInAct = sceneRepository.countByPlayAndActNumber(play, act);

        for(int i = 0; i < scenes; i++){

            Scene scene = Scene.builder()
                    .play(play)
                    .actNumber(act)
                    .sceneNumber(existingScenesInAct + i + 1)
                    .sceneName("Act" + act + " / Scene" + (existingScenesInAct + i + 1))
                     .build();
            sceneRepository.save(scene);
        }
    }

    public List<CustomMicrophoneListDTO> getCustomMicrophoneList(String playName) {
        Play play = playRepository.getReferenceById(playName);
        List<CustomMicrophoneListDTO> customMicrophoneListDTOs = new ArrayList<>();
        List<Scene_character> sceneCharacters = scene_characterRepository.findAll();

        for(Scene_character scene_character : sceneCharacters) {
           if(scene_character.getScene().getPlay().equals(play)) {
               if (scene_character.getPersonage() != null) {
                   if (scene_character.getMicrophone() != null) {
                       if(scene_character.getPersonage().getActor().getUser() != null) {
                           CustomMicrophoneListDTO customMicrophoneListDTO = CustomMicrophoneListDTO.builder()
                                   .scene_characterId(scene_character.getScene_character_id())
                                   .sceneId(scene_character.getScene().getSceneId())
                                   .sceneName(scene_character.getScene().getSceneName())
                                   .personageId(scene_character.getPersonage().getPersonageId())
                                   .personageName(scene_character.getPersonage().getPersonageName())
                                   .microphoneId(scene_character.getMicrophone().getMicrophoneId())
                                   .microphoneName(scene_character.getMicrophone().getMicrophoneName())
                                   .userId(scene_character.getPersonage().getActor().getUser().getUserId())
                                   .userName(scene_character.getPersonage().getActor().getUser().getUsername())
                                   .build();
                           customMicrophoneListDTOs.add(customMicrophoneListDTO);
                       }
                       else{
                           /*
                           CustomMicrophoneListDTO customMicrophoneListDTO = CustomMicrophoneListDTO.builder()
                                   .scene_characterId(scene_character.getScene_character_id())
                                   .sceneId(scene_character.getScene().getSceneId())
                                   .sceneName(scene_character.getScene().getSceneName())
                                   .personageId(scene_character.getPersonage().getPersonageId())
                                   .personageName(scene_character.getPersonage().getPersonageName())
                                   .microphoneId(scene_character.getMicrophone().getMicrophoneId())
                                   .microphoneName(scene_character.getMicrophone().getMicrophoneName())
                                   .userId(null)
                                   .userName(null)
                                   .build();
                           customMicrophoneListDTOs.add(customMicrophoneListDTO);

                            */
                       }

                   } else {
                       if(scene_character.getPersonage().getActor() != null) {
                           CustomMicrophoneListDTO customMicrophoneListDTO = CustomMicrophoneListDTO.builder()
                                   .scene_characterId(scene_character.getScene_character_id())
                                   .sceneId(scene_character.getScene().getSceneId())
                                   .sceneName(scene_character.getScene().getSceneName())
                                   .personageId(scene_character.getPersonage().getPersonageId())
                                   .personageName(scene_character.getPersonage().getPersonageName())
                                   .microphoneId(null)
                                   .microphoneName(null)
                                   .userId(scene_character.getPersonage().getActor().getUser().getUserId())
                                   .userName(scene_character.getPersonage().getActor().getUser().getUsername())
                                   .build();
                           customMicrophoneListDTOs.add(customMicrophoneListDTO);
                       }
                       else{

                           CustomMicrophoneListDTO customMicrophoneListDTO = CustomMicrophoneListDTO.builder()
                                   .scene_characterId(scene_character.getScene_character_id())
                                   .sceneId(scene_character.getScene().getSceneId())
                                   .sceneName(scene_character.getScene().getSceneName())
                                   .personageId(scene_character.getPersonage().getPersonageId())
                                   .personageName(scene_character.getPersonage().getPersonageName())
                                   .microphoneId(null)
                                   .microphoneName(null)
                                   .userId(null)
                                   .userName(null)
                                   .build();
                           customMicrophoneListDTOs.add(customMicrophoneListDTO);


                       }

                   }
               } else{
                   if (scene_character.getMicrophone() != null) {
                       /*
                       CustomMicrophoneListDTO customMicrophoneListDTO = CustomMicrophoneListDTO.builder()
                               .scene_characterId(scene_character.getScene_character_id())
                               .sceneId(scene_character.getScene().getSceneId())
                               .sceneName(scene_character.getScene().getSceneName())
                               .personageId(null)
                               .personageName(null)
                               .microphoneId(scene_character.getMicrophone().getMicrophoneId())
                               .microphoneName(scene_character.getMicrophone().getMicrophoneName())
                               .userId(null)
                               .userName(null)
                               .build();
                       customMicrophoneListDTOs.add(customMicrophoneListDTO);

                        */
                   } else{
                       /*
                       CustomMicrophoneListDTO customMicrophoneListDTO = CustomMicrophoneListDTO.builder()
                               .scene_characterId(scene_character.getScene_character_id())
                               .sceneId(scene_character.getScene().getSceneId())
                               .sceneName(scene_character.getScene().getSceneName())
                               .personageId(null)
                               .personageName(null)
                               .microphoneId(null)
                               .microphoneName(null)
                               .userId(null)
                               .userName(null)
                               .build();
                       customMicrophoneListDTOs.add(customMicrophoneListDTO);

                        */
                   }
               }

           }
        }

        return customMicrophoneListDTOs;
    }

    @Transactional
    public void addMicrophone(AddMicrophoneDTOForm form){
        Scene_character scene_character = scene_characterRepository.getReferenceById(form.scene_characterId());
        scene_character.setMicrophone(microphoneRepository.getReferenceById(form.microphoneId()));
        scene_characterRepository.save(scene_character);
    }

    @Transactional
    public void removeMicrophone(Long scene_characterId){
        Scene_character scene_character = scene_characterRepository.getReferenceById(scene_characterId);
        scene_character.setMicrophone(null);
        scene_characterRepository.save(scene_character);
    }

    @Transactional
    public void addSuggestedMicrophones(AddSuggestedMicrophonesDTOForm form) throws Exception {
        List<Microphone> microphones = microphoneRepository.findAll();
        int microphonesCount = microphones.size();

        for(ScenePersonageMicrophoneCustom element : form.getMicrophoneList()){
            Scene_character sc = scene_characterRepository.findById(element.getScenePersonageId()).orElseThrow();
            if(element.getMicrophoneId() > microphonesCount){
                throw new Exception("Not enough microphones in database");
            }
            sc.setMicrophone(microphoneRepository.getReferenceById(element.getMicrophoneId()));
            scene_characterRepository.save(sc);
        }
    }

    @Transactional
    public Actor createActorForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Check if an actor already exists for this user
        if (actorRepository.existsByUser(user)) {
            throw new RuntimeException("Actor already exists for this user");
        }

        Actor actor = Actor.builder()
                .user(user)
                .micSwitches(0) // or any default value
                .build();

        return actorRepository.save(actor);
    }
}
