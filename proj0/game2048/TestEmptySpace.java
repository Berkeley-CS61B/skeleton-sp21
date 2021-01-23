package game2048;

import org.junit.Test;

import static org.junit.Assert.*;

/** Tests the emptySpaceExists() static method of Model.
 *
 * @author Omar Khan
 */
public class TestEmptySpace {

    /** The Board that we'll be testing on. */
    static Board b;

    @Test
    /** Note that this isn't a possible board state. */
    public void testCompletelyEmpty() {
        int[][] rawVals = new int[][] {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        b = new Board(rawVals, 0);
        assertTrue("Board is full of empty space\n" + b,
                Model.emptySpaceExists(b));
    }

    @Test
    /** Tests a board that is completely full except for the top row. */
    public void testEmptyTopRow() {
        int[][] rawVals = new int[][] {
                {0, 0, 0, 0},
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
        };

        b = new Board(rawVals, 0);

        assertTrue("Top row is empty\n" + b, Model.emptySpaceExists(b));
    }

    @Test
    /** Tests a board that is completely full except for the bottom row. */
    public void testEmptyBottomRow() {
        int[][] rawVals = new int[][] {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {0, 0, 0, 0},
        };

        b = new Board(rawVals, 0);
        assertTrue("Bottom row is empty\n" + b,
                Model.emptySpaceExists(b));
    }


    @Test
    /** Tests a board that is completely full except for the left column. */
    public void testEmptyLeftCol() {
        int[][] rawVals = new int[][] {
                {0, 4, 2, 4},
                {0, 2, 4, 2},
                {0, 4, 2, 4},
                {0, 2, 4, 2},
        };

        b = new Board(rawVals, 0);

        assertTrue("Left col is empty\n" + b,
                Model.emptySpaceExists(b));
    }

    @Test
    /** Tests a board that is completely full except for the right column. */
    public void testEmptyRightCol() {
        int[][] rawVals = new int[][] {
                {2, 4, 2, 0},
                {4, 2, 4, 0},
                {2, 4, 2, 0},
                {4, 2, 4, 0},
        };

        b = new Board(rawVals, 0);

        assertTrue("Right col is empty\n" + b,
                Model.emptySpaceExists(b));
    }

    @Test
    /** Tests a completely full board except one piece. */
    public void testAlmostFullBoard() {
        int[][] rawVals = new int[][] {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 0, 2, 4},
                {4, 2, 4, 2},
        };

        b = new Board(rawVals, 0);

        assertTrue("Board is not full\n" + b,
                Model.emptySpaceExists(b));
    }

    @Test
    /** Tests a completely full board.
     * The game isn't over since you can merge, but the emptySpaceExists method
     * should only look for empty space (and not adjacent values). */
    public void testFullBoard() {
        int[][] rawVals = new int[][] {
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
        };

        b = new Board(rawVals, 0);

        assertFalse("Board is full\n" + b, Model.emptySpaceExists(b));
    }

    @Test
    /** Tests a completely full board. */
    public void testFullBoardNoMerge() {
        int[][] rawVals = new int[][] {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2},
        };

        b = new Board(rawVals, 0);

        assertFalse("Board is full\n" + b, Model.emptySpaceExists(b));
    }
}
