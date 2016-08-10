package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigException;
import models.*;
import models.rating.AnswerRating;
import models.rating.CardRating;
import models.rating.Rating;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.ActionAuthenticator;
import util.FileTypeChecker;
import util.JsonKeys;
import util.JsonUtil;
import views.html.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @BodyParser.Of(BodyParser.Json.class)
    public Result addCardDeck() {
        JsonNode json = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        try {
            CardDeck requestObject = mapper.convertValue(json, CardDeck.class);

            //retrieve the correct cards list by either parsing the id and getting the correct card or the attributes to a new card.
            List<FlashCard> cardList=new ArrayList<>();
            for (int i=0; i<requestObject.getCards().size(); i++) {
                FlashCard currentCard=requestObject.getCards().get(i);
                if(currentCard.getId()==0){
                    // TODO: 10.08.2016 parse flashcard by other values?
                }
                else {
                    try{
                        FlashCard tmp=FlashCard.find.byId(currentCard.getId());
                        if(tmp!=null)
                            cardList.add(tmp);
                        System.out.println(i+" >> "+cardList.size());
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,"No Card found with the given id",currentCard.getId()));
                    }
                }
            }
            requestObject.setCards(cardList);
            CardDeck deck=new CardDeck(requestObject);
            deck.save();
            System.out.println(deck.getId());
            return ok(JsonUtil.prepareJsonStatus(OK,"Carddeck has been created!",deck.getId()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Pattern p = Pattern.compile("\\\"(.*?)\\\"");
            Matcher m = p.matcher(e.getCause().toString());
            String cause = "";
            if (m.find())
                cause = m.group().substring(1, m.group().length() - 1);
            return badRequest(JsonUtil.prepareJsonStatus(BAD_REQUEST, "Request contained an element that is not expected for a card deck. Unknown Attribute=" + cause + ", expected Attributes=" + JsonKeys.CARDDECK_JSON_ELEMENTS));
        }


    }
}
