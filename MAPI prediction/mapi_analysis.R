
library(TSA)
library(forecast)

### Exploratory analysis

mapi<-read.table("mapi_data",header=F)
plot.ts(mapi$V1, ylab="MAPI", main="MAPI Data")
t.train=1:length(logmapi)

mapi.lo <- loess(mapi$V1 ~ t.train, span = 0.08) # loess smoothing---mean trend
lines(t.train, mapi.lo$fitted, col=2, lwd=2)

mapidat <- mapi$V1
tsdisplay(mapidat, main="MAPI Data",lag=100) # not stationary --- heteroscedasiticity

mapifft <- periodogram(log(mapidat)) # frequencies = 0.1, 0.2, 0.3 (high)
# plot(mapifft$freq, mapifft$spec) # periods = 10, 5, 3? hours

quantity=ts(mapidat,start=0,frequency=10) # tranform time unit (hours) into 10 hours 
plot.ts(quantity,main='MAPI Data')  # trend exists
abline(h=mean(quantity),lty=3, col=2) 

plot.ts(log(quantity),main='log(MAPI) Data')  # trend exists
abline(h=mean(log(quantity)),lty=3, col=2)

logquantity <- log(quantity) # log tranformation to get stationary time series
logmapi <- log(mapidat)

mapi.stl=stl(logquantity,s.window=9);plot(mapi.stl) # stl decomposition(sort of periodicity)

tsdisplay(logmapi,main="Y_t",lag=60) # trend
tsdisplay(diff(logmapi,1),main="(1-B)Y_t",lag=60) # no trend

-------------------------------------------------------------------------------------------
  ### Model fitting
  
  ## regular differencing (remove trend method 1, not considering periodicity)
  # we will try several models ARMA(p,1,q) # 1 in middle means difference once, then rest part--ARMA(p,q)
  detach("package:TSA")
m.d1=arima(logmapi,order=c(1,1,0)) # at least one significant p-values---not acceptable
summary(m.d1)
tsdiag(m.d1,gof=10) 

m.d2=arima(logmapi,order=c(0,1,1))  #not acceptable
summary(m.d2)
tsdiag(m.d2,gof=10)

m.d3=arima(logmapi,order=c(1,1,1))  #not acceptable
summary(m.d3)
tsdiag(m.d3,gof=10)

m.d4=arima(logmapi,order=c(2,1,0))   #not acceptable
summary(m.d4)
tsdiag(m.d4,gof=10)

m.d5=arima(logmapi,order=c(0,1,2))   #not acceptable
summary(m.d5)
tsdiag(m.d5,gof=10)

m.d6=arima(logmapi,order=c(2,1,1))   #not acceptable
summary(m.d6)
tsdiag(m.d6,gof=10)

m.d7=arima(logmapi,order=c(1,1,2))   #not acceptable. 
summary(m.d7)
tsdiag(m.d7,gof=10)

#Tried several other p and q values---still not acceptable


#-----------------
## find a model without differencing (remove trend method 2---regression, not considering periodicity)

m.t0=arima(logmapi,order=c(0,0,0),xreg=cbind(t.train,t.train^2,t.train^3))
summary(m.t0)
tsdiag(m.t0)

m.t1=arima(logmapi,order=c(1,0,0),xreg=cbind(t.train,t.train^2,t.train^3)) #not acceptable
summary(m.t1)
tsdiag(m.t1)

m.t2=arima(logmapi,order=c(1,0,1),xreg=cbind(t.train,t.train^2,t.train^3))  #not acceptable
summary(m.t2)
tsdiag(m.t2)

m.t3=arima(logmapi,order=c(2,0,0),xreg=cbind(t.train,t.train^2,t.train^3)) #not acceptable
summary(m.t3)
tsdiag(m.t3)

m.t4=arima(logmapi,order=c(2,0,1),xreg=cbind(t.train,t.train^2,t.train^3)) #not acceptable
summary(m.t4)
tsdiag(m.t4)

m.t5=arima(logmapi,order=c(3,0,0),xreg=cbind(t.train,t.train^2,t.train^3)) #not acceptable
summary(m.t5)
tsdiag(m.t5)

m.t6=arima(logmapi,order=c(4,0,0),xreg=cbind(t.train,t.train^2,t.train^3)) #not acceptable
summary(m.t6)
tsdiag(m.t6)

# from above results, we can see seasonality or periodicity of 10 in the residuals


#-------------------
## regular differencing + seasonal differencing (remove trend, consider periodicity)
tsdisplay(diff(diff(logmapi,10),1),main="(1-B)(1-B^10)Y_t",lag=60) # no trend

mm.dd0=arima(logmapi,order=c(1,1,0),seasonal=list(order=c(0,1,1),period=10))
summary(mm.dd0) 
tsdiag(mm.dd0,gof=10) #not acceptable 

mm.dd1=arima(logmapi,order=c(0,1,1),seasonal=list(order=c(0,1,1),period=10))
summary(mm.dd1)
tsdiag(mm.dd1,gof=10) #not acceptable 

mm.dd2=arima(logmapi,order=c(2,1,0),seasonal=list(order=c(0,1,1),period=10))
summary(mm.dd2)
tsdiag(mm.dd2,gof=10) #not acceptable

mm.dd3=arima(logmapi,order=c(0,1,2),seasonal=list(order=c(0,1,1),period=10))
summary(mm.dd3)
tsdiag(mm.dd3,gof=10) #not acceptable

mm.dd4=arima(logmapi,order=c(1,1,1),seasonal=list(order=c(0,1,1),period=10))
summary(mm.dd4)
tsdiag(mm.dd4,gof=10) #not acceptable

mm.dd5=arima(logmapi,order=c(2,1,1),seasonal=list(order=c(0,1,1),period=10))
summary(mm.dd5)
tsdiag(mm.dd5,gof=10) #not acceptable

mm.dd6=arima(logmapi,order=c(1,1,2),seasonal=list(order=c(0,1,1),period=10))
summary(mm.dd6)
tsdiag(mm.dd6,gof=10) #not acceptable

mm.dd7=arima(logmapi,order=c(1,1,0),seasonal=list(order=c(0,1,2),period=10))
summary(mm.dd7)
tsdiag(mm.dd7,gof=10) # not acceptable 

# try different p and q values and finally the following works:
mm.dd8=arima(logmapi,order=c(3,1,1),seasonal=list(order=c(0,1,2),period=10))
summary(mm.dd8)
tsdiag(mm.dd8,gof=10) # acceptable 

###########################################################
#   the model we found is ARIMA(3,1,1)*(ARIMA(0,1,2))_12  #
###########################################################

plot.ts(logmapi)
lines(logmapi-mm.dd8$residuals, lty=2, col=3)


#------------------------------
### prediction

# prediction of models
library(TSA)
t.pred=length(logmapi)+1:12

# log tranformed data, fitted one and prediction
plot.ts(logmapi)
lines(logmapi-mm.dd8$residuals, lty=2, col=3)
lines(t.pred,predict(mm.dd8,12)$pred, lty=3, col=2)

#ts.plot(predict(mm.dd8,12)$pred,lwd=2,type="b")

# original data, fitted one and prediction
plot.ts(mapi)
lines(exp(logmapi-mm.dd8$residuals), lty=2, col=3)
lines(t.pred,exp(predict(mm.dd8,12)$pred), lty=3, col=2)





