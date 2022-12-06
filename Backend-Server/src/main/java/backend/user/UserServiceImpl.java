package backend.user;

import backend.email.EmailService;
import backend.event.EventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final EventPublisher eventPublisher;

    @Override
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

        User user_create = new User();
        user_create.setUsername(user.getUsername());
        user_create.setEmail(user.getEmail());
        user_create.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user_create);
        System.out.println("--------------User with username: "+user_create.getUsername()+" has been created successfully!----------");
        return user_create;
    }

    @Override
    public String updateUser(User user){
        System.out.println(user.getUsername());
        if (checkIfUserExistByUsername(user.getUsername())) {
            User user_update = userRepository.findUserByUsername(user.getUsername()).get();
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

    @Override
    public String uploadImage(String avatar, String username){
        if (checkIfUserExistByUsername(username)) {
            User userToUpdate = userRepository.findUserByUsername(username).get();
            userToUpdate.setAvatar(avatar);

            //sending deleted event
            //eventPublisher.publishCustomEvent(user_delete.getUsername(), "user deletion");
            return "Avatar updated";
        }else {
            return "username error";
        }
    }

    @Override
    public void activateUserAccount(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public boolean checkIfUserExistByEmail(String email) {
        return userRepository.findUserByEmail(email).isPresent();
    }

    @Override
    public boolean checkIfUserExistByUsername(String username) {
        return userRepository.findUserByUsername(username).isPresent();
    }

    @Override
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

    @Override
    public ArrayList<ArrayList<String>> downloadImages(){
        ArrayList<User> data = userRepository.findAllByAuthority(new SimpleGrantedAuthority("USER"));
        ArrayList<ArrayList<String>> dataAfter = new ArrayList<>();
        for (User user : data) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(user.getUsername());
            temp.add(user.getAvatar());
            dataAfter.add(temp);
        }

        return dataAfter;
    }
}