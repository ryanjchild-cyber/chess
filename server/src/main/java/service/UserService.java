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
    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        if (req==null||isBlank(req.username())||isBlank(req.password())||isBlank(req.email())) {

        }
    }
}
