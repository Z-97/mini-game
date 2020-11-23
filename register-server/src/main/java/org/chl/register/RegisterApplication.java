/**
 * 
 */
package org.chl.register;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author linanjun
 *
 */
@EnableEurekaServer
@EnableDiscoveryClient
@SpringBootApplication(exclude =  {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
public class RegisterApplication {

    public static void main(String[] args) {
    	/**
    	 * WebApplicationType
    	 * NONE=不需要WEB容器
    	 * SERVLET=基于SERVLET的WEB项目
    	 * REACTIVE=响应式WEB应用
    	 */
        new SpringApplicationBuilder(RegisterApplication.class).web(WebApplicationType.SERVLET).run(args);
    }

}
