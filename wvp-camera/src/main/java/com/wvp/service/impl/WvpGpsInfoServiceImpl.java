package com.wvp.service.impl;

import java.util.List;
import com.wvp.common.utils.DateUtils;
import com.wvp.domain.WvpGpsInfo;
import com.wvp.mapper.WvpGpsInfoMapper;
import com.wvp.service.IWvpGpsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 定位信息记录Service业务层处理
 *
 * @author fs
 * @date 2022-03-07
 */
@Service
public class WvpGpsInfoServiceImpl implements IWvpGpsInfoService
{
    @Autowired
    private WvpGpsInfoMapper wvpGpsInfoMapper;

    /**
     * 查询定位信息记录
     *
     * @param id 定位信息记录主键
     * @return 定位信息记录
     */
    @Override
    public WvpGpsInfo selectWvpGpsInfoById(Long id)
    {
        return wvpGpsInfoMapper.selectWvpGpsInfoById(id);
    }

    /**
     * 查询定位信息记录列表
     *
     * @param wvpGpsInfo 定位信息记录
     * @return 定位信息记录
     */
    @Override
    public List<WvpGpsInfo> selectWvpGpsInfoList(WvpGpsInfo wvpGpsInfo)
    {
        return wvpGpsInfoMapper.selectWvpGpsInfoList(wvpGpsInfo);
    }

    /**
     * 新增定位信息记录
     *
     * @param wvpGpsInfo 定位信息记录
     * @return 结果
     */
    @Override
    public int insertWvpGpsInfo(WvpGpsInfo wvpGpsInfo)
    {
        wvpGpsInfo.setCreateTime(DateUtils.getNowDate());
        return wvpGpsInfoMapper.insertWvpGpsInfo(wvpGpsInfo);
    }

    /**
     * 修改定位信息记录
     *
     * @param wvpGpsInfo 定位信息记录
     * @return 结果
     */
    @Override
    public int updateWvpGpsInfo(WvpGpsInfo wvpGpsInfo)
    {
        return wvpGpsInfoMapper.updateWvpGpsInfo(wvpGpsInfo);
    }

    /**
     * 批量删除定位信息记录
     *
     * @param ids 需要删除的定位信息记录主键
     * @return 结果
     */
    @Override
    public int deleteWvpGpsInfoByIds(Long[] ids)
    {
        return wvpGpsInfoMapper.deleteWvpGpsInfoByIds(ids);
    }

    /**
     * 删除定位信息记录信息
     *
     * @param id 定位信息记录主键
     * @return 结果
     */
    @Override
    public int deleteWvpGpsInfoById(Long id)
    {
        return wvpGpsInfoMapper.deleteWvpGpsInfoById(id);
    }
}
