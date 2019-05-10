package team.edu.app.muternet.player;

import java.io.File;
import java.io.IOException;

public interface PlayerController {
    public void play();
    public void pause();
    public void load(File file) throws IOException;

}
