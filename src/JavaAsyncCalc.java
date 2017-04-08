/**
 * Rules:
 * Numbers should have their sign (positive/negative), if any, attached without spaces
 *
 * To build Async Calc I need some other approach, because I do not know how to implement recursive
 *  descend in parallel, so I am building an AST first...
 */
public class JavaAsyncCalc {
    char[] R; // string
    char C = '\00'; // current char
    int P; // current position
    public Expression root;

    class Expression {
        Character op;
        Term left;
        Term right;

        public Expression(Character op, Term left, Term right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return op == null ? left.toString() : String.format("(%c %s %s)", op, left, right);
        }
    }

    class Term {
        Character op;
        Factor left;
        Factor right;

        public Term(Character op, Factor left, Factor right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return op == null ? left.toString() : String.format("(%c %s %s)", op, left, right);
        }
    }

    class Factor {
        Double num;  // either num or expression
        Expression exp;

        public Factor(Double num, Expression exp) {
            this.num = num;
            this.exp = exp;
        }

        @Override
        public String toString() {
            return exp == null ? num.toString() : exp.toString();
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
        Expression exp = expression();
        root =  C == '\00' ? exp : new Expression(null, new Term(null, new Factor(0D, null), null), null); // if error in parsing just return 0
    }

    private Expression expression() {
        Term left = term();
        if (expect('+'))
            return new Expression('+', left, term());
        else if (expect('-'))
            return new Expression('-', left, term());
        return new Expression(null, left, null);
    }

    private Term term() {
        Factor left = factor();
        if (expect('*'))
            return new Term('*', left, factor());
        else if (expect('/'))
            return new Term('/', left, factor());
        return new Term(null, left, null);
    }

    private Factor factor() {
        if (expect('(')) {
            Expression subExp = expression();
            expect(')');
            return new Factor(null, subExp);
        } else
            return new Factor(number(), null);
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
        assert (s1.equals(s2)) : String.format("Expected \"%s\" but get \"%s\")", s2, s1);
    }

    public static void main(String[] args) {

        // -ea REMEMBER
        test (new JavaAsyncCalc("").root.toString(), "0.0"); // empty
        test (new JavaAsyncCalc("2").root.toString(), "2.0"); // number
        test (new JavaAsyncCalc("2+2").root.toString(), "(+ 2.0 2.0)"); // terms
        test (new JavaAsyncCalc("2*2+3*2").root.toString(), "(+ (* 2.0 2.0) (* 3.0 2.0))"); // factors
        test (new JavaAsyncCalc("2 * 2 + 3 * 2").root.toString(),  "(+ (* 2.0 2.0) (* 3.0 2.0))"); // spaces
        test (new JavaAsyncCalc("(1 + 1) * 2").root.toString(),  "(* (+ 1.0 1.0) 2.0)"); // subexpression
        test (new JavaAsyncCalc("2 * (1 + 1)").root.toString(),  "(* 2.0 (+ 1.0 1.0))"); // subexpression
        test (new JavaAsyncCalc("((1 - 2) * 2)").root.toString(),  "(* (- 1.0 2.0) 2.0)"); // nested expression
        test (new JavaAsyncCalc("(-1 + 2) * 2").root.toString(),  "(* (+ -1.0 2.0) 2.0)"); // negative number
        test (new JavaAsyncCalc("(1 + 2) * -2").root.toString(),  "(* (+ 1.0 2.0) -2.0)"); // negative number
        test (new JavaAsyncCalc("(1 - +2) * +2").root.toString(),  "(* (- 1.0 2.0) 2.0)"); // positive number
        test (new JavaAsyncCalc("2 * 2.5").root.toString(),  "(* 2.0 2.5)"); // real number
        test (new JavaAsyncCalc("0.5 * -2.25").root.toString(),  "(* 0.5 -2.25)"); // real numbers
        test (new JavaAsyncCalc("(-1) * -2 - -3").root.toString(),  "(- (* -1.0 -2.0) -3.0)"); // smth weird
//
        test (new JavaAsyncCalc("10%3").root.toString(), "0.0"); // if not supported, just get 0
        test (new JavaAsyncCalc("10-a").root.toString(), "0.0"); // if not supported, just get 0

        System.out.println("passed");
    }

}
