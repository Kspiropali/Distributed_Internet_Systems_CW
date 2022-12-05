package backend.user;


import java.util.ArrayList;

public interface UserService {
    User registerUser(User user);

    boolean checkIfUserExistByEmail(String email);

    boolean checkIfUserExistByUsername(String username);

    void activateUserAccount(User user);

    void sendRegistrationConfirmationEmail(String sendTo, String token);

    String updateUser(User user);

    String deleteUser(User user);

    String uploadImage(String avatar, String username);

    ArrayList<ArrayList<String>> downloadImages();
}