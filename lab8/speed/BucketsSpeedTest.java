package speed;

import edu.princeton.cs.algs4.Stopwatch;

import java.io.IOException;
import java.util.Scanner;

import hashmap.Map61B;
import hashmap.MyHashMapALBuckets;
import hashmap.MyHashMapLLBuckets;
import hashmap.MyHashMapTSBuckets;
import hashmap.MyHashMapHSBuckets;
import hashmap.MyHashMapPQBuckets;

/** Performs a timing test on three different set implementations.
 *  @author Neil Kulkarni adapted from Josh Hug, Brendan Hu
 */
public class BucketsSpeedTest {
    /**
     * Requests user input and performs tests of three different set
     * implementations. ARGS is unused. 
     */
    public static void main(String[] args) throws IOException {
        int N;
        Scanner input = new Scanner(System.in);

        System.out.println("\n This program inserts random "
                + "Strings of length L\n"
                + " Into different types of maps "
                + "as <String, Integer> pairs.\n");
        System.out.print("What would you like L to be?: ");
        int L = waitForPositiveInt(input);

        String repeat = "y";
        do {
            System.out.print("\nEnter # strings to insert into each map: ");
            N = waitForPositiveInt(input);

            // Test each of the map implementations
            timeRandomMap61B(new MyHashMapALBuckets<>(), N, L);
            timeRandomMap61B(new MyHashMapLLBuckets<>(), N, L);
            timeRandomMap61B(new MyHashMapTSBuckets<>(), N, L);
            timeRandomMap61B(new MyHashMapHSBuckets<>(), N, L);
            timeRandomMap61B(new MyHashMapPQBuckets<>(), N, L);

            System.out.print("\nWould you like to try more timed-tests? (y/n)");
            repeat = input.nextLine();
        } while (!repeat.equalsIgnoreCase("n") && !repeat.equalsIgnoreCase("no"));
        input.close();
    }

    /**
     * Returns time needed to put N random strings of length L into the
     * hashmap.Map61B 61bMap.
     */
    public static double insertRandom(Map61B<String, Integer> map61B, int N, int L) {
        Stopwatch sw = new Stopwatch();
        String s = "cat";
        for (int i = 0; i < N; i++) {
            s = StringUtils.randomString(L);
            map61B.put(s, new Integer(i));
        }
        return sw.elapsedTime();
    }

    /**
     * Attempts to insert N random strings of length L into map,
     * Prints time of the N insert calls, otherwise
     * Prints a nice message about the error
     */
    public static void timeRandomMap61B(Map61B<String, Integer> map, int N, int L) {
        try {
            double mapTime = insertRandom(map, N, L);
            System.out.printf(map.getClass() + ": %.2f sec\n", mapTime);
        } catch (StackOverflowError e) {
            printInfoOnStackOverflow(N, L);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Waits for the user on other side of Scanner
     * to enter a positive int,
     * and outputs that int
     */
    public static int waitForPositiveInt(Scanner input) {
        int ret = 0;
        do {
            while (!input.hasNextInt()) {
                errorBadIntegerInput();
                input.next();
            }
            ret = input.nextInt();
            input.nextLine(); //consume \n not taken by nextInt()
        } while (ret <= 0);
        return ret;
    }
    /* ------------------------------- Private methods ------------------------------- */
    /**
     * To be called after catching a StackOverflowError
     * Prints the error with corresponding N and L
     */
    private static void printInfoOnStackOverflow(int N, int L) {
        System.out.println("--Stack Overflow -- couldn't add " + N
                + " strings of length " + L + ".");
    }

    /**
     * Prints a nice message for the user on bad input
     */
    private static void errorBadIntegerInput() {
        System.out.print("Please enter a positive integer: ");
    }
}
