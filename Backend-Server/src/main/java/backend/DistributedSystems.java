package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
/*@CrossOrigin(origins = "*", allowedHeaders = "*")*/
public class DistributedSystems {

    public static void main(String[] args) {
        SpringApplication.run(DistributedSystems.class, args);
    }


    @Profile("local")
    @Bean
    public String devBean() {
        return "local";
    }

    @Profile("deploy")
    @Bean
    public String deployBean() {
        return "deploy";
    }


}