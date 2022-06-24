package com.wvp.mapper;

import com.wvp.domain.WvpGpsInfo;

import java.util.List;


/**
 * 定位信息记录Mapper接口
 *
 * @author fs
 * @date 2022-03-07
 */
public interface WvpGpsInfoMapper
{
    /**
     * 查询定位信息记录
     *
     * @param id 定位信息记录主键
     * @return 定位信息记录
     */
    public WvpGpsInfo selectWvpGpsInfoById(Long id);

    /**
     * 查询定位信息记录列表
     *
     * @param wvpGpsInfo 定位信息记录
     * @return 定位信息记录集合
     */
    public List<WvpGpsInfo> selectWvpGpsInfoList(WvpGpsInfo wvpGpsInfo);

    /**
     * 新增定位信息记录
     *
     * @param wvpGpsInfo 定位信息记录
     * @return 结果
     */
    public int insertWvpGpsInfo(WvpGpsInfo wvpGpsInfo);

    /**
     * 修改定位信息记录
     *
     * @param wvpGpsInfo 定位信息记录
     * @return 结果
     */
    public int updateWvpGpsInfo(WvpGpsInfo wvpGpsInfo);

    /**
     * 删除定位信息记录
     *
     * @param id 定位信息记录主键
     * @return 结果
     */
    public int deleteWvpGpsInfoById(Long id);

    /**
     * 批量删除定位信息记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWvpGpsInfoByIds(Long[] ids);
}
