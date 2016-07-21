package util;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 20/06/16.
 *         <p>
 *         Whenever a change is made here, the appropriate class needs to be resaved as well to update th keys.
 */
public class JsonKeys {

    //General
    public static final String RATING = "rating";
    public static final String DATE_CREATED = "created";
    public static final String DATE_UPDATED = "lastUpdated";
    public static final String AUTHOR = "author";
    public static final String URI = "mediaURI";
    public static final String TOKEN = "token";

    //Flashcard
    public static final String FLASHCARD_ID = "flashcardId";
    public static final String FLASHCARD_QUESTION = "question";
    public static final String FLASHCARD_ANSWERS = "answers";
    public static final String FLASHCARD_TAGS = "tags";
    public static final String FLASHCARD_MULTIPLE_CHOICE = "multipleChoice";
    public static final String FLASHCARD_PARAM_SIZE = "size";
    public static final String FLASHCARD_JSON_ELEMENTS = RATING + ", " + FLASHCARD_ANSWERS + ", " + FLASHCARD_QUESTION + ", " + AUTHOR + ", " + FLASHCARD_MULTIPLE_CHOICE + ", " + FLASHCARD_TAGS;


    //User
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "name";
    public static final String USER_PASSWORD = "password";
    public static final String USER_EMAIL = "email";
    public static final String USER_GROUP = "group";
    public static final String USER_JSON_ELEMENTS = USER_NAME + ", " + USER_EMAIL + ", " + USER_PASSWORD + ", " + USER_GROUP;
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
    public static final String ANSWER_ID = "answerId";
    public static final String ANSWER_TEXT = "answerText";
    public static final String ANSWER_HINT = "answerHint";
    public static final String ANSWER_JSON_ELEMENTS = ANSWER_TEXT + ", " + ANSWER_HINT + ", " + URI + ", " + AUTHOR;

    //Question
    public static final String QUESTION_ID = "questionId";
    public static final String QUESTION_TEXT = "questionText";
    public static final String QUESTION_JSON_ELEMENTS = QUESTION_TEXT + ", " + URI + ", " + AUTHOR;

    //Token
    public static final String TOKEN_ID = "tokenId";
    public static final String TOKEN_USER = "tokenUser";

}