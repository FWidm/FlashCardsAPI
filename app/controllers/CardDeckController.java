package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.typesafe.config.ConfigException;
import models.*;
import models.rating.AnswerRating;
import models.rating.CardRating;
import models.rating.Rating;
import play.Logger;
import play.api.mvc.Flash;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import scala.collection.concurrent.Debug;
import util.*;
import views.html.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.avaje.ebean.Expr.like;

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
            e.printStackTrace();
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
        ObjectMapper mapper = new ObjectMapper();
        try {
            CardDeck requestObject = mapper.convertValue(json, CardDeck.class);

            //retrieve the correct cards list by either parsing the id and getting the correct card or the attributes to a new card.
            List<FlashCard> cardList = parseCards(requestObject);

            CardDeck deck = new CardDeck(requestObject);

            boolean canSetCards = true;
            List<Object> cardIds = new ArrayList<>(cardList.size());
            for (FlashCard c : cardList) {
                Logger.debug(c.getId() + "|" + c.getDeck());
                if (c.getDeck() != null) {
                    canSetCards = false;
                    Logger.debug("Card has a deck already, id=" + c.getId());
                    cardIds.add(c.getId());
                }

            }

            if (canSetCards) {
                deck.setCards(cardList);
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
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateCardDeck(long id) {
        JsonNode json = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        boolean appendMode = false;
        //be able to move cards from deck a to b
        boolean redirectMode=false;
        Map<String, String[]> urlParams = Controller.request().queryString();

        if (urlParams.containsKey(RequestKeys.APPEND)) {
            appendMode = Boolean.parseBoolean(urlParams.get(RequestKeys.APPEND)[0]);
        }
        if (urlParams.containsKey(RequestKeys.REDIRECT)) {
            redirectMode = Boolean.parseBoolean(urlParams.get(RequestKeys.REDIRECT)[0]);
        }
        if (JsonKeys.debugging) Logger.debug("Appending mode enabled? " + appendMode);
        try {
            CardDeck toUpdate = CardDeck.find.byId(id);
            if (request().method().equals("PUT") && (!json.has(JsonKeys.CARDDECK_NAME) || !json.has(JsonKeys.CARDDECK_CARDS)
                    || !json.has(JsonKeys.CARDDECK_DESCRIPTION) /*|| !json.has(JsonKeys.CARDDECK_GROUP)*/ )) {
                if (JsonKeys.debugging)
                    Logger.debug(!json.has(JsonKeys.CARDDECK_NAME) + " " + !json.has(JsonKeys.CARDDECK_CARDS)
                            + " " + !json.has(JsonKeys.CARDDECK_DESCRIPTION));
                return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST,
                        "The Update method needs all details of the carddeck: "+JsonKeys.CARDDECK_JSON_ELEMENTS,id ));
            }

            CardDeck requestObject = mapper.convertValue(json, CardDeck.class);
            CardDeck deck = CardDeck.find.byId(id);

            if(requestObject.getName()!=null){
                deck.setName(requestObject.getName());
            }
            if(requestObject.getDescription()!=null){
                deck.setDescription(requestObject.getDescription());
            }
//            if(requestObject.getGroup()!=null){
//
//            }
            // TODO: 17.08.2016 Appendmode, testing 
            //retrieve the correct cards list by either parsing the id and getting the correct card or the attributes to a new card.
            try{
                List<FlashCard> cardList = parseCards(requestObject);
                Logger.debug("Cardlist size="+cardList.size());
                boolean canSetCards = true;
                List<Object> cardIds = new ArrayList<>(cardList.size());
                for (FlashCard c : cardList) {
                    Logger.debug(c.getId() + "|" + c.getDeck());
                    if (c.getDeck() != null) {
                        canSetCards = false;
                        Logger.debug("Card has a deck already, id=" + c.getId());
                        cardIds.add(c.getId());
                    }
                }

                if (canSetCards || redirectMode) {
                    deck.setCards(cardList);
                    Logger.debug("New cardlist="+deck.getCards());
                    deck.update();
                    deck.getCards().forEach(card -> card.setDeck(deck));
                } else {
                    // TODO: 17.08.2016 Rewrite this to have a better structure in the output {"statuscode":400,"description":"...","cards":[14,15]}
                    return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Could not create deck with given cards, some of them already are in a deck.", JsonKeys.CARDDECK_CARDS, cardIds));
                }
            }catch (NullPointerException e) {
                e.printStackTrace();
                return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "No Card found with the given id", id));
            }

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
        }
    }

    private List<FlashCard> parseCards(CardDeck requestObject) throws NullPointerException{
        List<FlashCard> cardList = new ArrayList<>();
        Logger.debug("Got cardlist="+requestObject.getCards());

        if(requestObject.getCards()!=null){
            for (int i = 0; i < requestObject.getCards().size(); i++) {
                FlashCard currentCard = requestObject.getCards().get(i);
                Logger.debug("current id="+currentCard.getId()+ "  read from db="+FlashCard.find.byId(currentCard.getId()));
                if (currentCard.getId() > 0) {
                        FlashCard retrievedCard = FlashCard.find.byId(currentCard.getId());
                        Logger.debug("Got Card tmp=" + retrievedCard);
                        if (retrievedCard != null) {
                            cardList.add(retrievedCard);
                        }
                        if (JsonKeys.debugging) Logger.debug(i + " >> " + cardList.size());
                } else {
                    // TODO: 17.08.2016 Parse new questions if needed.
                }
            }
        }
        return cardList;
    }
}
