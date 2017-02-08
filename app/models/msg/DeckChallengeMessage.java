package models.msg;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.CardDeck;
import models.User;
import util.JsonKeys;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by fabianwidmann on 08/02/17.
 */
@Entity
@DiscriminatorValue(JsonKeys.DECK_CHALLENGE_MESSAGE)
public class DeckChallengeMessage extends AbstractMessage {
    @ManyToOne
    @JsonProperty(JsonKeys.DECK_CHALLENGE_MESSAGE_DECK)
    CardDeck deck;

    public static Model.Finder<Long, DeckChallengeMessage> find = new Model.Finder<Long, DeckChallengeMessage>(DeckChallengeMessage.class);

    /**
     * Create a new message for one recipient with a specific string message
     *
     * @param recipient
     * @param content
     */
    public DeckChallengeMessage(User recipient, String content, CardDeck deck) {
        super(recipient, content);
        this.deck=deck;
    }

    public CardDeck getDeck() {
        return deck;
    }

    public void setDeck(CardDeck deck) {
        this.deck = deck;
    }

    @Override
    public String toString() {
        return "DeckChallengeMessage{" +
                "id=" + id +
                ", recipient=" + recipient +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                "deck=" + deck +
                '}';
    }
}
