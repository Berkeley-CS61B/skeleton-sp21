package game2048;

import java.util.Observer;
import java.util.Observable;

/** An observer that prints changes to a Model.
 *  @author P. N. Hilfinger
 */
class BoardLogger implements Observer {

    /** A line to separate each move. */
    private static final String LINE = "---------------------";

    @Override
    /** Prints the board state and how it was changed after each move. */
    public void update(Observable obs, Object arg) {
        Model model = (Model) obs;
        String direction;
        if (arg == null) {
            direction = "Randomly generated tiles placed on board";
        } else {
            direction = String.format("Board tilted %s", arg);
        }
        System.out.printf("%n%s%n%s%s", LINE, direction, model);
    }

}
