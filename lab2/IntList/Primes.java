package IntList;

public class Primes {

    /**
     * This (complicated) algorithm returns True if its argument is prime,
     * otherwise False. When you're debugging, stepping into this function may
     * not be the best idea! Consider instead stepping *over* this function,
     * and ensuring that its return value makes sense.
     *
     * If you're curious, this algorithm uses Fermat's Little Theorem as a
     * primality test, and returns the correct answer w.h.p. (due to the presence)
     * of Carmichael numbers. If this makes no sense to you, good! It shouldn't.
     * The goal of this function is to make sure you learn to abstract away the inner
     * workings of a function and debug it as a black-box with the "Step Over" feature.
     *
     * @source: https://www.geeksforgeeks.org/primality-test-set-2-fermet-method/
     * @param n an arbitary integrer
     * @return True iff. the integer is prime
     */
    public static boolean isPrime(int n) {
        // Corner cases
        if (n <= 1 || n == 4) return false;
        if (n <= 3) return true;

        int k = 3; // Try k = 3 times
        while (k > 0)
        {
            // Pick a random number in [2..n-2]
            // Above corner cases make sure that n > 4
            int a = 2 + (int)(Math.random() % (n - 4));

            // Fermat's little theorem
            if (power(a, n - 1, n) != 1)
                return false;

            k--;
        }

        return true;
    }

    /**
     * This is a helper method to isPrime. You can ignore this method.
     * It is an iterative Function to calculate a^n mod p in log time
     *
     * @source: https://www.geeksforgeeks.org/primality-test-set-2-fermet-method/
     */
    static int power(int a, int n, int p)
    {
        // Initialize result
        int res = 1;

        // Update 'a' if 'a' >= p
        a = a % p;

        while (n > 0)
        {
            // If n is odd, multiply 'a' with result
            if ((n & 1) == 1)
                res = (res * a) % p;

            // n must be even now
            n = n >> 1; // n = n/2
            a = (a * a) % p;
        }
        return res;
    }

    /** Driver Code */
    public static void main(String[] args) {
        /* Print the first 20 primes */
        int primeCount = 0;
        int x = 2;

        while (primeCount < 20) {
            if (isPrime(x)) {
                System.out.println(x);
                primeCount++;
            }
            x++;
        }
    }
}
