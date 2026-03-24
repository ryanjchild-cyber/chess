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

}
