if (exists("R")) remove("R")
R<-c(2000,0,1000,0,1000,0,0,1000,0,0)
summary(R)
boxplot(R,col="lightblue",horizontal=TRUE,match=TRUE,notch=TRUE)
