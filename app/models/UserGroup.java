package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

@Entity
@JsonPropertyOrder({ "groupId" }) //ensure that groupID is the first element in json.
public class UserGroup extends Model {

	private static final long serialVersionUID = -8033148832312413044L;
	@Id
	@GeneratedValue
	@Column(name = "group_id")
	@JsonProperty("groupId")
	private Long id;
	@Constraints.Required
	private String name, description;
	@OneToMany(mappedBy = "group")
	@JsonIgnore
	// to prevent endless recursion.
	private List<User> users;

	public static Model.Finder<Long, UserGroup> find = new Model.Finder<Long, UserGroup>(
			Long.class, UserGroup.class);

	public UserGroup(String name, String description, List<User> users) {
		super();
		this.name = name;
		this.description = description;
		this.users = users;
	}

	public UserGroup(UserGroup requestGroup) {
		super();
		this.name=requestGroup.getName();
		this.description=requestGroup.getDescription();
		this.users=requestGroup.getUsers();
		for(User u:users){
			u.setGroup(this);
			u.update();
		}
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public void addUser(User user) {
		if (!users.contains(user)) {
			users.add(user);
			user.setGroup(this);
			this.save();
		}
	}

	@Override
	public String toString() {
		return "UserGroup [id=" + id + ", name=" + name + ", description="
				+ description+"]";
	}

}
