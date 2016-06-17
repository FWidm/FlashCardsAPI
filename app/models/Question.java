package models;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.net.URI;

/**
 * Created by fabianwidmann on 17/06/16.
 */
@Entity
public class Question extends Model {
    @Id
    @GeneratedValue
    private long id;

    @Constraints.Required
    private String question;
    private URI mediaURI;

    @OneToOne(fetch= FetchType.LAZY)
    private User author;
    @ManyToOne(fetch= FetchType.LAZY)
    private FlashCard card;
}
