package com.gdou.order.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * @author xzh
 * @time 2023/1/27 0:30
 * 微信时间 转 Date工具类
 */

public class WxDateUtil {

    public static Date wxDateStrToDate(String dateStr) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr);
        Instant instant = offsetDateTime.toInstant();
        return Date.from(instant.plusSeconds(60 * 60 * 8));
    }

}

