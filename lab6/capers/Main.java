package capers;

import java.io.File;
import java.io.IOException;

import static capers.Utils.*;

/** Canine Capers: A Gitlet Prelude.
 * @author xUser5000
*/
public class Main {
    /**
     * Runs one of three commands:
     * story [text] -- Appends "text" + a newline to a story file in the
     *                 .capers directory. Additionally, prints out the
     *                 current story.
     *
     * dog [name] [breed] [age] -- Persistently creates a dog with
     *                             the specified parameters; should also print
     *                             the dog's toString(). Assume dog names are
     *                             unique.
     *
     * birthday [name] -- Advances a dog's age persistently
     *                    and prints out a celebratory message.
     *
     * All persistent data should be stored in a ".capers"
     * directory in the current working directory.
     *
     * Recommended structure (you do not have to follow):
     *
     * *YOU SHOULD NOT CREATE THESE MANUALLY,
     *  YOUR PROGRAM SHOULD CREATE THESE FOLDERS/FILES*
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all of the persistent data for dogs
     *    - story -- file containing the current story
     *
     * @param args arguments from the command line
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.exitWithError("Must have at least one argument");
        }

        try {
            CapersRepository.setupPersistence();

            switch (args[0]) {
                case "story":
                    validateNumArgs("story", args, 2);
                    String text = args[1];
                    CapersRepository.writeStory(text);
                    break;
                case "dog":
                    validateNumArgs("dog", args, 4);
                    String name = args[1];
                    String breed = args[2];
                    int age = Integer.parseInt(args[3]);
                    CapersRepository.makeDog(name, breed, age);
                    break;
                case "birthday":
                    validateNumArgs("birthday", args, 2);
                    String dogName = args[1];
                    CapersRepository.celebrateBirthday(dogName);
                    break;
                default:
                    exitWithError(String.format("Unknown command: %s", args[0]));
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
