package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.*;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.util.List;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@JsonPropertyOrder({ JsonKeys.CATEGORY_ID, JsonKeys.CATEGORY_NAME})
//@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property=JsonKeys.CATEGORY_ID)
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
    @JsonIgnore
    private List<CardDeck> cardDecks;

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

    public Category(String name, List<CardDeck> cardDecks, Category parent) {
        this.name = name;
        this.cardDecks = cardDecks;

        this.parent = parent;
    }

    /**
     * Create a new object with the same attribute value as another one.
     * @param category - copies the whole objects content in a new object.
     */
    public Category(Category category) {
        this.name = category.getName();
        this.parent=category.getParent();
        this.cardDecks =category.getCardDecks();
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
    public List<CardDeck> getCardDecks() {
        return cardDecks;
    }

    public void setCardDecks(List<CardDeck> cardDecks) {
        this.cardDecks = cardDecks;
        for (CardDeck deck: cardDecks) {
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
                ", cardDecks=" + cardDecks +
                ", parent=" + parent +
                '}';
    }
}