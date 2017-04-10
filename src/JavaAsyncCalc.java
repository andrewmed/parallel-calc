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

    @Override
    public String toString() {
        return root.toString();
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
        root = C == '\00' ? exp : new AST(0D); // if error in parsing just return 0
    }

    private AST expression() {
        // left-associativity, iteration
        AST ast = term();
        boolean plus;
        boolean minus;
        while ((plus = expect('+')) || (minus = expect('-'))) {
            if (plus)
                ast = new AST('+', ast, term());
            else
                ast = new AST('-', ast, term());
        }
        return ast;
    }

    private AST term() {
        // right-associativity, recursion
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

    public double calc() {
        return 0D; // FIXME
    }

    public static void testAST(JavaAsyncCalc c, String s) {
        String ast = c.toString();
        assert (ast.equals(s)) : String.format("Expected \"%s\" but get \"%s\"", s, ast);
    }

    public static void testCalc(JavaAsyncCalc c, double d) {
        double x = c.calc();
        assert (x == d) : String.format("Expected \"%d\" but get \"%d\"", d, x);
    }

    public static void main(String[] args) {

        // -ea REMEMBER
        testAST(new JavaAsyncCalc(""), "0.0"); // empty
        testAST(new JavaAsyncCalc("2"), "2.0"); // number
        testAST(new JavaAsyncCalc("2+2"), "(+ 2.0 2.0)"); // terms
        testAST(new JavaAsyncCalc("3-2"), "(- 3.0 2.0)"); // terms
        testAST(new JavaAsyncCalc("2+2+2"), "(+ (+ 2.0 2.0) 2.0)"); // terms
        testAST(new JavaAsyncCalc("3-2-1"), "(- (- 3.0 2.0) 1.0)"); // terms
        testAST(new JavaAsyncCalc("3*3*3"), "(* 3.0 (* 3.0 3.0))"); // terms
        testAST(new JavaAsyncCalc("2*2+3*2"), "(+ (* 2.0 2.0) (* 3.0 2.0))"); // factors
        testAST(new JavaAsyncCalc("2 * 2 + 3 * 2"), "(+ (* 2.0 2.0) (* 3.0 2.0))"); // spaces
        testAST(new JavaAsyncCalc("(1 + 1) * 2"), "(* (+ 1.0 1.0) 2.0)"); // subexpression
        testAST(new JavaAsyncCalc("2 * (1 + 1)"), "(* 2.0 (+ 1.0 1.0))"); // subexpression
        testAST(new JavaAsyncCalc("((1 - 2) * 2)"), "(* (- 1.0 2.0) 2.0)"); // nested expression
        testAST(new JavaAsyncCalc("(-1 + 2) * 2"), "(* (+ -1.0 2.0) 2.0)"); // negative number
        testAST(new JavaAsyncCalc("(1 + 2) * -2"), "(* (+ 1.0 2.0) -2.0)"); // negative number
        testAST(new JavaAsyncCalc("(1 - +2) * +2"), "(* (- 1.0 2.0) 2.0)"); // positive number
        testAST(new JavaAsyncCalc("2 * 2.5"), "(* 2.0 2.5)"); // real number
        testAST(new JavaAsyncCalc("0.5 * -2.25"), "(* 0.5 -2.25)"); // real numbers
        testAST(new JavaAsyncCalc("(-1) * -2 - -3"), "(- (* -1.0 -2.0) -3.0)"); // smth weird
        testAST(new JavaAsyncCalc("(1-1)*2+3*(1-3+4)+10/2"), "(+ (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) (/ 10.0 2.0))"); // large
//
        testAST(new JavaAsyncCalc("10%3"), "0.0"); // if not supported, just get 0
        testAST(new JavaAsyncCalc("10-a"), "0.0"); // if not supported, just get 0

        System.out.println("passed");
    }

}
