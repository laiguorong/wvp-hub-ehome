package com.wvp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wvp.common.annotation.Excel;
import com.wvp.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 设备列表对象 wvp_device
 *
 * @author fs
 * @date 2022-02-17
 */
@Data
public class WvpDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 设备序列号 */
    @Excel(name = "设备序列号")
    private String deviceId;

    /** 设备别名 */
    @Excel(name = "设备别名")
    private String deviceName;

    /** 设备ip */
    @Excel(name = "设备ip")
    private String deviceIp;

    /** 设备注册协议版本号 */
    @Excel(name = "设备注册协议版本号")
    private int deviceVersion;

    /** 设备是否在线0不在线1在线 */
    @Excel(name = "设备是否在线0不在线1在线")
    private int deviceOnline;

    /** 设备下线时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date offlineTime;

    /** 用户id 由SDK分配 */
    @Excel(name = "用户id 由SDK分配")
    private Long luserId;

    /** EhomeKey */
    @Excel(name = "EhomeKey")
    private String ehomeKey;

    /** 设备是否正在推流0未推流1推流 */
    @Excel(name = "设备是否正在推流0未推流1推流")
    private int pushState;

    /** 预览连接句柄 */
    private Long lLinkHandle;

    /** 预览sessionId */
    private Long sessionId;

    private WvpGpsInfo wvpGpsInfo;

    /** 对讲连接句柄 */
    private Long voiceHandle;

    /** rmtp视频流地址 */
    private String videoHlsUrl;
}
