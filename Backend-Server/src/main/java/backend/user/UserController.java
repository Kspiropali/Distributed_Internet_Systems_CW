package backend.user;

import backend.event.EventPublisher;
import backend.token.Token;
import backend.token.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Objects;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/user")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;
    private final EventPublisher eventPublisher;

    @CrossOrigin(origins = "http://localhost")
    @PostMapping(path = "/register")
    public String registerUser(@RequestBody User user) {
        //Email validator needed

        User newUser = userService.registerUser(user);
        Token newToken = tokenService.createToken(newUser);

        userService.sendRegistrationConfirmationEmail(user.getEmail(), newToken.getToken());

        //System.out.println("Your registration is: https://amazingapp.tplinkdns.com:8443/user/verifyRegistration?token=" + newToken.getToken());

        return "Success";
    }

    @CrossOrigin(origins = "http://localhost")
    @GetMapping(path = "/verifyRegistration")
    public ModelAndView verifyRegistration(@RequestParam("token") String token) {
        //searches for token in database, if its valid

        User validated_user = tokenService.validateToken(token);
        //set user's active status to true
        userService.activateUserAccount(validated_user);
        //remove user's token since validation is done
        tokenService.removeTokenByToken(token);
        //send event to update the registered list
        eventPublisher.publishCustomEvent(validated_user.getUsername(), "user register");


        return new ModelAndView("email_confirmed_page");
    }

    @CrossOrigin(origins = "http://localhost")
    @GetMapping("/resendVerificationToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken) {
        Token newVerificationToken = tokenService.generateNewToken(oldToken);
        User user = newVerificationToken.getUser();

        userService.sendRegistrationConfirmationEmail(user.getEmail(), newVerificationToken.getToken());

        return "Verification Link sent";
    }

    @CrossOrigin(origins = "http://localhost")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/update")
    public String updateUser(Authentication authentication, @RequestBody User user) {
        //header optimisation needed
        //Making sure that other users cant update other users other than themselves
        if (!Objects.equals(authentication.getName(), user.getUsername())) {
            return "You are not allowed to update this user";
        }
        return userService.updateUser(user);
    }

    @CrossOrigin(origins = "http://localhost")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/delete")
    public String deleteUser(Authentication authentication, @RequestBody User user) {
        //header optimisation needed
        //Making sure that other users cant delete other users other than themselves
        if (!Objects.equals(authentication.getName(), user.getUsername())) {
            return "You are not allowed to delete this user";
        }

        //System.out.println(authentication.getName());
        return userService.deleteUser(user);
    }

    @CrossOrigin(origins = "http://localhost")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/upload/avatar")
    public String uploadAvatarImage(Authentication authentication, @RequestBody User user) {
        //header optimisation needed
        //Making sure that other users cant delete other users other than themselves
        if (!Objects.equals(authentication.getName(), user.getUsername())) {
            return "You are not allowed to change of this user!";
        }

        //System.out.println(authentication.getName());
        return userService.uploadImage(user.getAvatar(), user.getUsername());
    }


    @CrossOrigin(origins = "http://localhost")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/download/avatars")
    public ArrayList<ArrayList<String>> downloadAvatarImages() {

        return userService.downloadImages();
    }


    @CrossOrigin(origins = "http://localhost")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/login")
    public String terminalLogin() {
        //System.out.println("User logged in");
        return "Logged in!";
    }

    @CrossOrigin(origins = "http://localhost")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/login")
    public String browserLogin() {

        return "Logged in!";
    }

}