public class HelloNumbers {
    public static void main(String[] args) {
        int x = 1;
        int total = 0;
        while (x <= 10) {
            System.out.print(total + " ");
            total = total + x;
            x = x + 1;
        }
	}
} 
