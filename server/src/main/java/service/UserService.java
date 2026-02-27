package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dao;
    public UserService(DataAccess dao) {
        this.dao=dao;
    }
    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request==null||isBlank(request.username())||isBlank(request.password())||isBlank(request.email())) {
            throw new BadRequestException();
        }
        if (dao.getUser(request.username())!=null) {
            throw new ForbiddenException();
        }
        dao.createUser(new UserData(request.username(),request.password(),request.email()));
        String token=UUID.randomUUID().toString();
        dao.createAuth(new AuthData(token,request.username()));
        return new RegisterResult(request.username(),token);
    }
    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request==null||isBlank(request.username())||isBlank(request.password())) {
            throw new BadRequestException();
        }
        UserData user=dao.getUser(request.username());
        if (user==null) {
            throw new UnauthorizedException();
        }
        String token=UUID.randomUUID().toString();
        dao.createAuth(new AuthData(token,request.username()));
        return new LoginResult(request.username(),token);
    }
    public void logout(String authToken) throws DataAccessException {
        if (isBlank(authToken)) throw new UnauthorizedException();
        AuthData auth=dao.getAuth(authToken);
        if (auth==null) throw new UnauthorizedException();
        dao.deleteAuth(authToken);
    }
    private static boolean isBlank(String s) {
        return s==null||s.isBlank();
    }
}
