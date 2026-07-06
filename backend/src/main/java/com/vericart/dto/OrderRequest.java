package com.vericart.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String note;
    private List<OrderItemRequest> items;
}
