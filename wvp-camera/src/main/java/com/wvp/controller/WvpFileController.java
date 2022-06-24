package com.wvp.controller;

import com.wvp.common.annotation.Log;
import com.wvp.common.core.controller.BaseController;
import com.wvp.common.core.domain.AjaxResult;
import com.wvp.common.core.page.TableDataInfo;
import com.wvp.common.enums.BusinessType;
import com.wvp.common.utils.poi.ExcelUtil;
import com.wvp.domain.WvpGpsInfo;
import com.wvp.service.IWvpGpsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 设备文件Controller
 *
 * @author fs
 * @date 2022-03-07
 */
@RestController
@RequestMapping("/wvp/file")
public class WvpFileController extends BaseController
{
    @Autowired
    private IWvpGpsInfoService wvpGpsInfoService;

    /**
     * 查询设备文件列表
     */
    @PreAuthorize("@ss.hasPermi('wvp:file:list')")
    @GetMapping("/list")
    public TableDataInfo list(WvpGpsInfo wvpGpsInfo)
    {
        startPage();
        List<WvpGpsInfo> list = wvpGpsInfoService.selectWvpGpsInfoList(wvpGpsInfo);
        return getDataTable(list);
    }

}
