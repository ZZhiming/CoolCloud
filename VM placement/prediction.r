rm(list=ls())
library(TSA)
library(forecast)

### Exploratory analysis

res.pred<-matrix(NA,5,4)

colnames(res.pred)<-c("pred.100","pred.50","pred.10","locf")
row.names(res.pred)<-paste("mapi",1:5,sep="")

for(jj in 1:5){
  name<-paste("mapi",jj,sep="")
  mapi<-read.table(name,header=F)
  
  mapi<-mapi$V1
  n<-length(mapi)
  test<-101
  n.test<-n-100
  pred.10<-pred.50<-pred.100<-rep(NA,n.test)
  real<-mapi[test:n]
  res.pred[jj,"locf"]<-mean((mapi[(test:n)-1]-mapi[test:n])^2)

  name<-paste("mapi",jj,sep="")
  mapi<-read.table(name,header=F)
  
  mapi<-mapi$V1
  n<-length(mapi)
  test<-101
  n.test<-n-100
  pred.10<-pred.50<-pred.100<-rep(NA,n.test)
  real<-mapi[test:n]
  
  #n=100 for trainning
  for(kk in 1:n.test){
    train<-1:100+(kk-1)
    mapi.train<-mapi[train]
    #plot.ts(mapi.train, ylab="MAPI", main="MAPI Data")
    logmapi<-log(mapi.train)
    t.train=1:length(logmapi)
    m.d3=arima(logmapi,order=c(0,1,1))  
    pred.100[kk]<-predict(m.d3,1)$pred
  }
  pred.100<-exp(pred.100)
  res.pred[jj,"pred.100"]<-mean((pred.100-real)^2)
  
  #n=50 for trainning
  for(kk in 1:n.test){
    train<-51:100+(kk-1)
    mapi.train<-mapi[train]
    #plot.ts(mapi.train, ylab="MAPI", main="MAPI Data")
    logmapi<-log(mapi.train)
    t.train=1:length(logmapi)
    m.d3=arima(logmapi,order=c(0,1,1))  
    pred.50[kk]<-predict(m.d3,1)$pred
  }
  pred.50<-exp(pred.50)
  res.pred[jj,"pred.50"]<-mean((pred.50-real)^2)
  
  #n=10 for trainning
  for(kk in 1:n.test){
    train<-91:100+(kk-1)
    mapi.train<-mapi[train]
    #plot.ts(mapi.train, ylab="MAPI", main="MAPI Data")
    logmapi<-log(mapi.train)
    t.train=1:length(logmapi)
    m.d3=arima(logmapi,order=c(0,1,1))  
    pred.10[kk]<-predict(m.d3,1)$pred
  }
  pred.10<-exp(pred.10)
  res.pred[jj,"pred.10"]<-mean((pred.10-real)^2)
  
  res.pred[jj,"locf"]<-mean((mapi[(test:n)-1]-mapi[test:n])^2)
}










