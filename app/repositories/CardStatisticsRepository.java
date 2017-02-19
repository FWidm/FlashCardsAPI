package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import models.FlashCard;
import models.User;
import models.statistics.CardStatistics;
import play.api.mvc.Flash;
import util.JsonKeys;
import util.RequestKeys;
import util.UrlParamHelper;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public static CardStatistics createCardStatistics(String email, JsonNode json) throws InvalidInputException, ParseException {
        CardStatistics cardStatistics;
        User user=null;
        FlashCard card=null;
        float knowledge=Float.MIN_VALUE;
        int drawer= Integer.MIN_VALUE;
        Date startDate=null, endDate = null;

        if (json.has(JsonKeys.STATISTICS_USER)) {
            if(json.get(JsonKeys.MESSAGE_RECIPIENT).has(JsonKeys.USER_ID))
                user = UserRepository.findById(json.get(JsonKeys.MESSAGE_RECIPIENT).get(JsonKeys.USER_ID).asLong());
        }

        if (json.has(JsonKeys.STATISTICS_CARD)) {
            if(json.get(JsonKeys.MESSAGE_RECIPIENT).has(JsonKeys.FLASHCARD_ID))
                card=FlashCardRepository.getFlashCard(json.get(JsonKeys.STATISTICS_CARD).get(JsonKeys.FLASHCARD_ID).asLong());
        }

        if(json.has(JsonKeys.STATISTICS_KNOWLEDGE)){
            knowledge= (float) json.get(JsonKeys.STATISTICS_KNOWLEDGE).asDouble();
        }

        if(json.has(JsonKeys.DATE_START)){
            String textDate = json.get(JsonKeys.DATE_START).asText();;
            DateFormat format = new SimpleDateFormat(JsonKeys.DATE_FORMAT);
            startDate = format.parse(textDate);
        }

        if(json.has(JsonKeys.DATE_END)){
            String textDate = json.get(JsonKeys.DATE_START).asText();;
            DateFormat format = new SimpleDateFormat(JsonKeys.DATE_FORMAT);
            endDate = format.parse(textDate);
        }

        if(json.has(JsonKeys.STATISTICS_DRAWER)){
            drawer=json.get(JsonKeys.STATISTICS_DRAWER).asInt();
        }

        if(user==null || card==null || startDate==null || endDate==null|| drawer < 0 || knowledge < 0 ){
            throw new InvalidInputException("Please provide all neccessary details to create a new CardStatistic such " +
                    "as: ("+JsonKeys.STATISTICS_CARD+", "+JsonKeys.STATISTICS_DRAWER+", "+JsonKeys.STATISTICS_KNOWLEDGE+
                    ", "+JsonKeys.STATISTICS_USER+" and two timestamps: "+JsonKeys.DATE_START+", "+ JsonKeys.DATE_END+" in the format: "+JsonKeys.DATE_FORMAT+").");
        }

        cardStatistics=new CardStatistics(user,card,knowledge,drawer,startDate,endDate);
        cardStatistics.save();

        return cardStatistics;
    }
}
