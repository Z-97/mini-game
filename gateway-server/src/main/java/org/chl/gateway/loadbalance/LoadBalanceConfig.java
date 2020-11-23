package org.chl.gateway.loadbalance;

import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalanceConfig {

    @Bean
    public IRule commonRule() {
        return new GameServerBalanceRule();
    }
}