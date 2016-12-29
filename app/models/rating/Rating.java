package models.rating;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.User;
import util.JsonKeys;

import javax.persistence.*;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(
        uniqueConstraints=
        @UniqueConstraint(columnNames={JsonKeys.USER_ID, JsonKeys.ANSWER_ID, JsonKeys.FLASHCARD_ID})
)
@DiscriminatorColumn(name=util.JsonKeys.RATING_TYPE, discriminatorType = DiscriminatorType.STRING)
public abstract class Rating extends Model {
    @Id @GeneratedValue
    @Column(name = JsonKeys.RATING_ID)
    @JsonProperty(JsonKeys.RATING_ID)
    protected long id;
    @ManyToOne
    @JoinColumn(name=JsonKeys.USER_ID, referencedColumnName = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.AUTHOR)
    protected User author;
    //VoteType for 5* vs. +-1 style?
    @JsonProperty(JsonKeys.RATING_MODIFIER)
    @Column(name = JsonKeys.RATING_MODIFIER)
    protected int ratingModifier;

    public static Model.Finder<Long, Rating> find = new Model.Finder<Long, Rating>(Rating.class);

    public long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public int getRatingModifier() {
        return ratingModifier;
    }

    public void setRatingModifier(int ratingModifier) {
        this.ratingModifier = ratingModifier;
    }
}
