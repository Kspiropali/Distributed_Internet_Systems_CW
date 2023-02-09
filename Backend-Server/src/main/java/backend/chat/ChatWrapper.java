package backend.chat;

import backend.event.EventPublisher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RequestMapping(path = "/download")
@RestController
@AllArgsConstructor
@Getter @Setter
public class ChatWrapper {
    private final SocketEventListener socketEventListener;
    private final MessageRepository messageRepository;
    private final EventPublisher eventPublisher;

    //registered users, the ones that are registered by the website
    //not the preexisting ones from userConfig
    private ArrayList<String> registeredUsers;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/chat/{roomId}/messages")
    public ArrayList<Object> getMessages(@PathVariable String roomId) {
        /*System.out.println(roomId);*/

        // finds all messages that are equal to a roomId(or topic)
        //and maps them to an arraylist: [[message1], [message2], [message3]]]
        // where message = ["id","content","sender","destination","type","timestamp"]

        //System.out.println("--------------exporting online users----------");

        return messageRepository
                .findAll()
                .stream()
                .filter(message -> message.getDestination().equals(roomId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/chat/users")
    public ArrayList<String> getOnlineUsers() {
        //find all users that are connected to an opened socket
        //System.out.println("--------------exporting online users----------");
        return socketEventListener.getUsers();
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/chat/registeredUsers")
    public ArrayList<String> getRegisteredUsers() {
       // System.out.println("--------------exporting registered users----------");
        //returns all registered and activated=true users
        return registeredUsers;
    }
}
