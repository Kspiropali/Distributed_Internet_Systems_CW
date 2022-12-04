package backend.email;

@SuppressWarnings("unused")
public interface EmailService {
    void sendSimpleMail(String sendTo, String token);

    String sendMailWithAttachment(String sendTo, String token);
}