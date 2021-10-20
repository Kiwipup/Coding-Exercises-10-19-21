public class LispValidator {

    public static void main(String[] args) {
        String testVal = "(defun fibonacci (N)\n" +
                "  \"Compute the N'th Fibonacci number.\"\n" +
                "  (if (or (zerop N) (= N 1))\n" +
                "      1\n" +
                "    (+ (fibonacci (- N 1)) (fibonacci (- N 2)))))";
        System.out.println(validateParentheses(testVal) ? "good" : "bad");
        System.out.println(validateParentheses("()()()(") ? "good" : "bad");
        System.out.println(validateParentheses("(v%^$#())") ? "good" : "bad");
        System.out.println(validateParentheses("()()($%#@())())") ? "good" : "bad");
        System.out.println(validateParentheses(")(") ? "good" : "bad");
    }

    public static boolean validateParentheses(String lisp) {
        String value = lisp.replaceAll(" ", "");

        int parenthesesCount = 0;
        for (char c : value.toCharArray()) {
            if (c == '(') {
                parenthesesCount++;
            } else if (c == ')') {
                parenthesesCount--;
            }
            if (parenthesesCount < 0)
                return false;
        }
        return parenthesesCount == 0;
    }
}
