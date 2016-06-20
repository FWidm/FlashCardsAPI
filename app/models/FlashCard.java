/**
 *
 */
package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.PrivateOwned;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *
 */
@Entity
public class FlashCard extends Model {
    @Id
    @GeneratedValue
    @Column(name = "flashcard_id")
    private long id;
    private int rating;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z", timezone = "Germany")
    @CreatedTimestamp
    private Date created;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z", timezone = "Germany")
    @UpdatedTimestamp
    private Date lastUpdated;
    //todo: how do we implement tags? ElementCollection does not work with ebeans, we might have to make our own tag class that contains an id and a string...

    //orphanRemoval means, that no single questions without a specific card may exist. This helps us keep the db clean.
    @OneToOne(fetch = FetchType.LAZY)
    @PrivateOwned //this means, that if the element is deleted with its parent.
    private Question question;

    @OneToMany(mappedBy = "card", cascade = CascadeType.REMOVE)
    @PrivateOwned
    private List<Answer> answers;
    @OneToOne //OneToMany??
    @JoinColumn(name="author_id", referencedColumnName="id")
    private User author;
    private boolean multipleChoice;

    @Transient //not persistent.
    @JsonIgnore
    private boolean isSelected, isMarked;

    public static Model.Finder<Long, FlashCard> find = new Model.Finder<Long, FlashCard>(FlashCard.class);

    public FlashCard(User author, boolean multipleChoice, List<String> tags) {
        this.author = author;
        this.multipleChoice = multipleChoice;
    }

    public FlashCard(User author, List<Answer> answers, Question question, boolean multipleChoice) {
        this.author = author;
        if(answers!=null){
            this.answers = answers;
            for (Answer a: answers) {
                if(a!=null){
                    a.setCard(this);
                    a.update();}
            }
        }
        if(question!=null){
            this.question = question;
        }
        this.multipleChoice = multipleChoice;
    }

    public FlashCard(Date created, Question question, List<Answer> answers, User author, boolean multipleChoice) {
        this.created = created;
        if(answers!=null){
            this.answers = answers;
            for (Answer a: answers) {
                if(a!=null){
                    a.setCard(this);
                    a.update();}
            }
        }
        if(question!=null){
            this.question = question;
        }
        this.author = author;
        this.multipleChoice = multipleChoice;
    }

    public FlashCard(FlashCard requestObject) {
        this.author=requestObject.getAuthor();
        this.answers=requestObject.getAnswers();
        this.question=requestObject.getQuestion();
        this.multipleChoice =requestObject.isMultipleChoice();
    }

    /**
     * Adds one answer to this specific flashcard, updates the flashcards in the DB.
     * @param answer
     */
    public void addAnswer(Answer answer){
        System.out.println("Flashcard: addAnswer a="+answer);
        if(answer!=null && !this.answers.contains(answer)){
            this.answers.add(answer);
            answer.setCard(this);
            this.update();
        }
    }

    /**
     * Sets the question of this card to a specific question object and updates the flashcard in the DB.
     * @param question
     */
    public void setQuestion(Question question) {
        System.out.println("Flashcard: setQuestion q="+question);
        this.question = question;
    }

    @Override
    public String toString() {
        return "FlashCard{" +
                "id=" + id +
                ", rating=" + rating +
                ", created=" + created +
                ", lastUpdated=" + lastUpdated +
                ", author=" + author +
                ", multipleChoice=" + multipleChoice +
                ", isSelected=" + isSelected +
                ", isMarked=" + isMarked +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Question getQuestion() {
        return question;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;

    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }
}
