package com.varun.resilience.spring.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ServiceA {

    ExecutorService es = Executors.newFixedThreadPool(15);

    @CircuitBreaker(name = "circuitBreakerA")
    //@CircuitBreaker(name = "circuitBreakerA", fallbackMethod = "fallbackForRetry")
    //@TimeLimiter(name = "timeLimiterA")
    @Retry(name="retry1")
    public String serviceWithDelayRetryable(int delayInSecs){
        try {
            Thread.sleep(delayInSecs*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Success";
    }
    @CircuitBreaker(name="circuitBreakerA")
    @Retry( name="retry1",fallbackMethod = "fallbackForRetry")
    public String serviceWithExceptionAndRetryable(String id){
        System.out.println("ServiceA " + id);
            double random = Math.random();
            if (random> 0.1d)
                throw new RuntimeException();
        return "Success";
    }

    private String fallbackForRetry(String id, Throwable exe)
    {
        System.out.println("Fallback: " + id + exe.getLocalizedMessage() );
    return "Failure on Retry " + id + exe.getLocalizedMessage();
    }


    @TimeLimiter(name="backendA")
    @Retry(name="retry3")
    public CompletionStage<String> letsGetData(String id, int delay){
        return  CompletableFuture.supplyAsync(()->{
             int ddelay = (int)(Math.random()*10d);
            System.out.println(new Date() + " delaying this thread " + id + " by " + ddelay);
            try {
                Thread.sleep(ddelay*1000); //hardcoded delay, change to use "delay" variable to see variable results/.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Success:" + ddelay + " . Serviced by " + Thread.currentThread().getName();
        }, es); //Important to give you own Thread pool

    }

    /**
     * Good example.
     * Retry counts from itself until max
     * @param id
     * @return
     */
    @Retry(name="retry1")
    @CircuitBreaker(name="circuitBreakerA")
    public String retryWithCktBreaker(String id){
        double random = Math.random();
        if(random>0.30d) {//70% chance of failure
            System.out.println("ID:" +id + " returning failure");
            throw new RuntimeException();
        }
        System.out.println("Returning success for ID: " + id);
        return "Success";
    }

    /*
    Example: Semaphore with 10 concurrent and a max wait
     */
    @Bulkhead(name = "bulkheadSemaphore", type = Bulkhead.Type.SEMAPHORE)
    public String mngResourcesWithBulHead(String id){
        int delay = (int)(Math.random()*10d);
        System.out.println(new Date() + " delaying this thread " + id + " by " + delay);
        try {
            Thread.sleep(8*1000); //hardcoded delay, change to use "delay" variable to see variable results/.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Success";
    }


    /*
    Example: Semaphore with 10 concurrent and a max wait
    Also a RETRY.
     */
    @Bulkhead(name = "bulkheadSemaphore", type = Bulkhead.Type.SEMAPHORE)
    @Retry(name="retry2")
    public String mngResourcesWithBulHeadRetry(String id){
        int prob = (int)(Math.random()*10d);
        System.out.println(new Date() + " this thread " + id + " has prob value " + prob);
        try {
            Thread.sleep(prob*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(prob> 5) {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            throw new RuntimeException(id);
        }
        return "Success";
    }
    /*
 Example: Semaphore with 10 concurrent and a max wait
    Requires CompletableFuture.
  */
    @Bulkhead(name = "bulkheadTP", type = Bulkhead.Type.THREADPOOL)
    public CompletableFuture<Void> mngResourcesWithBulHeadTP(String id){
        return CompletableFuture.runAsync(()->{
            int delay = (int)(Math.random()*10d);
            System.out.println(new Date() + " delaying this thread " + id + " by " + delay);
            try {
                Thread.sleep(delay*1000); //hardcoded delay, change to use "delay" variable to see variable results/.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }
}
