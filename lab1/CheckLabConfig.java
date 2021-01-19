import java.io.File;

public class CheckLabConfig {
	public static void checkConfig(boolean isWindows) {
		String repoVariableName = "REPO_DIR";
		String repoDir = System.getenv(repoVariableName);
		checkVariableNotNull(repoDir, repoVariableName);
		System.out.println("Validating your " + repoVariableName + 
			               " environment variable, which is currently set to: " +
			               repoDir);
		checkIsValidRepo(repoDir, repoVariableName, isWindows, false);

		String snapsVariableName = "SNAPS_DIR";
		String snapsRepoDir = System.getenv(snapsVariableName);

		checkVariableNotNull(snapsRepoDir, snapsVariableName);
		System.out.println("Validating your " + snapsVariableName + 
			               " environment variable, which is currently set to: " +
			               snapsRepoDir);
		checkIsValidRepo(snapsRepoDir, snapsVariableName, isWindows, true);
	}

	public static void checkVariableNotNull(String value, String name) {
		if (value == null) {
			System.out.println("ERROR! Environment variable " + name + " is not set.");
			System.out.println("If you've already set it using the lab 1 setup directions, " +
				               "then try restarting your terminal or IntelliJ.");
			System.exit(1);
		}
	}

	public static void checkIsValidRepo(String value, String name,
										boolean isWindows, boolean snapsCheck) {

		String splitString = "";
		if (isWindows) {
			splitString = "\\\\";
		} else {
			splitString = "/";
		}

		String[] tokens = value.split(splitString);
		String folderName = tokens[tokens.length - 1];

		String pattern;
		String expected;

		if (!snapsCheck) {
			pattern = "sp21-s[\\d]+";
			expected = "sp21-s1234";
		} else {
			pattern = "snaps-sp21-s[\\d]+";
			expected = "snaps-sp21-s1234";
		}

		if (!folderName.matches(pattern)) {
			System.out.println("ERROR! Your " + name + " environment variable is incorrect.");
			System.out.println("The folder name in the end should match this pattern: " + expected);
			System.exit(1);
		}

		File file = new File(value);

		boolean isDirectory = file.isDirectory(); 
		if (!isDirectory) {
			System.out.println("ERROR! " + value + " is not a valid folder.");
			System.out.println("Double check that this variable was set correctly.");
			System.exit(1);
		}

	}

	public static void main(String[] args) {
		System.out.println("Testing configuration. This program only works for the " +
			               "Spring 2021 edition of this course.");

		String yourOS = System.getProperty("os.name").toLowerCase();
		String yourOSVersion = System.getProperty("os.version");

		if (yourOS.contains("windows")) {
			checkConfig(true);
		} else {
			checkConfig(false);
		}

		/*if (yourOS.contains("mac")) {
			if (yourOSVersion.contains("10.15")) {
				checkConfig(false);
			} else {
				checkConfig(false);
			}
		} // for future reference in case we need to test configurations separately
		     for Mac OS, Catalina, Linux, etc*/

		System.out.println("Your system appears to be configured correctly. You've completed lab 1 setup.");
	}
}


