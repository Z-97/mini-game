package org.chl.gateway.dynamicroute;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.chl.gateway.definition.GatewayFilterDefinition;
import org.chl.gateway.definition.GatewayPredicateDefinition;
import org.chl.gateway.definition.GatewayRouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定时任务，拉取路由信息
 * 路由信息由路由项目单独维护
 */
@Component
@Slf4j
public class DynamicRouteScheduling implements CommandLineRunner {

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;
    @Autowired
    private RestTemplate restTemplate;


    //发布路由信息的版本号
    private static Long versionId = 0L;

    @Getter
    private static Set<String> routeIds = new HashSet<>();

    //每60秒中执行一次
    //如果版本号不相等则获取最新路由信息并更新网关路由
    @Scheduled(cron = "*/30 * * * * ?")
    public void getDynamicRouteInfo(){
        try{
            //Long resultVersionId = Long.valueOf(HttpUtil.get("http://47.56.245.118:8047/gatewayRoutes/lastVersion"));
            Long resultVersionId = restTemplate.getForObject("http://game-config-server/gatewayRoutes/lastVersion",Long.class);
            log.info("路由版本信息：本地版本号：" + versionId + "，远程版本号：" + resultVersionId);
            if (versionId.equals(resultVersionId)){
                return;
            }
            log.info("开始拉取路由信息......");
            //String resultRoutes = HttpUtil.get("http://47.56.245.118:8047/gatewayRoutes/list");
            String resultRoutes = restTemplate.getForObject("http://game-config-server/gatewayRoutes/list",String.class);
            log.info("路由信息为：" + resultRoutes);
            Set<String> newrouteIds = new HashSet<>();
            if(!StringUtils.isEmpty(resultRoutes)){
                List<GatewayRouteDefinition> list = JSON.parseArray(resultRoutes , GatewayRouteDefinition.class);

                for(GatewayRouteDefinition definition : list){
                    if (!routeIds.contains(definition.getRouteId())){
                        // 添加路由
                        RouteDefinition routeDefinition = assembleRouteDefinition(definition);
                        dynamicRouteService.add(routeDefinition);
                    }else {
                        // 更新路由
                        RouteDefinition routeDefinition = assembleRouteDefinition(definition);
                        dynamicRouteService.update(routeDefinition);
                    }

                    newrouteIds.add(definition.getRouteId());
                }

                routeIds.forEach(routeId ->{
                    // 删除
                    if (!newrouteIds.contains(routeId)){
                        log.info("删除路由:{}",routeId);
                        dynamicRouteService.delete(routeId);
                    }
                });
                versionId = resultVersionId;
            }

            routeIds = newrouteIds;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void init(){
        try{
            Long resultVersionId = restTemplate.getForObject("http://game-config-server/gatewayRoutes/lastVersion",Long.class);
            log.info("路由版本信息：本地版本号：" + versionId + "，远程版本号：" + resultVersionId);
            log.info("开始拉取路由信息......");
            String resultRoutes = restTemplate.getForObject("http://game-config-server/gatewayRoutes/list",String.class);
            log.info("路由信息为：" + resultRoutes);
            Set<String> newrouteIds = new HashSet<>();
            if(!StringUtils.isEmpty(resultRoutes)){
                List<GatewayRouteDefinition> list = JSON.parseArray(resultRoutes , GatewayRouteDefinition.class);
                for(GatewayRouteDefinition definition : list){
                    newrouteIds.add(definition.getRouteId());
                }

                for(GatewayRouteDefinition definition : list){
                    //添加路由
                    RouteDefinition routeDefinition = assembleRouteDefinition(definition);
                    dynamicRouteService.add(routeDefinition);
                }
                versionId = resultVersionId;
            }
            routeIds = newrouteIds;
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //把前端传递的参数转换成路由对象
    private RouteDefinition assembleRouteDefinition(GatewayRouteDefinition gwdefinition) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(gwdefinition.getRouteId());
        definition.setOrder(gwdefinition.getRouteOrder());

        //设置断言
        List<PredicateDefinition> pdList=new ArrayList<>();
        List<GatewayPredicateDefinition> gatewayPredicateDefinitionList=gwdefinition.getPredicates();
        for (GatewayPredicateDefinition gpDefinition: gatewayPredicateDefinitionList) {
            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setArgs(gpDefinition.getArgs());
            predicate.setName(gpDefinition.getName());
            pdList.add(predicate);
        }
        definition.setPredicates(pdList);

        //设置过滤器
        List<FilterDefinition> filters = new ArrayList();
        List<GatewayFilterDefinition> gatewayFilters = gwdefinition.getFilters();
        for(GatewayFilterDefinition filterDefinition : gatewayFilters){
            if (StringUtils.isEmpty(filterDefinition.getName()) || CollectionUtils.isEmpty(filterDefinition.getArgs()))continue;
            FilterDefinition filter = new FilterDefinition();
            filter.setName(filterDefinition.getName());
            filter.setArgs(filterDefinition.getArgs());
            filters.add(filter);
        }
        definition.setFilters(filters);

        URI uri = null;
        if(gwdefinition.getRouteUri().startsWith("http")){
            uri = UriComponentsBuilder.fromHttpUrl(gwdefinition.getRouteUri()).build().toUri();
        }else{
            uri = URI.create(gwdefinition.getRouteUri());
        }
        definition.setUri(uri);
        return definition;
    }

    @Override
    public void run(String... args) throws Exception {
        this.init();
    }
}
