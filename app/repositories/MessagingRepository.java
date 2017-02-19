package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import models.CardDeck;
import models.User;
import models.msg.AbstractMessage;
import models.msg.DeckChallengeMessage;
import play.Logger;
import util.JsonKeys;
import util.RequestKeys;
import util.UrlParamHelper;
import util.UserOperations;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.avaje.ebean.Expr.between;
import static com.avaje.ebean.Expr.eq;

/**
 * @author Fabian Widmann
 */
public class MessagingRepository {

    /**
     * Receive all messages for one user, can be filtered by specifying a start date where only Messages after this date
     * will be returned. One authenticated user is only able to receive messages for himself.
     *
     * @param email of the user
     * @return list of messages (can be empty).
     */
    public static List<AbstractMessage> getMessages(String email) throws NotAuthorizedException, ParseException {
        List<AbstractMessage> messages;
        User user = UserRepository.findUserByEmail(email);
        if(user==null)
            throw new NotAuthorizedException("User has to be logged in to retrieve messages");

        if (UrlParamHelper.checkForKey(RequestKeys.START_DATE)) {
            String textDate = UrlParamHelper.getValue(RequestKeys.START_DATE);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            Date date = format.parse(textDate);
            Logger.debug("Got date=" + date);
            messages = AbstractMessage.find.where().and(eq(JsonKeys.MESSAGE_RECIPIENT,user),between(JsonKeys.DATE_CREATED,date,new Date())).findList();

        } else {
            messages = AbstractMessage.find.where().eq(JsonKeys.MESSAGE_RECIPIENT, user).findList();
        }
        return messages;

    }

    /**
     * Allows the authenticated user to retrieve one specific message by id.
     *
     * @param id    of the message
     * @param email of the auth user
     * @return message
     * @throws NotAuthorizedException if the user does not have the right to read the message
     */
    public static AbstractMessage getMessage(Long id, String email) throws NotAuthorizedException {
        AbstractMessage msg = AbstractMessage.find.byId(id);
        User currentUser = UserRepository.findUserByEmail(email);
        if (currentUser.hasPermission(UserOperations.GET_MESSAGE, msg))
            return msg;
        else throw new NotAuthorizedException("This user may not receive this message");
    }

    /**
     * Create a new message -
     *
     * @param email of auth user
     * @param json  content of the body
     * @return AbstractMessage
     */
    public static AbstractMessage createMessage(String email, JsonNode json) throws InvalidInputException {
        AbstractMessage message = null;
        User recipient = null;
        String content = null;

        Logger.debug("Json="+json);
        User sender = UserRepository.findUserByEmail(email);

        if (json.has(JsonKeys.MESSAGE_RECIPIENT)) {
            if(json.get(JsonKeys.MESSAGE_RECIPIENT).has(JsonKeys.USER_ID))
                recipient = UserRepository.findById(json.get(JsonKeys.MESSAGE_RECIPIENT).get(JsonKeys.USER_ID).asLong());
        }
        if (json.has(JsonKeys.MESSAGE_CONTENT)) {
            content = json.get(JsonKeys.MESSAGE_CONTENT).asText();
        }
        if (json.has(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK)) {
            if(json.get(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK).has(JsonKeys.CARDDECK_ID)) {
                CardDeck deck = CardDeckRepository.getCardDeck(json.get(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK).get(JsonKeys.CARDDECK_ID).asLong());
                if(recipient!=null){
                    message = new DeckChallengeMessage(recipient, content, deck);
                    message.setSender(sender);
                    message.save();
                    return message;
                }
            }
        }

        throw new InvalidInputException("A message needs to consist of a recipient, the content and the deck id.");
    }

    /**
     * Delete one specific message by id
     *
     * @param id of the message
     * @return temporary remaining object of the message
     */
    public static AbstractMessage deleteMessage(Long id, String email) {
        AbstractMessage msg = AbstractMessage.find.byId(id);
        User currentUser = UserRepository.findUserByEmail(email);

        if (msg !=null && currentUser.hasPermission(UserOperations.GET_MESSAGE, msg)) {
            msg.delete();
            return msg;
        }
        throw new NullPointerException();
    }
}
