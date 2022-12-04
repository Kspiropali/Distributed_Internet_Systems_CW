package backend.token;

import backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("ALL")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "TOKENS")
public class Token {

    //Expiration time 10 minutes
    private static final int EXPIRATION_TIME = 10;


    @Id
    @SequenceGenerator(
            name = "tokens_sequence",
            sequenceName = "tokens_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "tokens_sequence"
    )
    private Long id;

    @Column(unique = true, updatable = true, nullable = false)
    private String token;

    @CreationTimestamp
    @Column(updatable = false, nullable = false, length = 30)
    private Timestamp createdAt;

    @Column(updatable = false)
    @Basic(optional = false)
    private Date expirationTime;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "users_id",
            foreignKey = @ForeignKey(name = "FK_USER_TOKEN"))
    private User user;

    public Token(User user, String token) {
        super();
        this.token = token;
        this.user = user;
        this.expirationTime = calculateExpirationDate();
    }

    private Date calculateExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, Token.EXPIRATION_TIME);
        return new Date(calendar.getTime().getTime());
    }

}


