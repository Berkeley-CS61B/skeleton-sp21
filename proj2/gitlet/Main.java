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
        String commitHash;
        String branchName;
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
                case "global-log":
                    validateNumArgs(args, 1);
                    Repository.globalLog();
                    break;
                case "find":
                    validateNumArgs(args, 2);
                    message = args[1];
                    Repository.find(message);
                    break;
                case "status":
                    validateNumArgs(args, 1);
                    Repository.status();
                    break;
                case "checkout":
                    switch (args.length) {
                        case 2:
                            branchName = args[1];
                            Repository.checkoutBranch(branchName);
                            break;
                        case 3:
                            if (!args[1].equals("--")) incorrectOperands();
                            fileName = args[2];
                            Repository.checkoutFile(fileName);
                            break;
                        case 4:
                            if (!args[2].equals("--")) incorrectOperands();
                            commitHash = args[1];
                            fileName = args[3];
                            Repository.checkoutFile(commitHash, fileName);
                            break;
                        default:
                            incorrectOperands();
                    }
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
            incorrectOperands();
        }
    }

    public static void incorrectOperands() {
        exitWithMessage("Incorrect operands.");
    }
}
