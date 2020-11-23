package org.chl.common.core.web;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: ChenHao
 * @Date: 2018/11/27  17:40
 */
@Slf4j
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 2332608236621015980L;

    private String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}