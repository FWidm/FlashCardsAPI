package util;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 20/06/16.
 */
public enum JsonKeys {
    USER_ID("userId"), CARD_ID(""), ANSWER_ID(""), GROUP_ID("groupId"), QUESTION_ID("");


    private String name;

    private JsonKeys(String s) {
        name = s;
    }
}
