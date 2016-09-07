package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.util.List;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@Entity
public class Category extends Model{
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.CATEGORY_ID)
    @JsonProperty(JsonKeys.CATEGORY_ID)
    private long id;

    @Constraints.Required
    @Column(name = JsonKeys.CATEGORY_NAME)
    @Constraints.MinLength(3)
    @Constraints.MaxLength(30)
    private String Name;

    @Column(name = JsonKeys.CATEGORY_DECK)
    @OneToMany(mappedBy = JsonKeys.CARDDECK_CATEGORY,fetch = FetchType.EAGER)
    private List<CardDeck> cardDeckList;

    @Column(name = JsonKeys.CATEGORY_PARENT)
    @ManyToOne
    private Category parent;

    public static Model.Finder<Long, Category> find = new Model.Finder<Long, Category>(Category.class);

    public Category(String name) {
        Name = name;
    }

    public Category(String name, Category parent) {
        Name = name;
        this.parent = parent;
    }

    public Category(String name, List<CardDeck> cardDeckList, Category parent) {
        Name = name;
        this.cardDeckList = cardDeckList;

        this.parent = parent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public List<CardDeck> getCardDeckList() {
        return cardDeckList;
    }

    public void setCardDeckList(List<CardDeck> cardDeckList) {
        this.cardDeckList = cardDeckList;
        for (CardDeck deck: cardDeckList) {
            deck.setCategory(this);
            deck.update();
        }
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", Name='" + Name + '\'' +
                ", cardDeckList=" + cardDeckList +
                ", parent=" + parent +
                '}';
    }
}
