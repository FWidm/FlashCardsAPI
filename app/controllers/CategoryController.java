package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CardDeck;
import models.Category;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import util.JsonKeys;
import util.JsonUtil;
import util.RequestKeys;
import util.UrlParamHelper;
import util.exceptions.ObjectNotFoundException;

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
        if(UrlParamHelper.checkBool(RequestKeys.ROOT)){
            List<Category> emptyGroups=Category.find.where().isNull(JsonKeys.CATEGORY_PARENT).findList();
            return ok(JsonUtil.getJson(emptyGroups));
        }
        return ok(JsonUtil.getJson(Category.find.all()));
    }

    /**
     * Retrieves the Category with the specific id, if it does not exist, return notFound.
     * @param id
     * @return
     */
    public Result getCategory(Long id) {
        try {
            if(UrlParamHelper.keyExists(RequestKeys.CHILDREN)){
                Category parent = Category.find.byId(id);
                List<Category> children = Category.find.where().eq(JsonKeys.CATEGORY_PARENT,parent).findList();
                return ok(JsonUtil.getJson(children));
            }
            return ok(JsonUtil.getJson(Category.find.byId(id)));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Category with the given id does not exist.", id));
        }
    }

    /**
     * Creates a new category, returns either an error or a success message.
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addCategory(){
        try{
            String information="";
            JsonNode json = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();

            Category receivedCategory=mapper.convertValue(json, Category.class);
            Category category=new Category(receivedCategory);
            Logger.debug("rcvd="+receivedCategory);

            if(receivedCategory.getId()>0){
                receivedCategory.setId(0);
            }
            //retrieve the parent by id
            category.setParent(JsonUtil.parseParent(receivedCategory));

            //handle cardDeck list
            List<CardDeck> cardDeckList=new ArrayList<>();
            if(receivedCategory.getCardDeckList()!=null){
                for (CardDeck cardDeck : receivedCategory.getCardDeckList()) {
                    CardDeck tmp = CardDeck.find.byId(cardDeck.getId());
                    //add it to the list if it isnt already in and isnt null
                    if(!cardDeckList.contains(tmp) && tmp!=null && tmp.getCategory()==null) {
                        cardDeckList.add(cardDeck);
                    }
                    //if it is null we can't handle the request, thus we send a notFound to the user
                    else if(tmp==null){
                        return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "One cardDeck could not be found.",cardDeck.getId()));
                    }
                    //if it is just a duplicate, add it to the information flag we add onto the reply for the user to read.
                    else if(cardDeckList.contains(tmp))
                        information+=" Error adding cardDeck"+cardDeck.getId()+", it was sent more than once.";
                    else if(tmp.getCategory()!=null){
                        information+=" Error adding cardDeck"+cardDeck.getId()+", it already has a parent.";
                    }
                }
                Logger.debug("deckList="+cardDeckList);
                category.setCardDeckList(cardDeckList);
            }
            
            Logger.debug("finishing="+category);
            category.save();
            //cardDecks themselves can be re-set to a new category at the moment,
            for (CardDeck cardDeck : cardDeckList) {
                cardDeck.setCategory(category);
                cardDeck.update();
            }
            String msg = "Category has been created!";
            if(information!="")
                msg+= information;
            return ok(JsonUtil.prepareJsonStatus(OK,msg ,category.getId()));

        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A category can contain: " + JsonKeys.CATEGORY_JSON_ELEMENTS));
        }catch (ObjectNotFoundException e){
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, e.getMessage()));
        }
    }



    @BodyParser.Of(BodyParser.Json.class)
    public Result updateCategory(Long id){
        try{
            String information="";
            boolean append=UrlParamHelper.checkBool(RequestKeys.APPEND);
            Logger.debug("Appending? "+append);
            JsonNode json = request().body().asJson();
            ObjectMapper mapper = new ObjectMapper();

            Category receivedCategory=mapper.convertValue(json, Category.class);
            Category category=Category.find.byId(id);

            //Check whether the request was a put and if it was check if a param is missing, if that is the case --> bad req.
            if(request().method().equals("PUT") && (!json.has(JsonKeys.CATEGORY_NAME) || !json.has(JsonKeys.CATEGORY_DECK) || !json.has(JsonKeys.CATEGORY_PARENT))){
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "The Update method needs all details of the category, such as name, " +
                                "an array of carddeck (ids) and a parent (null or id of another category).",id));
            }
            if(json.has(JsonKeys.CATEGORY_NAME)){
                category.setName(receivedCategory.getName());
            }

            //retrieve the parent by id
            if(json.has(JsonKeys.CATEGORY_PARENT)){
                category.setParent(JsonUtil.parseParent(receivedCategory));
            }

            // TODO: 10.09.2016 Append mode is currently the std.!
            if(json.has(JsonKeys.CATEGORY_DECK)) {
                //handle cardDeck list
                List<CardDeck> cardDeckList = new ArrayList<>();
                if(!append){
                    for (CardDeck cardDeck :
                            category.getCardDeckList()) {
                            cardDeck.setCategory(null);
                            cardDeck.update();
                    }
                }
                if (receivedCategory.getCardDeckList() != null) {
                    for (CardDeck cardDeck : receivedCategory.getCardDeckList()) {
                        CardDeck tmp = CardDeck.find.byId(cardDeck.getId());
                        //add it to the list if it isn't already in and isn't null

                        if (!cardDeckList.contains(tmp) && tmp != null && tmp.getCategory() == null) {
                            cardDeckList.add(cardDeck);
                            cardDeck.setCategory(category);
                            cardDeck.update();
                        }
                        //if it is null we can't handle the request, thus we send a notFound to the user
                        else if (tmp == null) {
                            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "One cardDeck could not be found.", cardDeck.getId()));
                        }
                        //if it is just a duplicate, add it to the information flag we add onto the reply for the user to read.
                        else if (cardDeckList.contains(tmp))
                            information += " Error adding cardDeck" + cardDeck.getId() + ", it was sent more than once.";
                        else if (tmp.getCategory() != null) {
                            information += " Error adding cardDeck" + cardDeck.getId() + ", it already has a parent.";
                        }
                    }
                    category.setCardDeckList(cardDeckList);
                    Logger.debug("deckList=" + cardDeckList+" category list="+category.getCardDeckList());
                }
            }

            category.update();

            String msg = "Category has been updated!";
            if(information!="")
                msg+= information;
            return ok(JsonUtil.prepareJsonStatus(OK,msg ,category.getId()));

        }catch (NullPointerException e){
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,"Error, category does not exist",id));
        }
        catch (Exception e){
            e.printStackTrace();
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST, "Body did contain elements that are not allowed/expected. A category can contain: " + JsonKeys.CATEGORY_JSON_ELEMENTS));
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
