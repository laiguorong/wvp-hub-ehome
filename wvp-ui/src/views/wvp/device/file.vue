<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件类型" prop="deviceOnline">
        <el-select v-model="queryParams.fileType" placeholder="请选择">
          <el-option
            v-for="dict in dict.type.wvp_file_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker
          v-model="dateRange"
          size="small"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

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
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getDeviceFileList"
    />

  </div>
</template>

<script>
  import { getDeviceFileList } from "@/api/wvp/device/device";

export default {
  name: "File",
  dicts: ['wvp_file_type'],
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        fileType: ''
      }
    };
  },
  created() {
  },
  methods: {
    getDeviceFileList(){
      this.loading = true;
      getDeviceFileList(this.queryParams, this.dateRange).then(response => {
        this.list = response.rows;
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
    }



  },
  computed: {

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
