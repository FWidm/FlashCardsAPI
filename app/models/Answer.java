package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.net.URI;
import java.util.Date;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 17/06/16.
 */
@Entity
public class Answer extends Model {
    @Id @GeneratedValue
    private long id;

    @Constraints.Required
    private String answerText;
    private String hint;
    private URI mediaURI;

   @ManyToOne//OneToMany??
   @JoinColumn(name="author_id", referencedColumnName="id")
    private User author;

    @ManyToOne
    @JoinColumn(name="parent_card_id")
    @JsonIgnore
    private FlashCard card;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z") @CreatedTimestamp
    private Date created;

    private int rating;

    public static Model.Finder<Long, Answer> find = new Model.Finder<Long, Answer>(Answer.class);

    public Answer(String answerText, String hint, User author) {
        this.answerText = answerText;
        this.hint = hint;
        this.author = author;
    }


    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", answerText='" + answerText + '\'' +
                ", hint='" + hint + '\'' +
                ", mediaURI=" + mediaURI +
                ", author=" + author +
                ", card=" + card +
                ", created=" + created +
                ", rating=" + rating +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
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

    public FlashCard getCard() {
        return card;
    }

    public Date getCreated() {
        return created;
    }

    public void setCard(FlashCard card) {
        this.card = card;
        this.update();
    }
}
