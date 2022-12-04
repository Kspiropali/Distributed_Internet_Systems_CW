package backend.user;

import backend.email.EmailService;
import backend.event.EventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final EventPublisher eventPublisher;


    public User registerUser(User user) {
        if (checkIfUserExistByEmail(user.getEmail())) {
            try {
                throw new Exception("User already exists with this email");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (checkIfUserExistByUsername(user.getUsername())) {
            try {
                throw new Exception("User already exists with this username");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //Password validation needed

        User user_create = new User();
        user_create.setUsername(user.getUsername());
        user_create.setEmail(user.getEmail());
        user_create.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user_create);

        return user_create;
    }

    @Override
    public String updateUser(User user){
        System.out.println(user.getUsername());
        if (checkIfUserExistByUsername(user.getUsername())) {
            User user_update = userRepository.findUserByUsername(user.getUsername()).get();
            System.out.println("User exists with username: "+user.getUsername());
            //checks
            if(user.getName() == null || !user.getName().matches("^[a-zA-Z]+$") || user.getName().matches("\"") || user.getName().length() < 3 || user.getName().length() > 20){
                return "Name error";
            }
            if(user.getSurname() == null || !user.getSurname().matches("^[a-zA-Z]+$") || user.getSurname().matches("\"") || user.getSurname().length() < 3 || user.getSurname().length() > 20){
                return "Surname error";
            }
            if(!user.getPhoneNumber().matches("([0-9\\-\\_]+)")){
                return "Phone error";
            }
            if(user.getPassword() == null || user.getPassword().matches("\"") || user.getPassword().length() < 8){
                return "Password error";
            }
            user_update.setName(user.getName());
            user_update.setSurname(user.getSurname());
            user_update.setPhoneNumber(user.getPhoneNumber());
            user_update.setLanguage(user.getLanguage());
            user_update.setPassword(passwordEncoder.encode(user.getPassword()));
            user_update.setOptIn(user.isOptIn());
            System.out.println("Saving user with details: "+user_update);

            userRepository.save(user_update);
        }else {
            return "Username does not exist";
        }

        return "User updated";
    }

    @Override
    public String deleteUser(User user){
        if (checkIfUserExistByUsername(user.getUsername())) {
            User user_delete = userRepository.findUserByUsername(user.getUsername()).get();
            System.out.println("User exists with username: " + user.getUsername());
            userRepository.delete(user_delete);
            //sending deleted event
            eventPublisher.publishCustomEvent(user_delete.getUsername(), "user deletion");
            return "User deleted";
        }else {
            return "username error";
        }
    }

    public void activateUserAccount(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    public boolean checkIfUserExistByEmail(String email) {
        return userRepository.findUserByEmail(email).isPresent();
    }


    public boolean checkIfUserExistByUsername(String username) {
        return userRepository.findUserByUsername(username).isPresent();
    }

    public void sendRegistrationConfirmationEmail(String sendTo, String token) {
        emailService.sendSimpleMail(sendTo, token);
    }

    //Core spring security method to validate user credentials
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format("user with email %s not found", username)));
    }
}


