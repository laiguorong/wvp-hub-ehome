package com.wvp.quartz.task;

import com.wvp.common.core.redis.RedisCache;
import com.wvp.domain.WvpDevice;
import com.wvp.hk.module.common.CommonMethod;
import com.wvp.hk.module.service.cms.ISUPCMSByJNA;
import com.wvp.hk.module.service.stream.EStream;
import com.wvp.hk.module.service.stream.Preview;
import com.wvp.mapper.WvpDeviceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.wvp.common.utils.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度测试
 *
 * @author ruoyi
 */
@Component("ryTask")
public class RyTask
{

    @Resource
    private RedisCache redisCache;

    @Value("${ehome.sms-preview-prot}")
    private short ehomeSmsPreViewProt;

    @Value("${ehome.in-ip}")
    private String ehomeInIp;

    @Value("${ehome.pu-ip}")
    private String ehomePuIp;

    //日志文件
    protected static final Logger logger = LoggerFactory.getLogger(EStream.class);
    @Autowired
    private WvpDeviceMapper wvpDeviceMapper;

    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format("执行多参方法： 字符串类型{}，布尔类型{}，长整型{}，浮点型{}，整形{}", s, b, l, d, i));
    }

    public void ryParams(String params)
    {
        System.out.println("执行有参方法：" + params);
    }

    public void ryNoParams()
    {
        System.out.println("执行无参方法");
    }


//    public void ehomePush(){
//        logger.info("定时任务执行：从数据库查询所有设备信息");
//        //从数据库查询所有设备信息
//        List<WvpDevice> wvpDeviceList=wvpDeviceMapper.selectWvpDeviceList(null);
//        for(WvpDevice wvpDevice:wvpDeviceList){
//            if(wvpDevice.getPushState()==0&&wvpDevice.getDeviceOnline()==1){
//                Object o=redisCache.getCacheObject(wvpDevice.getDeviceId());
//                if(o==null){
//                    redisCache.setCacheObject(wvpDevice.getDeviceId(),wvpDevice.getDeviceId(),60, TimeUnit.SECONDS);
//                    //重新开始推流
//                    pushVideo(wvpDevice);
//                }
//            }
//        }
//
//    }

    public void ehomeIsPushVideo(){
        logger.info("定时任务执行：redis查看遍历设备是否持续推流");
        //从数据库查询所有在线设备信息
        WvpDevice wvpDevice=new WvpDevice();
        wvpDevice.setDeviceOnline(1);
        List<WvpDevice> wvpDeviceList=wvpDeviceMapper.selectWvpDeviceList(wvpDevice);
        for(WvpDevice device:wvpDeviceList){
            //通过设备id 查询redis中缓存是否存在 不存在将重新开始推流
            Object o=redisCache.getCacheObject(device.getDeviceId());
            if(o==null){
//                //如果设备还未结束推流
//                if(device.getPushState()==1){
//                    //先向设备发送结束推流指令
//                    Preview.StopPreview(device.getDeviceId());
//                }


                //重新开始推流
                pushVideo(device);
                redisCache.setCacheObject(device.getDeviceId(),device.getDeviceId(),60, TimeUnit.SECONDS);
            }
        }
    }

    public void pushVideo(WvpDevice wvpDevice){
        ISUPCMSByJNA.NET_EHOME_PREVIEWINFO_IN_V11 struPreviewIn = new ISUPCMSByJNA.NET_EHOME_PREVIEWINFO_IN_V11();
        //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        struPreviewIn.dwStreamType = 0;
        //通道号
        struPreviewIn.iChannel = 1;
        //0-tcp  1-udp
        struPreviewIn.dwLinkMode = 0;
        CommonMethod.ByteCopy(ehomePuIp, struPreviewIn.struStreamSever.szIP);
        struPreviewIn.struStreamSever.wPort = ehomeSmsPreViewProt;
        struPreviewIn.byDelayPreview = 0;
        Preview.StartPreview(wvpDevice.getLuserId().intValue(), struPreviewIn);
    }

}
