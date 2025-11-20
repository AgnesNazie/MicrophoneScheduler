package org.workshop.microphoneschedulerapi.domain.dto;

public class SceneDTO {


    private String sceneName;
    private int actNumber;
    private int sceneNumber;
    private String playName;

    public SceneDTO() {
    }

    public SceneDTO(String sceneName, int actNumber, int sceneNumber, String  playName) {
        this.sceneName = sceneName;
        this.actNumber = actNumber;
        this.sceneNumber = sceneNumber;
        this.playName = playName;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public int getActNumber() {
        return actNumber;
    }

    public void setActNumber(int actNumber) {
        this.actNumber = actNumber;
    }

    public int getSceneNumber() {
        return sceneNumber;
    }

    public void setSceneNumber(int sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    public String getPlayName() {
        return playName;
    }

    public void setPlayId(String playName) {
        this.playName = playName;
    }
}
