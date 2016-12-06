package repositories;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Answer;
import models.FlashCard;
import models.User;
import models.rating.AnswerRating;
import models.rating.CardRating;
import models.rating.Rating;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;
import util.exceptions.DuplicateKeyException;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Map;

import static com.avaje.ebean.Expr.eq;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class RatingRepository {
    /**
     * Returns a list of all Ratings per default. When needed, the caller can pre-filter the list via:
     * {@link RequestKeys#FLASHCARD_RATING}, {@link RequestKeys#ANSWER_RATING}, {@link RequestKeys#ANSWER_ID},
     * {@link RequestKeys#FLASHCARD_ID}, {@link RequestKeys#USER_ID}.
     *
     * @return a (filtered) list of Ratings
     */
    public static List<Rating> getRatings(Map<String, String[]> urlParams) {
        List<Rating> ratingList;
        //by type
        if (urlParams.containsKey(RequestKeys.FLASHCARD_RATING)) {
            ratingList = Rating.find.where().eq(util.JsonKeys.RATING_TYPE, RequestKeys.FLASHCARD_RATING).findList();
            return ratingList;
        }
        if (urlParams.containsKey(RequestKeys.ANSWER_RATING)) {
            ratingList = Rating.find.where().eq(util.JsonKeys.RATING_TYPE, RequestKeys.ANSWER_RATING).findList();
            return ratingList;
        }
        //by id
        if (urlParams.containsKey(RequestKeys.USER_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.USER_ID)[0]);
            ratingList = Rating.find.where().eq(JsonKeys.USER_ID, id).findList();
            return ratingList;
        }
        if (urlParams.containsKey(RequestKeys.FLASHCARD_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.FLASHCARD_ID)[0]);
            ratingList = Rating.find.where().eq(JsonKeys.FLASHCARD_ID, id).findList();
            return ratingList;
        }
        if (urlParams.containsKey(RequestKeys.ANSWER_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.ANSWER_ID)[0]);
            ratingList = Rating.find.where().eq(JsonKeys.ANSWER_ID, id).findList();
            return ratingList;
        } else {
            ratingList = Rating.find.all();
            return ratingList;
        }
    }

    /**
     * Retrieves a rating by its id.
     * @param id
     * @return either the card or a notfound with an error status
     */
    public static Rating getRating(long id) throws ObjectNotFoundException {
        try {
            Rating rating = Rating.find.byId(id);
            return rating;
        } catch (NullPointerException e) {
            throw new ObjectNotFoundException("Error, no rating with the given id exists",id);
        }
    }

    /**
     * Creates a new Rating object for either type (Answer/Cardrating)
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Rating addRating(JsonNode json) throws DuplicateKeyException, InvalidInputException {
        if (JsonKeys.debugging)
            Logger.debug("json=" + json);
        //check if the rating is for an answer or a card
        if (json.has(JsonKeys.ANSWER)) {
            AnswerRating answerRating = parseAnswerRating(json);
            if (answerRating.getAuthor() != null && answerRating.getRatedAnswer() != null && answerRating.getRatingModifier() != 0) {
                //check for duplicates
                if (!AnswerRating.exists(answerRating.getAuthor(), answerRating.getRatedAnswer())) {
                    //when there are none, save, return ok.
                    answerRating.save();
                    return answerRating;
                } else {
                    Long authorId=answerRating.getAuthor().getId();
                    Long answerId=answerRating.getRatedAnswer().getId();
                    Logger.debug(">> Duplicate, throw exception! authorID="+authorId+" cardID="+answerId);
                    Long id=Rating.find.where().eq(JsonKeys.USER_ID,authorId).eq(JsonKeys.ANSWER_ID,answerId).findUnique().getId();
                    Logger.debug("Found Rating with author and answer, id="+id);
                    throw new DuplicateKeyException("Rating already exists!", id);
                }
            }
        } else if (json.has(JsonKeys.FLASHCARD)) {
            CardRating cardRating = parseCardRating(json);
            if (JsonKeys.debugging)
                Logger.debug("is cardRating: " + cardRating);
            if (cardRating.getAuthor() != null && cardRating.getRatedFlashCard() != null && cardRating.getRatingModifier() != 0) {
                //check for duplicates
                Logger.debug("duplicate check");
                if (!CardRating.exists(cardRating.getAuthor(), cardRating.getRatedFlashCard())) {
                    //when there are none, save, return ok.
                    cardRating.save();
                    return cardRating;
                } else {
                    Long authorId=cardRating.getAuthor().getId();
                    Long cardId=cardRating.getRatedFlashCard().getId();
                    Logger.debug(">> Duplicate, throw exception! authorID="+authorId+" cardID="+cardId);
                    Long id=Rating.find.where().eq(JsonKeys.USER_ID,authorId).eq(JsonKeys.FLASHCARD_ID,cardId).findUnique().getId();
                    Logger.debug("Found Rating with author and card, id="+id);
                    throw new DuplicateKeyException("Rating already exists!", id);
                }
            }
        }

        throw new InvalidInputException("Body did contain elements that are not allowed/expected. A rating can contain: " + JsonKeys.RATING_JSON_ELEMENTS);
    }

    /**
     * Deletes a rating by id, compensates the rating of affected users/cards/answers automagically.
     *
     * @param id
     * @return
     */
    public static Rating deleteRating(Long id) throws ObjectNotFoundException{
        try {
            //calls the delete Methods of either Card- or Answerrating and compensates the rating for affected objects
            Rating rating=Rating.find.byId(id);
            rating.delete();
            return rating;
        } catch (NullPointerException e) {
            throw new ObjectNotFoundException("No rating with the given id was found", id);
        }
    }

    /**
     * Parses a cardrating object from the given jsonnode.
     *
     * @param json
     * @return cardrating
     */
    public static CardRating parseCardRating(JsonNode json) {
        User author = null;
        FlashCard flashCard = null;
        int modifier = 0;

        if (json.has(JsonKeys.AUTHOR)) {
            author = User.find.byId(json.get(JsonKeys.AUTHOR).get(JsonKeys.USER_ID).asLong());
//            System.out.println("Rating user=" + author);

        }
        if (json.has(JsonKeys.FLASHCARD)) {
            flashCard = FlashCard.find.byId(json.get(JsonKeys.FLASHCARD).get(JsonKeys.FLASHCARD_ID).asLong());
//            System.out.println("Rating answer=" + flashCard);

        }
        if (json.has(JsonKeys.RATING_MODIFIER)) {
            modifier = json.get(JsonKeys.RATING_MODIFIER).asInt();
        }

        CardRating rating = new CardRating(author, flashCard, modifier);
//        System.out.println("Rating object=" + rating);

        return rating;
    }

    /**
     * Parses a answerrating object from the given jsonnode.
     *
     * @param json of a card
     * @return answerrating
     */
    private static AnswerRating parseAnswerRating(JsonNode json) {
        User author = null;
        Answer answer = null;
        int modifier = 0;

        if (json.has(JsonKeys.AUTHOR)) {
            author = User.find.byId(json.get(JsonKeys.AUTHOR).get(JsonKeys.USER_ID).asLong());
//            System.out.println("Rating user=" + author);

        }
        if (json.has(JsonKeys.ANSWER)) {
            answer = Answer.find.byId(json.get(JsonKeys.ANSWER).get(JsonKeys.ANSWER_ID).asLong());
//            System.out.println("Rating answer=" + answer);

        }
        if (json.has(JsonKeys.RATING_MODIFIER)) {
            modifier = json.get(JsonKeys.RATING_MODIFIER).asInt();
        }

        //        System.out.println("Rating object=" + rating);

        return new AnswerRating(author, answer, modifier);
    }
}
