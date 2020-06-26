package com.varun.resilience.spring.controller;

import com.varun.resilience.spring.service.ServiceA;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class EndPointController {

    @Resource
    private ServiceA serviceA;
    @GetMapping(path="/rest/serviceTooSlow")
    public ResponseEntity getDetailsOfService(int id, int delayInSecs){
        String message = this.serviceA.serviceWithDelayRetryable(delayInSecs);
        if (message.startsWith("Failure"))
            return new ResponseEntity(new Status(message, "Failure"), HttpStatus.EXPECTATION_FAILED);
        return new ResponseEntity(new Status(message, "Success"), HttpStatus.OK);
    }

    /**
     * Example: CircuitBreaker + Retry
     * @param id
     * @return
     */
    @GetMapping(path="/rest/serviceExceptionRetry")
    public ResponseEntity getserviceExceptionRetry(@RequestParam (required =  false) String id){
        String message = this.serviceA.retryWithCktBreaker(id);
        if (message.startsWith("Failure"))
            return new ResponseEntity(new Status(message, "Failure"), HttpStatus.EXPECTATION_FAILED);
        return new ResponseEntity(new Status(message, "Success"), HttpStatus.OK);
    }

    /**
     * Example: Retry Override.. OVERRIDE DOES NOT WORK.
     *
     * @param id
     * @return
     */
    @GetMapping(path="/rest/serviceRetryOverride")
    public ResponseEntity serviceRetryOverride(@RequestParam (required =  false) String id, @RequestParam(required = false) boolean alwaysFailure){
        String message = this.serviceA.retryOnResultToo(id, alwaysFailure);
        if (message.startsWith("Failure"))
            return new ResponseEntity(new Status(message, "Failure"), HttpStatus.EXPECTATION_FAILED);
        return new ResponseEntity(new Status(message, "Success"), HttpStatus.OK);
    }


    /**
     * Example: Bulkhead with semaphore - 10 concurrent and 6 second wait on others
     *
     * @param id
     * @return
     */
    @GetMapping(path="/rest/serviceBulkHead")
    public ResponseEntity serviceBulkHead(@RequestParam (required =  false) String id){
        String message = this.serviceA.mngResourcesWithBulHead(id);
        if (message.startsWith("Failure"))
            return new ResponseEntity(new Status(message, "Failure"), HttpStatus.EXPECTATION_FAILED);
        return new ResponseEntity(new Status(message, "Success"), HttpStatus.OK);
    }


    /**
     * Example: Bulkhead with semaphore - 10 max threads, 5 core,  and 6 second wait on others
     *
     * @param id
     * @return
     */
    @GetMapping(path="/rest/serviceBulkHeadTP")
    public ResponseEntity serviceBulkHeadTP(@RequestParam (required =  false) String id) throws ExecutionException, InterruptedException {
        String message = "Success";
        this.serviceA.mngResourcesWithBulHeadTP(id).get();
        if (message.startsWith("Failure"))
            return new ResponseEntity(new Status(message, "Failure"), HttpStatus.EXPECTATION_FAILED);
        return new ResponseEntity(new Status(message, "Success"), HttpStatus.OK);
    }

    /**
     * Example: Bulkhead with semaphore - 10 concurrent and 6 second wait on others
     * Also Have A RETRY
     *
     * @param id
     * @return
     */
    @GetMapping(path="/rest/serviceBulkHeadRetry")
    public ResponseEntity serviceBulkHeadRetry(@RequestParam (required =  false) String id){
        String message = this.serviceA.mngResourcesWithBulHeadRetry(id);
        if (message.startsWith("Failure"))
            return new ResponseEntity(new Status(message, "Failure"), HttpStatus.EXPECTATION_FAILED);
        return new ResponseEntity(new Status(message, "Success"), HttpStatus.OK);
    }

    @GetMapping(path="/rest/serviceRetry")
    public ResponseEntity getserviceExceptionRetryq(@RequestParam (required =  false) String id,
                                                    @RequestParam int delay) throws ExecutionException, InterruptedException {
        String message  = this.serviceA.letsGetData(id,delay).toCompletableFuture().get();

        if (message.startsWith("Failure"))
            return new ResponseEntity(new Status(message, "Failure"), HttpStatus.EXPECTATION_FAILED);
        return new ResponseEntity(new Status(message, "Success"), HttpStatus.OK);
    }


    private static class Status implements Serializable {
        private String message;
        private String status;

        public Status(String message, String status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
