package gitlet;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author xUser5000
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            exitWithMessage("Please enter a command.");
        }

        String command = args[0];
        String fileName;
        String message;
        try {
            switch (command) {
                case "init":
                    validateNumArgs(args, 1);
                    Repository.init();
                    break;
                case "add":
                    validateNumArgs(args, 2);
                    fileName = args[1];
                    Repository.add(fileName);
                    break;
                case "commit":
                    validateNumArgs(args, 2);
                    message = args[1];
                    Repository.commit(message);
                    break;
                case "rm":
                    validateNumArgs(args, 2);
                    fileName = args[1];
                    Repository.rm(fileName);
                    break;
                case "log":
                    validateNumArgs(args, 1);
                    Repository.log();
                    break;
                default:
                    exitWithMessage("No command with that name exists.");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            exitWithMessage("Incorrect operands.");
        }
    }
}
