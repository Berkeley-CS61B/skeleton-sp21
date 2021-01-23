package game2048;

import static org.junit.Assert.*;

public class TestUtils {

    /** The Model we'll be testing. */
    static Model model;
    /** The size of the Board on these tests. */
    public static final int SIZE = 4;

    /** Utility method to generate an error message. */
    public static String boardShouldChange(Side side) {
        return "When tilted to the " + side + ", the model should change, but"
                + " the call to tilt returned false.\nModel after call:" + model;
    }

    /** Utility method to generate an error message. */
    public static String boardShouldNotChange(Side side) {
        return "When tilted to the " + side + ", the model should NOT change,"
                + " but the call to tilt returned true.\nModel after call:"
                + model;
    }

    /**
     * Updates the static variable model to be the Model with board attribute
     * as described by VALUES.
     */
    public static void updateModel(int[][] values, int score, int maxScore,
                                   boolean gameOver) {
        assert values.length == SIZE : "board must have 4x4 dimensions";
        assert values[0].length == SIZE : "board must have 4x4 dimensions";
        model = new Model(values, score, maxScore, gameOver);
    }

    /**
     * Checks that the static variable model is configured as described by
     * VALUES with score attribute SCORE.
     *  @param values - a 2D array of integers describing the expected board a
     *               "0" element represents a null Tile.
     * @param score - what score the model should have.
     * @param maxScore
     * @param prevBoard - what the board looked like before this move.
     * @param currMove - the Side that we tilted towards.
     */
    public static void checkModel(int[][] values, int score, int maxScore,
                                  String prevBoard, Side currMove) {

        Model expected = new Model(values, score, maxScore, false);
        String errMsg = String.format("Board incorrect. Before tilting towards"
                        + " %s, your board looked like:%s%nAfter the call to"
                        + " tilt, we expected:%s%nBut your board looks like:%s.",
                currMove, prevBoard, expected.toString(), model.toString());
        assertEquals(errMsg, expected, model);
    }

    /**
     * Checks that the returned boolean of a call to the tilt method is correct.
     *
     * @param s - the side that was tilted (the parameter to tilt).
     * @param expected - what the expected return value is.
     * @param actual - what the actual return value is.
     */
    public static void checkChanged(Side s, boolean expected, boolean actual) {
        String changedErrMsg;
        if (expected) {
            changedErrMsg = boardShouldChange(s);
            assertTrue(changedErrMsg, actual);
        } else {
            changedErrMsg = boardShouldNotChange(s);
            assertFalse(changedErrMsg, actual);
        }
    }
}
