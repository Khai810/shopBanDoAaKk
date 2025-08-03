package com.projectshopbando.shopbandoapi.enums;

import com.projectshopbando.shopbandoapi.dtos.response.IpnRes;

public class IpnResConst {
    public static final IpnRes SUCCESS = new IpnRes("00", "Successful");
    public static final IpnRes INVALID_AMOUNT = new IpnRes("04", "Invalid Amount");
    public static final IpnRes ORDER_NOTFOUND = new IpnRes("01", "Order Not Found");
    public static final IpnRes ORDER_COMFIRMED = new IpnRes("02", "Order already confirmed");
    public static final IpnRes UNKNOWN_ERROR = new IpnRes("99", "Unknown error");
    public static final IpnRes INVALID_CHECKSUM = new IpnRes("97", "Invalid Checksum");
}
