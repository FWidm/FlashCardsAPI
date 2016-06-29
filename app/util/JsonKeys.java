package util;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 20/06/16.
 *
 * Whenever a change is made here, the appropriate class needs to be resaved as well to update th keys.
 */
public class JsonKeys {

    //General
    public static final String RATING = "rating";
    public static final String DATE_CREATED = "created";
    public static final String DATE_UPDATED = "lastUpdated";
    public static final String AUTHOR = "author";
    public static final String URI = "mediaURI";


    //Flashcard
    public static final String FLASHCARD_ID = "flashcardId";
    public static final String FLASHCARD_QUESTION = "question";
    public static final String FLASHCARD_ANSWERS = "answers";
    public static final String FLASHCARD_TAGS = "tags";
    public static final String FLASHCARD_MULTIPLE_CHOICE = "multipleChoice";
    public static final String FLASHCARD_PARAM_SIZE="size";

    //User
    public static final String USER_ID = "userId";
    public static final String USER_NAME="name";
    public static final String USER_PASSWORD="password";
    public static final String USER_EMAIL="email";
    public static final String USER_GROUP="group";
    public static final String USER_JSON_ELEMENTS=USER_NAME+", "+USER_EMAIL+", "+USER_PASSWORD+", "+USER_GROUP;
    //Tag
    public static final String TAG_ID = "tagId";

    //Group
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_NAME="name";
    public static final String GROUP_DESCRIPTION="description";
    public static final String GROUP_USERS="users";
    //Answer
    public static final String ANSWER_ID = "answerId";
    public static final String ANSWER_TEXT = "answerText";
    public static final String ANSWER_HINT = "answerHint";

    //Question
    public static final String QUESTION_ID = "questionId";
    public static final String QUESTION_TEXT = "questionText";
}