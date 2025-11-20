package org.workshop.microphoneschedulerapi.controller;

import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.microphoneschedulerapi.domain.dto.*;
import org.workshop.microphoneschedulerapi.domain.entity.Actor;
import org.workshop.microphoneschedulerapi.domain.entity.Play;
import org.workshop.microphoneschedulerapi.service.AdminService;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "", allowPrivateNetwork = "")
@RequestMapping("/api/v1/admin")
@RestController
public class AdminController {

    private AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    /**
     * Method for getting all theater production
     * Used in ActorScene.jsx, CharacterScene.jsx, ManageProductionHome.jsx,
     * MicrophoneProduction.jsx and Scene.jsx
     * 
     */
    @GetMapping("/listAllPlay")
    public ResponseEntity<List<Play>> listAllPlay() {
        List<Play> plays = adminService.getAllPlays();

        return ResponseEntity.ok(plays);
    }

    @PostMapping("/createActor/{userId}")
    public ResponseEntity<Actor> createActor(@PathVariable Long userId) {
        try {
            Actor actor = adminService.createActorForUser(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(actor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Method for creating a theater production
     * Used in ManageProductionHome.jsx
     * 
     */
    @PostMapping("/createPlay")
    public ResponseEntity<Play> createPlay(@RequestBody Play play) {
        try {
            Play created = adminService.createPlay(play);
            return ResponseEntity.ok(created);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for deleting a theater production
     * Used in ManageProductionHome.jsx
     * 
     */
    @DeleteMapping("/deletePlay")
    public ResponseEntity<Void> deletePlay(@PathParam("playName") String playName) {
        try {
            adminService.deletePlay(playName);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Method for updating a theater production
     * Used in ManageProductionHome.jsx
     * 
     */
    @PutMapping("/updatePlay")
    public ResponseEntity<Void> updatePlay(@RequestBody Play play) {
        try {
            adminService.updatePlay(play.getPlayName(), play.getPremiereDate(), play.getDescription());
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * Method for getting all scenes in a specific theater production
     * Used in Scene.jsx
     * 
     */
    @GetMapping("/allScenesInPlay")
    public ResponseEntity<SceneCustomListDTO> allScenesInPlay(@PathParam("playName") String playName) {
        try {
            SceneCustomListDTO customSceneList = adminService.getAllScenesInPlay(playName);
            return ResponseEntity.ok(customSceneList);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for creating custom scene in a specific theater production
     * Not in use
     * Could be used in Scene.jsx
     * 
     */
    /*
    @PostMapping("/createScene")
    public ResponseEntity<Void> createScene(@RequestBody CreateSceneDTOForm form) {
        try{
            adminService.createScene(form);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    */

    /**
     * Method for editing scene in a specific theater production
     * Used in Scene.jsx
     * 
     */
    @PutMapping("/editScene")
    public ResponseEntity<Void> editScene(@RequestBody UpdateSceneDTOForm form) {
        try{
            adminService.updateScene(form);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            if(e.getMessage().equals("Scene already exists")) {
                return ResponseEntity.status(HttpStatusCode.valueOf(491)).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for getting deleting scene in a specific theater production
     * Used in Scene.jsx
     * 
     */
    @DeleteMapping("/deleteScene")
    public ResponseEntity<Void> deleteScene(@PathParam("sceneId") int sceneId) {
        try{
            adminService.deleteScene(sceneId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
          return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for adding a character to specific scene
     * Used in CharacterScene.jsx
     * 
     */
    @PutMapping("/addPersonageToScene")
    public ResponseEntity<Void> addPersonageToScene(@RequestBody ManagePersonageToScene form){
        try{
            adminService.addPersonageToScene(form.sceneId(), form.personageId());
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            if(e.getMessage().equals("Character already in scene")) {
                return ResponseEntity.status(HttpStatusCode.valueOf(493)).build();
            }
            if(e.getMessage().equals("Actor already in scene")) {
                return ResponseEntity.status(HttpStatusCode.valueOf(494)).build();
            }
            if(e.getMessage().equals("Character used in other production already")) {
                return ResponseEntity.status(HttpStatusCode.valueOf(496)).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for removing a character from specific scene
     * Used in CharacterScene.jsx
     * 
     */
    @PutMapping("/removePersonageFromScene")
    public ResponseEntity<Void> removePersonageFromScene(@RequestBody ManagePersonageToScene form){
        try{
            adminService.removePersonageFromScene(form.sceneId(), form.personageId());
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for assigning actor to a character
     * Not in use, new method created for this
     * 
     */
    /*
    @PutMapping("/assignActorToPersonage")
    public ResponseEntity<Void> assignActorToPersonage(@RequestBody ManageActorToPersonage form){
        try{
            adminService.assignActorToPersonage(form.actorId(), form.personageId());
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    */

    /**
     * Method for creating a character from specific scene
     * Used in Character.jsx
     * 
     */
    @PostMapping("/createPersonage")
    public ResponseEntity<Void>  createPersonage(@RequestBody CreatePersonageDTOForm form){
        try{
            adminService.createPersonage(form);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for editing a character from specific scene
     * Used in Character.jsx
     * 
     */
    @PutMapping("/editPersonage")
    public ResponseEntity<Void> editPersonage(@RequestBody EditPersonageDTO form){
        try{
            adminService.editPersonage(form);
            return ResponseEntity.ok().build();
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for deleting a character from specific scene
     * Used in Character.jsx
     * 
     */
    @DeleteMapping("/deletePersonage")
    public ResponseEntity<Void> deletePersonage(@PathParam("personageId") int personageId) {
        try{
            adminService.deletePersonage(personageId);
            return ResponseEntity.ok().build();
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for getting all characters
     * Used in Character.jsx and CharacterScene.jsx
     * 
     */
    @GetMapping("getAllPersonageInDb")
    public ResponseEntity<PersonageInDbCustomDTO> getAllPersonageInDb() {
        try{
            PersonageInDbCustomDTO personages = adminService.getAllPersonages();
            return ResponseEntity.ok(personages);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for getting all characters in Play
     * Not in use
     * 
     */
    /*
    @GetMapping("/getAllPersonageInPlay")
    public ResponseEntity<PersonageInDbCustomDTO> getAllPersonageInPlay(@PathParam("playName") String playName) {
        try{
            PersonageInDbCustomDTO personages = adminService.getAllPersonagesInPlay(playName);
            return ResponseEntity.ok(personages);
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    */

    /**
     * Method for getting all characters scene
     * Not in use
     * 
     */
    /*
    @GetMapping("/getAllPersonageInScene")
    public ResponseEntity<PersonageInDbCustomDTO> getAllPersonageInScene(@PathParam("sceneId") int sceneId) {
        try{
            PersonageInDbCustomDTO personages = adminService.getAllPersonagesInScene(sceneId);
            return ResponseEntity.ok(personages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    */

    /**
     * Method for getting suggested microphonelist
     * Used in MicrophoneProduction.jsx
     * 
     */
    @GetMapping("/suggestMicrophoneSchedule")
    public ResponseEntity<MicrophoneScheduleSuggestedDTO> suggestMicrophoneSchedule(@PathParam("playName") String playName){
        try{
            MicrophoneScheduleSuggestedDTO microphoneScheduleSuggestedDTO = adminService.suggestMicrophoneSchedule(playName);

            return ResponseEntity.ok(microphoneScheduleSuggestedDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for getting users and roles
     * Used in UserService.jsx
     * 
     */
    @GetMapping("/getusersandroles")
    public ResponseEntity<List<UsersAndRolesDTO>> getUsersAndRoles(){
        try{
            List<UsersAndRolesDTO> usersAndRoles = adminService.getUsersAndRoles();
            return ResponseEntity.ok(usersAndRoles);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for setting a new role to user
     * Used in UserService.jsx
     * 
     */
    @PostMapping("/setuserrole")
    public ResponseEntity<Void> setUserRole(@RequestBody SetUserRoleDTOForm form){
        try{
            adminService.setUserRole(form.userId(), form.userRole());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for deleting a user
     * Used in UserService.jsx
     * 
     */
    @DeleteMapping("/deleteuser")
    public ResponseEntity<Void> deleteUser(@PathParam("userId") Long userId){
        try{
            adminService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for getting actors connected to users
     * Used in Character.jsx
     * 
     */
    @GetMapping("/getActorsConnectedToUsers")
    public ResponseEntity<List<ActorsConnectedToUsersDTO>> getActorsConnectedToUsers() {
        try{
            List<ActorsConnectedToUsersDTO> list = adminService.getActorsConnectedToUsers();
            return ResponseEntity.ok(list);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for getting custom scenelist
     * Used in CharacterScene.jsx
     * 
     */
    @GetMapping("/getCustomSceneList")
    public ResponseEntity<List<CustomScenePersonageDTO>> getCustomSceneList(@PathParam("playName") String playName){
        try{
            List<CustomScenePersonageDTO> list = adminService.getCustomScenePersonages(playName);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for adding act
     * Used in Scene.jsx
     * 
     */
    @PostMapping("/addAct")
    public ResponseEntity<Void> addAct(@RequestBody addActDTOForm form){
        try{
            adminService.addAct(form.playName(), form.act(), form.scenes());
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for getting custom microphone list
     * Used in MicrophoneProduction.jsx
     * 
     */
    @GetMapping("/getCustomMicrophoneList")
    public ResponseEntity<List<CustomMicrophoneListDTO>> getCustomMicrophoneList(@PathParam("playName") String playName){
        try{
            List<CustomMicrophoneListDTO> list = adminService.getCustomMicrophoneList(playName);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for adding microphone
     * Used in MicrophoneProduction.jsx
     * 
     */
    @PutMapping("/addMicrophone")
    public ResponseEntity<Void> addMicrophone(@RequestBody AddMicrophoneDTOForm form){
        try{
            adminService.addMicrophone(form);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for removing microphone
     * Used in MicrophoneProduction.jsx
     * 
     */
    @PutMapping("/removeMicrophone{scene_characterId}")
    public ResponseEntity<Void> removeMicrophone(@PathParam("scene_characterId") Long scene_characterId){
        try{
            adminService.removeMicrophone(scene_characterId);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Method for applying suggested microphone list
     * Used in MicrophoneProduction.jsx
     * 
     */
    @PutMapping("addSuggestedMicrophones")
    public ResponseEntity<Void> addSuggestedMicrophones(@RequestBody AddSuggestedMicrophonesDTOForm form){
        try{
            adminService.addSuggestedMicrophones(form);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if(e.getMessage().equals("Not enough microphones in database")) {
                return ResponseEntity.status(HttpStatusCode.valueOf(495)).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
}
