该项目使用的若依前后端分离框架，
主要通过ehome5.0协议实现海康ipc的预览、推流、语音对讲（设备和客户端）、云台控制、报警上传（gps）其他的车牌识别、安全帽脱帽报警、安全帽撞击报警等功能跟这个类似自己实现即可。

如果要实现rtmp推流需要自己搭建一个nginx-rtmp推流服务 更改项目中配置文件的推流地址即可。

linux环境ehome环境搭建
1、将lib-linux目录中的文件放到linux服务器下/home/hik/LinuxSDK 目录中。
2、/etc目录下ld.so.conf文件加入以下
/home/hik/LinuxSDK/
/home/hik/LinuxSDK/HCAapSDKCom
/usr/lib64/openssl/engines

保存后 在etc目录输入命令ldconfig重载文件
