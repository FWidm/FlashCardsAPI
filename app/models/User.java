package models;

import java.util.Date;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import play.data.validation.Constraints.*;
import util.JsonKeys;

import javax.persistence.*;
/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 13/06/16.
 */
@Entity
@JsonPropertyOrder({ JsonKeys.USER_ID})
public class User extends Model {
	private static final long serialVersionUID = -6538683107994877014L;

	@Id
	@GeneratedValue
    @Column(name = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.USER_ID)
	private Long id;
	@Required @MinLength(3)
    @JsonProperty(JsonKeys.USER_NAME)
    private String name;
    @Required @MinLength(3)
    @JsonProperty(JsonKeys.USER_PASSWORD)
    @JsonIgnore
    private String password;
	@Required @Column(unique = true) @Email
    @JsonProperty(JsonKeys.USER_EMAIL)
	private String email;

    @JsonProperty(JsonKeys.RATING)
	private int rating;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z") @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
	private Date created;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
    @JsonProperty(JsonKeys.USER_GROUP)
	private UserGroup group;

	public static Model.Finder<Long, User> find = new Model.Finder<Long, User>(User.class);


	public User(String name, String email, String password, int rating) {
        super();
		this.name = name;
		this.email = email;
		this.password = password;
		this.rating = rating;
//		created = new Date();
	}
	
	public User(User u){
		super();
		this.name=u.getName();
		this.email=u.getEmail();
		this.password=u.getPassword();
		this.rating=u.getRating();
//		created=new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

    @JsonIgnore
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", password=" + password
				+ ", email=" + email + ", rating=" + rating + ", created="
				+ created + ", group=" + group + "]";
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public UserGroup getGroup() {
		return group;
	}

	public void setGroup(UserGroup group) {
//        System.out.println(">> setting usergroup from "+this.getGroup()+" to "+group);
		this.group = group;
		//update group definition as well.
		if (group!=null && !group.getUsers().contains(this)) {
			group.addUser(this);
		}
        this.update();
	}

}
