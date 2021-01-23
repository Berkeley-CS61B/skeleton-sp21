package game2048;
import org.junit.Test;

import static org.junit.Assert.*;

/** Tests of the Model class.
 *
 * These tests will cover all of the things you've written together. You
 * shouldn't try to pass these tests until every other Test file passes.
 *
 * @author Omar Khan
 */
public class TestModel extends TestUtils {

    /**
     * ******************
     * *  TESTING TILT  *
     * ******************
     * <p>
     * The following tests determine the correctness of your `tilt`
     * method.
     */

    @Test
    /** Checks right two pieces merge when 3 adjacent pieces have same value. */
    public void testTripleMerge1() {
        int[][] before = new int[][]{
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {4, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Checks right two pieces merge when 3 adjacent pieces have same value. */
    public void testTripleMerge2() {
        int[][] before = new int[][]{
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 0, 0},
                {4, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Checks two adjacent merges work. */
    public void testQuadrupleMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 2},
                {0, 0, 0, 2},
                {0, 0, 0, 2},
                {0, 0, 0, 2},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 4},
                {0, 0, 0, 4},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 8, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Checks that a tile only merges once per tilt. */
    public void testSingleMergeUp() {
        int[][] before = new int[][]{
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {4, 0, 0, 0},
                {4, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Checks that a tile only merges once per tilt. */
    public void testSingleMergeSouth() {
        int[][] before = new int[][]{
                {4, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 0, 0, 0},
                {4, 0, 0, 0},
        };
        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Checks that a tile only merges once per tilt. */
    public void testSingleMergeEast() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 0, 2, 2},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 4, 4},
        };
        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Checks that a tile only merges once per tilt. */
    public void testSingleMergeWest() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 2, 0, 4},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 4, 0, 0},
        };
        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.WEST);
    }

    @Test
    /** Checks that a tilt that causes no change returns false. */
    public void testNoMove() {
        int[][] before = new int[][]{
                {2, 0, 2, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = before;

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, false, changed);
        checkModel(after, 0, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Move tiles up (no merging). */
    public void testUpNoMerge() {
        int[][] before = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 4, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Move adjacent tiles up (no merging). */
    public void testUpAdjacentNoMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 4, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Move non-adjacent tiles up (no merging). */
    public void testUpNonAdjacentNoMerge1() {
        int[][] before = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 0, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Move non-adjacent tiles up (no merging); case 2: both tiles move. */
    public void testMoveUpNonAdjacentNoMerge2() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 4, 0},
                {0, 0, 0, 0},
                {0, 0, 2, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Merge adjacent tiles up. */
    public void testUpAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 0, 2, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Merge non-adjacent tiles up. */
    public void testUpNonAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 0, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 2, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Move and merge adjacent tiles up. */
    public void testUpAdjacentMergeMove() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 2, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 4, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.NORTH);
        checkChanged(Side.NORTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.NORTH);
    }

    @Test
    /** Move tiles right (no merging). */
    public void testRightNoMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 4},
                {0, 0, 2, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 4},
                {0, 0, 0, 2},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Move adjacent tiles right (no merging). */
    public void testRightAdjacentNoMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 4, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 2, 4},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Move adjacent tiles right (no merging). */
    public void testRightNonAdjacentNoMerge1() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 0, 4},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 2, 4},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Move adjacent tiles right (no merging); case 2: both tiles move. */
    public void testRightNonAdjacentNoMerge2() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 4, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 2, 4},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Merge adjacent tiles right. */
    public void testRightAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 2, 2},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 4},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Merge non-adjacent tiles right. */
    public void testRightNonAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 0, 2},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 4},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Move and merge adjacent tiles right. */
    public void testRightAdjacentMergeMove() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 2, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 4},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Move and merge non-adjacent tiles right. */
    public void testRightNonAdjacentMergeMove() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 2, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 4},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.EAST);
        checkChanged(Side.EAST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.EAST);
    }

    @Test
    /** Move tiles down (no merging). */
    public void testDownNoMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 4, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 4, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Move adjacent tiles down (no merging). */
    public void testDownAdjacentNoMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 4, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 4, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Move non-adjacent tiles down (no merging). */
    public void testDownNonAdjacentNoMerge1() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 4, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Merge adjacent tiles down. */
    public void testDownAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 2, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Merge non-adjacent tiles down. */
    public void testDownNonAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Move and merge adjacent tiles down. */
    public void testDownAdjacentMergeMove() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Move and merge non-adjacent tiles down. */
    public void testDownNonAdjacentMergeMove() {
        int[][] before = new int[][]{
                {0, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.SOUTH);
        checkChanged(Side.SOUTH, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.SOUTH);
    }

    @Test
    /** Move tiles left (no merging). */
    public void testLeftNoMerge() {
        int[][] before = new int[][]{
                {4, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {4, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.WEST);
    }

    @Test
    /** Move adjacent tiles left (no merging). */
    public void testLeftAdjacentNoMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 4, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {4, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.WEST);
    }

    @Test
    /** Move non-adjacent tiles left (no merging). */
    public void testLeftNonAdjacentNoMerge1() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {4, 0, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {4, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 0, 0, prevBoard, Side.WEST);
    }

    @Test
    /** Merge adjacent tiles left. */
    public void testLeftAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {2, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {4, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.WEST);
    }

    @Test
    /** Merge non-adjacent tiles left. */
    public void testLeftNonAdjacentMerge() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {2, 0, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {4, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.WEST);
    }

    @Test
    /** Move and merge adjacent tiles left. */
    public void testLeftAdjacentMergeMove() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 2, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {4, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.WEST);
    }

    @Test
    /** Move and merge non-adjacent tiles left. */
    public void testLeftNonAdjacentMergeMove() {
        int[][] before = new int[][]{
                {0, 0, 0, 0},
                {0, 2, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        int[][] after = new int[][]{
                {0, 0, 0, 0},
                {4, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        updateModel(before, 0, 0, false);
        String prevBoard = model.toString();
        boolean changed = model.tilt(Side.WEST);
        checkChanged(Side.WEST, true, changed);
        checkModel(after, 4, 0, prevBoard, Side.WEST);
    }

    /**
     * ***********************
     * *  TESTING GAME OVER  *
     * ***********************
     * <p>
     * The following tests determine the correctness of your `checkGameOver`
     * method.
     */

    @Test
    /** No tilt can cause a change. */
    public void testGameOverNoChange1() {
        int[][] board = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        };

        updateModel(board, 0, 0, false);
        assertTrue("Game is over. No tilt would result in a change"
                + model, model.gameOver());
    }

    @Test
    /** The MAX_PIECE (2048) tile is on the board. */
    public void testGameOverMaxPiece() {
        int[][] board = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 2048}
        };

        updateModel(board, 0, 0, false);
        assertTrue("Game is over. Tile with 2048 is on board:"
                + model, model.gameOver());
    }

    @Test
    /** No tilt can cause a change. */
    public void testGameOverNoChange2() {
        int[][] board = {
                {128, 4, 2, 4},
                {4, 32, 4, 2},
                {8, 16, 2, 8},
                {4, 32, 4, 1024}
        };

        updateModel(board, 0, 0, false);
        assertTrue("Game is over. Tile with 2048 is on board:"
                + model, model.gameOver());
    }

    @Test
    /** Any tilt will change the board. */
    public void testGameNotOver1() {
        int[][] board = {
                {2, 4, 2, 2},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        };
        updateModel(board, 0, 0, false);
        assertFalse("Game isn't over. Any tilt will result in a change:"
                + model, model.gameOver());
    }

    @Test
    /** A tilt right or down will change the board. */
    public void testGameNotOver2() {
        int[][] board = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 0}
        };
        updateModel(board, 0, 0, false);
        assertFalse("Game isn't over. A tilt right or down will result"
                + " in a change:" + model, model.gameOver());
    }

    /**
     * *************************
     * *  MULTIPLE MOVE TESTS  *
     * *************************
     * <p>
     * The following tests will call the `tilt` method multiple times and check
     * the correctness of the board after each move. You shouldn't expect these
     * tests to pass until all of the above tests pass.
     */

    @Test
    /** Will test multiple moves on the Model. */
    public void testMultipleMoves1() {
        int[][] board = new int[][]{
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 2}
        };

        String prevBoard;
        String currBoard;
        Side currMove;
        Tile toAdd;
        int totalScore = 0;

        updateModel(board, 0, 0, false);

        prevBoard = board.toString();
        currMove = Side.EAST;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 2}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(2, 3, 1);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.NORTH;
        totalScore += 4;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 0, 4},
                {0, 0, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(2, 0, 1);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.EAST;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 0, 4},
                {0, 0, 0, 2},
                {0, 0, 0, 2},
                {0, 0, 0, 0}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(4, 2, 0);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.NORTH;
        totalScore += 4;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 4, 4},
                {0, 0, 0, 4},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(4, 0, 3);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.SOUTH;
        totalScore += 8;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 0, 4, 8}
        }, totalScore, 0, prevBoard, currMove);
    }

    @Test
    /** Will test multiple moves on the Model that end the game. */
    public void testMultipleMoves2() {
        int[][] board = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 256, 256, 0},
                {1024, 0, 0, 512}
        };

        String prevBoard;
        String currBoard;
        Side currMove;
        Tile toAdd;
        int totalScore = 0;

        updateModel(board, 0, 0, false);

        prevBoard = model.toString();
        currMove = Side.EAST;
        totalScore += 512;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 512},
                {0, 0, 1024, 512}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(2, 0, 0);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.SOUTH;
        model.tilt(currMove);
        totalScore += 1024;
        checkModel(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 1024, 1024}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(2, 0, 1);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.WEST;
        model.tilt(currMove);
        totalScore += 2048;
        assertTrue("Game is over. Tile with 2048 is on board:"
                + model, model.gameOver());
        checkModel(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 2048, 0, 0}
        }, totalScore, totalScore, prevBoard, currMove);
    }

    @Test
    /** Will test multiple moves on the Model. */
    public void testMultipleMoves3() {
        int[][] board = new int[][]{
                {0, 2, 2, 0},
                {4, 0, 4, 0},
                {4, 0, 8, 0},
                {8, 0, 0, 0}
        };

        String prevBoard;
        String currBoard;
        Side currMove;
        Tile toAdd;
        int totalScore = 0;

        updateModel(board, 0, 0, false);

        prevBoard = model.toString();
        currMove = Side.EAST;
        totalScore += 4 + 8;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 0, 4},
                {0, 0, 0, 8},
                {0, 0, 4, 8},
                {0, 0, 0, 8}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(2, 1, 2);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.SOUTH;
        totalScore += 16;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 4},
                {0, 0, 0, 8},
                {0, 2, 4, 16}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(2, 1, 1);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.NORTH;
        totalScore += 4;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 4, 4, 4},
                {0, 0, 0, 8},
                {0, 0, 0, 16},
                {0, 0, 0, 0}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(4, 0, 0);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.NORTH;
        model.tilt(currMove);
        checkModel(new int[][]{
                {4, 4, 4, 4},
                {0, 0, 0, 8},
                {0, 0, 0, 16},
                {0, 0, 0, 0}
        }, totalScore, 0, prevBoard, currMove);
        toAdd = Tile.create(2, 3, 0);
        model.addTile(toAdd);

        prevBoard = model.toString();
        currMove = Side.EAST;
        totalScore += 8 + 8;
        model.tilt(currMove);
        checkModel(new int[][]{
                {0, 0, 8, 8},
                {0, 0, 0, 8},
                {0, 0, 0, 16},
                {0, 0, 0, 2}
        }, totalScore, 0, prevBoard, currMove);
    }
}
