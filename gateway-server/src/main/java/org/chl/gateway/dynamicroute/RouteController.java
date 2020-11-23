package org.chl.gateway.dynamicroute;

import lombok.extern.slf4j.Slf4j;
import org.chl.gateway.definition.GatewayFilterDefinition;
import org.chl.gateway.definition.GatewayPredicateDefinition;
import org.chl.gateway.definition.GatewayRouteDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/route")
@Slf4j
public class RouteController {

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;

    //增加路由
    @PostMapping("/add")
    public String add(@RequestBody GatewayRouteDefinition gwdefinition) {
        String flag = "fail";
        try {
            RouteDefinition definition = assembleRouteDefinition(gwdefinition);
            flag = this.dynamicRouteService.add(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
    //删除路由
    @DeleteMapping("/routes/{ids}")
    public String delete(@PathVariable String ids) {
        try {
            String[] routeIds = ids.split(",");
            for (String routeId : routeIds) {
                this.dynamicRouteService.delete(routeId);
                DynamicRouteScheduling.getRouteIds().remove(routeId);
                log.info("删除路由:{}",routeId);
            }
            return "success";
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    //更新路由
    @PostMapping("/update")
    public String update(@RequestBody GatewayRouteDefinition gwdefinition) {
        RouteDefinition definition = assembleRouteDefinition(gwdefinition);
        return this.dynamicRouteService.update(definition);
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
}
