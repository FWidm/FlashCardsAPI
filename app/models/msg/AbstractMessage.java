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
    // TODO: 08.02.2017 find out why column name isnt working here but everywhere else.
    @ManyToOne
    @JoinColumn(name = JsonKeys.MESSAGE_RECIPIENT, referencedColumnName = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.MESSAGE_RECIPIENT)
    protected User recipientUser;
    @Column(name = JsonKeys.MESSAGE_CONTENT)
    @JsonProperty(JsonKeys.MESSAGE_CONTENT)
    protected String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
    protected Date timestamp;

    /**
     * Create a new message for one recipientUser with a specific string message
     *
     * @param recipient
     * @param content
     */
    public AbstractMessage(User recipient, String content) {
        this.recipientUser = recipient;
        this.content = content;
    }

    //getter
    public long getId() {
        return id;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    //setter
    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AbstractMessage{" +
                "id=" + id +
                ", recipientUser=" + recipientUser +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
