package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import repositories.CardDeckRepository;
import util.*;
import util.exceptions.DuplicateKeyException;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;
import util.exceptions.ObjectNotFoundException;

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
        return ok(JsonUtil.toJson(CardDeck.find.all()));
    }

    public Result getCardDeck(long id) {
        try {
            return ok(JsonUtil.toJson(CardDeckRepository.getCardDeck(id)));
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
            List<FlashCard> flashCards = CardDeckRepository.getCardDeckCards(id);
            return ok(JsonUtil.toJson(flashCards));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "CardDeck with the given id does not exist.", id));
        }
    }

    @Security.Authenticated(ActionAuthenticator.class)
    public Result deleteCardDeck(long id) {
       CardDeck cardDeck=CardDeckRepository.deleteCardDeck(id);
        return noContent();
    }

    /**
     * Calls the repository method to add a new deck, needs the caller to be authenticated via the token.
     * @return  OK if everything works,
     *          BAD_REQUEST if we run into an error,
     *          NOT_FOUND if child elements are specified that couldn't be found
     *          UNAUTHORIZED if the user does not have the rights to add a deck
     */
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addCardDeck() {
        JsonNode json = request().body().asJson();

        try {
            CardDeck deck = CardDeckRepository.addCardDeck(json);

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
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error while retrieving cards, " + e.getMessage()));
        } catch (DuplicateKeyException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Could not create deck with given cards, some of them already are in a deck.", JsonKeys.CARDDECK_CARDS, e.getObjects()));
        } catch (InvalidInputException e) {
            e.printStackTrace();
            if(JsonKeys.debugging && !e.getMessage().contains("did contain")){
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS+" | cause: "+e.getCause()));
            }
            else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, "Body did contain elements that are not allowed/expected. A card can contain: " + JsonKeys.FLASHCARD_JSON_ELEMENTS));
            }
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage()));
        }

    }

    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateCardDeck(long id) {
        JsonNode json = request().body().asJson();
        try {
            CardDeck deck = CardDeckRepository.updateCardDeck(id,request().username(),json,request().method());

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
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error while retrieving cards, " + e.getMessage()));
        } catch (DuplicateKeyException e) {
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Could not create deck with given cards, some of them already are in a deck.", JsonKeys.CARDDECK_CARDS, e.getObjects()));
        } catch (InvalidInputException e) {
            if(JsonKeys.debugging && !e.getMessage().contains("did contain")){
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
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, e.getMessage()));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage()));
        }
    }

}
