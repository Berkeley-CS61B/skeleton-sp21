package bstmap;

import java.util.regex.Pattern;
import java.util.Random;

/** Utility function for Strings.
 *  @author Josh Hug
 */
public class StringUtils {
    /** To get the style checker to be quiet. */
    private static final int ALPHABET_SIZE = 26;

    /** Random number generator for this class. */
    private static Random r = new Random();

    /** Sets random seed to L so that results of randomString are predictable.*/
    public static void setSeed(long l) {
        r = new Random(l);
    }

    /** Returns the next random string of length LENGTH. */
    public static String randomString(int length) {
        char[] someChars = new char[length];
        for (int i = 0; i < length; i++) {
            someChars[i] = (char) (r.nextInt(ALPHABET_SIZE) + 'a');
        }
        return new String(someChars);
    }

    /** Returns true if string S consists of characters between
      * 'a' and 'z' only. No spaces, numbers, upper-case, or any other
      * characters are allowed.
      */
    public static boolean isLowerCase(String s) {
        return Pattern.matches("[a-z]*", s);
    }

    /** Returns the string that comes right after S in alphabetical order.
      * For example, if s is 'potato', this method will return 'potatp'. If
      * the last character is a z, then we add to the next position, and so
      * on.
      */
    public static String nextString(String s) {
        /* Handle all zs as a special case to keep helper method simple. */
        if (isAllzs(s)) {
            return allAs(s.length() + 1);
        }
        char[] charVersion = s.toCharArray();
        incrementCharArray(charVersion, charVersion.length - 1);
        return new String(charVersion);
    }

    /** Helper function for nextString. Increments the Pth position of X
      * by one, wrapping around to 'a' if p == 'z'. If wraparound occurs,
      * then we need to carry the one, and we increment position P - 1.
      *
      * Will fail for a character array containing only zs.
      */
    private static void incrementCharArray(char [] x, int p) {
        if (x[p] != 'z') {
            x[p] += 1;
        } else {
            x[p] = 'a';
            incrementCharArray(x, p - 1);
        }
    }

    /** Returns a string of all 'a' of length LEN. */
    private static String allAs(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    /** Returns true if S is all 'z'. False for empty strings */
    public static boolean isAllzs(String s) {
        return Pattern.matches("[z]+", s);
    }

}
