package com.wvp.service;

import java.util.List;

import com.wvp.common.core.domain.AjaxResult;
import com.wvp.domain.WvpDevice;

/**
 * 设备列表Service接口
 *
 * @author fs
 * @date 2022-02-17
 */
public interface IWvpDeviceService
{
    /**
     * 查询设备列表
     *
     * @param id 设备列表主键
     * @return 设备列表
     */
    public WvpDevice selectWvpDeviceById(Long id);

    /**
     * 查询设备列表列表
     *
     * @param wvpDevice 设备列表
     * @return 设备列表集合
     */
    public List<WvpDevice> selectWvpDeviceList(WvpDevice wvpDevice);

    /**
     * 新增设备列表
     *
     * @param wvpDevice 设备列表
     * @return 结果
     */
    public int insertWvpDevice(WvpDevice wvpDevice);

    /**
     * 修改设备列表
     *
     * @param wvpDevice 设备列表
     * @return 结果
     */
    public int updateWvpDevice(WvpDevice wvpDevice);

    /**
     * 批量删除设备列表
     *
     * @param ids 需要删除的设备列表主键集合
     * @return 结果
     */
    public int deleteWvpDeviceByIds(Long[] ids);

    /**
     * 删除设备列表信息
     *
     * @param id 设备列表主键
     * @return 结果
     */
    public int deleteWvpDeviceById(Long id);


    /**
     * 修改数据库中的所有设备状态为不在线状态
     *
     * @return 结果
     */
    public int updateAllDeviceInit();


    /**
     * 获取所有在线设备列表
     *
     * @return 结果
     */
    public List<WvpDevice> selectWvpDeviceListByOnline();



    /**
     * 根据 用户id 由SDK分配 获取所有设备信息
     *
     * @param luserId 用户id 由SDK分配
     * @return 结果
     */
    public WvpDevice selectWvpDeviceByLuserId(Long luserId);

    /**
     * 根据 设备序列号获取设备信息
     *
     * @param deviceId 设备序列号
     * @return 结果
     */
    public WvpDevice selectWvpDeviceByDeviceId(String deviceId);


    /**
     * 根据 预览句柄查询设备信息
     *
     * @param iPreviewHandle 预览句柄
     * @return 结果
     */
    public WvpDevice selectWvpDeviceByIPreviewHandle(Long iPreviewHandle);

    /**
     * 根据 预览sessionId查询设备信息
     *
     * @param sessionId 预览sessionId
     * @return 结果
     */
    public WvpDevice selectWvpDeviceBySessionId(Long sessionId);

    /**
     * 根据 对讲句柄查询设备信息
     *
     * @param voiceHandle 对讲句柄
     * @return 结果
     */
    public WvpDevice selectWvpDeviceByVoiceHandle(Long voiceHandle);

    /**
     * 开启对讲前校验
     * @param deviceId 设备序列号
     * @return
     */
    public AjaxResult voiceCheck(String deviceId);
}
