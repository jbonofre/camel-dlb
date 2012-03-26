package org.apache.camel.dlb;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.processor.loadbalancer.LoadBalancerSupport;

public class DynamicLoadBalancer extends LoadBalancerSupport {
    
    private int threshold = 100;

    public boolean process(Exchange exchange, AsyncCallback callback) {
        // get the message rate

        // depending of the message rate and threshold, choose the target node pool

        callback.done(true);
        return true;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

}
