import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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
 * <p>
 * N.B. Calling in parallel (with Asyncs) does not make this calculator faster,
 * but this approach will speed up other cpu-intensive tasks on multicore cpu(s)
 */
public class JavaAsyncCalc {
    private final boolean DEBUG = true;

    char[] R; // string
    char C = '\00'; // current char
    int P; // current position
    public AST root;

    Executor executor;

    static class AST {
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

    public class Computor implements Supplier {
        AST ast;

        public Computor(AST ast) {
            this.ast = ast;
        }

        @Override
        public Double get() {
            double result = 0;
            try {
                if (ast.num != null)
                    return ast.num;
                CompletableFuture<Double> left = CompletableFuture.supplyAsync(new Computor(ast.left));
                if (ast.op == null)
                    return left.get(); // blocking
                if (DEBUG)
                    System.out.println(String.format("DEBUG: STARTED\t %s in thread %s - %d", ast.toString(), Thread.currentThread().getId(), System.currentTimeMillis() % 1024));
                CompletableFuture<Double> right = CompletableFuture.supplyAsync(new Computor(ast.right));
                BiFunction<Double, Double, Double> fn;
                switch (ast.op) {
                    case '+':
                        fn = (x, y) -> x + y;
                        break;
                    case '-':
                        fn = (x, y) -> x - y;
                        break;
                    case '*':
                        fn = (x, y) -> x * y;
                        break;
                    case '/':
                        fn = (x, y) -> x / y;
                        break;
                    default:
                        throw new RuntimeException("Not yet implemented: " + ast.op);
                }
                CompletableFuture<Double> combinator = left.thenCombineAsync(right, fn, executor);
                result = combinator.get(); //blocking
                if (DEBUG)
                    System.out.println(String.format("DEBUG: DONE\t %s in thread %s - %d", ast.toString(), Thread.currentThread().getId(), System.currentTimeMillis() % 1024));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public JavaAsyncCalc(String s) {
        executor = ForkJoinPool.commonPool();
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
        return new Computor(root).get(); //blocking
    }


    public static void testAST(JavaAsyncCalc c, String s) {
        String ast = c.toString();
        assert (ast.equals(s)) : String.format("Expected \"%s\" but get \"%s\"", s, ast);
    }

    public static void testCalc(JavaAsyncCalc c, double d) {
        double x = c.calc();
        assert (x == d) : String.format("Expected \"%f\" but get \"%f\"", d, x);
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
        testAST(new JavaAsyncCalc("10%3"), "0.0"); // if not supported, just get 0
        testAST(new JavaAsyncCalc("10-a"), "0.0"); // if not supported, just get 0

        testCalc(new JavaAsyncCalc(""), 0D); // empty
        testCalc(new JavaAsyncCalc("2"), 2D); // number
        testCalc(new JavaAsyncCalc("2+2"), 4D); // terms
        testCalc(new JavaAsyncCalc("2+2+2"), 6D); // terms
        testCalc(new JavaAsyncCalc("3-2-1"), 0); // terms
        testCalc(new JavaAsyncCalc("3*3*3"), 27D); // terms
        testCalc(new JavaAsyncCalc("2*2+3*2"), 10D); // factors
        testCalc(new JavaAsyncCalc("2 * 2 + 3 * 2"), 10D); // spaces
        testCalc(new JavaAsyncCalc("(1 + 1) * 2"), 4D); // subexpression
        testCalc(new JavaAsyncCalc("2 * (1 + 1)"), 4D); // subexpression
        testCalc(new JavaAsyncCalc("((1 - 2) * 2)"), -2D); // nested expression
        testCalc(new JavaAsyncCalc("(-1 + 2) * 2"), 2D); // negative number
        testCalc(new JavaAsyncCalc("(1 + 2) * -2"), -6D); // negative number
        testCalc(new JavaAsyncCalc("(1 - +2) * +2"), -2D); // positive number
        testCalc(new JavaAsyncCalc("2 * 2.5"), 5D); // real number
        testCalc(new JavaAsyncCalc("0.5 * -2.25"), -1.125D); // real numbers
        testCalc(new JavaAsyncCalc("(-1) * -2 - -3"), 5D); // smth weird
        testCalc(new JavaAsyncCalc("(1-1)*2+3*(1-3+4)+10/2"), 11D); // large
        testCalc(new JavaAsyncCalc("10%3"), 0D); // if not supported, just get 0
        testCalc(new JavaAsyncCalc("10-a"), 0D); // if not supported, just get 0

        System.out.println("passed");
    }
}
