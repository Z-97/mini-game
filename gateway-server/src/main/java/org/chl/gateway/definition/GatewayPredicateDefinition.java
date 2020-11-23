package org.chl.gateway.definition;


import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @description: 路由断言模型
 * @author mice
 * @date 2019/12/4
*/
@Data
public class GatewayPredicateDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    //断言对应的Name
    private String name;
    //配置的断言规则
    private Map<String, String> args = new LinkedHashMap<>();
}
