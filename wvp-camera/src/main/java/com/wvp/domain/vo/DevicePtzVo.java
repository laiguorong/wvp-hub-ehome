package com.wvp.domain.vo;

import lombok.Data;

/**
 * @ClassName DevicePtzVo
 * @Description TODO
 * @Author fs
 * @Date 2022/3/2 11:01
 */
@Data
public class DevicePtzVo {
    //云台控制方向
    private byte ptzCmd;
    //PTZ 速度，取值范围从 0 到 70。值越大，代表速度越快
    private byte ptzSpeed;
    //PTZ 控制：0-开始，1-停止。
    private byte byAction;
    //注册句柄
    private int luserId;
}
