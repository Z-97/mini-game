package org.chl.gateway.definition;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @description: 过滤器模型
 * @author mice
 * @date 2019/12/4
*/
@Data
public class GatewayFilterDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    //Filter Name
    private String name;
    //对应的路由规则
    private Map<String, String> args = new LinkedHashMap<>();
}
