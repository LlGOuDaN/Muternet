package team.edu.app.muternet.player;

import java.io.File;
import java.io.IOException;

public class PlayerUtil implements PlayerController {
    private static PlayerUtil playerInstance = new PlayerUtil();
    private PlayerController controller = null;
    private PlayerUtil(){

    }
    public static PlayerUtil getInstance(){
        return playerInstance;
    }
    public void setUpPlayer(PlayerController controller){
        this.controller = controller;
    }
    @Override
    public void play() {
        this.controller.play();
    }

    @Override
    public void pause() {
        this.controller.pause();
    }

    @Override
    public void load(File file) throws IOException {
        this.controller.load(file);
    }

    @Override
    public int getPosition() {
        return controller.getPosition();
    }

    @Override
    public void seekTo(int t) {
        controller.seekTo(t);
    }

}
