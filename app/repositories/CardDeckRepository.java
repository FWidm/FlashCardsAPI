package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CardDeck;
import models.FlashCard;
import models.User;
import models.UserGroup;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import util.JsonKeys;
import util.RequestKeys;
import util.UrlParamHelper;
import util.UserOperations;
import util.exceptions.DuplicateKeyException;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;
import util.exceptions.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Widmann
 */
public class CardDeckRepository {
    public static List<CardDeck> getCardDecks() {
        return CardDeck.find.all();
    }

    public static CardDeck getCardDeck(long id) {
        return CardDeck.find.byId(id);
    }

    /**
     * Returns the Cards in one specific deck. Can be filtered by specifying the starting index (?start=x) and
     * size of the returned list (?size=y).
     * ex. [1][2][3][4] with size=2, start=1 -> [2][3]
     * - if start is bigger than the highest index, an empty list is returned.
     * - if size is equal to 0, an empty list is returned
     *
     * @param id of a card
     * @return cards of a deck or an error.
     */
    public static List<FlashCard> getCardDeckCards(long id) {
        List<FlashCard> flashCards = CardDeck.find.byId(id).getCards();
        String limitVal = UrlParamHelper.getValue(RequestKeys.SIZE);
        String startVal = UrlParamHelper.getValue(RequestKeys.START);

        if (limitVal != null) {
            if (startVal != null) {
                int start = Integer.parseInt(startVal);
                if (start >= 0 && start < flashCards.size()) {
                    flashCards = flashCards.subList(start,
                            Math.min(Integer.parseInt(limitVal) + start, flashCards.size()));
                } else
                    flashCards = new LinkedList<>();

            } else {
                flashCards = flashCards.subList(0,
                        Math.min(Integer.parseInt(limitVal), flashCards.size()));
            }
        }
        return flashCards;
    }

    /**
     * Delete a carddeck by id
     *
     * @param id    of the deck
     * @param email of the deleting user
     * @return deck or exception
     * @throws NotAuthorizedException if user is not authorized
     */
    public static CardDeck deleteCardDeck(long id, String email) throws NotAuthorizedException {
        User author = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        CardDeck deck = CardDeck.find.byId(id);
        if (deck == null)
            throw new NullPointerException();
        if (!author.hasRight(UserOperations.EDIT_DECK, deck))
            throw new NotAuthorizedException("This user is not authorized to delete the deck with this id.");
        deck.delete();

        return deck;
    }

    /**
     * Adds a new carddeck via the passed jsonnode
     *
     * @param json content of the carddeck:
     *             {
     *             "cardDeckName": "{{cardDeckName}}",
     *             "cardDeckDescpription": "",
     *             "cards": [
     *             {
     *             "flashcardId": {{cardId1}}
     *             }
     *             ],
     *             "userGroup":{
     *             "groupId":{{groupId}}
     *             }
     *             }
     * @return the newly created deck or an exception
     * @throws InvalidInputException if no group is specified
     * @throws DuplicateKeyException if the passed cards are already in another deck
     * @throws NotAuthorizedException if the user is not authorized
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static CardDeck addCardDeck(JsonNode json) throws InvalidInputException, DuplicateKeyException, NotAuthorizedException {

        ObjectMapper mapper = new ObjectMapper();
        CardDeck requestObject = mapper.convertValue(json, CardDeck.class);

        //retrieve the correct cards list by either parsing the id and getting the correct card or the attributes to a new card.
        List<FlashCard> cardList = parseCards(requestObject);
        Logger.debug("Group="+requestObject.getUserGroup());
        CardDeck deck = new CardDeck(requestObject);
        if (requestObject.getUserGroup()==null || requestObject.getUserGroup().getId() == null) {
            throw new InvalidInputException("Could not create deck without specifying the group it belongs to.");
        }

        boolean canSetCards = true;
        List<Object> cardIds = new ArrayList<>(cardList.size());
        for (FlashCard c : cardList) {
//                Logger.debug(c.getId() + "|" + c.getDeck());
            if (c.getDeck() != null) {
                canSetCards = false;
                Logger.debug("Card has a deck already, id=" + c.getId());
                cardIds.add(c.getId());
            }

        }
//            Logger.debug("deck=" + cardList + " size=" + cardList.size());
        if (canSetCards) {
            deck.setCards(cardList);
            Logger.debug("Saving deck with cards=" + deck.getCards());
            deck.save();

            deck.getCards().forEach(card -> card.setDeck(deck));
        } else {
            // TODO: 17.08.2016 Rewrite this to have a better structure in the output {"statuscode":400,"description":"...","cards":[14,15]}
            throw new DuplicateKeyException("Could not create deck with given cards, some of them already are in a deck.", cardIds);
        }

        return deck;

    }

    /**
     * Get the Usergroup of one deck by id.
     *
     * @param id of the deck
     * @return usergroup of the deck
     * @throws NullPointerException if deck does not exist.
     */
    public static UserGroup getDeckUserGroup(long id) throws NullPointerException {
        CardDeck deck = CardDeck.find.byId(id);
        Logger.debug("Deck found: " + deck);
        UserGroup group = deck.getUserGroup();
        return group;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static CardDeck updateCardDeck(long id, String email, JsonNode json, String method) throws InvalidInputException, ObjectNotFoundException, DuplicateKeyException, NotAuthorizedException {
        User author = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        CardDeck deck = CardDeck.find.byId(id);
        if (JsonKeys.debugging)
            Logger.debug("updateCardDeck");

        if (!author.hasRight(UserOperations.EDIT_DECK, deck))
            throw new NotAuthorizedException("This user is not authorized to modify the deck with this id.");

        boolean appendMode = false;
        //be able to move cards from deck a to b
        boolean redirectMode = false;
        Map<String, String[]> urlParams = Controller.request().queryString();

        if (urlParams.containsKey(RequestKeys.APPEND)) {
            appendMode = Boolean.parseBoolean(urlParams.get(RequestKeys.APPEND)[0]);
        }
        if (urlParams.containsKey(RequestKeys.REDIRECT)) {
            redirectMode = Boolean.parseBoolean(urlParams.get(RequestKeys.REDIRECT)[0]);
        }
        if (JsonKeys.debugging)
            Logger.debug("Appending mode enabled? " + appendMode + " redirect the cards from other decks? " + redirectMode);

        CardDeck toUpdate = CardDeck.find.byId(id);
        if (method.equals("PUT") && (!json.has(JsonKeys.CARDDECK_NAME) || !json.has(JsonKeys.CARDDECK_CARDS)
                || !json.has(JsonKeys.CARDDECK_DESCRIPTION) || !json.has(JsonKeys.CARDDECK_GROUP))) {
            if (JsonKeys.debugging)
                Logger.debug(!json.has(JsonKeys.CARDDECK_NAME) + " " + !json.has(JsonKeys.CARDDECK_CARDS)
                        + " " + !json.has(JsonKeys.CARDDECK_DESCRIPTION));
            throw new InvalidInputException("Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS);
        }
        ObjectMapper mapper = new ObjectMapper();
        CardDeck requestObject = mapper.convertValue(json, CardDeck.class);


        if (requestObject.getName() != null) {
            deck.setName(requestObject.getName());
        }
        if (requestObject.getDescription() != null) {
            deck.setDescription(requestObject.getDescription());
        }
        if (requestObject.getUserGroup() != null) {
            UserGroup userGroup = UserGroup.find.byId(requestObject.getUserGroup().getId());
            if (userGroup != null) {
                deck.setUserGroup(userGroup);
            } else {
                throw new ObjectNotFoundException("Request contained a group id that does not exist.", requestObject.getUserGroup().getId());
            }
        }
        //retrieve the correct cards list by either parsing the id and getting the correct card or the attributes to a new card.
        if (json.has(JsonKeys.CARDDECK_CARDS)) {
            List<FlashCard> cardList = new ArrayList<>();
            if (appendMode) {
                cardList.addAll(deck.getCards());
                cardList.addAll(parseCards(requestObject));

            } else {
                cardList = parseCards(requestObject);
                // TODO: 05.01.2017 delete replaced cards
            }

            boolean canSetCards = true;
            List<Object> cardIds = new ArrayList<>(cardList.size());
            for (FlashCard c : cardList) {
//                    Logger.debug(c.getId() + "|" + c.getDeck());
                if (c.getDeck() != null && c.getDeck().getId() != id) {
                    canSetCards = false;
                    Logger.debug("Card has a deck already, id=" + c.getId());
                    cardIds.add(c.getId());
                }
            }

            if (canSetCards || redirectMode) {
                // TODO: 05.01.2017 Test redirect
                deck.setCards(cardList);
                deck.update();
                deck.getCards().forEach(card -> card.setDeck(deck));
            } else {
                // TODO: 17.08.2016 Rewrite this to have a better structure in the output {"statuscode":400,"description":"...","cards":[14,15]}
                throw new DuplicateKeyException("Could not create deck with given cards, some of them already are in a deck.", cardIds);
            }
        }
        deck.update();

        return deck;
    }

    private static List<FlashCard> parseCards(CardDeck requestObject) throws NullPointerException {
        List<FlashCard> cardList = new ArrayList<>();
        Logger.debug("Parsecards: Got cardlist=" + requestObject.getCards());

        if (requestObject.getCards() != null) {
            for (int i = 0; i < requestObject.getCards().size(); i++) {
                FlashCard currentCard = requestObject.getCards().get(i);
                Logger.debug("current id=" + currentCard.getId() + "  read from db=" + FlashCard.find.byId(currentCard.getId()));
                if (currentCard.getId() > 0) {
                    FlashCard retrievedCard = FlashCard.find.byId(currentCard.getId());
                    if (retrievedCard != null) {
                        cardList.add(retrievedCard);
                    } else
                        throw new NullPointerException("Card with the id=" + requestObject.getCards().get(i).getId() + " does not exist");
                } else {
                    // TODO: 17.08.2016 Parse new questions if needed.
                }
            }
        }
        Logger.debug("cards=" + cardList);
        return cardList;
    }
}
