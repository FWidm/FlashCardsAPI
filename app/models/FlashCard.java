/**
 *
 */
package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import play.db.jpa.*;

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

    @ElementCollection
    private List<String> tags;
    @OneToOne(fetch = FetchType.LAZY)
    private Question question;

    @OneToMany(mappedBy = "card")
    private List<Answer> answers;

    private User author;
    private boolean isMultipleChoice;

    @Transient //not persistent.
    private boolean isSelected, isMarked;
}
