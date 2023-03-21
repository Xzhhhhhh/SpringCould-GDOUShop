package com.gdou.order.constant;

public class MqConstants {

    public static final String ORDER_QUEUE_NAME = "orderQueue";

    public static final String ORDER_EXPIRE_QUEUE_NAME = "orderExpireQueue";

    public static final String ORDER_EXPIRE_EXCHANGE_NAME = "order_expire_delayed_exchange";

    public static final String ORDER_EXCHANGE_NAME = "order_exchange";

    public static final String ORDER_QUEUE_ROUTE_KEY = "order";

    public static final String ORDER_EXPIRE_QUEUE_ROUTE_KEY = "order_expire";

    public static final Integer MESSAGE_EXPIRE_TIME = 1;

    public static final Integer MESSAGE_NO_RECEIVE_STATUE = 0;

    public static final Integer MESSAGE_RECEIVE_STATUE = 1;

    public static final Integer MESSAGE_FAIL_RECEIVE_STATUE = 2;
    public static final Long MESSAGE_ORDER_DELAY_TIME = 1000*60*30L;
    //public static final Long MESSAGE_ORDER_DELAY_TIME = 1000L;


}
