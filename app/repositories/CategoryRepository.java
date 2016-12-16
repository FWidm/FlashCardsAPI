package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CardDeck;
import models.Category;
import play.Logger;
import play.mvc.BodyParser;
import util.JsonKeys;
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
public class CategoryRepository {
    /**
     * Retrieves all Categories.
     *
     * @return HTTPResult
     */
    public static List<Category> getCategoryList() {
        if (UrlParamHelper.checkBool(RequestKeys.ROOT)) {
            List<Category> emptyGroups = Category.find.where().isNull(JsonKeys.CATEGORY_PARENT).findList();
            return emptyGroups;
        }
        return Category.find.all();
    }

    /**
     * Retrieves the Category with the specific id, if it does not exist, return notFound.
     *
     * @param id
     * @return
     */
    public static Category getCategory(Long id) {
        return Category.find.byId(id);
    }

    /**
     * Get all card decks in a category.
     *
     * @param id
     * @return
     */
    public static List<CardDeck> getCategoryCardDecks(Long id) {
        return Category.find.byId(id).getCardDecks();
    }

    /**
     * Returns the children of the specified carddeck.
     *
     * @param id
     * @return list of children
     */
    public static List<Category> getChildren(Long id) {
        Category parent = Category.find.byId(id);
        List<Category> children = Category.find.where().eq(JsonKeys.CATEGORY_PARENT, parent).findList();
        children.forEach(c-> System.out.println("c="+c));
        return children;
    }

    /**
     * Creates a new category, returns either an error or a success message.
     *
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Category addCategory(JsonNode json) throws PartiallyModifiedException, ObjectNotFoundException {
        String information = "";
        ObjectMapper mapper = new ObjectMapper();

        Category receivedCategory = mapper.convertValue(json, Category.class);
        Category category = new Category(receivedCategory);
        Logger.debug("rcvd=" + receivedCategory);

        if (receivedCategory.getId() > 0) {
            receivedCategory.setId(0);
        }
        //retrieve the parent by id
        if (json.has(JsonKeys.CATEGORY_PARENT) && json.get(JsonKeys.CATEGORY_PARENT).has(JsonKeys.CATEGORY_ID)) {
            Long parentId=json.get(JsonKeys.CATEGORY_PARENT).get(JsonKeys.CATEGORY_ID).asLong();
            category.setParent(parseParent(parentId));
        }

        //handle cardDeck list
        List<CardDeck> cardDeckList = new ArrayList<>();
        if (receivedCategory.getCardDecks() != null) {
            for (CardDeck cardDeck : receivedCategory.getCardDecks()) {
                CardDeck tmp = CardDeck.find.byId(cardDeck.getId());
                //add it to the list if it isnt already in and isnt null
                if (!cardDeckList.contains(tmp) && tmp != null && tmp.getCategory() == null) {
                    cardDeckList.add(cardDeck);
                }
                //if it is null we can't handle the request, thus we send a notFound to the user
                else if (tmp == null) {
                    throw new ObjectNotFoundException("One cardDeck could not be found.", cardDeck.getId());
                }
                //if it is just a duplicate, add it to the information flag we add onto the reply for the user to read.
                else if (cardDeckList.contains(tmp))
                    information += " Error adding cardDeck" + cardDeck.getId() + ", it was sent more than once.";
                else if (tmp.getCategory() != null) {
                    information += " Error adding cardDeck" + cardDeck.getId() + ", it already has a parent.";
                }
            }
            Logger.debug("deckList=" + cardDeckList);
            category.setCardDecks(cardDeckList);
        }

        Logger.debug("finishing=" + category);
        category.save();
        //cardDecks themselves can be re-set to a new category at the moment,
        for (CardDeck cardDeck : cardDeckList) {
            cardDeck.setCategory(category);
            cardDeck.update();
        }
        String msg = "Category has been created!";
        if (information != "") {
            throw new PartiallyModifiedException("Category has been created! Additional information: " + information, category.getId());
        }
        return category;


    }


    @BodyParser.Of(BodyParser.Json.class)
    public static Category updateCategory(Long id, JsonNode json, String method) throws InvalidInputException, ObjectNotFoundException, PartiallyModifiedException {
            String information = "";
            boolean append = UrlParamHelper.checkBool(RequestKeys.APPEND);
            Logger.debug("Appending? " + append);
            ObjectMapper mapper = new ObjectMapper();

            Category receivedCategory = mapper.convertValue(json, Category.class);
            Category category = Category.find.byId(id);

            //Check whether the request was a put and if it was check if a param is missing, if that is the case --> bad req.
            if (method.equals("PUT") && (!json.has(JsonKeys.CATEGORY_NAME) || !json.has(JsonKeys.CATEGORY_DECK) || !json.has(JsonKeys.CATEGORY_PARENT))) {
                throw new InvalidInputException("The Update method needs all details of the category, such as name, " +
                                "an array of carddeck (ids) and a parent (null or id of another category).");
            }
            if (json.has(JsonKeys.CATEGORY_NAME)) {
                category.setName(receivedCategory.getName());
            }

            //retrieve the parent by id
            if (json.has(JsonKeys.CATEGORY_PARENT)) {
                if(json.get(JsonKeys.CATEGORY_PARENT).has(JsonKeys.CATEGORY_ID)){
                    Long parentId=json.get(JsonKeys.CATEGORY_PARENT).get(JsonKeys.CATEGORY_ID).asLong();
                    if(id!=parentId)
                        category.setParent(parseParent(parentId));
                }
                else{
                    category.setParent(null);

                }
            }

            // TODO: 10.09.2016 Append mode is currently the std.!
            if (json.has(JsonKeys.CATEGORY_DECK)) {
                //handle cardDeck list
                List<CardDeck> cardDeckList = new ArrayList<>();
                if (!append) {
                    for (CardDeck cardDeck :
                            category.getCardDecks()) {
                        cardDeck.setCategory(null);
                        cardDeck.update();
                    }
                }
                if (receivedCategory.getCardDecks() != null) {
                    for (CardDeck cardDeck : receivedCategory.getCardDecks()) {
                        CardDeck tmp = CardDeck.find.byId(cardDeck.getId());
                        //add it to the list if it isn't already in and isn't null

                        if (!cardDeckList.contains(tmp) && tmp != null && tmp.getCategory() == null) {
                            cardDeckList.add(cardDeck);
                            cardDeck.setCategory(category);
                            cardDeck.update();
                        }
                        //if it is null we can't handle the request, thus we send a notFound to the user
                        else if (tmp == null) {
                            throw new ObjectNotFoundException("One cardDeck could not be found.", cardDeck.getId());
                        }
                        //if it is just a duplicate, add it to the information flag we add onto the reply for the user to read.
                        else if (cardDeckList.contains(tmp))
                            information += " Error adding cardDeck" + cardDeck.getId() + ", it was sent more than once.";
                        else if (tmp.getCategory() != null) {
                            information += " Error adding cardDeck" + cardDeck.getId() + ", it already has a parent.";
                        }
                    }
                    category.getCardDecks();
                    Logger.debug("deckList=" + cardDeckList + " category list=" + category.getCardDecks());
                }
            }

            category.update();

            if (information != "") {
                throw new PartiallyModifiedException("Category has been updated! Additional information: " + information, category.getId());
            }
            return category;
    }

/*    public static Result deleteCategory(Long id){
        try{
            Category.find.byId(id).delete();
            return noContent();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return notFound();
    }*/

    /**
     * Retrieves the parent category from the given category. If the id of the parent object cant be found in the database, throw the exception.
     * @param id of the parent
     * @return the category from db or null if null is received
     * @throws ObjectNotFoundException
     */
    public static Category parseParent(Long id) throws ObjectNotFoundException {
        if(id>0){
            Category parent = Category.find.byId(id);
            Logger.debug("got parent="+parent);
            if(parent!=null){
                return parent;
            }
            else
                throw new ObjectNotFoundException("Parent does not exist with the id="+id);
        }
        else
            return null;
    }
}
