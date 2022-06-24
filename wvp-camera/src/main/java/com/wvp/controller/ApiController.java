package com.wvp.controller;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.wvp.common.annotation.Log;
import com.wvp.common.core.controller.BaseController;
import com.wvp.common.core.domain.AjaxResult;
import com.wvp.common.core.page.TableDataInfo;
import com.wvp.common.enums.BusinessType;
import com.wvp.common.utils.poi.ExcelUtil;
import com.wvp.domain.WvpDevice;
import com.wvp.domain.po.Result;
import com.wvp.domain.vo.DevicePtzVo;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA;
import com.wvp.hk.module.service.stream.Preview;
import com.wvp.hk.module.service.stream.VoiceTalkWithServer;
import com.wvp.service.IWvpDeviceService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 设备列表Controller
 *
 * @author fs
 * @date 2022-02-17
 */
@RestController
@RequestMapping("/test/api")
public class ApiController extends BaseController
{

    @PostMapping("/getName")
    public String getName(@RequestParam ("name") String name,@RequestParam ("age") int age){
        System.out.println(name);
        System.out.println(age);
        JSONObject jsonData=new JSONObject();
        jsonData.put("name","小张");
        return jsonData.toString();
    }
}
