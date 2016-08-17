package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xpath.internal.SourceTree;
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

import java.util.List;
import java.util.Map;

import static com.avaje.ebean.Expr.eq;


/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class RatingController extends Controller {
    /**
     * Returns a list of all Ratings per default. When needed, the caller can pre-filter the list via:
     * {@link RequestKeys#FLASHCARD_RATING}, {@link RequestKeys#ANSWER_RATING}, {@link RequestKeys#ANSWER_ID},
     * {@link RequestKeys#FLASHCARD_ID}, {@link RequestKeys#USER_ID}.
     *
     * @return a (filtered) list of Ratings
     */
    public Result getRatingList() {
        Map<String, String[]> urlParams = Controller.request().queryString();
        List<Rating> r;
        //by type
        if (urlParams.containsKey(RequestKeys.FLASHCARD_RATING)) {
            r = Rating.find.where().eq(util.JsonKeys.RATING_TYPE, RequestKeys.FLASHCARD_RATING).findList();
            return ok(JsonUtil.getJson(r));
        }
        if (urlParams.containsKey(RequestKeys.ANSWER_RATING)) {
            r = Rating.find.where().eq(util.JsonKeys.RATING_TYPE, RequestKeys.ANSWER_RATING).findList();
            return ok(JsonUtil.getJson(r));
        }
        //by id
        if (urlParams.containsKey(RequestKeys.USER_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.USER_ID)[0]);
            r = Rating.find.where().eq(JsonKeys.USER_ID, id).findList();
            return ok(JsonUtil.getJson(r));
        }
        if (urlParams.containsKey(RequestKeys.FLASHCARD_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.FLASHCARD_ID)[0]);
            r = Rating.find.where().eq(JsonKeys.FLASHCARD_ID, id).findList();
            return ok(JsonUtil.getJson(r));
        }
        if (urlParams.containsKey(RequestKeys.ANSWER_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.ANSWER_ID)[0]);
            r = Rating.find.where().eq(JsonKeys.ANSWER_ID, id).findList();
            return ok(JsonUtil.getJson(r));
        } else {
            r = Rating.find.all();
            return ok(JsonUtil.getJson(r));
        }
    }

    /**
     * Retrieves a rating by its id.
     *
     * @param id
     * @return either the card or a notfound with an error status
     */
    public Result getRating(long id) {
        try {
            Rating card = Rating.find.byId(id);
            return ok(JsonUtil.getJson(card));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no rating with id=" + id + " exists."));
        }
    }

    /**
     * Creates a new Rating object for either type (Answer/Cardrating)
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addRating() {
        try {
            JsonNode json = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();
            if(JsonKeys.debugging) Logger.debug("json="+json);
            if (json.has(JsonKeys.ANSWER)) {
                AnswerRating answerRating = JsonUtil.parseAnswerRating(json);
                if (answerRating.getAuthor() != null && answerRating.getRatedAnswer() != null && answerRating.getRatingModifier() != 0) {
                    //check for duplicates
                    if (!AnswerRating.exists(answerRating.getAuthor(), answerRating.getRatedAnswer())) {
                        //when there are none, save, return ok.
                        answerRating.save();
                        return ok(JsonUtil.prepareJsonStatus(OK, "Rating has been created!", answerRating.getId()));
                    } else {
                        //return forbidden with the id the user is most likely searching for.
                        Long id = (Rating.find.where().and(eq(JsonKeys.USER_ID, answerRating.getAuthor().getId()), eq(JsonKeys.ANSWER_ID, answerRating.getRatedAnswer().getId())).findUnique().getId());
                        return forbidden(JsonUtil.prepareJsonStatus(FORBIDDEN, "Rating already exists!", id));
                    }
                }
            } else if (json.has(JsonKeys.FLASHCARD)) {
                CardRating cardRating = JsonUtil.parseCardRating(json);
                if (cardRating.getAuthor() != null && cardRating.getRatedFlashCard() != null && cardRating.getRatingModifier() != 0) {
                    //check for duplicates
                    if (!CardRating.exists(cardRating.getAuthor(), cardRating.getRatedFlashCard())) {
                        //when there are none, save, return ok.
                        cardRating.save();
                        return ok(JsonUtil.prepareJsonStatus(OK, "Rating has been created!", cardRating.getId()));
                    } else {
                        //return forbidden with the id the user is most likely searching for.
                        Long id = (Rating.find.where().and(eq(JsonKeys.USER_ID, cardRating.getAuthor().getId()), eq(JsonKeys.ANSWER_ID, cardRating.getRatedFlashCard().getId())).findUnique().getId());
                        return forbidden(JsonUtil.prepareJsonStatus(FORBIDDEN, "Rating already exists!", id));
                    }
                }
            } else
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A rating can contain: " + JsonKeys.RATING_JSON_ELEMENTS));

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return ok();
    }

    /**
     * Deletes a rating by it's id, compensates the rating of affected users/cards/answers automagically.
     * @param id
     * @return
     */
    public Result deleteRating(Long id) {
        try {
            Rating.find.byId(id).delete();
            return noContent();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,"No rating with the given id was found",id));
        }
    }
}
