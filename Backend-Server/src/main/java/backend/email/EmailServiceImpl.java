package backend.email;


import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final String msgBody = "Your registration is: https://amazingapp.tplinkdns.com:8443/user/verifyRegistration?token=";
    private final String subject = "Verify your email address";
    private final JavaMailSender javaMailSender;

    @Override
    public void sendSimpleMail(String sendTo, String token) {
        try {

            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom("amazingapp.tplinkdns.com@gmail.com");
            mailMessage.setTo(sendTo);
            mailMessage.setText(msgBody+token);
            mailMessage.setSubject(subject);

            // Sending the mail
           // System.out.println("Sending email to: " + sendTo);
            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            System.out.println("Could not send Email because of: " + e);
        }
    }

    @Override
    public String sendMailWithAttachment(String sendTo, String token) {
        MimeMessage mimeMessage
                = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {

            mimeMessageHelper
                    = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom("amazingapp.tplinkdns.com@gmail.com");
            mimeMessageHelper.setTo(sendTo);
            mimeMessageHelper.setText(msgBody);
            mimeMessageHelper.setSubject(subject);

            //To be implemented later if needed mail + attachment
           /* FileSystemResource file = new FileSystemResource(
                    new File();

            mimeMessageHelper.addAttachment(
                    Objects.requireNonNull(file.getFilename()), file);*/

            javaMailSender.send(mimeMessage);
            return "Mail sent Successfully";
        } catch (Exception e) {

            return "Error while sending mail!!!";
        }
    }
}