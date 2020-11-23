package org.chl.common.core.web;

/**
 * @Author: ChenHao
 * @Date: 2018/5/22 17:21
 */
public enum BaseResultEnum {
    PARAM_ERROR(1000,"参数错误"),

    SUCCESS(1,"成功");
    private Integer code;
    private String msg;
    BaseResultEnum(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }
    public Integer getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}
