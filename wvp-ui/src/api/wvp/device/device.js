import request from '@/utils/request'

// 查询设备列表列表
export function listDevice(query) {
  return request({
    url: '/wvp/device/list',
    method: 'get',
    params: query
  })
}

// 查询设备列表详细
export function getDevice(id) {
  return request({
    url: '/wvp/device/' + id,
    method: 'get'
  })
}

// 新增设备列表
export function addDevice(data) {
  return request({
    url: '/wvp/device',
    method: 'post',
    data: data
  })
}

// 修改设备列表
export function updateDevice(data) {
  return request({
    url: '/wvp/device',
    method: 'put',
    data: data
  })
}

// 推流当前设备
export function startPreviewDevice(luserId) {
  return request({
    url: '/wvp/device/startPreviewDevice/' + luserId,
    method: 'get'
  })
}

// 推流当前设备
export function stopPreviewDevice(data) {
  return request({
    url: '/wvp/device/stopPreviewDevice',
    method: 'post',
    data: data
  })
}

// 开启对讲
export function startVoiceTalk(data) {
  return request({
    url: '/wvp/device/startVoiceTalk',
    method: 'post',
    data: data
  })
}

// 云台控制：开始
export function ptzControlStart(data) {
  return request({
    url: '/wvp/device/ptzControlStart',
    method: 'post',
    data: data
  })
}

// 云台控制：停止
export function ptzControlStop(data) {
  return request({
    url: '/wvp/device/ptzControlStop',
    method: 'post',
    data: data
  })
}

//获取所有文件列表
export function getDeviceFileList(query) {
  return request({
    url: '/wvp/file/list',
    method: 'get',
    params: query
  })
}

