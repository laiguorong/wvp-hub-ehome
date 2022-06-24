package com.wvp.domain;

import com.wvp.common.annotation.Excel;
import com.wvp.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 定位信息记录对象 wvp_gps_info
 *
 * @author fs
 * @date 2022-03-07
 */
@Data
public class WvpGpsInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 设备序列号 */
    @Excel(name = "设备序列号")
    private String deviceId;

    /** 经度 */
    @Excel(name = "经度")
    private String longitude;

    /** 维度 */
    @Excel(name = "维度")
    private String latitude;

}
