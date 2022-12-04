package backend.user;

import backend.event.EventPublisher;
import backend.token.Token;
import backend.token.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/user")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;
    private final EventPublisher eventPublisher;

    @PostMapping(path = "/register")
    public String registerUser(@RequestBody User user) {
        //Email validator needed

        User newUser = userService.registerUser(user);
        Token newToken = tokenService.createToken(newUser);

        //userService.sendRegistrationConfirmationEmail(user.getEmail(), newToken.getToken());

        System.out.println("Your registration is: http://localhost:8080/user/verifyRegistration?token=" + newToken.getToken());

        return "Success";
    }

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

    @GetMapping("/resendVerificationToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken) {
        Token newVerificationToken = tokenService.generateNewToken(oldToken);
        User user = newVerificationToken.getUser();

        userService.sendRegistrationConfirmationEmail(user.getEmail(), newVerificationToken.getToken());

        return "Verification Link sent";
    }

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

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/delete")
    public String deleteUser(Authentication authentication, @RequestBody User user) {
        //header optimisation needed
        //Making sure that other users cant delete other users other than themselves
        if (!Objects.equals(authentication.getName(), user.getUsername())) {
            return "You are not allowed to delete this user";
        }

        System.out.println(authentication.getName());
        return userService.deleteUser(user);
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/login")
    //@PreAuthorize("hasAuthority('USER_WRITE')")
    public String terminalLogin() {
        System.out.println("User logged in");
        /*TODO: We cant just return an id here. Implementation of login tokens are required. We can save the token in
        cookies with expiration time etc.*/
        return "Logged in!";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/login")
    //@PreAuthorize("hasAuthority('USER_WRITE')")
    public String browserLogin() {
        /*TODO: We cant just return an id here. Implementation of login tokens are required. We can save the token in
        cookies with expiration time etc.*/

        return "Logged in!";
    }


}