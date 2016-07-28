package controllers;

import models.FlashCard;
import models.User;
import models.rating.Rating;
import play.mvc.Controller;
import play.mvc.Result;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;

import java.util.List;
import java.util.Map;


/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class RatingController extends Controller{
    /**
     * Returns a list of all Ratings per default. When needed, the caller can pre-filter the list via:
     * {@link RequestKeys#FLASHCARD_RATING}, {@link RequestKeys#ANSWER_RATING}, {@link RequestKeys#ANSWER_ID},
     * {@link RequestKeys#FLASHCARD_ID}, {@link RequestKeys#USER_ID}.
     * @return a (filtered) list of Ratings
     */
    public Result getRatingList() {
        Map<String, String[]> urlParams = Controller.request().queryString();
        List<Rating> r;
        //by type
        if (urlParams.containsKey(RequestKeys.FLASHCARD_RATING)) {
            r=Rating.find.where().eq(util.JsonKeys.RATING_TYPE,RequestKeys.FLASHCARD_RATING).findList();
            return ok(JsonUtil.getJson(r));
        }
        if(urlParams.containsKey(RequestKeys.ANSWER_RATING)) {
            r=Rating.find.where().eq(util.JsonKeys.RATING_TYPE,RequestKeys.ANSWER_RATING).findList();
            return ok(JsonUtil.getJson(r));
        }
        //by id
        if(urlParams.containsKey(RequestKeys.USER_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.USER_ID)[0]);
            r=Rating.find.where().eq(JsonKeys.USER_ID,id).findList();
            return ok(JsonUtil.getJson(r));
        }
        if(urlParams.containsKey(RequestKeys.FLASHCARD_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.FLASHCARD_ID)[0]);
            r=Rating.find.where().eq(JsonKeys.FLASHCARD_ID,id).findList();
            return ok(JsonUtil.getJson(r));
        }
        if(urlParams.containsKey(RequestKeys.ANSWER_ID)) {
            Long id = Long.parseLong(urlParams.get(RequestKeys.ANSWER_ID)[0]);
            r=Rating.find.where().eq(JsonKeys.ANSWER_ID,id).findList();
            return ok(JsonUtil.getJson(r));
        }
        else{
            r = Rating.find.all();
            return ok(JsonUtil.getJson(r));
        }
    }

    /**
     * Retrieves a rating by its id.
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
}
