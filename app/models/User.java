package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import models.rating.Rating;
import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import util.JsonKeys;
import util.UserOperations;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Fabian Widmann
 *         on 13/06/16.
 */
@Entity
@JsonPropertyOrder({JsonKeys.USER_ID})
public class User extends Model {
    public static Model.Finder<Long, User> find = new Model.Finder<>(User.class);
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.USER_ID)
    private Long id;
    @Lob //blob
    @JsonProperty(JsonKeys.USER_AVATAR)
    private String avatar;
    @Required
    @MinLength(JsonKeys.USER_NAME_MIN_LENGTH)
    @JsonProperty(JsonKeys.USER_NAME)
    private String name;
    @Required
    @MinLength(JsonKeys.USER_PASSWORD_MIN_LENGTH)
    @JsonProperty(JsonKeys.USER_PASSWORD)
    @JsonIgnore
    private String password;
    @Required
    @Column(unique = true)
    @Email
    @JsonProperty(JsonKeys.USER_EMAIL)
    private String email;
    @JsonProperty(JsonKeys.RATING)
    private int rating;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
    private Date created;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_LAST_LOGIN)
    @Column(name = JsonKeys.DATE_LAST_LOGIN)
    private Date lastLogin;
    @ManyToMany/*(cascade = CascadeType.ALL)*/
    @JoinTable(name = JsonKeys.USER_GROUP_JOIN_TABLE,
            joinColumns = @JoinColumn(name = JsonKeys.USER_ID, referencedColumnName = JsonKeys.USER_ID),
            inverseJoinColumns = @JoinColumn(name = JsonKeys.GROUP_ID, referencedColumnName = JsonKeys.GROUP_ID))
    @JsonProperty(JsonKeys.USER_GROUPS)
    @JsonIgnore
    private List<UserGroup> userGroups;
    @OneToMany(mappedBy = "user")
    @JsonIgnore    // to prevent endless recursion.
    private List<AuthToken> authTokenList;


    public User(String name, String email, String password, int rating) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.rating = rating;
        authTokenList = new ArrayList<>();
    }

    public User(User u) {
        super();
        this.name = u.getName();
        this.email = u.getEmail();
        this.password = u.getPassword();
        this.rating = u.getRating();
        authTokenList = new ArrayList<>();
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
        //todo: A new random salt must be generated each time a user creates an account or changes their password.
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
                + created + ", userGroups=" + userGroups + "]";
    }

    public Date getCreated() {
        return created;
    }

    @JsonIgnore
    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
//        System.out.println(">> setting usergroup from "+this.getUserGroups()+" to "+userGroups);
        this.userGroups = userGroups;
        //update userGroups definition as well.
/*		if (userGroups !=null && !userGroups.getUsers().contains(this)) {
            userGroups.addUser(this);
		}*/
        this.update();
    }

    public List<AuthToken> getAuthTokenList() {
        return authTokenList;
    }

    public void setAuthTokenList(List<AuthToken> authTokenList) {
        this.authTokenList = authTokenList;
    }

    /**
     * Adds one token to the tokenlist, updates this entity.
     *
     * @param token - to be added to the list.
     */
    public void addAuthToken(AuthToken token) {
        if (!authTokenList.contains(token)) {
            authTokenList.add(token);
            this.update();
        }
    }

    /**
     * Deletes all Tokens associated with this entity.
     */
    public void deleteTokens() {
        authTokenList.forEach(token -> token.delete());
        authTokenList = new ArrayList<>();
        this.update();
    }

    public void deleteToken(AuthToken authToken) {
        if (authTokenList.remove(authToken))
            authToken.delete();
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Adds the given rating to the current rating, updates this instance.
     *
     * @param ratingModifier - describes the value that is added/subtracted from the current rating
     */
    void updateRating(int ratingModifier) {
        Logger.debug("Userid=" + id + " | " + new Date() + " Modifying rating from=" + rating + " by modifier=" + ratingModifier + " to=" + (rating + ratingModifier));
        this.rating += ratingModifier;
        this.update();
    }


    @Override
    public void delete() {
        //Get all tags and unlink them from this card. Tag still exists to this point.
        List<Answer> givenAnswers = Answer.find.where().eq(JsonKeys.USER_ID, id).findList();
        System.out.println("Answers from the user has size=" + givenAnswers.size());

        for (Answer a : givenAnswers) {
            System.out.println(">> Trying to null author on answer a=" + a + " where author was: " + a.getAuthor());
            a.setAuthor(null);
            a.update();
        }


        List<FlashCard> cards = FlashCard.find.where().eq(JsonKeys.USER_ID, id).findList();
        System.out.println("Created cards list has size=" + cards.size());

        for (FlashCard c : cards) {
            System.out.println(">> Trying to null author on card c=" + c + " where author was: " + c.getAuthor());
            c.setAuthor(null);
            c.update();
        }


        List<Question> questions = Question.find.where().eq(JsonKeys.USER_ID, id).findList();
        System.out.println("Questions from the user has size=" + questions.size());
        for (Question q : questions) {
            System.out.println(">> Trying to null author on question q=" + q + " where author was: " + q.getAuthor());
            q.setAuthor(null);
            q.update();
        }

        //delete all existing tokens before deleting
        List<AuthToken> tokens = AuthToken.find.where().eq(JsonKeys.TOKEN_USER, id).findList();
        Logger.debug("Tokens:");
        tokens.forEach(token -> {
            Logger.debug(">> " + token);
            token.delete();
        });

        super.delete();
    }

    void removeGroup(UserGroup userGroup) {
        if (userGroups.contains(userGroup)) {
            userGroups.remove(userGroup);
            this.update();
        }
    }

    // TODO: 19.01.2017 write the whole method when we decided what has to be done, also adapt values

    /**
     * Checks whether the current user has the rights to perform the operation we want to check. If an object is passed
     * we can check if the user is in any way an owner and has rights regardless of his rating.
     *
     * @param userOperation - the operation the user wants to do
     * @param manipulated   - the manipulated object
     * @return true if the user can do the operation, else false.
     */
    public boolean hasRight(UserOperations userOperation, Object manipulated) {
        Logger.debug("Checking " + email + ": for (" + userOperation + "|" + manipulated + ")");
        final int RATING_CREATE_CATEGORY = 1000;
        final int RATING_DELETE_CATEGORY = 1000;
        final int RATING_EDIT_CATEGORY = 1000;

        final int RATING_DELETE_CARD = 100;
        final int RATING_EDIT_CARD = 100;

        final int RATING_DELETE_ANSWER = 100;
        final int RATING_EDIT_ANSWER = 100;

        final int RATING_DELETE_DECK = 100;
        final int RATING_EDIT_DECK = 100;

        final int RATING_EDIT_USER = 1000;

        final int RATING_EDIT_GROUP = 1000;
        final int RATING_DELETE_GROUP = 1000;

        //Used class compare instead of instanceof due to performance reasons.
        switch (userOperation) {
            //category
            case CREATE_CATEGORY: {
                if (rating >= RATING_CREATE_CATEGORY) {
                    return true;
                }
            }
            case DELETE_CATEGORY: {
                if (rating >= RATING_DELETE_CATEGORY) {
                    return true;
                }
            }
            case EDIT_CATEGORY: {
                if (rating >= RATING_EDIT_CATEGORY) {
                    return true;
                }
            }
            //cards
            case DELETE_CARD: {
                if (manipulated != null && manipulated.getClass() == FlashCard.class) {
                    FlashCard card = (FlashCard) manipulated;
                    Logger.debug("isAuthor=" + (card.getAuthor() == this) + " | has Rating? " + (rating >= RATING_DELETE_CARD));

                    //can delete own cards OR any cards when this user's rating is over a specific value
                    if (card.getAuthor() == this || rating >= RATING_DELETE_CARD) {
                        return true;
                    }
                }
            }
            case EDIT_CARD_QUESTION: {
                if (manipulated != null && manipulated.getClass() == FlashCard.class) {
                    FlashCard card = (FlashCard) manipulated;
                    //can edit own cards OR any cards when this user's rating is over a specific value
                    if (card.getAuthor() == this || rating >= RATING_EDIT_CARD) {
                        return true;
                    }
                }
            }
            //answers
            case DELETE_ANSWER: {
                if (manipulated != null && manipulated.getClass() == Answer.class) {
                    Answer answer = (Answer) manipulated;
                    //can delete own cards OR any cards when this user's rating is over a specific value
                    if (answer.getAuthor() == this || rating >= RATING_DELETE_ANSWER) {
                        return true;
                    }
                }
            }
            case EDIT_ANSWER: {
                if (manipulated != null && manipulated.getClass() == Answer.class) {
                    Answer answer = (Answer) manipulated;
                    //can edit own cards OR any cards when this user's rating is over a specific value
                    if (answer.getAuthor() == this || rating >= RATING_EDIT_ANSWER) {
                        return true;
                    }
                }
            }
            //deck - check users usergroups
            case DELETE_DECK: {
                if (manipulated != null && manipulated.getClass() == CardDeck.class) {
                    CardDeck deck = (CardDeck) manipulated;
                    UserGroup group = deck.getUserGroup();

                    //can delete own cards OR any cards when this user's rating is over a specific value
                    if (rating >= RATING_DELETE_DECK || (group != null && group.getUsers().contains(this))) {
                        return true;
                    }
                }
            }
            case EDIT_DECK: {
                if (manipulated != null && manipulated.getClass() == CardDeck.class) {
                    CardDeck deck = (CardDeck) manipulated;
                    UserGroup group = deck.getUserGroup();
                    Logger.debug("group: " + group.getUsers());
                    //can delete own cards OR any cards when this user's rating is over a specific value

                    if (rating >= RATING_EDIT_DECK || group.getUsers().contains(this)) {
                        return true;
                    }
                }
            }
            case EDIT_USER: {
                if (manipulated != null && manipulated.getClass() == User.class) {
                    User user = (User) manipulated;
                    if (this.equals(user) || rating >= RATING_EDIT_USER)
                        return true;
                }
            }
            case DELETE_USER: {
                if (manipulated != null && manipulated.getClass() == User.class) {
                    User user = (User) manipulated;
                    if (this.equals(user))
                        return true;
                }
            }
            case EDIT_GROUP: {
                if (manipulated != null && manipulated.getClass() == UserGroup.class) {
                    UserGroup group = (UserGroup) manipulated;
                    Logger.debug("First condition: " + group.getUsers().contains(this) + " | second condition: " + (rating > RATING_EDIT_GROUP));
                    group.getUsers().forEach(u -> Logger.debug("u=" + u));
                    if (group.getUsers().contains(this) || rating > RATING_EDIT_GROUP) {
                        return true;
                    }
                }
            }
            case DELETE_GROUP: {
                if (manipulated != null && manipulated instanceof UserGroup) {
                    UserGroup group = (UserGroup) manipulated;
                    if (group.getUsers().contains(this) || rating > RATING_DELETE_GROUP) {
                        return true;
                    }
                }
            }
            case EDIT_RATING: {
                if (manipulated != null && (manipulated instanceof Rating)) {
                    Rating ratingObj = (Rating) manipulated;
                    if (Objects.equals(ratingObj.getAuthor().getId(), id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addUserGroup(UserGroup newGroup) {
        if (!userGroups.contains(newGroup)) {
            userGroups.add(newGroup);
            this.save();
        }
    }
}
