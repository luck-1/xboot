// 统一请求路径前缀在libs/axios.js中修改
import { getRequest, postRequest, putRequest, deleteRequest, importRequest, uploadFileRequest } from '@/libs/axios';



// 导出模型
export const exportModel = "/xboot/actModel/export/"
// 通过文件部署模型流程
export const deployByFile = "/xboot/actModel/deployByFile"
// 导出流程资源
export const exportResource = "/xboot/actProcess/export"


// 获取模型
export const getModelDataList = (params) => {
    return getRequest('/actModel/getByCondition', params)
}
// 添加模型
export const addModel = (params) => {
    return postRequest('/actModel/add', params)
}
// 部署模型
export const deployModel = (id, params) => {
    return getRequest(`/actModel/deploy/${id}`, params)
}
// 删除模型
export const deleteModel = (ids, params) => {
    return deleteRequest(`/actModel/delByIds/${ids}`, params)
}



// 获取流程数据
export const getProcessDataList = (params) => {
    return getRequest('/actProcess/getByCondition', params)
}
// 修改流程分类或备注
export const updateInfo = (params) => {
    return postRequest('/actProcess/updateInfo', params)
}
// 修改流程状态 激活/挂起
export const updateStatus = (params) => {
    return postRequest('/actProcess/updateStatus', params)
}
// 转化流程为模型
export const convertToModel = (id, params) => {
    return getRequest(`/actProcess/convertToModel/${id}`, params)
}
// 删除模型
export const deleteProcess = (ids, params) => {
    return deleteRequest(`/actProcess/delByIds/${ids}`, params)
}
// 删除模型运行实例
export const deleteProcessIns = (ids, params) => {
    return deleteRequest(`/actProcess/delInsByIds/${ids}`, params)
}