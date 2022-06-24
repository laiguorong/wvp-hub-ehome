package com.wvp.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.wvp.domain.WvpGpsInfo;
import com.wvp.service.IWvpGpsInfoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wvp.common.annotation.Log;
import com.wvp.common.core.controller.BaseController;
import com.wvp.common.core.domain.AjaxResult;
import com.wvp.common.enums.BusinessType;

import com.wvp.common.utils.poi.ExcelUtil;
import com.wvp.common.core.page.TableDataInfo;

/**
 * 定位信息记录Controller
 *
 * @author fs
 * @date 2022-03-07
 */
@RestController
@RequestMapping("/wvp/gps-info")
public class WvpGpsInfoController extends BaseController
{
    @Autowired
    private IWvpGpsInfoService wvpGpsInfoService;

    /**
     * 查询定位信息记录列表
     */
    @PreAuthorize("@ss.hasPermi('wvp:gps-info:list')")
    @GetMapping("/list")
    public TableDataInfo list(WvpGpsInfo wvpGpsInfo)
    {
        startPage();
        List<WvpGpsInfo> list = wvpGpsInfoService.selectWvpGpsInfoList(wvpGpsInfo);
        return getDataTable(list);
    }

    /**
     * 导出定位信息记录列表
     */
    @PreAuthorize("@ss.hasPermi('wvp:gps-info:export')")
    @Log(title = "定位信息记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WvpGpsInfo wvpGpsInfo)
    {
        List<WvpGpsInfo> list = wvpGpsInfoService.selectWvpGpsInfoList(wvpGpsInfo);
        ExcelUtil<WvpGpsInfo> util = new ExcelUtil<WvpGpsInfo>(WvpGpsInfo.class);
        util.exportExcel(response, list, "定位信息记录数据");
    }

    /**
     * 获取定位信息记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('wvp:gps-info:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(wvpGpsInfoService.selectWvpGpsInfoById(id));
    }

    /**
     * 新增定位信息记录
     */
    @PreAuthorize("@ss.hasPermi('wvp:gps-info:add')")
    @Log(title = "定位信息记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WvpGpsInfo wvpGpsInfo)
    {
        return toAjax(wvpGpsInfoService.insertWvpGpsInfo(wvpGpsInfo));
    }

    /**
     * 修改定位信息记录
     */
    @PreAuthorize("@ss.hasPermi('wvp:gps-info:edit')")
    @Log(title = "定位信息记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WvpGpsInfo wvpGpsInfo)
    {
        return toAjax(wvpGpsInfoService.updateWvpGpsInfo(wvpGpsInfo));
    }

    /**
     * 删除定位信息记录
     */
    @PreAuthorize("@ss.hasPermi('wvp:gps-info:remove')")
    @Log(title = "定位信息记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(wvpGpsInfoService.deleteWvpGpsInfoByIds(ids));
    }
}
