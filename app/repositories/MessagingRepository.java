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

import java.util.List;

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
    public static List<AbstractMessage> getMessages(String email) throws NotAuthorizedException {
        List<AbstractMessage> messages;
        User user = UserRepository.findUserByEmail(email);
        if(user==null)
            throw new NotAuthorizedException("User has to be logged in to retrieve messages");

        if (UrlParamHelper.checkForKey(RequestKeys.START_DATE)) {
            String date = UrlParamHelper.getValue(RequestKeys.START_DATE);
            Logger.debug("Got date=" + date);
            // TODO: 10/02/17 query by date.
            messages = null;
        } else {
            messages = AbstractMessage.find.where().eq(JsonKeys.MESSAGE_RECIPIENT, user.getId()).findList();
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
        if (currentUser.hasRight(UserOperations.GET_MESSAGE, msg))
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
        User user = null;
        String content = null;
        if (json.has(JsonKeys.MESSAGE_RECIPIENT)) {
            user = UserRepository.findById(json.get(JsonKeys.MESSAGE_RECIPIENT).asLong());
        }
        if (json.has(JsonKeys.MESSAGE_CONTENT)) {
            content = json.get(JsonKeys.MESSAGE_CONTENT).asText();
        }
        if (json.has(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK)) {
            CardDeck deck = CardDeckRepository.getCardDeck(json.get(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK).asLong());
            message = new DeckChallengeMessage(user, content, deck);
            message.save();
            return message;
        }

        throw new InvalidInputException("A message needs to consist of a recipient, the content and the deck id.");
    }

    /**
     * Delete one specific message by id
     *
     * @param id of the message
     * @return temporary remaining object of the message
     */
    public static AbstractMessage deleteMessage(Long id) {
        AbstractMessage msg = AbstractMessage.find.byId(id);
        msg.delete();
        return msg;
    }
}
