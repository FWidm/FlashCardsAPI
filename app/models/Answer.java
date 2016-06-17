package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.net.URI;

/**
 * Created by fabianwidmann on 17/06/16.
 */
@Entity
public class Answer extends Model {
    @Id @GeneratedValue
    private long id;
    private int rating;

    @Constraints.Required
    private String answerText;
    private String hint;
    private URI mediaURI;
    @OneToOne(fetch=FetchType.LAZY)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private FlashCard card;

}
