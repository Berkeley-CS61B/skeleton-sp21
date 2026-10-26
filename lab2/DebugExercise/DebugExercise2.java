package DebugExercise;

/**
 * Exercise to showcase the step over button.
 * Code adapted from https://stackoverflow.com/questions/4895173/bitwise-multiply-and-add-in-java and https://stackoverflow.com/questions/1533131/what-useful-bitwise-operator-code-tricks-should-a-developer-know-about
 */
public class DebugExercise2 {
    /** Returns the max of a and b. Do not step into this function. */
    public static int max(int a, int b) {
        int c = a - b;
        if (c > 0) {
            return a;
        }
        return b;
    }


    /** Returns the sum of a and b. Do not step into this function. */
    public static int add(int a, int b) {
        return a + b;
    }

    /** Returns a new array where entry i is the max of
     * a[i] and b[i]. For example, if a = {1, -10, 3}
     * and b = {0, 20, 5}, this function will return {1, 20, 5}.
     * */
    public static int[] arrayMax(int[] a, int[] b) {
        if (a.length != b.length) {
            System.out.println("ERROR! Arrays don't match");
            return null;
        }
        int[] returnArray = new int[a.length];
        for (int i = 0; i < a.length; i += 1) {
            int biggerValue = max(a[i], b[i]);
            returnArray[i] = biggerValue;
        }

        return returnArray;
    }

    /** Returns the sum of all elements in x. */
    public static int arraySum(int[] x) {
        int i = 0;
        int sum = 0;
        while (i < x.length) {
            sum = add(sum, x[i]);
            i = i + 1;
        }
        return sum;
    }

    /** Returns the sum of the element-wise max of a and b.
     *  For example if a = {2, 0, 10, 14} and b = {-5, 5, 20, 30},
     *  the result should be 57.
     * */
    public static int sumOfElementwiseMaxes(int[] a, int[] b) {
        int[] maxes = arrayMax(a, b);
        int sumofMaxes = arraySum(maxes);
        return sumofMaxes;
    }


    public static void main(String[] args) {
        int[] a = {1, 11, -1, -11};
        int[] b = {3, -3, 2, -1};

        int sumOfElementwiseMaxes = sumOfElementwiseMaxes(a, b);
        System.out.println(sumOfElementwiseMaxes);
    }
}
