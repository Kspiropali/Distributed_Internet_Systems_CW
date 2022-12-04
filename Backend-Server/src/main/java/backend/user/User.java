package backend.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("ALL")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "_users")
public class User implements UserDetails {

    @Id
    @SequenceGenerator(
            name = "users_sequence",
            sequenceName = "users_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "users_sequence"
    )
    @Column(unique = true)
    private Long id;

    @Column(unique = true)
    private String username;


    @Column(unique = true)
    private String email;

    @Column(length = 60)
    private String password;


    private String name;
    private String surname;
    private String phoneNumber;
    private String language;
    private boolean optIn = false;

    private boolean accountNonLocked = true;
    private boolean accountNonExpired = true;
    private int failedLoginAttempts = 0;
    private boolean credentialsNonExpired = true;
    private boolean enabled = false;

    GrantedAuthority authority = new SimpleGrantedAuthority("USER");

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(authority);
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }
}
