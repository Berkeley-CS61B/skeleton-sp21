/** Class that prints the Collatz sequence starting from a given number.
 *  @author YOUR NAME HERE
 */
public class Collatz {

    /** Buggy implementation of nextNumber! */
    public static int nextNumber(int n) {
        if (n  % 2 == 0) { // n 除 2 沒餘數 = 偶數
            return n >> 1; // bitwise右移一位
        } else if (n % 2 == 1) { // 判斷 n 除 2 的餘數有 1 = 奇數
            return 3 * n + 1;
        } else {
            return 0; // 特殊情況跳過
        }
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}

