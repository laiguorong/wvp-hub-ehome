package com.wvp.hk.module.service.util;


import com.sun.jna.NativeLong;
import com.wvp.common.core.redis.RedisCache;
import com.wvp.hk.module.service.stream.EStream;
import com.wvp.hk.module.service.stream.Preview;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * 海康sdk接收实时视频回调演示demo
 * @author eguid
 *
 */
public class HaiKangSdkAccess {

    RedisCache redisCache;

    //日志文件
    protected static final Logger logger = LoggerFactory.getLogger(EStream.class);

    JavaCVProcessThread t=null;

    private String deviceId;
    private Long luserId;
    private int lLinkHandle;
    private String rtmpUrl;

    public HaiKangSdkAccess(String deviceId, Long luserId, int lLinkHandle, String rtmpUrl,RedisCache redisCache) {
        this.deviceId = deviceId;
        this.luserId = luserId;
        this.lLinkHandle = lLinkHandle;
        this.rtmpUrl = rtmpUrl;
        this.redisCache=redisCache;
    }

    //通过海康/大华sdk回调函数每次回调传输过来的视频字节数组数据写入到管道流
    public void onMediaStream(byte[] data, boolean isAudio) throws IOException {
        if(t==null){
            //启动javacv解析处理器线程
            logger.info("启动javacv解析处理器线程"+rtmpUrl+deviceId);
            t=new JavaCVProcessThread(rtmpUrl+deviceId,deviceId,redisCache);
//            t=new JavaCVProcessThread("rtmp://127.0.0.1:1935/live/"+deviceId);
            t.start();
        }
        if(t!=null){
            //写出视频码流到javacv多线程解析处理器
            redisCache.setCacheObject(deviceId,deviceId,60, TimeUnit.SECONDS);
            t.push(data, data.length,deviceId);
        }

    }
}

/**
 * javacv多线程解析处理器，用于读取海康/大华/宇视设备sdk回调视频码流并解析转码推流到rtmp
 * @author fs
 *
 */
class JavaCVProcessThread extends Thread{


    //日志文件
    protected static final Logger logger = LoggerFactory.getLogger(EStream.class);
    FFmpegFrameGrabber grabber=null;
    FFmpegFrameRecorder recorder = null;
    PipedInputStream inputStream;
    PipedOutputStream outputStream;
    String pushAddress;
    String deviceId;

    RedisCache redisCache;
    /**
     * 创建用于把字节数组转换为inputstream流的管道流
     * @throws IOException
     */
    public JavaCVProcessThread(String output,String deviceId,RedisCache redisCache) throws IOException {
        this.redisCache=redisCache;
        this.deviceId=deviceId;
        pushAddress = output;
        outputStream = new PipedOutputStream();
        inputStream = new PipedInputStream(outputStream, 2048);

        System.out.println("创建线程："+outputStream);
    }

    /**
     * 异步接收海康/大华/宇视设备sdk回调实时视频裸流数据
     * @param data
     * @param size
     */
    public void push(byte[] data,int size,String deviceId) throws IOException {
//        System.out.println(outputStream+"设备ID："+deviceId);

        try {

            outputStream.write(data, 0, size);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Preview.StopPreview(deviceId);

        }

//        if(data!=null&&outputStream!=null){
//            try {
//
//                outputStream.write(data, 0, size);
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                Preview.StopPreview(deviceId);
//
//            }
//        }else{
//            System.out.println("对象地址：,数据："+data);
//            Preview.StopPreview(deviceId);
//        }



    }


    @Override
    public void run() {

        grabber = new FFmpegFrameGrabber(inputStream, 0);
        grabber.setOption("rtsp_transport", "tcp");
        //黑白
//        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        grabber.setAudioStream(Integer.MIN_VALUE);
        grabber.setFormat("mpeg");

        long stime = System.currentTimeMillis();
        // 检测回调函数书否有数据流产生，防止avformat_open_input函数阻塞

        try {
            do {
                Thread.sleep(100);
                if (System.currentTimeMillis() - stime > 2000) {
                    System.out.println("-----SDK回调无视频流产生------");
                    return;
                }
            } while (inputStream.available() != 2048);

            // 只打印错误日志
            avutil.av_log_set_level(avutil.AV_LOG_QUIET);

//            avutil.av_log_set_level(avutil.AV_LOG_ERROR);
            FFmpegLogCallback.set();
            grabber.start();
            System.out.println("--------开始推送视频流---------");
            FFmpegFrameRecorder recorder=new FFmpegFrameRecorder(pushAddress,grabber.getImageWidth(),grabber.getImageHeight(), grabber.getAudioChannels());
            recorder.setInterleaved(true);
            // 画质参数
            recorder.setVideoOption("crf", "28");
            // H264编/解码器
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoBitrate(grabber.getVideoBitrate());
            //推rtmp这里是必须设置成flv
            recorder.setFormat("flv");
            //推rtsp必须设置rtsp
//            recorder.setFormat("rtsp");

            // 视频帧率，最低保证15
            recorder.setFrameRate(15);
            // 关键帧间隔 一般与帧率相同或者是帧率的两倍
            recorder.setGopSize(30);
            // yuv420p
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setAudioChannels(1);
            // 降低编码延时
            recorder.setVideoOption("tune", "zerolatency");
            // 提升编码速度
            recorder.setVideoOption("preset", "ultrafast");
            recorder.start();
            System.err.println("启动recorder");


            int count = 0;
            Frame frame;
            while (grabber.hasVideo() && (frame = grabber.grab()) != null ) {
                count++;
                if (count % 100 == 0) {
                    System.out.println("推送视频帧次数："+count);
                    logger.info("推送视频帧次数："+count);
                    count=0;
                }
                if (frame.samples != null) {
                    System.out.println("检测到音频");
                }
                recorder.record(frame);
            }
            if (grabber != null) {
                grabber.stop();
                grabber.release();
            }
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            inputStream=null;
            outputStream=null;
        } catch (Exception e) {
            inputStream=null;
            outputStream=null;
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            } catch (Exception er) {
                er.printStackTrace();
            }
            e.printStackTrace();
//            System.err.println(e.toString());
//            System.out.println("结束线程"+Thread.currentThread());
//            Thread.currentThread().interrupt();
//            Thread.currentThread().join();

        }finally {
            inputStream=null;
            outputStream=null;
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
