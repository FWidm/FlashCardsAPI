package models.rating;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Answer;
import models.User;
import util.JsonKeys;
import util.RequestKeys;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static com.avaje.ebean.Expr.eq;

/**
 * @author Fabian Widmann
 */
@Entity
@DiscriminatorValue(RequestKeys.ANSWER_RATING)
public class AnswerRating extends Rating {
    public static Model.Finder<Long, AnswerRating> find = new Model.Finder<Long, AnswerRating>(AnswerRating.class);
    @ManyToOne
    @JoinColumn(name = JsonKeys.ANSWER_ID, referencedColumnName = JsonKeys.ANSWER_ID)
    @JsonProperty(JsonKeys.ANSWER)
    protected Answer ratedAnswer;

    public AnswerRating(User author, Answer ratedAnswer, int ratingModifier) {
        this.ratedAnswer = ratedAnswer;
        this.author = author;
        this.ratingModifier = ratingModifier;
    }

    /**
     * Checks if a combination of user and answer is already in the database.
     *
     * @return false if no such combination exists.
     */
    public static boolean exists(User author, Answer answer) {
        int hits = (Rating.find.where().and(eq(JsonKeys.USER_ID, author.getId()), eq(JsonKeys.ANSWER_ID, answer.getId())).findList().size());
        return hits > 0;
    }

    public Answer getRatedAnswer() {
        return ratedAnswer;
    }

    /**
     * Changes the rating to either add or substract the ratingmodifier. Updates the answer object to save those changes.
     */
    @Override
    public void apply() {
        //System.out.println("Modifying rating of answer="+ ratedAnswer.getId()+": "+ratedAnswer.getRating()+" to: "+(ratedAnswer.getRating()+ratingModifier));
        ratedAnswer.updateRating(ratingModifier);
        ratedAnswer.update();
    }


    /**
     * Changes the rating to either add or substract the ratingmodifier. Updates the answer object to save those changes.
     */
    @Override
    public void compensate() {
//        System.out.println("Compensating rating of answer="+ ratedAnswer.getId()+": "+ratedAnswer.getRating()+" to: "+(ratedAnswer.getRating()-ratingModifier));
        ratedAnswer.updateRating(-1 * ratingModifier);
        ratedAnswer.update();
    }

    @Override
    public String toString() {
        return "[id=" + id + ", author=" + author + ", ratingModifier=" + ratingModifier + ", ratedAnswer=" + ratedAnswer + "]";
    }

    @Override
    public void save() {
        super.save();
        apply();
    }

    @Override
    public void delete() {
        compensate();
        super.delete();
    }

}
