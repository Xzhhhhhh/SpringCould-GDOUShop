package com.gdou.order.entity;


import com.gdou.feign.entity.dto.GoodsCartDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqOrder {

    private long orderId;

    private String userId;

    private String wxPrepayOrderId;

    private Map<String,Object> payOrderMap;

    private BigDecimal amount;

    private String address;

    private Integer orderType;

    private List<Cart> cartGoodsList;

    private Date createTime;

    private List<GoodsCartDto> goodsCartInfoList;

}
