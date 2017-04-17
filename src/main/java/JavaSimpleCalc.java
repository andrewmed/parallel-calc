/**
 * Supported operations:
 * basic ariphmetic operations: + - / *
 * grouping: ()
 * <p>
 * Rules:
 * Numbers should have their sign (positive/negative), if any, attached without spaces
 */
public class JavaSimpleCalc {
    char[] R; // string
    char C = '\00'; // current char
    int P; // current position

    public JavaSimpleCalc(String s) {
        R = s.toCharArray();
        if (R.length > 0)
            C = R[0];
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

    public double calc() {
        double x = expression();
        return C == '\00' ? x : 0; // if error in parsing just return 0
    }

    private double expression() {
        double x = term();
        while (true) {
            if (expect('+'))
                x += term();
            else if (expect('-'))
                x -= term();
            else
                return x;
        }
    }

    private double term() {
        double x = factor();
        while (true) {
            if (expect('*'))
                x *= factor();
            else if (expect('/'))
                x /= factor();
            else
                return x;
        }
    }

    private double factor() {
        double x;
        if (expect('(')) {
            x = expression();
            expect(')');
        } else
            x = number();
        return x;
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


    public static void main(String[] args) {

        // -ea REMEMBER
        assert (new JavaSimpleCalc("").calc() == 0); // empty
        assert (new JavaSimpleCalc("2").calc() == 2); // number
        assert (new JavaSimpleCalc("2+2").calc() == 4); // terms
        assert (new JavaSimpleCalc("2+2+2").calc() == 6); // terms
        assert (new JavaSimpleCalc("3*3*3").calc() == 27); // terms
        assert (new JavaSimpleCalc("2*2+3*2").calc() == 10); // factors
        assert (new JavaSimpleCalc("2 * 2 + 3 * 2").calc() == 10); // spaces
        assert (new JavaSimpleCalc("(1 + 1) * 2").calc() == 4); // subexpression
        assert (new JavaSimpleCalc("2 * (1 + 1)").calc() == 4); // subexpression
        assert (new JavaSimpleCalc("((1 - 2) * 2)").calc() == -2); // nested expression
        assert (new JavaSimpleCalc("(-1 + 2) * 2").calc() == 2); // negative number
        assert (new JavaSimpleCalc("(1 + 2) * -2").calc() == -6); // negative number
        assert (new JavaSimpleCalc("(1 - +2) * +2").calc() == -2); // positive number
        assert (new JavaSimpleCalc("2 * 2.5").calc() == 5); // real number
        assert (new JavaSimpleCalc("0.5 * -2.25").calc() == -1.125); // real numbers
        assert (new JavaSimpleCalc("(-1) * -2 - -3").calc() == 5); // smth weird
        assert (new JavaSimpleCalc("(1-1)*2+3*(1-3+4)+10/2").calc() == 11); // large

        assert (new JavaSimpleCalc("10%3").calc() == 0); // if not supported, just get 0
        assert (new JavaSimpleCalc("10-a").calc() == 0); // if not supported, just get 0

        System.out.println("passed");
    }

}
