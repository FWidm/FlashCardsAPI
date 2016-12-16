package util;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 20/06/16.
 *         <p>
 *         Whenever a change is made here, the appropriate class needs to be resaved as well to update th keys.
 */
public class JsonKeys {
    public static final boolean debugging=true;

    //General
    public static final String RATING = "rating";
    public static final String DATE_CREATED = "created";
    public static final String DATE_UPDATED = "lastUpdated";
    public static final String DATE_LAST_LOGIN="lastLogin";
    public static final String AUTHOR = "author";
    public static final String URI = "mediaURI";
    public static final String TOKEN = "token";
    public static final String STATUS_CODE = "statusCode";
    public static final String DESCRIPTION = "description";

    //table names, for camelcase Classes
    public static final String USER_GROUP_TABLE_NAME="userGroup";
    public static final String FLASH_CARD_TABLE_NAME="flashCard";
    public static final String AUTH_TOKEN_TABLE_NAME="authToken";
    public static final String CARDDECK_TABLE_NAME="cardDeck";


    //Jointable names
    public static final String CARD_TAG_JOIN_TABLE="cardTagJoinTable";
    public static final String USER_GROUP_JOIN_TABLE="userGroupJoinTable";


    //Flashcard
    public static final String FLASHCARD = "flashcard";
    public static final String FLASHCARD_ID = "flashcardId";
    public static final String FLASHCARD_QUESTION = "question";
    public static final String FLASHCARD_ANSWERS = "answers";
    public static final String FLASHCARD_TAGS = "tags";
    public static final String FLASHCARD_MULTIPLE_CHOICE = "multipleChoice";
    public static final String FLASHCARD_PARENT_ID="cardDeckId";
    public static final String FLASHCARD_DECK= "deck";

    public static final String FLASHCARD_JSON_ELEMENTS = RATING + ", " + FLASHCARD_ANSWERS + ", " + FLASHCARD_QUESTION + ", " + AUTHOR + ", " + FLASHCARD_MULTIPLE_CHOICE + ", " + FLASHCARD_TAGS;

    //User
    public static final String USER_ID = "userId";
    public static final String USER_AVATAR = "avatar";
    public static final String USER_NAME = "name";
    public static final String USER_PASSWORD = "password";
    public static final String USER_EMAIL = "email";
    public static final String USER_GROUPS = "groups";
    public static final String USER_JSON_ELEMENTS = USER_NAME + ", " + USER_EMAIL + ", " + USER_PASSWORD + ", " + USER_GROUPS;
    //Tag
    public static final String TAG_ID = "tagId";
    public static final String TAG_NAME = "tagName";
    public static final String TAG_CARDS = "cads";

    //Group
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_NAME = "name";
    public static final String GROUP_DESCRIPTION = "description";
    public static final String GROUP_USERS = "users";
    public static final String GROUP_JSON_ELEMENTS = GROUP_NAME + ", " + GROUP_DESCRIPTION + ", " + GROUP_USERS;

    //Answer
    public static final String ANSWER = "answer" ;
    public static final String ANSWER_ID = "answerId";
    public static final String ANSWER_TEXT = "answerText";
    public static final String ANSWER_HINT = "answerHint";
    public static final String ANSWER_CARD_ID = "cardId";
    public static final String ANSWER_CORRECT = "answerCorrect";
    public static final String ANSWER_JSON_ELEMENTS = ANSWER_TEXT + ", " + ANSWER_HINT + ", " + URI + ", " + AUTHOR+", "+ANSWER_CORRECT;

    //Question
    public static final String QUESTION_ID = "questionId";
    public static final String QUESTION_TEXT = "questionText";
    public static final String QUESTION_JSON_ELEMENTS = QUESTION_TEXT + ", " + URI + ", " + AUTHOR;

    //Token
    public static final String TOKEN_ID = "tokenId";
    public static final String TOKEN_USER = "tokenUser";

    //Rating
    public static final String RATING_MODIFIER="ratingModifier";
    public static final String RATING_ID="ratingId";
    public static final String RATING_TYPE="ratingType";
    public static final String RATING_JSON_ELEMENTS = AUTHOR+" (userID), "+FLASHCARD+" (flashcardID) OR "+ANSWER+" (answerId), "+RATING_MODIFIER;

    //CardDeck
    public static final String CARDDECK_ID = "cardDeckId";
    public static final String CARDDECK_NAME = "cardDeckName";
    public static final String CARDDECK_DESCRIPTION ="cardDeckDescpription";
    public static final String CARDDECK_CARDS="cards";
    public static final String CARDDECK_VISIBLE="visible";
    public static final String CARDDECK_GROUP="userGroup";
    public static final String CARDDECK_CATEGORY= "category";
    public static final String CARDDECK_JSON_ELEMENTS = CARDDECK_NAME+", "+CARDDECK_DESCRIPTION+", "+CARDDECK_CARDS+" (child: list containing flashcardIds)";

    //Category
    public static final String CATEGORY_ID = "categoryId";
    public static final String CATEGORY_NAME = "categoryName";
    public static final String CATEGORY_PARENT = "parent";
    public static final String CATEGORY_DECK = "cardDecks";
    public static final String CATEGORY_JSON_ELEMENTS = CATEGORY_NAME+", "+CATEGORY_PARENT+", "+CATEGORY_DECK+" (list containing cardDeckIds)";

    //Media Upload
    public static final String MEDIA_ID = "mediaId";
    public static final String MEDIA_TYPE = "mediaType";





}