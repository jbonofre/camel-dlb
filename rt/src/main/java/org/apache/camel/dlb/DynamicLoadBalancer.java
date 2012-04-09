package org.apache.camel.dlb;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.management.DefaultManagementAgent;
import org.apache.camel.processor.loadbalancer.LoadBalancerSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DynamicLoadBalancer extends LoadBalancerSupport {

    private int threshold = 2;

    private BundleContext bundleContext;

    public boolean process(Exchange exchange, AsyncCallback callback) {
        // get the dlb route
        Route dlb = this.getRoute("dlb", null);
        if (dlb == null) {
            throw new IllegalStateException("dlb route not found");
        }

        // get the message rate
        CamelContext camelContext = dlb.getRouteContext().getCamelContext();
        MBeanServer mBeanServer = camelContext.getManagementStrategy().getManagementAgent().getMBeanServer();
        Set<ObjectName> set = null;
        try {
            set = mBeanServer.queryNames(new ObjectName(DefaultManagementAgent.DEFAULT_DOMAIN + "type=routes,name=\"dlb\",*"), null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // construct the processors set and start the processor vm route

        // stop not used processors

        callback.done(true);
        return true;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
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
