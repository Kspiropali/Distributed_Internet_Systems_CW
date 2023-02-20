/*
package backend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // allow cros path
                .allowedOriginPatterns("*")    // allow cros origin
                .allowedMethods("POST", "GET")// allow method
                .maxAge(1728000) //
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}*/
