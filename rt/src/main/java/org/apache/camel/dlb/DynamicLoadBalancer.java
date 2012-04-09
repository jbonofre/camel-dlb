package org.apache.camel.dlb;

import org.apache.camel.*;
import org.apache.camel.management.DefaultManagementAgent;
import org.apache.camel.processor.loadbalancer.QueueLoadBalancer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.*;

public class DynamicLoadBalancer extends QueueLoadBalancer {

    private long threshold = 2;

    private BundleContext bundleContext;

    protected synchronized Processor chooseProcessor(List<Processor> processors, Exchange exchange) {
        Processor choosed = null;
        
        // get the dlb route
        Route dlb = this.getRoute("dlb", null);
        if (dlb == null) {
            throw new IllegalStateException("dlb route not found");
        }

        // looking for node(.*) routes
        List<Route> nodeRoutes = this.getNodeRoutes();

        // get the message rate
        CamelContext camelContext = dlb.getRouteContext().getCamelContext();
        MBeanServer mBeanServer = camelContext.getManagementStrategy().getManagementAgent().getMBeanServer();
        long processorsCount = 0;
        Set<ObjectName> set = null;
        try {
            set = mBeanServer.queryNames(new ObjectName(DefaultManagementAgent.DEFAULT_DOMAIN + "type=routes,name=\"dlb\",*"), null);
            Iterator<ObjectName> iterator = set.iterator();
            if (iterator.hasNext()) {
                ObjectName routeMBean = iterator.next();
                Long exchangesCompleted = (Long) mBeanServer.getAttribute(routeMBean, "ExchangesCompleted");
                Date firstExchangeCompletedDate = (Date) mBeanServer.getAttribute(routeMBean, "FirstExchangeCompletedTimestamp");
                long routePeriod = (System.currentTimeMillis() - firstExchangeCompletedDate.getTime())/60;
                long currentRate = exchangesCompleted/routePeriod;
                processorsCount = currentRate/threshold;
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // start the processor vm route
        for (int i = 0; i < processorsCount; i++) {
            // lookup on node{i} route
            Route route = this.getRoute("node" + i, null);
            // starting node{i} route if required
            CamelContext routeCamelContext = route.getRouteContext().getCamelContext();
            try {
                routeCamelContext.startRoute("node" + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // stop the processor vm route outside the count
        for (long i = processorsCount; i < nodeRoutes.size(); i++) {
            // lookup on node{i} route
            Route route = this.getRoute("node" + i, null);
            // starting node{i} route if required
            CamelContext routeCamelContext = route.getRouteContext().getCamelContext();
            try {
                routeCamelContext.stopRoute("node" + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // TODO add the vm:node{i} in the processors set and use LoadBalancerSupport
        // random pick up of a processor in the rate
        while (true) {
            int index = (int) Math.round(Math.random() * processorsCount);
            if (index < processorsCount) {
                return processors.get(index);
            }
        }
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private List<CamelContext> getCamelContexts() {
        List<CamelContext> camelContexts = new ArrayList<CamelContext>();
        try {
            ServiceReference[] references = bundleContext.getServiceReferences(CamelContext.class.getName(), null);
            if (references != null) {
                for (ServiceReference reference : references) {
                    if (reference != null) {
                        CamelContext camelContext = (CamelContext) bundleContext.getService(reference);
                        if (camelContext != null) {
                            camelContexts.add(camelContext);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camelContexts;
    }

    private CamelContext getCamelContext(String name) {
        for (CamelContext camelContext : this.getCamelContexts()) {
            if (camelContext.getName().equals(name)) {
                return camelContext;
            }
        }
        return null;
    }
    
    private List<Route> getNodeRoutes() {
        List<Route> routes = new ArrayList<Route>();
        List<CamelContext> camelContexts = this.getCamelContexts();
        for (CamelContext camelContext : camelContexts) {
            for (Route route : camelContext.getRoutes()) {
                if (route.getId().matches("[N-n]ode\\d+")) {
                    routes.add(route);
                }
            }
        }
        return routes;
    }

    private List<Route> getRoutes(String camelContextName) {
        if (camelContextName != null) {
            CamelContext context = this.getCamelContext(camelContextName);
            if (context != null) {
                return context.getRoutes();
            }
        } else {
            List<Route> routes = new ArrayList<Route>();
            List<CamelContext> camelContexts = this.getCamelContexts();
            for (CamelContext camelContext : camelContexts) {
                for (Route route : camelContext.getRoutes()) {
                    routes.add(route);
                }
            }
            return routes;
        }
        return null;
    }
    
    private Route getRoute(String routeId, String camelContextName) {
        List<Route> routes = this.getRoutes(camelContextName);
        for (Route route : routes) {
            if (route.getId().equals(routeId)) {
                return route;
            }
        }
        return null;
    }

}
