### Whats this

Simple (direct calculation) and Parallel (AST based) calculator

#### Technical details
* Java direct computation version computes on the go, so no AST is necessary
* Java async version is pure JDK. It builds AST tree first using recursive descent (this step is non parallelized). For subexpression computation `CompletableFuture` is used and subexpressions are calculated in different threads asynchronously.
* Akka version, in addition, makes queuing expression calls asynchronous (java async version is blocking per one expression call).

#### How to use
```
sbt "runMain JavaSimpleCalc"
sbt "runMain JavaAsyncCalc"
sbt "runMain JavaAkkaCalcApp"
```

For example:
```
$ sbt "runMain JavaAsyncCalc"
[info] Running JavaAsyncCalc
[info] DEBUG: STARTED	 (+ 2.0 2.0) in thread 1 - 776
[info] DEBUG: DONE	 (+ 2.0 2.0) in thread 1 - 872
[info] DEBUG: STARTED	 (+ (+ 2.0 2.0) 2.0) in thread 1 - 873
[info] DEBUG: STARTED	 (+ 2.0 2.0) in thread 15 - 874
[info] DEBUG: DONE	 (+ 2.0 2.0) in thread 15 - 876
[info] DEBUG: DONE	 (+ (+ 2.0 2.0) 2.0) in thread 1 - 877
[info] DEBUG: STARTED	 (- (- 3.0 2.0) 1.0) in thread 1 - 877
[info] DEBUG: STARTED	 (- 3.0 2.0) in thread 24 - 880
[info] DEBUG: DONE	 (- 3.0 2.0) in thread 24 - 883
[info] DEBUG: DONE	 (- (- 3.0 2.0) 1.0) in thread 1 - 883
[info] DEBUG: STARTED	 (* 3.0 (* 3.0 3.0)) in thread 1 - 884
[info] DEBUG: STARTED	 (* 3.0 3.0) in thread 34 - 886
[info] DEBUG: DONE	 (* 3.0 3.0) in thread 34 - 887
[info] DEBUG: DONE	 (* 3.0 (* 3.0 3.0)) in thread 1 - 888
[info] DEBUG: STARTED	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 1 - 888
[info] DEBUG: STARTED	 (* 2.0 2.0) in thread 40 - 889
[info] DEBUG: STARTED	 (* 3.0 2.0) in thread 41 - 890
[info] DEBUG: DONE	 (* 2.0 2.0) in thread 40 - 892
[info] DEBUG: DONE	 (* 3.0 2.0) in thread 41 - 893
[info] DEBUG: DONE	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 1 - 894
[info] DEBUG: STARTED	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 1 - 895
[info] DEBUG: STARTED	 (* 2.0 2.0) in thread 51 - 896
[info] DEBUG: STARTED	 (* 3.0 2.0) in thread 52 - 897
[info] DEBUG: DONE	 (* 2.0 2.0) in thread 51 - 898
[info] DEBUG: DONE	 (* 3.0 2.0) in thread 52 - 899
[info] DEBUG: DONE	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 1 - 900
[info] DEBUG: STARTED	 (* (+ 1.0 1.0) 2.0) in thread 1 - 903
[info] DEBUG: STARTED	 (+ 1.0 1.0) in thread 63 - 903
[info] DEBUG: DONE	 (+ 1.0 1.0) in thread 63 - 905
[info] DEBUG: DONE	 (* (+ 1.0 1.0) 2.0) in thread 1 - 906
[info] DEBUG: STARTED	 (* 2.0 (+ 1.0 1.0)) in thread 1 - 907
[info] DEBUG: STARTED	 (+ 1.0 1.0) in thread 75 - 908
[info] DEBUG: DONE	 (+ 1.0 1.0) in thread 75 - 908
[info] DEBUG: DONE	 (* 2.0 (+ 1.0 1.0)) in thread 1 - 909
[info] DEBUG: STARTED	 (* (- 1.0 2.0) 2.0) in thread 83 - 910
[info] DEBUG: STARTED	 (- 1.0 2.0) in thread 85 - 911
[info] DEBUG: DONE	 (- 1.0 2.0) in thread 85 - 913
[info] DEBUG: DONE	 (* (- 1.0 2.0) 2.0) in thread 83 - 914
[info] DEBUG: STARTED	 (* (+ -1.0 2.0) 2.0) in thread 1 - 915
[info] DEBUG: STARTED	 (+ -1.0 2.0) in thread 95 - 917
[info] DEBUG: DONE	 (+ -1.0 2.0) in thread 95 - 919
[info] DEBUG: DONE	 (* (+ -1.0 2.0) 2.0) in thread 1 - 920
[info] DEBUG: STARTED	 (* (+ 1.0 2.0) -2.0) in thread 1 - 921
[info] DEBUG: STARTED	 (+ 1.0 2.0) in thread 105 - 922
[info] DEBUG: DONE	 (+ 1.0 2.0) in thread 105 - 926
[info] DEBUG: DONE	 (* (+ 1.0 2.0) -2.0) in thread 1 - 927
[info] DEBUG: STARTED	 (* (- 1.0 2.0) 2.0) in thread 1 - 930
[info] DEBUG: STARTED	 (- 1.0 2.0) in thread 115 - 931
[info] DEBUG: DONE	 (- 1.0 2.0) in thread 115 - 932
[info] DEBUG: DONE	 (* (- 1.0 2.0) 2.0) in thread 1 - 932
[info] DEBUG: STARTED	 (* 2.0 2.5) in thread 1 - 933
[info] DEBUG: DONE	 (* 2.0 2.5) in thread 1 - 935
[info] DEBUG: STARTED	 (* 0.5 -2.25) in thread 1 - 936
[info] DEBUG: DONE	 (* 0.5 -2.25) in thread 1 - 936
[info] DEBUG: STARTED	 (- (* -1.0 -2.0) -3.0) in thread 1 - 937
[info] DEBUG: STARTED	 (* -1.0 -2.0) in thread 132 - 943
[info] DEBUG: DONE	 (* -1.0 -2.0) in thread 132 - 946
[info] DEBUG: DONE	 (- (* -1.0 -2.0) -3.0) in thread 1 - 947
[info] DEBUG: STARTED	 (+ (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) (/ 10.0 2.0)) in thread 1 - 948
[info] DEBUG: STARTED	 (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) in thread 142 - 952
[info] DEBUG: STARTED	 (* (- 1.0 1.0) 2.0) in thread 143 - 959
[info] DEBUG: STARTED	 (* 3.0 (+ (- 1.0 3.0) 4.0)) in thread 146 - 961
[info] DEBUG: STARTED	 (/ 10.0 2.0) in thread 144 - 965
[info] DEBUG: STARTED	 (- 1.0 1.0) in thread 148 - 966
[info] DEBUG: STARTED	 (+ (- 1.0 3.0) 4.0) in thread 154 - 968
[info] DEBUG: DONE	 (/ 10.0 2.0) in thread 144 - 973
[info] DEBUG: DONE	 (- 1.0 1.0) in thread 148 - 973
[info] DEBUG: DONE	 (* (- 1.0 1.0) 2.0) in thread 143 - 974
[info] DEBUG: STARTED	 (- 1.0 3.0) in thread 160 - 972
[info] DEBUG: DONE	 (- 1.0 3.0) in thread 160 - 981
[info] DEBUG: DONE	 (+ (- 1.0 3.0) 4.0) in thread 154 - 982
[info] DEBUG: DONE	 (* 3.0 (+ (- 1.0 3.0) 4.0)) in thread 146 - 982
[info] DEBUG: DONE	 (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) in thread 142 - 984
[info] DEBUG: DONE	 (+ (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) (/ 10.0 2.0)) in thread 1 - 986
[info] passed
[success] Total time: 1 s, completed Apr 17, 2017 6:10:38 PM

$ sbt "runMain JavaAkkaCalcApp"
[info] Running JavaAkkaCalcApp
[info] DEBUG: STARTED	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 11 - 68
[info] DEBUG: STARTED	 (- (- 3.0 2.0) 1.0) in thread 15 - 70
[info] DEBUG: STARTED	 (* 2.0 (+ 1.0 1.0)) in thread 12 - 72
[info] DEBUG: STARTED	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 11 - 72
[info] DEBUG: STARTED	 (* (+ 1.0 1.0) 2.0) in thread 12 - 73
[info] DEBUG: STARTED	 (* (+ -1.0 2.0) 2.0) in thread 11 - 74
[info] DEBUG: STARTED	 (* 2.0 2.0) in thread 12 - 74
[info] DEBUG: STARTED	 (* 3.0 2.0) in thread 11 - 74
[info] DEBUG: STARTED	 (* (+ 1.0 2.0) -2.0) in thread 12 - 75
[info] DEBUG: STARTED	 (* 2.0 2.0) in thread 14 - 84
[info] DEBUG: STARTED	 (* (- 1.0 2.0) 2.0) in thread 14 - 85
[info] DEBUG: STARTED	 (* 2.0 2.5) in thread 9 - 85
[info] DEBUG: STARTED	 (* 3.0 2.0) in thread 14 - 86
[info] DEBUG: STARTED	 (* 0.5 -2.25) in thread 14 - 87
[info] DEBUG: STARTED	 (+ 2.0 2.0) in thread 14 - 89
[info] DEBUG: DONE	 (* 2.0 2.0) in thread 10 - 92
[info] DEBUG: STARTED	 (* (- 1.0 2.0) 2.0) in thread 12 - 93
[info] DEBUG: STARTED	 (- 1.0 2.0) in thread 10 - 93
[info] DEBUG: STARTED	 (+ (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) (/ 10.0 2.0)) in thread 15 - 82
[info] DEBUG: STARTED	 (* 3.0 (* 3.0 3.0)) in thread 16 - 98
[info] DEBUG: STARTED	 (+ 1.0 1.0) in thread 16 - 99
[info] DEBUG: STARTED	 (* 3.0 3.0) in thread 16 - 101
[info] DEBUG: STARTED	 (+ (+ 2.0 2.0) 2.0) in thread 17 - 104
[info] DEBUG: STARTED	 (+ 2.0 2.0) in thread 17 - 104
[info] DEBUG: STARTED	 (- 3.0 2.0) in thread 17 - 105
[info] DEBUG: STARTED	 (+ 1.0 2.0) in thread 10 - 109
[info] DEBUG: DONE	 (* 3.0 2.0) in thread 10 - 110
[info] DEBUG: STARTED	 (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) in thread 10 - 116
[info] DEBUG: STARTED	 (/ 10.0 2.0) in thread 10 - 122
[info] DEBUG: STARTED	 (* (- 1.0 1.0) 2.0) in thread 15 - 124
[info] DEBUG: STARTED	 (+ -1.0 2.0) in thread 14 - 120
[info] DEBUG: STARTED	 (* 3.0 (+ (- 1.0 3.0) 4.0)) in thread 15 - 124
[info] DEBUG: DONE	 (* 2.0 2.5) in thread 14 - 127
[info] DEBUG: STARTED	 (- (* -1.0 -2.0) -3.0) in thread 9 - 129
[info] DEBUG: STARTED	 (* -1.0 -2.0) in thread 9 - 130
[info] DEBUG: DONE	 (* 0.5 -2.25) in thread 15 - 133
[info] DEBUG: DONE	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 12 - 120
[info] DEBUG: DONE	 (/ 10.0 2.0) in thread 15 - 134
[info] DEBUG: DONE	 (* 3.0 3.0) in thread 12 - 134
[info] DEBUG: STARTED	 (+ 1.0 1.0) in thread 12 - 135
[info] DEBUG: DONE	 (+ 2.0 2.0) in thread 12 - 136
[info] DEBUG: STARTED	 (- 1.0 2.0) in thread 16 - 118
[info] DEBUG: DONE	 (* 3.0 2.0) in thread 11 - 117
[info] DEBUG: DONE	 (- 1.0 2.0) in thread 16 - 140
[info] DEBUG: DONE	 (* (- 1.0 2.0) 2.0) in thread 11 - 140
[info] DEBUG: DONE	 (- 1.0 2.0) in thread 16 - 141
[info] DEBUG: STARTED	 (- 1.0 1.0) in thread 11 - 141
[info] DEBUG: DONE	 (- 1.0 1.0) in thread 11 - 143
[info] DEBUG: DONE	 (+ 1.0 1.0) in thread 10 - 132
[info] DEBUG: DONE	 (+ 2.0 2.0) in thread 17 - 122
[info] DEBUG: DONE	 (* 3.0 (* 3.0 3.0)) in thread 15 - 147
[info] DEBUG: DONE	 (+ (+ 2.0 2.0) 2.0) in thread 15 - 148
[info] DEBUG: STARTED	 (+ (- 1.0 3.0) 4.0) in thread 15 - 148
[info] DEBUG: DONE	 (* (- 1.0 2.0) 2.0) in thread 15 - 149
[info] DEBUG: DONE	 (+ -1.0 2.0) in thread 15 - 150
[info] DEBUG: DONE	 (* 2.0 2.0) in thread 11 - 146
[info] DEBUG: DONE	 (+ (* 2.0 2.0) (* 3.0 2.0)) in thread 10 - 152
[info] DEBUG: DONE	 (* 2.0 (+ 1.0 1.0)) in thread 11 - 153
[info] DEBUG: DONE	 (* -1.0 -2.0) in thread 11 - 153
[info] DEBUG: DONE	 (- (* -1.0 -2.0) -3.0) in thread 11 - 154
[info] DEBUG: STARTED	 (- 1.0 3.0) in thread 10 - 154
[info] DEBUG: DONE	 (+ 1.0 2.0) in thread 9 - 146
[info] DEBUG: DONE	 (* (+ 1.0 2.0) -2.0) in thread 9 - 157
[info] DEBUG: DONE	 (* (- 1.0 1.0) 2.0) in thread 16 - 165
[info] DEBUG: DONE	 (+ 1.0 1.0) in thread 11 - 167
[info] DEBUG: DONE	 (* (+ -1.0 2.0) 2.0) in thread 16 - 167
[info] DEBUG: DONE	 (- 3.0 2.0) in thread 14 - 146
[info] DEBUG: DONE	 (- (- 3.0 2.0) 1.0) in thread 14 - 169
[info] DEBUG: DONE	 (* (+ 1.0 1.0) 2.0) in thread 14 - 169
[info] DEBUG: DONE	 (- 1.0 3.0) in thread 10 - 174
[info] DEBUG: DONE	 (+ (- 1.0 3.0) 4.0) in thread 10 - 179
[info] DEBUG: DONE	 (* 3.0 (+ (- 1.0 3.0) 4.0)) in thread 16 - 180
[info] DEBUG: DONE	 (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) in thread 15 - 180
[info] DEBUG: DONE	 (+ (+ (* (- 1.0 1.0) 2.0) (* 3.0 (+ (- 1.0 3.0) 4.0))) (/ 10.0 2.0)) in thread 10 - 183
[success] Total time: 5 s, completed Apr 17, 2017 6:05:38 PM
```