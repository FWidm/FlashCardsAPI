package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
    private String tag;


}
