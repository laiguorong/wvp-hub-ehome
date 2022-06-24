DROP TABLE IF EXISTS `wvp_gps_info`;
CREATE TABLE `wvp_gps_info`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `longitude` float(6, 0) NULL DEFAULT NULL COMMENT '经度',
  `latitude` float(6, 0) NULL DEFAULT NULL COMMENT '维度',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定位信息记录' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `wvp_device`;
CREATE TABLE `wvp_device`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备别名',
  `device_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备ip',
  `device_version` int(0) NULL DEFAULT NULL COMMENT '设备注册协议版本号',
  `device_online` int(0) NULL DEFAULT NULL COMMENT '设备是否在线0不在线1在线',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '第一次注册时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `offline_time` datetime(0) NULL DEFAULT NULL COMMENT '设备下线时间',
  `luser_id` int(0) NULL DEFAULT -1 COMMENT '设备注册用户句柄',
  `ehome_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'EhomeKey',
  `push_state` int(0) NULL DEFAULT NULL COMMENT '设备是否正在推流0未推流1推流',
  `llink_handle` int(0) NULL DEFAULT -1 COMMENT '预览连接句柄',
  `session_id` int(0) NULL DEFAULT NULL COMMENT '预览sessionId',
  `voice_handle` int(0) NULL DEFAULT NULL COMMENT '对讲连接句柄',
  `video_hls_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频流地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备表' ROW_FORMAT = Dynamic;
