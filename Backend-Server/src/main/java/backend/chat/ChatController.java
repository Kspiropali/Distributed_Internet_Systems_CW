package backend.chat;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import static java.lang.String.format;


import java.util.*;
import java.util.concurrent.CompletableFuture;

@Controller
@AllArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MessageRepository messageRepository;
    private final ChatWrapper chatWrapper;


    //When users send CHAT message
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable String roomId, @Payload Message message) {
        //using async
        CompletableFuture.supplyAsync(() -> {
            message.setDestination(roomId);
            //saving the message to the message db
            messageRepository.save(message);
            return message;
        });

        //relaying the message to the destination socket(/ws/app/chat/{destination or roomId}/sendMessage)
        messagingTemplate.convertAndSend(format("/channel/%s", roomId), message);
    }


    //when users JOIN,LEAVE
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload Message message,
                        SimpMessageHeaderAccessor headerAccessor) {
        //find roomId by searching its headers session attributed
        String currentRoomId = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("room_id", roomId);
        //if null that means that the user is not in any room, reply with LEAVE status
        if (currentRoomId != null) {
            Message leaveMessage = new Message();
            leaveMessage.setType(String.valueOf(Message.MessageType.LEAVE));
            leaveMessage.setSender(message.getSender());
            messagingTemplate.convertAndSend(format("/channel/%s", currentRoomId), leaveMessage);
        }
        // if not null, the user connected to a valid socket, send JOIN status
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        messagingTemplate.convertAndSend(format("/channel/%s", roomId), message);
    }

    //when a user registers, relay to the dedicated socket /ws/app/chat/registerCallbackSocket/sendmessage
    //the user's name
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("/chat/registerCallbackSocket/sendMessage")
    public void sendRegistrationMessage(String username) {
        if (!chatWrapper.getRegisteredUsers().contains(username)) {
            chatWrapper.getRegisteredUsers().add(username);
        }
        messagingTemplate.convertAndSend("/channel/registerCallbackSocket", new Message(Message.MessageType.REGISTER, username));
    }

    public void sendRemovalMessage(String username) {
        //System.out.println("removing user from list: " + username);
        chatWrapper.getRegisteredUsers().remove(username);
        messagingTemplate.convertAndSend("/channel/registerCallbackSocket", new Message(Message.MessageType.REMOVE, username));
    }
}