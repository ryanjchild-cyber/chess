package client;
import model.GameData;
public interface NotificationHandler {
    void loadGame(GameData game);
    void notifyMessage(String message);
    void errorMessage(String message);
}