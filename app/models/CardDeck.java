package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.PrivateOwned;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.util.List;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 09/08/16.
 */
@Entity
@Table(name = JsonKeys.CARDDECK_TABLE_NAME)
public class CardDeck extends Model {
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.CARDDECK_ID)
    @JsonProperty(JsonKeys.CARDDECK_ID)
    private long id;
    @Constraints.Required
    @Column(name = JsonKeys.CARDDECK_NAME)
    @Constraints.MinLength(3)
    @Constraints.MaxLength(16)
    @JsonProperty(JsonKeys.CARDDECK_NAME)
    private String name;
    @JsonProperty(JsonKeys.CARDDECK_DESCRIPTION)
    private String description;
    //this cascades from the "tag" to "join_cards_tag" - e.g. tag.delete -> delete evey entry with tag.id
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deck")
    @PrivateOwned
    @JsonProperty(JsonKeys.CARDDECK_CARDS)
    private List<FlashCard> cards;

    public static Finder<Long, CardDeck> find = new Finder<Long, CardDeck>(CardDeck.class);


    public CardDeck(String name) {
        this.name = name;
    }

    public CardDeck(String name, String description, List<FlashCard> cards) {
        this.name = name;
        this.description = description;
        this.cards = cards;
    }

    public CardDeck(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CardDeck(CardDeck otherDeck) {
        this.name = otherDeck.getName();
        this.description = otherDeck.getDescription();
        this.cards = otherDeck.getCards();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public List<FlashCard> getCards() {
        return cards;
    }

    public void setCards(List<FlashCard> cards) {
        this.cards = cards;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void delete() {
        for (FlashCard card : cards) {
            card.setDeck(null);
            card.update();
        }
        super.delete();
    }

    @Override
    public String toString() {
        return "CardDeck{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cards=" + cards +
                '}';
    }
}
