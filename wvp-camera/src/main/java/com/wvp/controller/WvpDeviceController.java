package com.wvp.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.wvp.domain.vo.DevicePtzVo;
import com.wvp.hk.module.RemoteManage.DeviceConfig;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ECMS;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA;
import com.wvp.hk.module.service.stream.Preview;
import com.wvp.hk.module.service.stream.VoiceTalk;
import com.wvp.hk.module.service.stream.VoiceTalkWithServer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wvp.common.annotation.Log;
import com.wvp.common.core.controller.BaseController;
import com.wvp.common.core.domain.AjaxResult;
import com.wvp.common.enums.BusinessType;
import com.wvp.domain.WvpDevice;
import com.wvp.service.IWvpDeviceService;
import com.wvp.common.utils.poi.ExcelUtil;
import com.wvp.common.core.page.TableDataInfo;

/**
 * 设备列表Controller
 *
 * @author fs
 * @date 2022-02-17
 */
@RestController
@RequestMapping("/wvp/device")
public class WvpDeviceController extends BaseController
{
    @Autowired
    private IWvpDeviceService wvpDeviceService;

    /**
     * 查询设备列表列表
     */
    @PreAuthorize("@ss.hasPermi('wvp:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(WvpDevice wvpDevice)
    {
        startPage();
        List<WvpDevice> list = wvpDeviceService.selectWvpDeviceList(wvpDevice);
        return getDataTable(list);
    }

    /**
     * 导出设备列表列表
     */
    @PreAuthorize("@ss.hasPermi('wvp:device:export')")
    @Log(title = "设备列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WvpDevice wvpDevice)
    {
        List<WvpDevice> list = wvpDeviceService.selectWvpDeviceList(wvpDevice);
        ExcelUtil<WvpDevice> util = new ExcelUtil<WvpDevice>(WvpDevice.class);
        util.exportExcel(response, list, "设备列表数据");
    }

    /**
     * 获取设备列表详细信息
     */
    @PreAuthorize("@ss.hasPermi('wvp:device:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(wvpDeviceService.selectWvpDeviceById(id));
    }

    /**
     * 新增设备列表
     */
    @PreAuthorize("@ss.hasPermi('wvp:device:add')")
    @Log(title = "设备列表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WvpDevice wvpDevice)
    {
        return toAjax(wvpDeviceService.insertWvpDevice(wvpDevice));
    }

    /**
     * 修改设备列表
     */
    @PreAuthorize("@ss.hasPermi('wvp:device:edit')")
    @Log(title = "设备列表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WvpDevice wvpDevice)
    {
        return toAjax(wvpDeviceService.updateWvpDevice(wvpDevice));
    }

    /**
     * 开始推流
     */
	@GetMapping(value ="/startPreviewDevice/{luserId}")
    public AjaxResult startPreviewDevice(@PathVariable int luserId)
    {
//        ISUPCMSByJNA.NET_EHOME_PREVIEWINFO_IN_V11 struPreviewIn = new ISUPCMSByJNA.NET_EHOME_PREVIEWINFO_IN_V11();
//        //码流类型：0- 主码流，1- 子码流, 2- 第三码流
//        struPreviewIn.dwStreamType = 0;
//        //通道号
//        struPreviewIn.iChannel = 1;
//        //0-tcp  1-udp
//        struPreviewIn.dwLinkMode = 0;
//        CommonMethod.ByteCopy("192.168.5.158", struPreviewIn.struStreamSever.szIP);
//        struPreviewIn.struStreamSever.wPort = 7662;
//        struPreviewIn.byDelayPreview = 0;
//        Preview.StartPreview(luserId, struPreviewIn);
        return toAjax(1);
    }

    /**
     * 停止推流
     */
    @PostMapping(value ="/stopPreviewDevice")
    public AjaxResult stopPreviewDevice(@RequestBody WvpDevice wvpDevice)
    {
        Preview.StopPreview(wvpDevice.getDeviceId());
        return toAjax(1);
    }

    /**
     * 开始对讲
     */
    @PostMapping(value ="/startVoiceTalk")
    public AjaxResult startVoiceTalk(@RequestBody WvpDevice wvpDevice)
    {
        VoiceTalkWithServer.StartVoiceTalk(wvpDevice.getLuserId().intValue());
        return toAjax(1);
    }


    /**
     * 云台控制：启动
     */
    @PostMapping(value ="/ptzControlStart")
    public AjaxResult ptzControlStart(@RequestBody DevicePtzVo devicePtzVo)
    {
        ISUPCMSByJNA.NET_EHOME_REMOTE_CTRL_PARAM netEhomeRemoteCtrlParam=new ISUPCMSByJNA.NET_EHOME_REMOTE_CTRL_PARAM();
        ISUPCMSByJNA.NET_EHOME_PTZ_PARAM netEhomePtzParam=new ISUPCMSByJNA.NET_EHOME_PTZ_PARAM();
        netEhomePtzParam.read();
        netEhomePtzParam.dwSize=netEhomePtzParam.size();
        netEhomePtzParam.byPTZCmd=devicePtzVo.getPtzCmd();
        netEhomePtzParam.byAction=0;
        netEhomePtzParam.bySpeed=devicePtzVo.getPtzSpeed();
        netEhomePtzParam.write();

        netEhomeRemoteCtrlParam.read();
        netEhomeRemoteCtrlParam.dwSize=netEhomeRemoteCtrlParam.size();
        netEhomeRemoteCtrlParam.lpInbuffer=netEhomePtzParam.getPointer();
        netEhomeRemoteCtrlParam.dwInBufferSize=netEhomePtzParam.size();
        IntByReference channle=new IntByReference();
        netEhomeRemoteCtrlParam.lpCondBuffer=channle.getPointer();
        netEhomeRemoteCtrlParam.dwCondBufferSize=4;
        netEhomeRemoteCtrlParam.write();

        boolean b_ptz=ECMS.ptz((long)devicePtzVo.getLuserId(),1000,netEhomeRemoteCtrlParam);
        if (!b_ptz) {
            int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
            System.out.println("NET_ECMS_XMLConfig failed,error：" + iErr);

        }
        System.out.println("云台控制调用成功");
        return toAjax(1);
    }

    /**
     * 云台控制：停止
     */
    @PostMapping(value ="/ptzControlStop")
    public AjaxResult ptzControlStop(@RequestBody DevicePtzVo devicePtzVo)
    {
        ISUPCMSByJNA.NET_EHOME_REMOTE_CTRL_PARAM netEhomeRemoteCtrlParam=new ISUPCMSByJNA.NET_EHOME_REMOTE_CTRL_PARAM();
        ISUPCMSByJNA.NET_EHOME_PTZ_PARAM netEhomePtzParam=new ISUPCMSByJNA.NET_EHOME_PTZ_PARAM();
        netEhomePtzParam.read();
        netEhomePtzParam.dwSize=netEhomePtzParam.size();
        netEhomePtzParam.byPTZCmd=devicePtzVo.getPtzCmd();
        netEhomePtzParam.byAction=1;
        netEhomePtzParam.bySpeed=devicePtzVo.getPtzSpeed();
        netEhomePtzParam.write();

        netEhomeRemoteCtrlParam.read();
        netEhomeRemoteCtrlParam.dwSize=netEhomeRemoteCtrlParam.size();
        netEhomeRemoteCtrlParam.lpInbuffer=netEhomePtzParam.getPointer();
        netEhomeRemoteCtrlParam.dwInBufferSize=netEhomePtzParam.size();
        IntByReference channle=new IntByReference(1);
        netEhomeRemoteCtrlParam.lpCondBuffer=channle.getPointer();
        netEhomeRemoteCtrlParam.dwCondBufferSize=4;
        netEhomeRemoteCtrlParam.write();

        boolean b_ptz=ECMS.ptz((long)devicePtzVo.getLuserId(),1000,netEhomeRemoteCtrlParam);
        if (!b_ptz) {
            int iErr = ECMS.GetCMSInstance().NET_ECMS_GetLastError();
            System.out.println("NET_ECMS_XMLConfig failed,error：" + iErr);

        }
        System.out.println("云台控制调用成功");
        return toAjax(1);
    }


}
