<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="序列号" prop="deviceId">
        <el-input
          v-model="queryParams.deviceId"
          placeholder="请输入序列号"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备别名" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入设备别名"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备状态" prop="deviceOnline">
        <el-input
          v-model="queryParams.deviceOnline"
          placeholder="请选择设备状态"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['wvp:device:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['wvp:device:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['wvp:device:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        label="序号"
        align="center"
        type="index"
        width="50">
      </el-table-column>
      <el-table-column label="设备序列号" align="center" prop="deviceId" />
      <el-table-column label="设备别名" align="center" prop="deviceName" />
      <el-table-column label="设备ip" align="center" prop="deviceIp" />
      <el-table-column label="设备注册协议版本号" align="center" prop="deviceVersion" />
      <el-table-column label="设备状态" align="center" prop="deviceOnline" >
        <template slot-scope="scope">
          <dict-tag :options="dict.type.wvp_device_state" :value="scope.row.deviceOnline"/>
        </template>
      </el-table-column>
      <el-table-column label="经度" align="center" prop="wvpGpsInfo.longitude" />
      <el-table-column label="维度" align="center" prop="wvpGpsInfo.latitude" />
      <el-table-column label="拉流状态" align="center" prop="pushState">
      <template slot-scope="scope">
        <dict-tag :options="dict.type.wvp_push_state" :value="scope.row.pushState"/>
      </template>
      </el-table-column>
      <el-table-column label="注册句柄" align="center" prop="luserId" />
      <el-table-column label="预览句柄" align="center" prop="llinkHandle" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wvp:device:edit']"
          >修改</el-button>
          <el-button
            v-if="scope.row.pushState==0"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="startPreviewDevice(scope.row)"
          >推流</el-button>
          <el-button
            v-if="scope.row.pushState==1"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="stopPreviewDevice(scope.row)"
          >结束推流</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-phone-outline"
            @click="startVoiceTalk(scope.row)"
          >对讲</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-video-camera"
            @click="openPreviewFun(scope.row)"
          >预览</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-folder"
            @click="fileListFun(scope.row)"
          >文件列表</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改设备列表对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="序列号" prop="deviceId">
          <el-input v-model="form.deviceId" placeholder="请输入设备序列号" />
        </el-form-item>
        <el-form-item label="设备别名" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备别名" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 预览窗口对话框 -->
    <el-dialog class="preview-c" :title="previewTitle" :visible.sync="previewOpen" destroy-on-close width="1100px"  append-to-body >
      <div class="video-play">
        <video-player
          ref="videoPlayer"
          class="video-player vjs-custom-skin"
          :playsinline="true"
          :options="playerOptions"
          @play="onPlayerPlay($event)"
          @pause="onPlayerPause($event)"
        />
      </div>
      <div class="ptz-control" >
        <span>云台速度</span>
        <select v-model="ptzData.ptzSpeed" @change="selectPtzSpeed($event)">
          <option value="10">1</option>
          <option value="20">2</option>
          <option value="30">3</option>
          <option value="40">4</option>
          <option value="50">5</option>
          <option value="60">6</option>
          <option value="70">7</option>
        </select>
        <table>
          <tr>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,4)" v-on:mouseup="ptzControlStopFun(1,4)">左上</button></td>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,0)" v-on:mouseup="ptzControlStopFun(1,0)">上</button></td>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,6)" v-on:mouseup="ptzControlStopFun(1,6)">右上</button></td>
          </tr>
          <tr>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,2)" v-on:mouseup="ptzControlStopFun(1,16)">左</button></td>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,16)" v-on:mouseup="ptzControlStopFun(1,16)">自动</button></td>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,3)" v-on:mouseup="ptzControlStopFun(1,3)">右</button></td>
          </tr>
          <tr>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,5)" v-on:mouseup="ptzControlStopFun(1,5)">左下</button></td>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,1)" v-on:mouseup="ptzControlStopFun(1,1)">下</button></td>
            <td><button class="ptz-btn" v-on:mousedown="ptzControlStartFun(0,7)" v-on:mouseup="ptzControlStopFun(1,7)">右下</button></td>
          </tr>
        </table>
        <table>
          <tr>
            <td><button class="ptz-btn-2" v-on:mousedown="ptzControlStartFun(0,9)" v-on:mouseup="ptzControlStopFun(1,9)">变焦+</button></td>
            <td><button class="ptz-btn-2" v-on:mousedown="ptzControlStartFun(0,8)" v-on:mouseup="ptzControlStopFun(1,8)">变焦-</button></td>
          </tr>
          <tr>
            <td><button class="ptz-btn-2" v-on:mousedown="ptzControlStartFun(0,11)" v-on:mouseup="ptzControlStopFun(1,11)">聚焦+</button></td>
            <td><button class="ptz-btn-2" v-on:mousedown="ptzControlStartFun(0,10)" v-on:mouseup="ptzControlStopFun(1,10)">聚焦-</button></td>
          </tr>
          <tr>
            <td><button class="ptz-btn-2" v-on:mousedown="ptzControlStartFun(0,12)" v-on:mouseup="ptzControlStopFun(1,12)">光圈+</button></td>
            <td><button class="ptz-btn-2" v-on:mousedown="ptzControlStartFun(0,13)" v-on:mouseup="ptzControlStopFun(1,13)">光圈-</button></td>
          </tr>
          <tr>
            <td><button class="ptz-btn-3" v-on:mousedown="ptzControlStartFun(0,14)" v-on:mouseup="ptzControlStopFun(1,14)">补光灯</button></td>
            <td><button class="ptz-btn-2" v-on:mousedown="ptzControlStartFun(0,15)" v-on:mouseup="ptzControlStopFun(1,15)">雨刷</button></td>
          </tr>
        </table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import { listDevice, getDevice, startPreviewDevice, stopPreviewDevice, startVoiceTalk, addDevice, updateDevice, ptzControlStart, ptzControlStop } from "@/api/wvp/device/device";

export default {
  name: "Device",
  dicts: ['wvp_device_state','wvp_push_state'],
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 设备列表表格数据
      deviceList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceId: null,
        deviceName: null,
        deviceIp: null,
        deviceVersion: null,
        deviceOnline: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      },
      //预览窗口标题
      previewTitle:"控制台",
      //是否显示预览窗口
      previewOpen: false,
      //当前设备参数
      deviceInfo:{},
      // 视频播放
      playerOptions: {
        // playbackRates: [0.7, 1.0, 1.5, 2.0], //播放速度
        autoplay: true, // 如果true,浏览器准备好时开始回放。
        muted: false, // 默认情况下将会消除任何音频。
        loop: true, // 导致视频一结束就重新开始。
        preload: 'auto', // 建议浏览器在<video>加载元素后是否应该开始下载视频数据。auto浏览器选择最佳行为,立即开始加载视频（如果浏览器支持）
        language: 'zh-CN',
        aspectRatio: '16:9', // 将播放器置于流畅模式，并在计算播放器的动态大小时使用该值。值应该代表一个比例 - 用冒号分隔的两个数字（例如"16:9"或"4:3"）
        fluid: true, // 当true时，Video.js player将拥有流体大小。换句话说，它将按比例缩放以适应其容器。
        sources: [
          {
            type: 'application/x-mpegURL', // 这里的种类支持很多种：基本视频格式、直播、流媒体等，具体可以参看git网址项目
            src: 'null' // url地址
          }
        ],
        hls: true,
        poster: 'http://pic33.nipic.com/20131007/13639685_123501617185_2.jpg', // 你的封面地址
        // width: 700, // 播放器宽度
        notSupportedMessage: '此视频暂无法播放，请稍后再试', // 允许覆盖Video.js无法播放媒体源时显示的默认信息。
        controlBar: {
          timeDivider: true,
          durationDisplay: true,
          remainingTimeDisplay: false,
          fullscreenToggle: true // 全屏按钮
        }
      },
      ptzData:{
        ptzCmd: null,
        ptzSpeed: 10,
        byAction: null,
        luserId: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询设备列表列表 */
    getList() {
      this.loading = true;
      listDevice(this.queryParams).then(response => {
        this.deviceList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        deviceId: null,
        deviceName: null,
        deviceIp: null,
        deviceVersion: null,
        deviceOnline: null,
        createTime: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加设备列表";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getDevice(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改设备列表";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateDevice(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDevice(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 推流按钮操作 */
    startPreviewDevice(row) {
      console.log(row);
      this.$modal.confirm('是否开始推流当前设备"' +row.deviceId+ '"？').then(function() {
        return startPreviewDevice(row.luserId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("推流成功");
      }).catch(() => {});
    },
    /** 结束推流按钮操作 */
    stopPreviewDevice(row) {
      console.log(row);
      this.$modal.confirm('是否结束当前设备"' +row.deviceId+ '"的推流？').then(function() {
        return stopPreviewDevice(row);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("结束推流成功");
      }).catch(() => {});
    },
    /** 开始对讲 */
    startVoiceTalk(row){
      this.$modal.confirm('是否开始语音对讲"' +row.deviceId+ '"？').then(function() {
        return startVoiceTalk(row);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("开启成功");
      }).catch(() => {});
    },
    /** 云台控制:开始 */
    ptzControlStartFun(byAction,ptzCmd){
      this.ptzData.byAction=byAction;
      this.ptzData.ptzCmd=ptzCmd;
      ptzControlStart(this.ptzData)
    },
    /** 云台控制:停止 */
    ptzControlStopFun(byAction,ptzCmd){
      this.ptzData.byAction=byAction;
      this.ptzData.ptzCmd=ptzCmd;
      ptzControlStop(this.ptzData)
    },
    openPreviewFun(row){
      console.log(row.deviceId)
      this.ptzData.luserId=row.luserId;
      this.previewOpen=true;
      //this.playerOptions.sources[0].src="hls/"+row.deviceId+".m3u8";
      this.playerOptions.sources[0].src=row.videoHlsUrl;
      // this.playerOptions.sources[0].src="http://27.128.201.188:50008/hls/J43940835.m3u8";
      // this.playerOptions.sources[0].src="rtmp://192.168.5.5:12020/hls/J43940835";
      // this.playerOptions.sources[0].src="http://127.0.0.1:8099/live_hls/J43940835.m3u8";
    },
    fileListFun(row){
      console.log('文件查找')
      //跳转文件查找页面
      this.$router.push('/device/fileList')
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('wvp/device/export', {
        ...this.queryParams
      }, `device_${new Date().getTime()}.xlsx`)
    },
    fullScreen() {
      const player = this.$refs.videoPlayer.player
      player.requestFullscreen()//调用全屏api方法
      player.isFullscreen(true)
      player.play()
    },
    onPlayerPlay(player) {
      player.play()
    },
    onPlayerPause(player) {
      // alert("pause");
    },
    onPlayerEnded(){
      console.log("关闭事件")
      const player = this.$refs.videoPlayer.player
      player.dispose;
    },
    selectPtzSpeed(event){
      this.ptzData.ptzSpeed = event.target.value; //获取option对应的value值 select_class_id是后台约定的提交数据的名称
    },
  },
  computed: {
    player() {
      return this.$refs.videoPlayer.player
    },
    playerEnded() {
      return this.$refs.videoPlayer.ended
    }
  }
};
</script>
<style scoped>
  .video-js .vjs-big-play-button {
    width: 72px;
    height: 72px;
    border-radius: 100%;
    z-index: 100;
    background-color: #ffffff;
    border: solid 1px #979797;
  }
  .preview-c /deep/ .el-dialog{
    height: 650px;
  }
  .ptz-btn{
    width: 50px;
  }
  .ptz-btn-2{
    width: 55px;
  }
  .ptz-btn-3{
    width: 65px;
  }
  .video-play{
    width: 800px;
    float: left;
  }
  .ptz-control{
    float: left;
  }
</style>
