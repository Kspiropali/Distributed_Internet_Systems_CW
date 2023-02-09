package backend.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/")
public class WebRootController {

        @PreAuthorize("hasRole('USER')")
        @GetMapping("/h2-console")
        public String h2console(Authentication authentication){
                //System.out.println("h2console: " + authentication.getName());
                if (authentication.getName().equals("admin")){
                        //redirect to h2-console
                        return "redirect:/h2-console/y4DQbwMYMHHYLQ2wDAZeb9CnhZJK59essSAvpY";
                }

                return "redirect:/chat";

        }

        @PostMapping("/xsrf")
        public void getXsrfToken(){
                //System.out.println("xsrf: " + authentication.getName());
        }
}
