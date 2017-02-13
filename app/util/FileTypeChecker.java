package util;

/**
 * @author Fabian Widmann
 */
public class FileTypeChecker {
    public static String getFileType(String base64FileString) {
        System.out.println("Input=" + base64FileString);
        String result = "." + base64FileString.substring(base64FileString.indexOf("/") + 1, base64FileString.indexOf(";"));
        System.out.println("Filetype=" + result);
        return result;
    }
}
