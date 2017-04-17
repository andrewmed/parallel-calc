### Whats this

Simple (direct calculation) and Parallel (AST based) calculator

#### Technical details
* Java direct computation version computes on the go, so no AST is necessary
* Java async pure JDK computation version builds AST tree first using recursive descent (this step is non parallelized). For subexpression computation `CompletableFuture` is used, thus subexpression are calculated in parallel.
* Akka version, in addition, makes queuing expression calls asynchronous (java async version is blocking per one expression call).

#### How to use
```
sbt "runMain JavaSimpleCalc"
sbt "runMain JavaAsyncCalc"
sbt "runMain JavaAkkaCalcApp"
```