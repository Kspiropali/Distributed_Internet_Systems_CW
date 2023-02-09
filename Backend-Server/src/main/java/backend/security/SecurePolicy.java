package backend.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@SuppressWarnings({"rawtypes", "deprecation"})
public class SecurePolicy extends WebSecurityConfigurerAdapter {


    private final UserDetailsService userService;
    private final PasswordEncoder passwordEncoder;
    private final MyBasicAuthenticationEntryPoint authenticationEntryPoint;

    //WhiteListed urls from authentication
    private static final String[] WHITE_LIST_URLS = {
            "/user/register**",
            "/user/verifyRegistration**",
            "/user/resendVerificationToken**",
            "/user/loggedout**",
            "/css/**",
            "/js/**",
            "/error**",
            "/images/**",
            "/webfonts/**",
            "/favicon.ico**",
            "/config/**",
            "/error**",
            "/.well-known/acme-challenge/**"
    };

    public SecurePolicy(@Qualifier("userServiceImpl") UserDetailsService userService, PasswordEncoder passwordEncoder, MyBasicAuthenticationEntryPoint authenticationEntryPoint) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    //Security context configurer and session provider configurer
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.headers().frameOptions().disable().and()
                .cors().disable()
                //Cross site request forgery disable for testing purposes
                .csrf().disable()//ignoringAntMatchers("/h2-console/**", "/user/delete/**").csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
                .formLogin().loginPage("/").permitAll().usernameParameter("username").passwordParameter("password")
                .and()
                .authorizeRequests()
                //Allow all white-listed urls without authentication
                .antMatchers(WHITE_LIST_URLS).permitAll()
                .anyRequest()
                .authenticated().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).maximumSessions(1).maxSessionsPreventsLogin(true).sessionRegistry(sessionRegistry()).expiredUrl("/?invalid-session=true").and().and()
                .logout().deleteCookies().invalidateHttpSession(true)
                .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint);

    }

    @Bean
    public WebFilter addCsrfToken() {
        return (exchange, next) -> Objects.requireNonNull(exchange.<Mono<CsrfToken>>getAttribute(CsrfToken.class.getName()))
                .doOnSuccess(token -> {}) // do nothing, just subscribe :/
                .then(next.filter(exchange));
    }


    //Data access object spring security configuration
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://amazin12gapp.tplinkdns.com:8443/"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
    }
}

