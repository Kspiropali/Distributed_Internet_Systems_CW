package backend.chat;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;

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
    //SPECIAL is for when users update their profile picture, other users can liveload the new picture
    public enum MessageType {
        JOIN, LEAVE, CHAT, PICTURE, RECORDING, REGISTER, REMOVE, SPECIAL
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
    @Column
    private String sender;
    @Column
    private String type;
    @Column
    private String destination;

    @CreationTimestamp
    @Column
    private Timestamp time;

    @Override
    public String toString(){
        return content+","+sender+","+type+","+destination+" "+time.getHours()+":"+time.getMinutes();
    }
}
