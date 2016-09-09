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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        Map<String, String[]> urlParams = Controller.request().queryString();
        if(urlParams.keySet().contains(RequestKeys.ROOT)){
            if(urlParams.get(RequestKeys.ROOT)[0].equals("true")){
                List<Category> emptyGroups=Category.find.where().isNull(JsonKeys.CATEGORY_PARENT).findList();
                return ok(JsonUtil.getJson(emptyGroups));
            }
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
            //retrieve the parent by it's id
            if(receivedCategory.getParent()!=null && receivedCategory.getParent().getId()>0){
                category.setParent(Category.find.byId(receivedCategory.getParent().getId()));
            }
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
            //cardDecks themselve can be re-set to a new category at the moment,
            for (CardDeck cardDeck : cardDeckList) {
                cardDeck.setCategory(category);
                cardDeck.update();
            }
            String msg = "Category has been created!";
            if(information!="")
                msg+= information;
            return ok(JsonUtil.prepareJsonStatus(OK,msg ,category.getId()));

        }catch (Exception e){
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
