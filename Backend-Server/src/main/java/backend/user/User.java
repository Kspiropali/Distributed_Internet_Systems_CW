package backend.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

    //private static final long serialVersionUID = 123L;
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

    private String email;

    @Column(length = 60)
    private String password;

    //lazy way to add default image for users
    @Column(name = "avatar", nullable = true, length = 200000)
    private String avatar = "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAABmJLR0QA/wD/AP+gvaeTAAAEEElEQVR4nO2dTahUZRjHf/d6y1tZftyCbrQK7PoZgtKiDwKNSKIgFy3VthKB4EpcuHChmHi1lSAELQsMCWlhuMllUYmbkuAWkZoiiNUkeB0X7wzK4Nxz3jPnff/PmXl+8Ocu7jnzfPzPmTnnnXfeA47jOI7jOE79jKkTiGQc2ARs7vx9EXgeeKLz/3+BP4FfgB+Ac8D3wN3smQ4508AB4A+gHanfO/tOZ896CFkOHANaxBvRq1bntZZlrWCI2AZcZXAjenWl89pOSRYRjuS6jejVbCeWswCTwCnSm9HVqU5M5yEsAr4knxldnQYmMtTXOHK8TfXT0Qz1NYpt6MxoE+5T3k9eZUNYCvyF1pA24epreeJaG8Fx9GZ0dSxxreZ5FvgPvRFdtYDnklZcwLgyOPAR8Jg4hweZBHapk1AxThhnUp8VvZpDf6BKeBl98/tpU8K6F0R5JGwRxi5isyqw0pCNwthFjOQZMiOMXcQqVWClIdLLywJkX2YpDVkijF3Ek6rAyu/U28LYZZD0ZiSvty3jhhjDDTGGG2IMN8QYbogx3BBjuCHGcEOM4YYYww0xhtKQ28LYRbRUgZWG3BTGLkKWm9KQq8LYRfytCqw05KIwdhEXVIGVhvwkjF3Ez+oEFGxAP92nn9YmrNssY9icKPdbyqKLUL5ltYHPhPH7cVKdgJJpwv2I+qzoqgU8k7TiAtR36peBE+IcHmQWuKZOQs0UcAP92XEZ4fQfa3yA1oy7wHvJq2wYJ9EZ8kmG+hrHo8AZ8pvxFb6AQF8eB74lnxmnCQeCswAT5PnN+gkMLhhgeb2sduLXN1m7+j7E6cENMYYbYgw3xBhuiDHcEGO4IcZwQ4xh1ZCtGWK8miFG41kM7Af+J/3QyQ1ge56ymscjwIfAJfKP9p4FXklfYjPork81R34jenUOeAe7b+NJWQbswcZai726BOxmRJYknyEMrd9C3/gitYDPgZeSdELMa8DXhO+u1Y2uovPAu7V3RcCbhGLUDa1L3yFc3GwQVgJfoG9gKp0F1tTWrYRMAoexNRsxlW4Dhwj3TiaZAX5E36jcugisq6F/tbIT+Ad9c1S6BewYuIs1MEYY6lA3xIoOIp488elDkhp1zQ7U0QHYVzLBUdTeAfpaibeB+RoSH1bNA29V7m4kU4SfDauLtq5KzyOpMsn4CPBGhf1GjSWEucrfxOwUe0XwAvArPlu8LHcIoxZzZXeIHfv/GDcjhgnCM1JKE3OGTBCWw1gRE8DhOuFJQvNlNo45Q17HzajC00RMqIgxpJHDzkYo/ayUGEPWV0jECZQefIwxRPZMjSFgddkNYwzxz4/qTJXdMMaQpyok4gSWlt0w5rK3XSER5z6lej2Sk8Icx3Ecx3Ecx3Gc+9wD6vspBPkgA3MAAAAASUVORK5CYII=";

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
