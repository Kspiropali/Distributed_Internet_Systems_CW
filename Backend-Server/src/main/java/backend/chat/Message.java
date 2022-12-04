package backend.chat;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@SuppressWarnings({"deprecation", "ALL"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity @Table(name = "_messages")
public class Message {
    //7 static message types
    //Join, Leave for online offline users
    //Register, Remove for registered users(with isActivated=true) and deleted users
    //CHAT, PICTURE and RECORDING for messages
    public enum MessageType {
        JOIN, LEAVE, CHAT, PICTURE, RECORDING, REGISTER, REMOVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;


    public Message(MessageType messageType, String content){
        this.type = String.valueOf(messageType);
        this.content = content;
    }

    //large object data(because of images) or @Lob instead of length
    @Column(name = "content", nullable = true,length = 1000000)
    private String content;
    @Column(nullable = true, length = 25, unique = false)
    private String sender;
    @Column(nullable = true, length = 10, unique = false)
    private String type;
    @Column(nullable = true, length = 25, unique = false)
    private String destination;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, length = 30)
    private Timestamp time;

    @Override
    public String toString(){
        return content+","+sender+","+type+","+destination+" "+time.getHours()+":"+time.getMinutes();
    }
}
