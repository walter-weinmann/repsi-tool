if (exists("A_A")) remove("A_A")
if (exists("A_U")) remove("A_U")
if (exists("C_A")) remove("C_A")
if (exists("C_U")) remove("C_U")
A_U<-c(90083,85632,58503,62711,92412,59340,52912,64134,53006,58866,56543,51622,58430,60225,60579,61647,54429,52416,55759,58609,57293,61319,57873,55655,59951,58138,57744,60637,59749,52117,54628,58726,55577,68541,59748,60123,52925,51856,72760,57243,69757,70715,62681,67006,63633,61495,62416,55019,54157,53794)
A_A<-c(263486,92290,80888,56202,59671,61978,53001,67210,52018,61393,60982,56506,63817,66242,63046,61130,59655,53102,66021,55780,54681,60167,60701,55856,60441,59175,55391,60272,59963,53805,54729,62850,55874,56052,64646,59992,60278,53391,51595,59107,65196,68788,63811,72489,51798,56816,60783,57083,56531,55304)
if (exists("A_U")) boxplot.stats(A_U)
if (exists("A_A")) boxplot.stats(A_A)
if (exists("C_U")) boxplot.stats(C_U)
if (exists("C_A")) boxplot.stats(C_A)
if (exists("A_U")) summary(A_U)
if (exists("A_A")) summary(A_A)
if (exists("C_U")) summary(C_U)
if (exists("C_A")) summary(C_A)
if (exists("A_U")) boxplot(A_A,A_U,col="lightblue",horizontal=TRUE,log="x",match=TRUE,names=c("(A_A)","(A_U)"),notch=TRUE)
if (exists("C_U")) boxplot(C_A,C_U,col="lightblue",horizontal=TRUE,log="x",match=TRUE,names=c("(C_A)","(C_U)"),notch=TRUE)
