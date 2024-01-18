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

        Repository repository = new Repository(System.getProperty("user.dir"));

        String command = args[0];
        String fileName;
        String message;
        String commitHash;
        String branchName;
        try {
            switch (command) {
                case "init":
                    validateNumArgs(args, 1);
                    repository.init();
                    break;
                case "add":
                    validateNumArgs(args, 2);
                    fileName = args[1];
                    repository.add(fileName);
                    break;
                case "commit":
                    validateNumArgs(args, 2);
                    message = args[1];
                    repository.commit(message);
                    break;
                case "rm":
                    validateNumArgs(args, 2);
                    fileName = args[1];
                    repository.rm(fileName);
                    break;
                case "log":
                    validateNumArgs(args, 1);
                    repository.log();
                    break;
                case "global-log":
                    validateNumArgs(args, 1);
                    repository.globalLog();
                    break;
                case "find":
                    validateNumArgs(args, 2);
                    message = args[1];
                    repository.find(message);
                    break;
                case "status":
                    validateNumArgs(args, 1);
                    repository.status();
                    break;
                case "checkout":
                    switch (args.length) {
                        case 2:
                            branchName = args[1];
                            repository.checkoutBranch(branchName);
                            break;
                        case 3:
                            if (!args[1].equals("--")) incorrectOperands();
                            fileName = args[2];
                            repository.checkoutFile(fileName);
                            break;
                        case 4:
                            if (!args[2].equals("--")) incorrectOperands();
                            commitHash = args[1];
                            fileName = args[3];
                            repository.checkoutFile(commitHash, fileName);
                            break;
                        default:
                            incorrectOperands();
                    }
                    break;
                case "branch":
                    validateNumArgs(args, 2);
                    branchName = args[1];
                    repository.branch(branchName);
                    break;
                case "rm-branch":
                    validateNumArgs(args, 2);
                    branchName = args[1];
                    repository.rmBranch(branchName);
                    break;
                case "reset":
                    validateNumArgs(args, 2);
                    commitHash = args[1];
                    repository.reset(commitHash);
                    break;
                case "merge":
                    validateNumArgs(args, 2);
                    branchName = args[1];
                    repository.merge(branchName);
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
