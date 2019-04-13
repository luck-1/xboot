<style lang="less">
</style>
<template>
    <div class="search">
      <Row>
        <Card>     
          <p slot="title">
            富文本编辑 - <a href="http://www.wangeditor.com/" target="_blank">wangEditor</a>&nbsp;&nbsp;&nbsp;
            其他编辑器推荐：<a href="https://github.com/notadd/neditor" target="_blank">neditor</a>
          </p>
          <Form ref="form" :label-width="90">
            <Form-item label="内容详情">
              <div ref="editor"></div>
            </Form-item>
            <Form-item label="" class="br">
              <Button @click="editHTML" type="primary" icon="ios-create-outline">编辑HTML代码</Button>
              <Button @click="fullscreenModal=true" icon="md-eye">全屏预览</Button>
            </Form-item>
            <Form-item label="说明：">
              使用开源wangEditor编辑器，已配置好图片上传、表情包、编辑HTML代码、全屏预览、XSS攻击过滤。<br>
              当然你可以根据官方文档自行配置使用，由于wangEditor轻量简洁美观等特点这里没有选用neditor。
            </Form-item>
          </Form> 
        </Card>
      </Row>
      <Modal title="编辑html代码" v-model="showHTMLModal" :mask-closable='false' :width="900" :fullscreen="full">
        <Input v-if="!full" v-model="dataEdit" :autosize="true" type="textarea" style="max-height:60vh;overflow:auto"/>
        <Input v-if="full" v-model="dataEdit" :autosize="true" type="textarea"/>
        <div slot="footer">
          <Button @click="full=!full" icon="md-expand">全屏开/关</Button>
          <Button @click="editHTMLOk" type="primary" icon="md-checkmark-circle-outline">确定保存</Button>
        </div>
      </Modal>
      <Modal title="预览" v-model="fullscreenModal" fullscreen>
        <div v-html="data">{{data}}</div>
        <div slot="footer">
          <Button @click="fullscreenModal=false">关闭</Button>
        </div>
      </Modal>
    </div>
</template>

<script>
import { uploadFile } from "@/api/index";
import E from "wangeditor";
// 表情包配置 自定义表情可在该js文件中统一修改
import { sina } from "@/libs/emoji";
let editor = null;
var xss = require("xss");
export default {
  name: "html-edit",
  data() {
    return {
      data: "", // 富文本数据
      dataEdit: "", // 编辑数据
      showHTMLModal: false, // 显示html
      full: false, // html全屏开关
      fullscreenModal: false // 显示全屏预览
    };
  },
  methods: {
    init() {
      this.initEditor();
    },
    initEditor() {
      let that = this;
      // 详见wangeditor3官网文档 https://www.kancloud.cn/wangfupeng/wangeditor3/332599
      editor = new E(this.$refs.editor);
      // 编辑内容绑定数据
      editor.customConfig.onchange = html => {
        this.data = xss(html);
      };
      // 配置上传图片服务器端地址
      editor.customConfig.uploadImgServer = uploadFile;
      // xboot如要header中传入token鉴权
      editor.customConfig.uploadImgHeaders = {
        accessToken: that.getStore("accessToken")
      };
      editor.customConfig.uploadFileName = "file";
      editor.customConfig.uploadImgHooks = {
        before: function(xhr, editor, files) {
          // 图片上传之前触发
        },
        success: function(xhr, editor, result) {
          // 图片上传并返回结果，图片插入成功之后触发
        },
        fail: function(xhr, editor, result) {
          // 图片上传并返回结果，但图片插入错误时触发
          that.$Message.error("上传图片失败");
        },
        error: function(xhr, editor) {
          // 图片上传出错时触发
          that.$Message.error("上传图片出错");
        },
        timeout: function(xhr, editor) {
          // 图片上传超时时触发
          that.$Message.error("上传图片超时");
        },
        // 如果服务器端返回的不是 {errno:0, data: [...]} 这种格式，可使用该配置
        customInsert: function(insertImg, result, editor) {
          if (result.success === true) {
            let url = result.result;
            insertImg(url);
            that.$Message.success("上传图片成功");
          } else {
            that.$Message.error(result.message);
          }
        }
      };
      editor.customConfig.customAlert = function(info) {
        // info 是需要提示的内容
        // that.$Message.info(info);
      };
      // 字体
      editor.customConfig.fontNames = ["微软雅黑", "宋体", "黑体", "Arial"];
      // 表情面板可以有多个 tab ，因此要配置成一个数组。数组每个元素代表一个 tab 的配置
      editor.customConfig.emotions = [
        {
          // tab 的标题
          title: "新浪",
          // type -> 'emoji' / 'image'
          type: "image",
          // content -> 数组
          content: sina
        }
      ];
      editor.create();
    },
    editHTML() {
      this.dataEdit = this.data;
      this.showHTMLModal = true;
    },
    editHTMLOk() {
      editor.txt.html(this.dataEdit);
      this.data = xss(this.dataEdit);
      this.showHTMLModal = false;
    }
  },
  mounted() {
    this.init();
  }
};
</script>