import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Supported operations:
 * basic ariphmetic operations: + - / *
 * grouping: ()
 * <p>
 * Rules:
 * Numbers should have their sign (positive/negative), if any, attached without spaces
 * <p>
 *
 * Akka version uses same Java parser.
 */

public class JavaAkkaCalcApp extends AbstractActor {

    private static ActorSystem system;

    // helper class for testing
    Pair pair;

    static class Pair {
        String expression;
        double answer;

        public Pair(String exp, double ans) {
            this.expression = exp;
            this.answer = ans;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // chaining calculation
                .match(Pair.class, pair -> {
                    this.pair = pair;
                    ActorRef actor = getContext().actorOf(Props.create(JavaAkkaCalc.class));
                    JavaAsyncCalc calc = new JavaAsyncCalc(pair.expression);
                    actor.tell(calc.root, self());
                })
                // processing callbacks, testing result
                .match(Double.class, d -> {
                    assert (d == pair.answer) : String.format("Expected \"%f\" but get \"%f\"", pair.answer, d);
                    getContext().stop(self());
                })
                .build();
    }

    static void testCalc(String exp, double d) {
        ActorRef actor = system.actorOf(Props.create(JavaAkkaCalcApp.class));
        actor.tell(new Pair(exp, d), null);
    }

    public static void main(String[] args) throws Exception {

        // -ea REMEMBER

        system = ActorSystem.create("JavaAkkaCalcApp");

        testCalc("", 0D); // empty
        testCalc("2", 2D); // number
        testCalc("2+2", 4D); // terms
        testCalc("2+2+2", 6D); // terms
        testCalc("3-2-1", 0); // terms
        testCalc("3*3*3", 27D); // terms
        testCalc("2*2+3*2", 10D); // factors
        testCalc("2 * 2 + 3 * 2", 10D); // spaces
        testCalc("(1 + 1) * 2", 4D); // subexpression
        testCalc("2 * (1 + 1)", 4D); // subexpression
        testCalc("((1 - 2) * 2)", -2D); // nested expression
        testCalc("(-1 + 2) * 2", 2D); // negative number
        testCalc("(1 + 2) * -2", -6D); // negative number
        testCalc("(1 - +2) * +2", -2D); // positive number
        testCalc("2 * 2.5", 5D); // real number
        testCalc("0.5 * -2.25", -1.125D); // real numbers
        testCalc("(-1) * -2 - -3", 5D); // smth weird
        testCalc("(1-1)*2+3*(1-3+4)+10/2", 11D); // large
        testCalc("10%3", 0D); // if not supported, just get 0
        testCalc("10-a", 0D); // if not supported, just get 0

        Thread.sleep(2000);
        system.terminate();
    }
}
