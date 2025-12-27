package util;

import game.GameState;

import java.io.*;

public class SaveManager {

    public static void save(GameState state) throws IOException {
        ObjectOutputStream out =
                new ObjectOutputStream(new FileOutputStream("save.dat"));
        out.writeObject(state);
        out.close();
    }

    public static GameState load() throws IOException, ClassNotFoundException {
        ObjectInputStream in =
                new ObjectInputStream(new FileInputStream("save.dat"));
        return (GameState) in.readObject();
    }
}
