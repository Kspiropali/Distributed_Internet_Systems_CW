package backend.user;

import backend.chat.ChatWrapper;
import backend.security.PasswordEncoder;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;


//Pre-creating users and saving them for testing purposes
@Configuration
public class UserConfig {
    private final UserRepository userRepository;
    private final ChatWrapper chatWrapper;

    private final PasswordEncoder passwordEncoder;
    public UserConfig(UserRepository userRepository, ChatWrapper chatWrapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.chatWrapper = chatWrapper;
        this.passwordEncoder = passwordEncoder;
        addUsers();
    }

    public void addUsers() {
        System.out.println("--------------PreSetting up and enabling test users!----------");
        //Testing only, Setup couple of users for testing
        User bob = new User("bob@bob.com", "bob", passwordEncoder.encode("bob"));
        User sam = new User("sam@sam.com","sam", passwordEncoder.encode("sam"));
        User john = new User("john@john.com", "john", passwordEncoder.encode("john"));
        bob.setEnabled(true);
        sam.setEnabled(true);
        john.setEnabled(true);
        ArrayList<User> users = new ArrayList<>();
        users.add(bob);
        users.add(sam);
        users.add(john);
        userRepository.saveAll(users);

        //passing through the above automatically registered users(for testing) to the registeredUsers
        chatWrapper.setRegisteredUsers(users.stream().map(User::getUsername).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }
}
