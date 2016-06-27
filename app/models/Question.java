package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.net.URI;

/**
 * Created by fabianwidmann on 17/06/16.
 */
@Entity
@JsonPropertyOrder({ JsonKeys.QUESTION_ID})
public class Question extends Model {
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.QUESTION_ID)
    @JsonProperty(JsonKeys.QUESTION_ID)
    private long id;

    @Constraints.Required
    private String question;
    private URI mediaURI;

    @ManyToOne
    @JoinColumn(name="author_id", referencedColumnName=JsonKeys.USER_ID)
    private User author;
//    @OneToOne(fetch= FetchType.LAZY)
//    @JoinColumn(name="parent_card_id")
//    @JsonIgnore
//    private FlashCard card;

    public static Model.Finder<Long, Question> find = new Model.Finder<Long, Question>(Question.class);

    public Question(String question, User author) {
        this.question = question;
        this.author = author;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", mediaURI=" + mediaURI +
                ", author=" + author +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public URI getMediaURI() {
        return mediaURI;
    }

    public void setMediaURI(URI mediaURI) {
        this.mediaURI = mediaURI;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
