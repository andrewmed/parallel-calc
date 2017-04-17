import akka.actor.*;
import java.util.function.BiFunction;

public class JavaAkkaCalc extends AbstractActor {
    private static final boolean DEBUG = true;

    // state for callbacks
    Character op;
    ActorRef lActor;
    Double lAnswer;
    ActorRef rActor;
    Double rAnswer;
    BiFunction<Double, Double, Double> fn;

    // state for debugging
    JavaAsyncCalc.AST ast;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // chaining calculation
                .match(JavaAsyncCalc.AST.class, ast -> {
                        if (ast.num != null)
                            sendBack(ast.num);
                        else if (ast.op == null)
                            lActor = forward(ast.left);
                        else {
                            if (DEBUG)
                                System.out.println(String.format("DEBUG: STARTED\t %s in thread %s - %d", ast.toString(), Thread.currentThread().getId(), System.currentTimeMillis() % 1024));
                            saveState(ast);
                            lActor = forward(ast.left);
                            rActor = forward(ast.right);
                        }
                })
                // processing callbacks
                .match(Double.class, d -> {
                    if (getSender().equals(lActor))
                        lAnswer = d;
                    else
                        rAnswer = d;
                    if (op == null && lAnswer != null)
                        sendBack(lAnswer);
                    if (lAnswer != null && rAnswer != null)
                        sendBack(fn.apply(lAnswer, rAnswer));
                })
                .build();
    }

    /**
     * Forwards calculation to a sub-Actor
     */
    private ActorRef forward(JavaAsyncCalc.AST ast) {
        ActorRef actor = getContext().actorOf(Props.create(JavaAkkaCalc.class));
        actor.tell(ast, self());
        return actor;
    }

    /**
     * Saves state to process values from forwarded calculations
     */
    private void saveState(JavaAsyncCalc.AST ast) {
        this.ast = ast;
        op = ast.op;
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
    }

    /**
     * Returns value to parent and terminates itself
     */
    public void sendBack(double d) {
        if (DEBUG && ast != null)
            System.out.println(String.format("DEBUG: DONE\t %s in thread %s - %d", ast.toString(), Thread.currentThread().getId(), System.currentTimeMillis() % 1024));
        getContext().parent().tell(d, self());
        getContext().stop(self());
    }

}
