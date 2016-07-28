package models.rating;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.FlashCard;
import models.User;
import util.JsonKeys;
import util.RequestKeys;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.util.Date;

import static com.avaje.ebean.Expr.eq;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@Entity
@DiscriminatorValue(RequestKeys.CARD_RATING)
public class CardRating extends Rating{

    @ManyToOne
    @JoinColumn(name= JsonKeys.FLASHCARD_ID, referencedColumnName = JsonKeys.FLASHCARD_ID)
    @JsonProperty(JsonKeys.ANSWER_ID)
    protected FlashCard ratedFlashCard;

    public CardRating(User author, FlashCard ratedFlashCard, int ratingModifier ) {
        this.ratedFlashCard = ratedFlashCard;
        this.author=author;
        this.ratingModifier=ratingModifier;
    }

    public static Model.Finder<Long, AnswerRating> find = new Model.Finder<Long, AnswerRating>(AnswerRating.class);

    /**
     * Changes the rating to either add or substract the ratingmodifier. Updates the answer object to save those changes.
     */
    public void apply(){
//        System.out.println("Modifying rating of ratedFlashCard="+ ratedFlashCard.getId()+": "+ratedFlashCard.getRating()+" to: "+(ratedFlashCard.getRating()+ratingModifier));
        System.out.println(new Date().getTime()+"calling updateRating");
        ratedFlashCard.updateRating(ratingModifier);
        ratedFlashCard.update();
    }

    /**
     * Changes the rating to either add or substract the ratingmodifier. Updates the answer object to save those changes.
     */
    private void compensate(){
//        System.out.println("Compensating rating of answer="+ ratedFlashCard.getId()+": "+ratedFlashCard.getRating()+" to: "+(ratedFlashCard.getRating()-ratingModifier));
        System.out.println(new Date().getTime()+"calling updateRating");
        ratedFlashCard.updateRating(-1*ratingModifier);
        ratedFlashCard.update();
    }
    /**
     * Checks if a combination of user and card is already in the database.
     * @return false if no such combination exists.
     */
    public static boolean exists(User author, FlashCard card){
        return (Rating.find.where().and(eq(JsonKeys.USER_ID,author.getId()),eq(JsonKeys.FLASHCARD_ID,card.getId())).findList().size()!=0);
    }

    @Override
    public String toString(){
        return "[id="+id+", author="+author+", ratingModifier="+ratingModifier+", ratedFlashCard="+ ratedFlashCard +"]";
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
