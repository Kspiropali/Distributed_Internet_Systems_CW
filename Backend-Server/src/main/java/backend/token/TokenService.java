package backend.token;

import backend.user.User;

public interface TokenService {
    void removeTokenByToken(String token);

    Token createToken(User user);

    User validateToken(String token);

    Token generateNewToken(String oldToken);
}
