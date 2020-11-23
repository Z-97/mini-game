package org.chl.gateway.loadbalance;

import com.alibaba.fastjson.JSONObject;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;

import java.util.List;

/**
 * @description: 根据userId对服务进行负载均衡。同一个用户id的请求，都转发到同一个服务实例上面。
 * @author mice
 * @date 2019/10/22
*/
@Slf4j
public class GameServerBalanceRule extends AbstractLoadBalancerRule {

    @Override
    public Server choose(Object key) {//这里的key就是过滤器中传过来的userId

        List<Server> servers = this.getLoadBalancer().getReachableServers();
        log.info("===================>servers{}", JSONObject.toJSONString(servers));
        if (servers.isEmpty()) {
            return null;
        }
        if (servers.size() == 1) {
            return servers.get(0);
        }
        if (key == null) {
            return randomChoose(servers);
        }
        return hashKeyChoose(servers, key);
    }

    /**
     * 
     * <p>Description:随机返回一个服务实例 </p>
     * @param servers
     * @return
     *
     */
    private Server randomChoose(List<Server> servers) {
        int randomIndex = RandomUtils.nextInt(servers.size());
        return servers.get(randomIndex);
    }
    /**
     * 
     * <p>Description:使用key的hash值，和服务实例数量求余，选择一个服务实例 </p>
     * @param servers
     * @param key
     *
     */
    private Server hashKeyChoose(List<Server> servers, Object key) {
        int hashCode = Math.abs(key.hashCode());
        if (hashCode < servers.size()) {
            return servers.get(hashCode);
        }
        int index = hashCode % servers.size();
        return servers.get(index);

    }

    @Override
    public void initWithNiwsConfig(IClientConfig config) {

    }

}