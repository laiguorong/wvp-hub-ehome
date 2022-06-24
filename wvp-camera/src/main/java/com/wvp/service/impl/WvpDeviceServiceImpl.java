package com.wvp.service.impl;

import java.util.List;

import com.wvp.common.core.domain.AjaxResult;
import com.wvp.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wvp.mapper.WvpDeviceMapper;
import com.wvp.domain.WvpDevice;
import com.wvp.service.IWvpDeviceService;

import javax.annotation.Resource;

/**
 * 设备列表Service业务层处理
 *
 * @author fs
 * @date 2022-02-17
 */
@Service
public class WvpDeviceServiceImpl implements IWvpDeviceService
{
    @Autowired
    private WvpDeviceMapper wvpDeviceMapper;

    /**
     * 查询设备列表
     *
     * @param id 设备列表主键
     * @return 设备列表
     */
    @Override
    public WvpDevice selectWvpDeviceById(Long id)
    {
        return wvpDeviceMapper.selectWvpDeviceById(id);
    }

    /**
     * 查询设备列表列表
     *
     * @param wvpDevice 设备列表
     * @return 设备列表
     */
    @Override
    public List<WvpDevice> selectWvpDeviceList(WvpDevice wvpDevice)
    {
        return wvpDeviceMapper.selectWvpDeviceList(wvpDevice);
    }

    /**
     * 新增设备列表
     *
     * @param wvpDevice 设备列表
     * @return 结果
     */
    @Override
    public int insertWvpDevice(WvpDevice wvpDevice)
    {
        wvpDevice.setCreateTime(DateUtils.getNowDate());
        return wvpDeviceMapper.insertWvpDevice(wvpDevice);
    }

    /**
     * 修改设备列表
     *
     * @param wvpDevice 设备列表
     * @return 结果
     */
    @Override
    public int updateWvpDevice(WvpDevice wvpDevice)
    {
        wvpDevice.setUpdateTime(DateUtils.getNowDate());
        return wvpDeviceMapper.updateWvpDevice(wvpDevice);
    }

    /**
     * 批量删除设备列表
     *
     * @param ids 需要删除的设备列表主键
     * @return 结果
     */
    @Override
    public int deleteWvpDeviceByIds(Long[] ids)
    {
        return wvpDeviceMapper.deleteWvpDeviceByIds(ids);
    }

    /**
     * 删除设备列表信息
     *
     * @param id 设备列表主键
     * @return 结果
     */
    @Override
    public int deleteWvpDeviceById(Long id)
    {
        return wvpDeviceMapper.deleteWvpDeviceById(id);
    }


    /**
     * 修改数据库中的所有设备状态为不在线状态
     *
     * @return 结果
     */
    @Override
    public int updateAllDeviceInit() {
        return wvpDeviceMapper.updateAllDeviceInit();
    }

    /**
     * 获取所有在线设备列表
     *
     * @return 结果
     */
    @Override
    public List<WvpDevice> selectWvpDeviceListByOnline() {
        return wvpDeviceMapper.selectWvpDeviceListByOnline();
    }


    /**
     * 根据 用户id 由SDK分配 获取所有设备信息
     *
     * @param luserId 用户id 由SDK分配
     * @return 结果
     */
    @Override
    public WvpDevice selectWvpDeviceByLuserId(Long luserId) {
        return wvpDeviceMapper.selectWvpDeviceByLuserId(luserId);
    }

    /**
     * 根据 设备序列号获取设备信息
     *
     * @param deviceId 设备序列号
     * @return 结果
     */
    @Override
    public WvpDevice selectWvpDeviceByDeviceId(String deviceId) {
        return wvpDeviceMapper.selectWvpDeviceByDeviceId(deviceId);
    }

    /**
     * 根据 预览句柄查询设备信息
     *
     * @param iPreviewHandle 预览句柄
     * @return 结果
     */
    @Override
    public WvpDevice selectWvpDeviceByIPreviewHandle(Long iPreviewHandle) {
        return wvpDeviceMapper.selectWvpDeviceByIPreviewHandle(iPreviewHandle);
    }

    /**
     * 根据 预览sessionId查询设备信息
     *
     * @param sessionId 预览sessionId
     * @return 结果
     */
    @Override
    public WvpDevice selectWvpDeviceBySessionId(Long sessionId) {
        return wvpDeviceMapper.selectWvpDeviceBySessionId(sessionId);
    }

    @Override
    public WvpDevice selectWvpDeviceByVoiceHandle(Long voiceHandle) {
        return wvpDeviceMapper.selectWvpDeviceByVoiceHandle(voiceHandle);
    }

    @Override
    public AjaxResult voiceCheck(String deviceId) {
        WvpDevice wvpDevice=wvpDeviceMapper.selectWvpDeviceByDeviceId(deviceId);
        if(wvpDevice==null){
            return new AjaxResult(501,"未找到当前设备请确保设备连接的服务没有问题");
        }else{
            if(wvpDevice.getDeviceOnline()==0){
                return new AjaxResult(501,"设备不在线，开启对讲失败");
            }else{
                if (wvpDevice.getVoiceHandle().equals(-1)||wvpDevice.getVoiceHandle()==-1) {
                    return new AjaxResult(200,"可以开启对讲");
                } else {
                    return new AjaxResult(501,"该设备已被使用，无法开启对讲");
                }
            }
        }
    }
}
