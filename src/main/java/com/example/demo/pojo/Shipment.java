package com.example.demo.pojo;


import lombok.Getter;
import lombok.Setter;

/**
 * @author Ron
 * @date 2021/3/22 上午 11:42
 */
@Getter
@Setter
public class Shipment {
    private String shipmentId;
    private String name;
    private String currentLocation;
    private String deliveryAddress;
    private String status;
}
