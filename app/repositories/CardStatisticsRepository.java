package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import models.CardDeck;
import models.FlashCard;
import models.User;
import models.statistics.CardStatistics;
import play.Logger;
import play.api.mvc.Flash;
import util.JsonKeys;
import util.RequestKeys;
import util.UrlParamHelper;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;

import javax.smartcardio.Card;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.avaje.ebean.Expr.allEq;
import static com.avaje.ebean.Expr.eq;

/**
 * @author Fabian Widmann
 *         <p>
 *         CRUD Methods for CardStatistics
 */
public class CardStatisticsRepository {

    /**
     * Retrieve all cardStatistics for the logged in user. When ?count=x is sent, the result can be an empty list if client and api are synced or the whole list of statistics.
     * @param email
     * @return all cardStatistics for the logged in user
     * @throws NotAuthorizedException
     * @throws ParseException
     */
    public static List<CardStatistics> getCardStatistics(String email) throws NotAuthorizedException, ParseException {
        List<CardStatistics> statistics;
        User user = UserRepository.findUserByEmail(email);
        if (user == null)
            throw new NotAuthorizedException("User has to be logged in to retrieve messages");
        if(UrlParamHelper.checkForKey(RequestKeys.DECK_ID)){
            Long deckId=Long.parseLong(UrlParamHelper.getValue(RequestKeys.DECK_ID));
            return getCardStatisticsFromDeck(email,deckId);
        }
        if (UrlParamHelper.checkForKey(RequestKeys.COUNT)) {
            Long count = Long.parseLong(UrlParamHelper.getValue(RequestKeys.COUNT));
            if (count < CardStatistics.finder.where().eq(JsonKeys.STATISTICS_USER, user).findRowCount())
                statistics = CardStatistics.finder.where().eq(JsonKeys.STATISTICS_USER, user).findList();
            else return new ArrayList<CardStatistics>();

        } else {
            statistics = CardStatistics.finder.where().eq(JsonKeys.STATISTICS_USER, UserRepository.findUserByEmail(email)).findList();
        }
        return statistics;
    }

    /**
     * Creates one new CardStatistics object
     * @param email of the authenticated user
     * @param json content of the new object
     * @return new object or exception
     * @throws InvalidInputException if some of the required attributes are missing
     * @throws ParseException if we cannot retrieve the date from the datestring.
     */
    public static CardStatistics createCardStatistics(String email, JsonNode json) throws InvalidInputException, ParseException {
        CardStatistics cardStatistics;
        User user=UserRepository.findUserByEmail(email);;
        FlashCard card=null;
        float knowledge=Float.MIN_VALUE;
        int drawer= Integer.MIN_VALUE;
        Date startDate=null, endDate = null;


        if (json.has(JsonKeys.STATISTICS_CARD)) {
            if(json.get(JsonKeys.STATISTICS_CARD).has(JsonKeys.FLASHCARD_ID))
                card=FlashCardRepository.getFlashCard(json.get(JsonKeys.STATISTICS_CARD).get(JsonKeys.FLASHCARD_ID).asLong());
        }

        if(json.has(JsonKeys.STATISTICS_KNOWLEDGE)){
            knowledge= (float) json.get(JsonKeys.STATISTICS_KNOWLEDGE).asDouble();
        }

        if(json.has(JsonKeys.DATE_START)){
            String textDate = json.get(JsonKeys.DATE_START).asText();
            DateFormat format = new SimpleDateFormat(JsonKeys.DATE_FORMAT);
            startDate = format.parse(textDate);
            // TODO: 20.02.2017 decide if we want to invalidate sent dates.
            /*if(startDate.after(new Date()))
                startDate=null;*/
        }

        if(json.has(JsonKeys.DATE_END)){
            String textDate = json.get(JsonKeys.DATE_START).asText();;
            DateFormat format = new SimpleDateFormat(JsonKeys.DATE_FORMAT);
            endDate = format.parse(textDate);
            // TODO: 20.02.2017 decide if we want to invalidate sent dates.
            /*if(endDate.after(new Date()))
                endDate=null;*/
        }

        if(json.has(JsonKeys.STATISTICS_DRAWER)){
            drawer=json.get(JsonKeys.STATISTICS_DRAWER).asInt();
        }

        Logger.debug("user="+user);
        Logger.debug("card="+card);
        Logger.debug("startDate="+startDate);
        Logger.debug("endDate="+endDate);
        Logger.debug("knowledge="+knowledge);
        Logger.debug("drawer="+drawer);

        if(card==null)
            throw new InvalidInputException("The requested card does not exist. No CardStatistics-object was created.");
        if(user==null || drawer < 0 || knowledge < 0 ){
            throw new InvalidInputException("Please provide all necessary details to create a new CardStatistic such " +
                    "as: ("+JsonKeys.STATISTICS_CARD+", "+JsonKeys.STATISTICS_DRAWER+" (positive), "+JsonKeys.STATISTICS_KNOWLEDGE+
                    "(positive, float) and two timestamps: "+JsonKeys.DATE_START+", "+ JsonKeys.DATE_END+" in the format: "+JsonKeys.DATE_FORMAT+").");
        }

        cardStatistics=new CardStatistics(user,card,knowledge,drawer,startDate,endDate);
        cardStatistics.save();

        return cardStatistics;
    }


    public static List<CardStatistics> getCardStatisticsFromDeck(String email,Long deckId) {
        List<FlashCard> cards=FlashCard.find.where().eq(JsonKeys.FLASHCARD_DECK, CardDeck.find.byId(deckId)).findList();
        List<CardStatistics> cardStatisticsList=new ArrayList<>();
        cards.forEach(card-> {
            Logger.debug("CardID="+card.getId());
            cardStatisticsList.addAll(CardStatistics.finder.where().and(eq(JsonKeys.STATISTICS_USER, UserRepository.findUserByEmail(email)),/*allEq(queryMap)*/eq(JsonKeys.STATISTICS_CARD, card)).findList());
        }
        );
        return cardStatisticsList;
    }
}
