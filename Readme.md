**The Resilience4j Aspects order is following:
Retry ( CircuitBreaker ( RateLimiter ( TimeLimiter ( Bulkhead ( Function ) ) ) ) )**

If you want to change this order, use decorator way of adding resiliency

#### _Salient Points from Examples:_
###### A) Allow Property based config (See appliction.yaml) .

###### B) Time Limiter did not work for some reason. Need to investigate further.

###### C) If a "NAME" of a Resilience annotation is not in properties file, it takes defaults

###### D) you can always have a "Predicate" class that can be mentioned in properties file for attributes like retryOnPredicate etc

###### E) The sequence of aspect above works like , On an exception, CktBreaker will be applied first, and then Retry. So if Ckt is open, there is no retry and all further failures are recorded in CktBreaker Metrics

###### F) Tried to inject a Bean and use it in Aspect, but that did not work . Aspect was unable to look up that Bean (See BeanFarm Package)

Also JMeter File is attached: 