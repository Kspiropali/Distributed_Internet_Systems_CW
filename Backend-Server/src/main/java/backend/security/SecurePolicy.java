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
import org.springframework.security.web.session.HttpSessionEventPublisher;

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
            "/error**"
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
                .cors().and()
                //Cross site request forgery disable for testing purposes
                .csrf().disable()

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
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
    }
}

