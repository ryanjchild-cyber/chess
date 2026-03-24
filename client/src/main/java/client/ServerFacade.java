package client;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
public class ServerFacade {
    private final String serverUrl;
    private final Gson gson=new Gson();
    public ServerFacade(int port) {
        this.serverUrl="http://localhost:"+port;
    }
    public AuthData register(String username,String password,String email) throws Exception {
        var body=Map.of(
                "username",username,
                "password",password,
                "email",email
        );
        return makeRequest("POST","/user",null,body,AuthData.class);
    }
    public AuthData login(String username,String password) throws Exception {
        var body=Map.of(
                "username",username,
                "password",password
        );
        return makeRequest("POST", "/session",null,body,AuthData.class);
    }
    public void logout(String authToken) throws Exception {
        makeRequest("DELETE","/session",authToken,null,null);
    }
    public Integer createGame(String authToken,String gameName) throws Exception {
        var body = Map.of("gameName",gameName);
        var response = makeRequest("POST", "/game",authToken,body,CreateGameResponse.class);
        return response.gameID();
    }
    public List<GameData> listGames(String authToken) throws Exception {
        var response = makeRequest("GET", "/game", authToken, null, ListGamesResponse.class);
        return response.games();
    }
    public void joinGame(String authToken,String playerColor,int gameID) throws Exception {
        var body=Map.of(
                "playerColor",playerColor,
                "gameID",gameID
        );
        makeRequest("PUT","/game",authToken,body,null);
    }
    public void clear() throws Exception {
        makeRequest("DELETE","/db",null,null,null);
    }
    public <T> T makeRequest(String method,String path,String authToken,Object requestBody,Class<T> responseClass) throws Exception {
        URL url=URI.create(serverUrl+path).toURL();
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        if (authToken!=null) {
            connection.setRequestProperty("Authorization",authToken);
        }
        if (requestBody!=null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json");
            String json=gson.toJson(requestBody);
            try (OutputStream reqBody=connection.getOutputStream()) {
                reqBody.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
        connection.connect();
        int status=connection.getResponseCode();
        boolean success=status/100==2;
        InputStream stream=success?connection.getInputStream():connection.getErrorStream();
        String responseJson="";
        if (stream!=null) {
            responseJson=new String(stream.readAllBytes(),StandardCharsets.UTF_8);
        }
        if (!success) {
            String message = extractErrorMessage(responseJson);
            throw new ClientException(status,message);
        }
        if (responseClass==null||responseJson.isBlank()) {
            return null;
        }
        return gson.fromJson(responseJson, responseClass);
    }
    private record CreateGameResponse(int gameID) {}
    private record ListGamesResponse(List<GameData> games) {}

}
