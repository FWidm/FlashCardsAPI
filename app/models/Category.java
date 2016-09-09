package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.util.List;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@JsonPropertyOrder({ JsonKeys.CATEGORY_ID, JsonKeys.CATEGORY_NAME})
@Entity
public class Category extends Model{
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.CATEGORY_ID)
    @JsonProperty(JsonKeys.CATEGORY_ID)
    private long id;

    @Constraints.Required
    @Column(name = JsonKeys.CATEGORY_NAME)
    @JsonProperty(JsonKeys.CATEGORY_NAME)
    @Constraints.MinLength(3)
    @Constraints.MaxLength(30)
    private String name;

    @Column(name = JsonKeys.CATEGORY_DECK)
    @OneToMany(mappedBy = JsonKeys.CARDDECK_CATEGORY,fetch = FetchType.EAGER)
    @JsonProperty(JsonKeys.CATEGORY_DECK)

    private List<CardDeck> cardDeckList;

    @Column(name = JsonKeys.CATEGORY_PARENT)
    @JsonProperty(JsonKeys.CATEGORY_PARENT)

    @ManyToOne
    private Category parent;

    public static Model.Finder<Long, Category> find = new Model.Finder<Long, Category>(Category.class);

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    public Category(String name, List<CardDeck> cardDeckList, Category parent) {
        this.name = name;
        this.cardDeckList = cardDeckList;

        this.parent = parent;
    }

    /**
     * Create a new object with the same attribute value as another one.
     * @param category
     */
    public Category(Category category) {
        this.name = category.getName();
        this.parent=category.getParent();
        this.cardDeckList=category.getCardDeckList();
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

    public List<CardDeck> getCardDeckList() {
        return cardDeckList;
    }

    public void setCardDeckList(List<CardDeck> cardDeckList) {
        this.cardDeckList = cardDeckList;
        for (CardDeck deck: cardDeckList) {
            deck.setCategory(this);
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
                ", name='" + name + '\'' +
                ", cardDeckList=" + cardDeckList +
                ", parent=" + parent +
                '}';
    }
}
