package models.msg;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.User;
import util.JsonKeys;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by fabianwidmann on 08/02/17.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(
        uniqueConstraints=
        @UniqueConstraint(columnNames={JsonKeys.USER_ID, JsonKeys.CARDDECK_ID, JsonKeys.FLASHCARD_ID})
)
@DiscriminatorColumn(name= JsonKeys.MESSAGE_TYPE, discriminatorType = DiscriminatorType.STRING)
public abstract class Message extends Model {
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.MESSAGE_ID)
    @JsonProperty(JsonKeys.MESSAGE_ID)
    private long id;

    @Column(name = JsonKeys.MESSAGE_RECIPIENT)
    @JsonProperty(JsonKeys.MESSAGE_RECIPIENT)
    private User recipient;

    @Column(name = JsonKeys.MESSAGE_CONTENT)
    @JsonProperty(JsonKeys.MESSAGE_CONTENT)
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
    private Date timestamp;

    public static Finder<Long,Message> find = new Finder<>(Message.class);

    /**
     * Create a new message for one recipient with a specific string message
     * @param recipient
     * @param content
     */
    public Message(User recipient, String content) {
        this.recipient = recipient;
        this.content = content;
    }

    //setter
    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public void setContent(String content) {
        this.content = content;
    }

    //getter
    public long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
