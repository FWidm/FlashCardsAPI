package models.statistics;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.FlashCard;
import models.User;
import util.JsonKeys;

import javax.persistence.*;
import javax.smartcardio.Card;
import java.util.Date;

/**
 * @author Fabian Widmann
 * private long id;
 * private long userId;
 * private long cardId;
 * private float knowledge;
 * private int drawer;
 * private long startDate;
 * private long endDate;
 */
@Entity
public class CardStatistics extends Model{
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.STATISTICS_ID)
    @JsonProperty(JsonKeys.STATISTICS_ID)
    private long id;

    @ManyToOne
    @JoinColumn(name = JsonKeys.STATISTICS_USER, referencedColumnName = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.STATISTICS_USER)
    private User user;
    @ManyToOne
    @JoinColumn(name = JsonKeys.STATISTICS_CARD, referencedColumnName = JsonKeys.FLASHCARD_ID)
    @JsonProperty(JsonKeys.STATISTICS_CARD)
    private FlashCard card;
    // knowledge in %
    @Column(name = JsonKeys.STATISTICS_KNOWLEDGE)
    @JsonProperty(JsonKeys.STATISTICS_KNOWLEDGE)
    private float knowledge;
    //knowledge assessment - see drawer principle for card learning
    @Column(name = JsonKeys.STATISTICS_DRAWER)
    @JsonProperty(JsonKeys.STATISTICS_DRAWER)
    private int drawer;

    @Column(name = JsonKeys.DATE_START)
    @JsonProperty(JsonKeys.DATE_START)
    private Date startDate;

    @Column(name = JsonKeys.DATE_END)
    @JsonProperty(JsonKeys.DATE_END)
    private Date endDate;

    public static Model.Finder<Long, CardStatistics> finder = new Model.Finder<Long,CardStatistics>(CardStatistics.class);

    public CardStatistics(User user, FlashCard card, float knowledge, int drawer, Date startDate, Date endDate) {
        this.user = user;
        this.card = card;
        this.knowledge = knowledge;
        this.drawer = drawer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FlashCard getCard() {
        return card;
    }

    public void setCard(FlashCard card) {
        this.card = card;
    }

    public float getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(float knowledge) {
        this.knowledge = knowledge;
    }

    public int getDrawer() {
        return drawer;
    }

    public void setDrawer(int drawer) {
        this.drawer = drawer;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}