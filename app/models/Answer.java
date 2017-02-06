package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.net.URI;
import java.util.Date;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 17/06/16.
 */
@Entity
@JsonPropertyOrder({ JsonKeys.ANSWER_ID})
public class Answer extends Model {
    @Id @GeneratedValue
    @Column(name = JsonKeys.ANSWER_ID)
    @JsonProperty(JsonKeys.ANSWER_ID)
    private long id;

    @Constraints.Required
    @Column(name = JsonKeys.ANSWER_TEXT)
    @JsonProperty(JsonKeys.ANSWER_TEXT)
    private String answerText;

    @JsonProperty(JsonKeys.ANSWER_HINT)
    @Column(name = JsonKeys.ANSWER_HINT)
    private String hintText;

    @JsonProperty(JsonKeys.URI)
    @Column(name = JsonKeys.URI)
    private URI uri;

   @ManyToOne
   @JoinColumn(name=JsonKeys.USER_ID, referencedColumnName=JsonKeys.USER_ID)
   @JsonProperty(JsonKeys.AUTHOR)
    private User author;

    @ManyToOne
    @JoinColumn(name=JsonKeys.ANSWER_CARD_ID)
    @JsonIgnore
    private FlashCard card;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z") @CreatedTimestamp
    @Column(name = JsonKeys.DATE_CREATED)
    @JsonProperty(JsonKeys.DATE_CREATED)
    private Date created;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @UpdatedTimestamp
    @Column(name = JsonKeys.DATE_UPDATED)
    @JsonProperty(JsonKeys.DATE_UPDATED)
    private Date lastUpdated;
    @JsonProperty(JsonKeys.RATING)
    private int rating;

    @JsonProperty(JsonKeys.ANSWER_CORRECT)
    @Column(name = JsonKeys.ANSWER_CORRECT)
    private boolean isCorrect;

    public static Model.Finder<Long, Answer> find = new Model.Finder<Long, Answer>(Answer.class);

    public Answer(String answerText, String hintText, User author) {
        this.answerText = answerText;
        this.hintText = hintText;
        this.author = author;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", answerText='" + answerText + '\'' +
                ", hintText='" + hintText + '\'' +
                ", uri=" + uri +
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

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
    @JsonIgnore
    public FlashCard getCard() {
        return card;
    }

    public Date getCreated() {
        return created;
    }


    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public void setCard(FlashCard card) {
        this.card = card;
        this.update();
    }
    /**
     * Adds the given rating to the current rating, updates this instance and calls the function on the corresponding user.
     * @param ratingModifier - describes the value that is added/subtracted from the current rating
     */
    public void updateRating(int ratingModifier){
        this.rating+=ratingModifier;
        this.update();
        //update user as well, work on the newest data from the db, not our local reference.
        User.find.byId(author.getId()).updateRating(ratingModifier);
    }
}
