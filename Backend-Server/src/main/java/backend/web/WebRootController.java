package backend.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/")
public class WebRootController {

        @RequestMapping(path = "/h2-console")
        public void index() {
        }
}
