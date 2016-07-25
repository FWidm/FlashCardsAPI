package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 27/06/16.
 */
@Entity
public class Tag extends Model {
    @Id @GeneratedValue
    @Column(name = JsonKeys.TAG_ID)
    @JsonProperty(JsonKeys.TAG_ID)
    private long id;
    @Constraints.Required
    @Column(unique = true) @Constraints.MinLength(3) @Constraints.MaxLength(16)
    @JsonProperty(JsonKeys.TAG_NAME)
    private String name;
    //this cascade from the "tag" to "join_cards_tag" - e.g. tag.delete -> delete evey entry with tag.id
    @ManyToMany(mappedBy = "tags"/*, cascade = CascadeType.ALL*/)
    @JsonProperty(JsonKeys.TAG_CARDS)
    @JsonIgnore
    private List<FlashCard> cards;

    public static Model.Finder<Long, Tag> find = new Model.Finder<Long, Tag>(Tag.class);


    public Tag(String name) {
        this.name = name;
    }

    public Tag(String name, List<FlashCard> cards) {
        this.name = name;
        this.cards = cards;
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

    public void addFlashCard(FlashCard flashCard){
        if(!cards.contains(flashCard)){
            cards.add(flashCard);
            this.update();
            flashCard.addTag(this);
        }
    }

    /**
     *  Deletes the given card from he list hen it is an element
     * @param flashCard - will be removed
     */
    public void removeFlashCard(FlashCard flashCard){
        if (cards.contains( flashCard)){
            cards.remove(flashCard);
            this.update();
        }
    }
    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cards=" + cards +
                '}';
    }
}
