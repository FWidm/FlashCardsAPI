package util;

import models.*;
import models.msg.AbstractMessage;
import models.rating.Rating;
import play.Logger;

import java.util.Objects;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
public class UserRightManagement {
    public static final int RATING_CREATE_CATEGORY = 1000;
    public static final int RATING_DELETE_CATEGORY = 1000;
    public static final int RATING_EDIT_CATEGORY = 1000;

    public static final int RATING_DELETE_CARD = 100;
    public static final int RATING_EDIT_CARD = 100;

    public static final int RATING_DELETE_ANSWER = 100;
    public static final int RATING_EDIT_ANSWER = 100;

    public static final int RATING_DELETE_DECK = 100;
    public static final int RATING_EDIT_DECK = 100;

    public static final int RATING_EDIT_USER = 1000;

    public static final int RATING_EDIT_GROUP = 1000;
    public static final int RATING_DELETE_GROUP = 1000;

    /**
     * Checks whether the current user has the rights to perform the operation we want to check. If an object is passed
     * we can check if the user is in any way an owner and has rights regardless of his rating.
     *
     * @param userOperation - the operation the user wants to do
     * @param manipulated   - the manipulated object
     * @return true if the user can do the operation, else false.
     */
    public boolean hasRight(User user, UserOperations userOperation, Object manipulated) {
        Logger.debug("Checking " + user.getEmail() + ": for (" + userOperation + "|" + manipulated + ")");



        //Used class compare instead of instanceof due to performance reasons.
        switch (userOperation) {
            //message
            case GET_MESSAGE:{
                //receive single message
                if (manipulated != null && manipulated instanceof AbstractMessage) {
                    AbstractMessage msg = (AbstractMessage) manipulated;
                    Logger.debug("is Recipient=" + (msg.getRecipient().getId() == user.getId()));

                    //can delete own cards OR any cards when this user's rating is over a specific value
                    if (msg.getRecipient().getId() == user.getId()) {
                        return true;
                    }
                }
                //receive list of messages
                if(manipulated==null){

                }
            }
            //category
            case CREATE_CATEGORY: {
                if (user.getRating() >= RATING_CREATE_CATEGORY) {
                    return true;
                }
            }
            case DELETE_CATEGORY: {
                if (user.getRating() >= RATING_DELETE_CATEGORY) {
                    return true;
                }
            }
            case EDIT_CATEGORY: {
                if (user.getRating() >= RATING_EDIT_CATEGORY) {
                    return true;
                }
            }
            //cards
            case DELETE_CARD: {
                if (manipulated != null && manipulated.getClass() == FlashCard.class) {
                    FlashCard card = (FlashCard) manipulated;
                    Logger.debug("isAuthor=" + (card.getAuthor() == user) + " | has Rating? " + (user.getRating() >= RATING_DELETE_CARD));

                    //can delete own cards OR any cards when this user's rating is over a specific value
                    if (card.getAuthor() == user || user.getRating() >= RATING_DELETE_CARD) {
                        return true;
                    }
                }
            }
            case EDIT_CARD: {
                if (manipulated != null && manipulated.getClass() == FlashCard.class) {
                    FlashCard card = (FlashCard) manipulated;
                    //can edit own cards OR any cards when this user's rating is over a specific value
                    if (card.getAuthor() == user || user.getRating() >= RATING_EDIT_CARD) {
                        return true;
                    }
                }
            }
            //answers
            case DELETE_ANSWER: {
                if (manipulated != null && manipulated.getClass() == Answer.class) {
                    Answer answer = (Answer) manipulated;
                    //can delete own cards OR any cards when this user's rating is over a specific value
                    if (answer.getAuthor() == user || user.getRating() >= RATING_DELETE_ANSWER) {
                        return true;
                    }
                }
            }
            case EDIT_ANSWER: {
                if (manipulated != null && manipulated.getClass() == Answer.class) {
                    Answer answer = (Answer) manipulated;
                    //can edit own cards OR any cards when this user's rating is over a specific value
                    if (answer.getAuthor() == user || user.getRating() >= RATING_EDIT_ANSWER) {
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
                    if (user.getRating() >= RATING_DELETE_DECK || (group != null && group.getUsers().contains(this))) {
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

                    if (user.getRating() >= RATING_EDIT_DECK || group.getUsers().contains(this)) {
                        return true;
                    }
                }
            }
            case EDIT_USER: {
                if (manipulated != null && manipulated.getClass() == User.class) {
                    User targetUser = (User) manipulated;
                    if (targetUser.getId()==user.getId() || user.getRating() >= RATING_EDIT_USER)
                        return true;
                }
            }
            case DELETE_USER: {
                if (manipulated != null && manipulated.getClass() == User.class) {
                    User targetUser = (User) manipulated;
                    if (targetUser.getId()==user.getId())
                        return true;
                }
            }
            case EDIT_GROUP: {
                if (manipulated != null && manipulated.getClass() == UserGroup.class) {
                    UserGroup group = (UserGroup) manipulated;
                    Logger.debug("First condition: " + group.getUsers().contains(this) + " | second condition: " + (user.getRating() > RATING_EDIT_GROUP));
                    group.getUsers().forEach(u -> Logger.debug("u=" + u));
                    if (group.getUsers().contains(this) || user.getRating() > RATING_EDIT_GROUP) {
                        return true;
                    }
                }
            }
            case DELETE_GROUP: {
                if (manipulated != null && manipulated instanceof UserGroup) {
                    UserGroup group = (UserGroup) manipulated;
                    if (group.getUsers().contains(this) || user.getRating() > RATING_DELETE_GROUP) {
                        return true;
                    }
                }
            }
            case EDIT_RATING: {
                if (manipulated != null && (manipulated instanceof Rating)) {
                    Rating ratingObj = (Rating) manipulated;
                    if (Objects.equals(ratingObj.getAuthor().getId(), user.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
