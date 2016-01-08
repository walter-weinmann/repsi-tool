if (exists("R")) remove("R")
R<-c(1067,557,506,526,551,532,531,541,531,532)
summary(R)
boxplot(R,col="lightblue",horizontal=TRUE,match=TRUE,notch=TRUE)
