package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import util.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 09/08/16.
 */
public class CardDeckController extends Controller {

    public Result getCardDecks() {
        return ok(JsonUtil.getJson(CardDeck.find.all()));
    }

    public Result getCardDeck(long id) {
        try {
            return ok(JsonUtil.getJson(CardDeck.find.byId(id)));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "CardDeck with the given id does not exist.", id));
        }
    }

    /**
     * Returns the Cards in one specific deck. Can be filtered by specifying the starting index (?start=x) and
     * size of the returned list (?size=y).
     * ex. [1][2][3][4] with size=2, start=1 -> [2][3]
     * - if start is bigger than the highest index, an empty list is returned.
     * - if size is equal to 0, an empty list is returned
     * @param id
     * @return
     */
    public Result getCardDeckCards(long id) {
        try {
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
                        flashCards= new LinkedList<>();

                } else {
                    flashCards = flashCards.subList(0,
                            Math.min(Integer.parseInt(limitVal), flashCards.size()));
                }
            }
            return ok(JsonUtil.getJson(flashCards));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "CardDeck with the given id does not exist.", id));
        }
    }

    public Result deleteCardDeck(long id) {
        CardDeck.find.byId(id).delete();
        return noContent();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result addCardDeck() {
        JsonNode json = request().body().asJson();
        Logger.debug("AddCard json=" + json);

        ObjectMapper mapper = new ObjectMapper();
        try {
            CardDeck requestObject = mapper.convertValue(json, CardDeck.class);

            //retrieve the correct cards list by either parsing the id and getting the correct card or the attributes to a new card.
// TODO: 23/08/16 When an empty group is specified, catch error
            List<FlashCard> cardList = parseCards(requestObject);

            CardDeck deck = new CardDeck(requestObject);
            if (deck.getUserGroup() == null) {
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Could not create deck without specifying the group it belongs to."));
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
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Could not create deck with given cards, some of them already are in a deck.", JsonKeys.CARDDECK_CARDS, cardIds));
            }

            return ok(JsonUtil.prepareJsonStatus(OK, "Carddeck has been created!", deck.getId()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Pattern p = Pattern.compile("\\\"(.*?)\\\"");
            Matcher m = p.matcher(e.getCause().toString());
            String cause = "";
            if (m.find())
                cause = m.group().substring(1, m.group().length() - 1);
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Request contained an element that is not " +
                    "expected for a card deck. Unknown Attribute=" + cause + ", expected Attributes=" + JsonKeys.CARDDECK_JSON_ELEMENTS));
        } catch (NullPointerException e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error while retrieving cards, " + e.getMessage()));
        }

    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateCardDeck(long id) {
        JsonNode json = request().body().asJson();
        Logger.debug("UpdateCardDeck json=" + json);
        ObjectMapper mapper = new ObjectMapper();
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
        try {
            CardDeck toUpdate = CardDeck.find.byId(id);
            if (request().method().equals("PUT") && (!json.has(JsonKeys.CARDDECK_NAME) || !json.has(JsonKeys.CARDDECK_CARDS)
                    || !json.has(JsonKeys.CARDDECK_DESCRIPTION) || !json.has(JsonKeys.CARDDECK_GROUP))) {
                if (JsonKeys.debugging)
                    Logger.debug(!json.has(JsonKeys.CARDDECK_NAME) + " " + !json.has(JsonKeys.CARDDECK_CARDS)
                            + " " + !json.has(JsonKeys.CARDDECK_DESCRIPTION));
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "The Update method needs all details of the carddeck: " + JsonKeys.CARDDECK_JSON_ELEMENTS, id));
            }

            CardDeck requestObject = mapper.convertValue(json, CardDeck.class);
            CardDeck deck = CardDeck.find.byId(id);

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
                    return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Request contained a group id that does not exist.", requestObject.getUserGroup().getId()));
                }
            }
            //retrieve the correct cards list by either parsing the id and getting the correct card or the attributes to a new card.
            if (json.has(JsonKeys.CARDDECK_CARDS)) {
                try {
                    List<FlashCard> cardList = new ArrayList<>();
                    if (appendMode) {
                        cardList.addAll(deck.getCards());
                        cardList.addAll(parseCards(requestObject));
                    } else {
                        cardList = parseCards(requestObject);
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
                        deck.setCards(cardList);
                        deck.update();
                        deck.getCards().forEach(card -> card.setDeck(deck));
                    } else {
                        // TODO: 17.08.2016 Rewrite this to have a better structure in the output {"statuscode":400,"description":"...","cards":[14,15]}
                        return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Could not create deck with given cards, some of them already are in a deck.", JsonKeys.CARDDECK_CARDS, cardIds));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error while retrieving cards, " + e.getMessage()));
                }
            }
            deck.update();

            return ok(JsonUtil.prepareJsonStatus(OK, "Carddeck has been updated!", deck.getId()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Pattern p = Pattern.compile("\\\"(.*?)\\\"");
            Matcher m = p.matcher(e.getCause().toString());
            String cause = "";
            if (m.find())
                cause = m.group().substring(1, m.group().length() - 1);
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Request contained an element that is not " +
                    "expected for a card deck. Unknown Attribute=" + cause + ", expected Attributes=" + JsonKeys.CARDDECK_JSON_ELEMENTS));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "CardDeck with the given id does not exist.", id));
        }
    }

    private List<FlashCard> parseCards(CardDeck requestObject) throws NullPointerException {
        List<FlashCard> cardList = new ArrayList<>();
//        Logger.debug("Got cardlist=" + requestObject.getCards());

        if (requestObject.getCards() != null) {
            for (int i = 0; i < requestObject.getCards().size(); i++) {
                FlashCard currentCard = requestObject.getCards().get(i);
//                Logger.debug("current id=" + currentCard.getId() + "  read from db=" + FlashCard.find.byId(currentCard.getId()));
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
