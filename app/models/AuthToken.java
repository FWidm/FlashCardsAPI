package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import util.JsonKeys;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@Entity
public class AuthToken extends Model {
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.TOKEN_ID)
    @JsonProperty(JsonKeys.TOKEN_ID)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.TOKEN_USER)
    User user;
    @JsonProperty(JsonKeys.TOKEN)
    @Column(unique = true)
    String token;

    public static Model.Finder<Long, AuthToken> find = new Model.Finder<Long, AuthToken>(AuthToken.class);


    public AuthToken(User user) {
        this.user = user;
        //create new tokens while we find that it is already in use. Should not happen theoretically.
        do{
            token = UUID.randomUUID().toString();
        }while(AuthToken.find.where().eq(JsonKeys.TOKEN,token).findUnique()!=null);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "AuthToken{" +
                "id=" + id +
                ", user=" + user +
                ", token='" + token + '\'' +
                '}';
    }
}
