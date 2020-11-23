package org.chl.gateway.definition;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @description: 路由模型
 * @author mice
 * @date 2019/12/4
*/
@Data
public class GatewayRouteDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    //路由的Id
    private String routeId;
    //路由规则转发的目标uri
    private String routeUri;
    //路由执行的顺序
    private int routeOrder = 0;
    //路由断言集合配置
    private List<GatewayPredicateDefinition> predicates = new ArrayList<>();
    //路由过滤器集合配置
    private List<GatewayFilterDefinition> filters = new ArrayList<>();
}
