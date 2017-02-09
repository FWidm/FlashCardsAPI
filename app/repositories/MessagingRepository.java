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

import java.util.List;

/**
 * @author Fabian Widmann
 */
public class MessagingRepository {

    /**
     * Receive all messages for one user, can be filtered by specifying a start date where only Messages after this date
     * will be returned.
     *
     * @param email of the user
     * @return list of messages (can be empty).
     */
    public List<AbstractMessage> getMessages(String email) {
        List<AbstractMessage> messages;
        User user = UserRepository.findUserByEmail(email);

        if (UrlParamHelper.checkForKey(RequestKeys.START_DATE)) {
            String date = UrlParamHelper.getValue(RequestKeys.START_DATE);
            Logger.debug("Got date=" + date);
            messages = null;
        } else {
            messages = AbstractMessage.find.where().eq(JsonKeys.MESSAGE_RECIPIENT, email).findList();
        }
        return messages;
    }

    public AbstractMessage createMessage(String email, JsonNode json){
        AbstractMessage message=null;
        User user=null;
        String content=null;
        if(json.has(JsonKeys.MESSAGE_RECIPIENT)){
            user=UserRepository.findById(json.get(JsonKeys.MESSAGE_RECIPIENT).asLong());
        }
        if(json.has(JsonKeys.MESSAGE_CONTENT)){
            content=json.get(JsonKeys.MESSAGE_CONTENT).asText();
        }
        if(json.has(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK)){
            CardDeck deck = CardDeckRepository.getCardDeck(json.get(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK).asLong());
            message=new DeckChallengeMessage(user,content,deck);
        }

        message.save();
        return message;
    }
}
