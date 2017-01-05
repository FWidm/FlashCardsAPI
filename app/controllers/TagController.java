package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Tag;
import models.rating.Rating;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.RatingRepository;
import repositories.TagRepository;
import util.JsonUtil;
import util.RequestKeys;
import util.exceptions.DuplicateKeyException;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Map;


/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class TagController extends Controller {
    /**
     * Returns a json list of all tags
     * @return ok - contains a list of tags or an empty list
     */
    public Result getTags() {
        List<Tag> tagList= TagRepository.getTags();
        return ok(JsonUtil.toJson(tagList));
    }

    /**
     * Returns either one tag
     * @param id
     * @return ok - and the card or notFound if the object with the given id does not exist
     */
    public Result getTag(Long id) {
        try{
            Tag tag= TagRepository.getTag(id);
            return ok(JsonUtil.toJson(tag));

        }catch (NullPointerException e){
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no tag with id=" + id + " exists."));
        }
    }

    public Result getAttachedCards(Long id){
        try{
            return ok(JsonUtil.toJson(TagRepository.getAttachedCards(id)));
        }catch (NullPointerException e){
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no tag with id=" + id + " exists."));
        }
    }

    // TODO: 05.01.2017 Search for tag array 
}
