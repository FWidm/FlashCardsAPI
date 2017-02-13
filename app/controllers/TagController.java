package controllers;

import models.Tag;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.TagRepository;
import util.JsonUtil;
import util.RequestKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author Fabian Widmann
 */
public class TagController extends Controller {
    /**
     * Returns a json list of all tags
     *
     * @return ok - contains a list of tags or an empty list
     */
    public Result getTags() {
        List<Tag> tagList = TagRepository.getTags();
        return ok(JsonUtil.toJson(tagList));
    }

    /**
     * Returns either one tag
     *
     * @param id of the tag
     * @return ok - and the card or notFound if the object with the given id does not exist
     */
    public Result getTag(Long id) {
        try {
            Tag tag = TagRepository.getTag(id);
            return ok(JsonUtil.toJson(tag));

        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no tag with id=" + id + " exists."));
        }
    }

    public Result getAttachedCards(Long id) {
        try {
            return ok(JsonUtil.toJson(TagRepository.getAttachedCards(id)));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no tag with id=" + id + " exists."));
        }
    }

    /**
     * get all cards attached to the tag. this is done by providing key value pairs via the query string.
     *
     * @return appropriate Result containing the cards or error
     */
    public Result getAttachedCardsByTags() {
        List<Long> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        try {
            Controller.request().queryString().forEach(
                    (String key, String[] values) -> {
                        Logger.debug("k=" + key + " v=" + Arrays.toString(values));
                        if (key.toLowerCase().contains(RequestKeys.GET_BY_ID)) {
                            for (String value : values) {
                                ids.add(Long.parseLong(value));
                            }
                        }
                        if (key.toLowerCase().contains(RequestKeys.GET_BY_NAME)) {
                            Collections.addAll(names, values);
                        }
                    });
            Logger.debug("names.size=" + names.size() + " || ids.size=" + ids.size());
            if (ids.size() == 0 && names.size() == 0)
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "This method does only work when ids and/or names of tags are passed via URL parameters '"
                                + RequestKeys.GET_BY_ID + "' or '" + RequestKeys.GET_BY_NAME + "' example: 'tags//cards?id=1&id=2&id=3...'"));

            return ok(JsonUtil.toJson(TagRepository.getCardByTagArray(ids, names)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                    "This method does only work when ids and/or names of tags are passed via URL parameters '"
                            + RequestKeys.GET_BY_ID + "' or '" + RequestKeys.GET_BY_NAME + "' example: 'tags//cards?id=1&id=2&id=3...'"));
        }
    }
}
