if (exists("A_A")) remove("A_A")
if (exists("A_U")) remove("A_U")
if (exists("C_A")) remove("C_A")
if (exists("C_U")) remove("C_U")
A_U<-c(78160,79209,77609,78946,79232,78997,78845,81546,82000,76618,79868,78318,80433,83194,78940,111301,79687,76925,79661,85436,85410,82510,78645,76665,131497,81368,78152,78316,77618,79944,78229,78077,96027,78145,79440,77327,77744,92291,79885,97592,81934,78460,76485,78073,105697,81461,78716,77734,78718,81475)
A_A<-c(2041497,1118799,1175782,1114999,1125601,1109963,1152239,1120919,1143445,1153327,1113433,1115499,1117435,1118420,1116070,1240817,1264811,1216705,1112716,1134531,1125238,1117847,1126544,1103208,1459145,1908308,1143741,1112795,1144586,1139830,1164401,1114366,1137145,1238118,1146141,1127049,1126736,1176386,1269820,1371195,1351002,1105707,1145167,1105186,1131993,1151874,1110150,1116647,1126832,1103940)

for (i in 1:length(A_A)) {
   A_A[i]=A_A[i]/33.183;
   A_U[i]=A_U[i]/1.774;
}

if (exists("A_U")) boxplot.stats(A_U)
if (exists("A_A")) boxplot.stats(A_A)
if (exists("C_U")) boxplot.stats(C_U)
if (exists("C_A")) boxplot.stats(C_A)
if (exists("C_U")) summary(C_U)
if (exists("C_A")) summary(C_A)
if (exists("A_U")) summary(A_U)
if (exists("A_A")) summary(A_A)

if (exists("A_U")) boxplot(A_A,A_U,col="lightblue",horizontal=TRUE,match=TRUE,names=c("(A_A)","(A_U)"),notch=TRUE)
if (exists("C_U")) boxplot(C_A,C_U,col="lightblue",horizontal=TRUE,match=TRUE,names=c("(C_A)","(C_U)"),notch=TRUE)
