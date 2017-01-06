package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CardDeck;
import models.Category;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.CategoryRepository;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;
import util.UrlParamHelper;
import util.exceptions.InvalidInputException;
import util.exceptions.ObjectNotFoundException;
import util.exceptions.PartiallyModifiedException;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class CategoryController extends Controller {

    /**
     * Retrieves all Categories.
     *
     * @return HTTPResult
     */
    public Result getCategoryList() {
        return ok(JsonUtil.toJson(CategoryRepository.getCategoryList()));
    }

    /**
     * Retrieves the Category with the specific id, if it does not exist, return notFound.
     *
     * @param id
     * @return
     */
    public Result getCategory(Long id) {
        try {
            return ok(JsonUtil.toJson(CategoryRepository.getCategory(id)));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Category with the given id does not exist.", id));
        }
    }

    /**
     * Get all card decks in a category.
     *
     * @param id
     * @return
     */
    public Result getCategoryCardDecks(Long id) {
        try {
            return ok(JsonUtil.toJson(CategoryRepository.getCategoryCardDecks(id)));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Category with the given id does not exist.", id));
        }
    }

    /**
     * Returns the children of the specified carddeck.
     *
     * @param id
     * @return
     */
    public Result getChildren(Long id) {
        return ok(JsonUtil.toJson(CategoryRepository.getChildren(id)));
    }

    /**
     * Creates a new category, returns either an error or a success message.
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addCategory() {
        JsonNode json = request().body().asJson();

        try {
            Category addCategory=CategoryRepository.addCategory(json);
            return created(JsonUtil.prepareJsonStatus(CREATED,"Category has been created!",addCategory.getId()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            if(JsonKeys.debugging){
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS+" | cause: "+e.getCause()));
            }
            else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
        } catch (ObjectNotFoundException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        } catch (PartiallyModifiedException e) {
            return created(JsonUtil.prepareJsonStatus(OK,e.getMessage(), e.getObjectId()));
        }
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result updateCategory(Long id) {
        JsonNode json = request().body().asJson();

        try {
            Category updateCategory=CategoryRepository.updateCategory(id,json,request().method());
            return ok(JsonUtil.prepareJsonStatus(OK, "Category has been updated.", updateCategory.getId()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            if(JsonKeys.debugging){
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS+" | cause: "+e.getCause()));
            }
            else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
        } catch (ObjectNotFoundException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        } catch (InvalidInputException e) {
            return badRequest(JsonUtil
                    .prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        } catch (PartiallyModifiedException e) {
            return ok(JsonUtil.prepareJsonStatus(OK,e.getMessage(), e.getObjectId()));
        }
    }

/*    public Result deleteCategory(Long id){
        try{
            Category.find.byId(id).delete();
            return noContent();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return notFound();
    }*/
}
