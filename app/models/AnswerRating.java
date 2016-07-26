package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import util.JsonKeys;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.List;

import static com.avaje.ebean.Expr.eq;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@Entity
@DiscriminatorValue("AnswerRating")
public class AnswerRating extends Rating{
    @ManyToOne
    @JoinColumn(name= JsonKeys.ANSWER_ID, referencedColumnName = JsonKeys.ANSWER_ID)
    @JsonProperty(JsonKeys.ANSWER_ID)
    protected Answer ratedAnswer;

    public AnswerRating(User author, Answer ratedAnswer, int ratingModifier ) {
            this.ratedAnswer=ratedAnswer;
            this.author=author;
            this.ratingModifier=ratingModifier;
    }

    public static Model.Finder<Long, AnswerRating> find = new Model.Finder<Long, AnswerRating>(AnswerRating.class);

    /**
     * Checks if a combination of user and answer is already in the database.
     * @return false if no such combination exists.
     */
    public static boolean exists(User author, Answer answer){
        int hits=(Rating.find.where().and(eq(JsonKeys.USER_ID,author.getId()),eq(JsonKeys.ANSWER_ID,answer.getId())).findList().size());
        return hits!=0;
    }

    /**
     * Changes the rating to either add or substract the ratingmodifier. Updates the answer object to save those changes.
     */
    public void changeRating(){
        System.out.println("Modifying rating of answer="+ ratedAnswer.getId()+": "+ratedAnswer.getRating()+" to: "+(ratedAnswer.getRating()+ratingModifier));
        ratedAnswer.setRating(ratedAnswer.getRating()+ratingModifier);
        ratedAnswer.update();
    }

    /**
     * Changes the rating to either add or substract the ratingmodifier. Updates the answer object to save those changes.
     */
    public void compensate(){
        System.out.println("Compensating rating of answer="+ ratedAnswer.getId()+": "+ratedAnswer.getRating()+" to: "+(ratedAnswer.getRating()-ratingModifier));
        ratedAnswer.setRating(ratedAnswer.getRating()-ratingModifier);
        ratedAnswer.update();
    }

    @Override
    public String toString(){
        return "[id="+id+", author="+author+", ratingModifier="+ratingModifier+", ratedAnswer="+ratedAnswer+"]";
    }

    @Override
    public void save() {
        super.save();
        changeRating();
    }

    @Override
    public void delete() {
        compensate();
        super.delete();
    }
}
