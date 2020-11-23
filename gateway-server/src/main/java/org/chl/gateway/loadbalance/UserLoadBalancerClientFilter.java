package org.chl.gateway.loadbalance;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

public class UserLoadBalancerClientFilter extends LoadBalancerClientFilter {

    public UserLoadBalancerClientFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties) {
        super(loadBalancer, properties);
    }

    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        //这里可以拿到web请求的上下文，可以从header中取出来自己定义的数据。
        URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
        String userId = null;
        String scheme = requestUrl.getScheme();

        // 处理websocket
        if (scheme.equals("ws")||scheme.equals("wss")){
            userId = requestUrl.getPath().split("/")[2];
            return super.choose(exchange);
        }else {
            //处理http
            userId = exchange.getRequest().getHeaders().getFirst("userId");
            if (userId == null) {
                return super.choose(exchange);
            }
        }
        if (this.loadBalancer instanceof RibbonLoadBalancerClient) {
            RibbonLoadBalancerClient client = (RibbonLoadBalancerClient) this.loadBalancer;
            String serviceId = ((URI) exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR)).getHost();
            //这里使用userId做为选择服务实例的key
            return client.choose(serviceId, userId);
        }
        return super.choose(exchange);
    }
}