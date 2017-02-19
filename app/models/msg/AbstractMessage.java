package models.msg;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import models.User;
import util.JsonKeys;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by fabianwidmann on 08/02/17.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = JsonKeys.MESSAGE)
@DiscriminatorColumn(name = JsonKeys.MESSAGE_TYPE, discriminatorType = DiscriminatorType.STRING)
@JsonPropertyOrder({JsonKeys.MESSAGE_ID})
public abstract class AbstractMessage extends Model {
    public static Finder<Long, AbstractMessage> find = new Finder<>(AbstractMessage.class);
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.MESSAGE_ID)
    @JsonProperty(JsonKeys.MESSAGE_ID)
    protected long id;

    @ManyToOne
    @JoinColumn(name = JsonKeys.MESSAGE_RECIPIENT, referencedColumnName = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.MESSAGE_RECIPIENT)
    protected User recipient;

    @ManyToOne
    @JoinColumn(name = JsonKeys.MESSAGE_SENDER, referencedColumnName = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.MESSAGE_SENDER)
    protected User sender;

    @Column(name = JsonKeys.MESSAGE_CONTENT)
    @JsonProperty(JsonKeys.MESSAGE_CONTENT)
    protected String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @CreatedTimestamp
    @Column(name = JsonKeys.DATE_CREATED)
    @JsonProperty(JsonKeys.DATE_CREATED)
    protected Date timestamp;

    /**
     * Create a new message for one recipient with a specific string message
     *
     * @param recipient
     * @param content
     */
    public AbstractMessage(User recipient, String content) {
        this.recipient = recipient;
        this.content = content;
    }


    //getter
    public long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public User getSender() {
        return sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }


    //setter
    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public void setSender(User sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "AbstractMessage{" +
                "id=" + id +
                ", recipient=" + recipient +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
