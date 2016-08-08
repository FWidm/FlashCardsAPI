package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import util.JsonKeys;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@Entity
@Table (name = JsonKeys.AUTH_TOKEN_TABLE_NAME)
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


    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z") @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
    private Date created;

    public static Model.Finder<Long, AuthToken> find = new Model.Finder<Long, AuthToken>(AuthToken.class);

    /**
     * Create a new auth token, make sure it is unique.
     * @param user
     */
    public AuthToken(User user) {
        this.user = user;
        //create new tokens while we find that it is already in use. Should not happen theoretically.
        do{
            try {
                token=nextBase64String(32);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }while(AuthToken.find.where().eq(JsonKeys.TOKEN,token).findUnique()!=null);
    }

    /**
     * Returns one random sequence of characters of length n in UTF-8.
     * @param n length of the returned String
     * @return random sequence of characters
     * @throws UnsupportedEncodingException
     */
    private String nextBase64String(int n) throws UnsupportedEncodingException {
        SecureRandom csprng = new SecureRandom();
        // NIST SP800-90A recommends a seed length of 440 bits (i.e. 55 bytes)
        csprng.setSeed(csprng.generateSeed(55));
        byte[] bytes =new byte[n];
        csprng.nextBytes(bytes);
        byte[] encoded=Base64.getUrlEncoder().encode(bytes);
        return new String(encoded,"UTF-8");
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

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "AuthToken{" +
                "id=" + id +
                ", user=" + user +
                ", token='" + token + '\'' +
                ", created=" + created +
                '}';
    }
}
