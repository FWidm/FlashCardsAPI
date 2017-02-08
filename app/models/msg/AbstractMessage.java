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
@Table (name = JsonKeys.MESSAGE)
@DiscriminatorColumn(name = JsonKeys.MESSAGE_TYPE, discriminatorType = DiscriminatorType.STRING)
@JsonPropertyOrder({JsonKeys.MESSAGE_ID})
public abstract class AbstractMessage extends Model {
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.MESSAGE_ID)
    @JsonProperty(JsonKeys.MESSAGE_ID)
    protected long id;

    // TODO: 08.02.2017 find out why column name isnt working here but everywhere else. 
    @ManyToOne
    @Column(name = JsonKeys.MESSAGE_RECIPIENT)
    @JsonProperty(JsonKeys.MESSAGE_RECIPIENT)
    protected User recipient;

    @Column(name = JsonKeys.MESSAGE_CONTENT)
    @JsonProperty(JsonKeys.MESSAGE_CONTENT)
    protected String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
    protected Date timestamp;

    public static Finder<Long, AbstractMessage> find = new Finder<>(AbstractMessage.class);

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
