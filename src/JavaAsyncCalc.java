/**
 * Supported operations:
 * basic ariphmetic operations: + - / *
 * grouping: ()
 * <p>
 * Rules:
 * Numbers should have their sign (positive/negative), if any, attached without spaces
 * <p>
 * To build Async Calc I need some other approach, because I do not know how to implement recursive
 * descend in parallel, so I am building an AST first...
 */
public class JavaAsyncCalc {
    char[] R; // string
    char C = '\00'; // current char
    int P; // current position
    public AST root;

    class AST {
        Character op;
        Double num;
        AST left;
        AST right;

        public AST(Character op, AST left, AST right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        public AST(AST left) {
            this.left = left;
        }

        public AST(double num) {
            this.num = num;
        }

        @Override
        public String toString() {
            if (num != null)
                return num.toString();
            if (op == null)
                return left.toString();
            return String.format("(%c %s %s)", op, left, right);
        }
    }

    public JavaAsyncCalc(String s) {
        R = s.toCharArray();
        if (R.length > 0)
            C = R[0];
        parse();
    }

    private void read() {
        C = ++P < R.length ? R[P] : '\00';
    }

    private boolean expect(char c) {
        while (C == ' ')
            read();
        if (C == c) {
            read();
            return true;
        }
        return false;
    }

    public void parse() {
        AST exp = expression();
        root = C == '\00' ? exp : new AST(null, new AST(null, new AST(0D), null), null); // if error in parsing just return 0
    }

    private AST expression() {
        AST left = term();
        if (expect('+'))
            return new AST('+', left, expression());
        else if (expect('-'))
            return new AST('-', left, expression());
        return new AST(null, left, null);
    }

    private AST term() {
        AST left = factor();
        if (expect('*'))
            return new AST('*', left, term());
        else if (expect('/'))
            return new AST('/', left, term());
        return new AST(left);
    }

    private AST factor() {
        if (expect('(')) {
            AST subExp = expression();
            expect(')');
            return new AST(subExp);
        } else
            return new AST(number());
    }

    private double number() {
        double x = 0;
        boolean negative = false;
        if (C == '-' || C == '+') {
            if (C == '-')
                negative = true;
            read();
        }
        if (Character.isDigit(C)) {
            while (Character.isDigit(C)) {
                x = x * 10 + Character.getNumericValue(C);
                read();
            }
            if (C == '.') {
                int div = 1;
                read();
                while (Character.isDigit(C)) {
                    div *= 10;
                    x = x + (double) Character.getNumericValue(C) / div;
                    read();
                }
            }
        }
        return negative ? -x : x;
    }

    public static void test(String s1, String s2) {
        assert (s1.equals(s2)) : String.format("Expected \"%s\" but get \"%s\"", s2, s1);
    }

    public static void main(String[] args) {

        // -ea REMEMBER
        test(new JavaAsyncCalc("").root.toString(), "0.0"); // empty
        test(new JavaAsyncCalc("2").root.toString(), "2.0"); // number
        test(new JavaAsyncCalc("2+2").root.toString(), "(+ 2.0 2.0)"); // terms
        test(new JavaAsyncCalc("2+2+2").root.toString(), "(+ 2.0 (+ 2.0 2.0))"); // terms
        test(new JavaAsyncCalc("3*3*3").root.toString(), "(* 3.0 (* 3.0 3.0))"); // terms
        test(new JavaAsyncCalc("2*2+3*2").root.toString(), "(+ (* 2.0 2.0) (* 3.0 2.0))"); // factors
        test(new JavaAsyncCalc("2 * 2 + 3 * 2").root.toString(), "(+ (* 2.0 2.0) (* 3.0 2.0))"); // spaces
        test(new JavaAsyncCalc("(1 + 1) * 2").root.toString(), "(* (+ 1.0 1.0) 2.0)"); // subexpression
        test(new JavaAsyncCalc("2 * (1 + 1)").root.toString(), "(* 2.0 (+ 1.0 1.0))"); // subexpression
        test(new JavaAsyncCalc("((1 - 2) * 2)").root.toString(), "(* (- 1.0 2.0) 2.0)"); // nested expression
        test(new JavaAsyncCalc("(-1 + 2) * 2").root.toString(), "(* (+ -1.0 2.0) 2.0)"); // negative number
        test(new JavaAsyncCalc("(1 + 2) * -2").root.toString(), "(* (+ 1.0 2.0) -2.0)"); // negative number
        test(new JavaAsyncCalc("(1 - +2) * +2").root.toString(), "(* (- 1.0 2.0) 2.0)"); // positive number
        test(new JavaAsyncCalc("2 * 2.5").root.toString(), "(* 2.0 2.5)"); // real number
        test(new JavaAsyncCalc("0.5 * -2.25").root.toString(), "(* 0.5 -2.25)"); // real numbers
        test(new JavaAsyncCalc("(-1) * -2 - -3").root.toString(), "(- (* -1.0 -2.0) -3.0)"); // smth weird
        test(new JavaAsyncCalc("(1-1)*2+3*(1-3+4)+10/2").root.toString(), "(+ (* (- 1.0 1.0) 2.0) (+ (* 3.0 (- 1.0 (+ 3.0 4.0))) (/ 10.0 2.0)))"); // large
//
        test(new JavaAsyncCalc("10%3").root.toString(), "0.0"); // if not supported, just get 0
        test(new JavaAsyncCalc("10-a").root.toString(), "0.0"); // if not supported, just get 0

        System.out.println("passed");
    }

}
