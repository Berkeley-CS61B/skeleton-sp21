package game2048;

import java.util.Random;

import ucb.util.CommandArgs;

/** The main class for the 2048 game.
 *  @author P. N. Hilfinger
 */
public class Main {

    /** Number of squares on the side of a board. */
    static final int BOARD_SIZE = 4;
    /** Probability of choosing 2 as random tile (as opposed to 4). */
    static final double TILE2_PROBABILITY = 0.9;

    /** The main program.  ARGS may contain the options --seed=NUM,
     *  (random seed); --log (record moves and random tiles
     *  selected.). */
    public static void main(String... args) {
        CommandArgs options =
            new CommandArgs("--seed=(\\d+) --log=(.+)",
                            args);
        if (!options.ok()) {
            System.err.println("Usage: java game2048.Main [ --seed=NUM ] "
                               + "[ --log=LOG_FILE ]");
            System.exit(1);
        }

        Random gen = new Random();
        if (options.contains("--seed")) {
            gen.setSeed(options.getLong("--seed"));
        }

        Model model = new Model(BOARD_SIZE);

        GUI gui;

        gui = new GUI("2048 61B", model);
        gui.display(true);

        InputSource inp;

        inp = new GUISource(gui, gen, TILE2_PROBABILITY,
                            options.getFirst("--log"));

        Game game = new Game(model, inp);

        try {
            while (game.playing()) {
                game.playGame();
            }
        } catch (IllegalStateException excp) {
            System.err.printf("Internal error: %s%n", excp.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

}
