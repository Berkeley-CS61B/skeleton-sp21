package game2048;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/** A type of InputSource that receives commands from a GUI.
 *  @author P. N. Hilfinger
 */
class GUISource implements InputSource {

    /** Provides input from SOURCE, logging it iff LOG. Use RANDOMSOURCE to
     *  select random tiles having value 2 with probability PROBOF2. */
    GUISource(GUI source, Random randomSource, double probOf2,
              String logFileName) {
        _source = source;
        _randomSource = randomSource;
        _probOf2 = probOf2;

        if (logFileName != null) {

            File logFile = new File(logFileName);
            try {
                _logFileWriter = new FileWriter(logFile);
            } catch (IOException e) {
                System.err.println("Error: no such file " + logFileName);
                System.exit(1);
            }
        }
    }

    @Override
    /** Return and log which direction arrow was pressed. */
    public String getKey() {
        String command = _source.readKey();
        switch (command) {
            case "↑" :
                command = "Up";
                break;
            case "→" :
                command = "Right";
                break;
            case "↓" :
                command = "Down";
                break;
            case "←" :
                command = "Left";
                break;
            default :
                break;
        }

        String logLine = String.format("K %s%n", command);

        if (_logFileWriter != null) {
            System.out.print(logLine);
            try {
                _logFileWriter.write(logLine);
            } catch (IOException e) {
                System.err.print("Error: cannot write to log file");
                System.exit(1);
            }
        }
        return command;
    }

    @Override
     /** Return a randomly positioned tile with either value of 2 with 
      * probability _probOf2 or a value of 4 with probability 1 - _probOf2 in a
      * board with size SIZE. */
    public Tile getNewTile(int size) {
        int c = _randomSource.nextInt(size), r = _randomSource.nextInt(size);
        int v = _randomSource.nextDouble() <= _probOf2 ? 2 : 4;
        if (_logFileWriter != null) {
            System.out.printf("T %d %d %d%n", v, c, r);
        }
        return Tile.create(v, c, r);
    }

    /** Input source. */
    private GUI _source;
    /** Random source for Tiles. */
    private Random _randomSource;
    /** Probabilty that value of new Tile is 2 rather than 4. */
    private double _probOf2;
    /** The FileWriter to log inputs to (null if no logging required). */
    private FileWriter _logFileWriter;

}
