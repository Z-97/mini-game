package org.chl.gateway;

import org.chl.common.core.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.chl.common.core.config.RedisRegionConfig.USER_WHITE_DEVICE_CODE;

/**
 * @Description
 * @Author mice
 * @Date 2019/12/26 16:54
 * @Version V1.0
 **/
@RestController
public class MaintainController {
    public static boolean MAINTAIN_STATUS = false;
    public static String MAINTAIN_INFO = "";
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("getMaintainInfo/{deviceCode}")
    public Map<String,Object> getMaintainInfo(@PathVariable(value = "deviceCode")String deviceCode){
        // 白名单过滤
        Map<String,Object> map = new HashMap<>();

        if (MAINTAIN_STATUS == false){
            map.put("maintainStatus",false);
        }else {
            if (redisUtil.hasKey(USER_WHITE_DEVICE_CODE+":"+deviceCode)){
                map.put("maintainStatus",false);
            }else {
                map.put("maintainStatus",true);
            }
            map.put("maintainInfo",MAINTAIN_INFO);
        }
        return map;
    }

    @GetMapping("getMaintainInfo")
    public Map<String,Object> getMaintainInfo(){
        // 白名单过滤
        Map<String,Object> map = new HashMap<>();

        if (MAINTAIN_STATUS == false){
            map.put("maintainStatus",false);
        }else {
            map.put("maintainStatus",true);
            map.put("maintainInfo",MAINTAIN_INFO);
        }
        return map;
    }

    @PostMapping("doMaintain")
    public String doMaintain(@RequestParam("maintainInfo")String maintainInfo){
        MAINTAIN_STATUS = true;
        MAINTAIN_INFO = maintainInfo;
        return "success";
    }

    @PostMapping("cancelMaintain")
    public String cancelMaintain(){
        MAINTAIN_STATUS = false;
        return "success";
    }
}