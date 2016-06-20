package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.net.URI;

/**
 * Created by fabianwidmann on 17/06/16.
 */
@Entity
public class Answer extends Model {
    @Id @GeneratedValue
    private long id;
    private int rating;

    @Constraints.Required
    private String answerText;
    private String hint;
    private URI mediaURI;
    @OneToOne(fetch=FetchType.LAZY) //OneToMany??
    @JoinColumn(name="author_id", referencedColumnName="id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_card_id")
    @JsonIgnore
    private FlashCard card;

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
                ", rating=" + rating +
                ", answerText='" + answerText + '\'' +
                ", hint='" + hint + '\'' +
                ", mediaURI=" + mediaURI +
                ", author=" + author +
                ", card=" + card +
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

    public void setCard(FlashCard card) {
        this.card = card;
        this.update();
    }
}
