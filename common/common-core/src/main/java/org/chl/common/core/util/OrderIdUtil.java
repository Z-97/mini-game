package org.chl.common.core.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单id生成
 */
public class OrderIdUtil {
    private static String personKey = "";
    private static AtomicInteger personNum = new AtomicInteger();

    private static String platformKey = "";
    private static AtomicInteger platformNum = new AtomicInteger();

    private static String withdrawKey = "";
    private static AtomicInteger withdrawNum = new AtomicInteger();

    private static String transferAccountKey = "";
    private static AtomicInteger transferAccountNum = new AtomicInteger();

    /**
     * 人工充值
     */
    public static String personOrderId(int type) {
        String typeStr = "" + type;
        if (type < 10) {
            typeStr = "0" + typeStr;
        }
        String tk = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT);
        if (!StringUtils.equals(tk, personKey)) {
            personKey = tk;
            personNum.set(0);
        }
        int times = personNum.incrementAndGet();
        String timesStr = "" + times;
        if (times < 10) {
            timesStr = "0" + timesStr;
        }
        return tk + timesStr + typeStr;
    }

    /**
     * 渠道充值
     */
    public static String platformOrderId(int type) {
        String typeStr = "" + type;
        if (type < 10) {
            typeStr = "0" + typeStr;
        }
        String tk = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT);
        if (!StringUtils.equals(tk, platformKey)) {
            platformKey = tk;
            platformNum.set(0);
        }
        int times = platformNum.incrementAndGet();
        String timesStr = "" + times;
        if (times < 10) {
            timesStr = "0" + timesStr;
        }
        return tk + timesStr + typeStr;
    }

    /**
     * 提现订单
     */
    public static String withOrderId(int type) {
        String typeStr = "" + type;
        if (type < 10) {
            typeStr = "0" + typeStr;
        }
        String tk = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT);
        if (!StringUtils.equals(tk, withdrawKey)) {
            withdrawKey = tk;
            withdrawNum.set(0);
        }
        int times = withdrawNum.incrementAndGet();
        String timesStr = "" + times;
        if (times < 10) {
            timesStr = "0" + timesStr;
        }
        return tk + timesStr + typeStr;
    }

    /**
     * 代理转账订单
     */
    public static String transferAccountOrderId() {
        String typeStr = "01";
//        String typeStr = "" + type;
//        if (type < 10) {
//            typeStr = "0" + typeStr;
//        }
        String tk = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT);
        if (!StringUtils.equals(tk, transferAccountKey)) {
            transferAccountKey = tk;
            transferAccountNum.set(0);
        }
        int times = transferAccountNum.incrementAndGet();
        String timesStr = "" + times;
        if (times < 10) {
            timesStr = "0" + timesStr;
        }
        return tk + timesStr + typeStr;
    }

}
