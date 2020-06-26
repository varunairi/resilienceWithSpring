package com.varun.resilience.spring.beanFarm;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class Resilience {

    //DOES NOT WORK. Idea was that since properties did not allow RetryONResult , I can inject another Retry bean
    // with unique name and then later use it , but this bean is not being recognized.
    @Bean(name="retryB")
    public Retry getRetryWithResultPredicate(){
        RetryRegistry reg = RetryRegistry.of(RetryConfig.custom().maxAttempts(5)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(5)))

                .retryOnResult(o -> ((String)o).equalsIgnoreCase("Partial"))
                .build());
        return reg.retry("retryB");
    }
}
