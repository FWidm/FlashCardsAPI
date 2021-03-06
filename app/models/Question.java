package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by fabianwidmann on 17/06/16.
 */
@Entity
@JsonPropertyOrder({JsonKeys.QUESTION_ID})
public class Question extends Model {
    public static Model.Finder<Long, Question> find = new Model.Finder<Long, Question>(Question.class);
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.QUESTION_ID)
    @JsonProperty(JsonKeys.QUESTION_ID)
    private long id;
    @Constraints.Required
    @JsonProperty(JsonKeys.QUESTION_TEXT)
    @Column(name = JsonKeys.QUESTION_TEXT)
    @Lob
    private String questionText;
    @JsonProperty(JsonKeys.URI)
    @Column(name = JsonKeys.URI, length = 2048)
    private URI uri;
//    @OneToOne(fetch= FetchType.LAZY)
//    @JoinColumn(name="parent_card_id")
//    @JsonIgnore
//    private FlashCard card;
    @ManyToOne
    @JoinColumn(name = JsonKeys.USER_ID, referencedColumnName = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.AUTHOR)
    private User author;

    public Question(String questionText, User author) {
        this.questionText = questionText;
        this.author = author;
    }

    /**
     * Parses a question from the given JsonNode node.
     *
     * @param node the json node to parse
     * @return a question object containing the information
     * @throws URISyntaxException when the uri is malformed
     */
    public static Question parseQuestion(User author, JsonNode node) throws URISyntaxException {
        String questionText = null;
        if (node.has(JsonKeys.AUTHOR)) {
            if (node.get(JsonKeys.AUTHOR).has(JsonKeys.USER_ID)) {
                long uid = node.get(JsonKeys.AUTHOR).get(JsonKeys.USER_ID).asLong();
                if (author == null)
                    author = User.find.byId(uid);
                System.out.println("Search for user with id=" + uid + " details=" + author);
            }
        }
        if (node.has(JsonKeys.QUESTION_TEXT)) {
            questionText = node.get(JsonKeys.QUESTION_TEXT).asText();
        }
        Question question = new Question(questionText, author);

        if (node.has(JsonKeys.URI)) {
            question.setUri(new URI(node.get(JsonKeys.URI).asText()));
        }
        return question;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", uri=" + uri +
                ", author=" + author +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
