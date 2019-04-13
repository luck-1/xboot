<style lang="less">
@import "./messageManage.less";
</style>
<template>
  <div class="search">
    <Row>
      <Col>
        <Card>
          <p slot="title">
            <span v-if="type==0">发送新消息</span>
            <span v-else>编辑消息</span>
          </p>
          <Row>
            <Form ref="form" :model="form" :label-width="70" :rules="formValidate" style="position:relative">
              <FormItem label="消息类型" prop="type">
                <Select v-model="form.type" placeholder="请选择" style="width:250px">
                  <Option v-for="(item, i) in dictMessageType" :key="i" :value="Number(item.value)">{{item.title}}</Option>
                </Select>
              </FormItem>
              <FormItem label="标题" prop="title">
                <Input v-model="form.title" style="width:600px"/>
              </FormItem>
              <FormItem label="内容" prop="content">
                <div ref="editor"></div>
              </FormItem>
              <Form-item label="" class="br">
                <Button @click="editHTML" icon="ios-create-outline">编辑HTML代码</Button>
                <Button @click="fullscreenModal=true" icon="md-eye">全屏预览</Button>
              </Form-item>
              <FormItem label="新创建账号也推送" prop="createSend">
                <i-switch size="large" v-model="form.createSend">
                  <span slot="open">开启</span>
                  <span slot="close">关闭</span>
                </i-switch>
              </FormItem>
              <div v-if="type==0">
                <FormItem label="发送范围" prop="sex">
                  <RadioGroup v-model="form.range" @on-change="changeRange">
                    <Radio :label="0">全体用户</Radio>
                    <Radio :label="1">指定用户成员</Radio>
                  </RadioGroup>
                </FormItem>
                <div>
                  <FormItem label="选择用户" v-if="showChooseUser">
                    <Button @click="userModalVisible=true" icon="md-person-add" style="margin-right:10px">选择发送用户</Button>
                  </FormItem>
                  <FormItem label="">
                    <Collapse simple>
                      <Panel name="1">
                        已选择 <span class="select-count">{{selectUserCount}}</span> 人 
                        <p slot="content" v-if="showChooseUser">
                          <Tag v-for="(item, i) in selectUsers" :key="i" :name="item.id" color="default" closable @on-close="handleCancelUser">{{item.username}}</Tag>
                        </p>
                        <p slot="content" v-else>
                          <span>所有用户</span>
                        </p>
                      </Panel>
                    </Collapse>
                  </FormItem>
                </div>
              </div>
              <Form-item class="br">
                <Button type="primary" :loading="submitLoading" @click="handelSubmit" style="width:100px">提交</Button>
                <Button @click="handelCancel">取消</Button>
              </Form-item>
              <Spin size="large" fix v-if="loading"></Spin>
            </Form>
          </Row>
        </Card>
      </Col>
    </Row>
    <!-- Drawer抽屉式选择用户 -->
    <Drawer title="选择用户" closable v-model="userModalVisible" width="1000">
      <Form ref="searchUserForm" :model="searchUserForm" inline :label-width="70" class="search-form">
        <Form-item label="搜索用户" prop="username">
          <Input type="text" v-model="searchUserForm.username" clearable placeholder="请输入用户名" style="width: 250px"/>
        </Form-item>
        <Form-item style="margin-left:-35px;" class="br">
          <Button @click="handleSearchUser" type="primary" icon="ios-search">搜索</Button>
          <Button @click="handleResetUser">重置</Button>
        </Form-item>
      </Form>
      <Table :loading="userLoading" border :columns="userColumns" :data="userData" ref="userTable"></Table>
      <Row type="flex" justify="end" class="code-row-bg page" style="margin: 10px 0;">
        <Page :current="searchUserForm.pageNumber" :total="totalUser" :page-size="searchUserForm.pageSize" @on-change="changeUserPage" @on-page-size-change="changeUserPageSize" :page-size-opts="[9,18,36]" size="small" show-total show-elevator show-sizer></Page>
      </Row>
      <div class="drawer-footer">
        已选择 <span class="select-count">{{selectUserCount}}</span> 人
        <Button @click="clearSelectAllUser" style="margin-left:10px">清空已选</Button>
        <Button @click="userModalVisible=false" style="margin-left:5px" type="primary">关闭</Button>
      </div>
    </Drawer>
    <!-- html编辑 -->
    <Modal title="编辑html代码" v-model="showHTMLModal" :mask-closable='false' :width="900" :fullscreen="full">
      <Input v-if="!full" v-model="dataEdit" :autosize="true" type="textarea" style="max-height:60vh;overflow:auto"/>
      <Input v-if="full" v-model="dataEdit" :autosize="true" type="textarea"/>
      <div slot="footer">
        <Button @click="full=!full" icon="md-expand">全屏开/关</Button>
        <Button @click="editHTMLOk" type="primary" icon="md-checkmark-circle-outline">确定保存</Button>
      </div>
    </Modal>
    <Modal title="预览" v-model="fullscreenModal" fullscreen>
      <div v-html="form.content">{{form.content}}</div>
      <div slot="footer">
        <Button @click="fullscreenModal=false">关闭</Button>
      </div>
    </Modal>
    <!-- Modal弹出式选择用户 -->
    <!-- <Modal title="选择用户" v-model="userModalVisible" :width="1200" :styles="{top: '30px'}">
      <Form ref="searchUserForm" :model="searchUserForm" inline :label-width="70" class="search-form">
        <Form-item label="搜索用户" prop="username">
          <Input type="text" v-model="searchUserForm.username" clearable placeholder="请输入用户名" style="width: 250px"/>
        </Form-item>
        <Form-item style="margin-left:-35px;" class="br">
          <Button @click="handleSearchUser" type="primary" icon="ios-search">搜索</Button>
          <Button @click="handleResetUser">重置</Button>
        </Form-item>
      </Form>
      <Table :loading="userLoading" border :columns="userColumns" :data="userData" ref="userTable"></Table>
      <Row type="flex" justify="end" class="code-row-bg page" style="margin-top: 10px;">
        <Page :current="searchUserForm.pageNumber" :total="total" :page-size="searchUserForm.pageSize" @on-change="changeUserPage" @on-page-size-change="changeUserPageSize" :page-size-opts="[9,18,36]" size="small" show-total show-elevator show-sizer></Page>
      </Row>
      <div slot="footer">
        已选择 <span class="select-count">{{selectUserCount}}</span> 人
        <Button @click="clearSelectAllUser" style="margin-left:10px">清空已选</Button>
        <Button @click="userModalVisible=false" style="margin-left:5px" type="primary">关闭</Button>
      </div>
    </Modal> -->
  </div>
</template>

<script>
import {
  getUserListData,
  getMessageDataById,
  addMessage,
  editMessage,
  getDictDataByType,
  uploadFile
} from "@/api/index";
import E from "wangeditor";
// 表情包配置 自定义表情可在该js文件中统一修改
import { sina } from "@/libs/emoji";
let editor = null;
var xss = require("xss");
export default {
  title: "add-or-edit-message",
  data() {
    return {
      type: 0,
      loading: false, // 表单加载状态
      userLoading: true,
      selectUserCount: 0,
      selectUsers: [],
      userModalVisible: false,
      modalTitle: "", // 添加或编辑标题
      showChooseUser: false,
      form: {
        // 添加或编辑表单对象初始化数据
        title: "",
        content: "",
        type: 0,
        range: 0
      },
      searchUserForm: {
        username: "",
        type: "",
        status: "",
        pageNumber: 1, // 当前页数
        pageSize: 9, // 页面大小
        sort: "createTime", // 默认排序字段
        order: "desc" // 默认排序方式
      },
      formValidate: {
        // 表单验证规则
        title: [{ required: true, message: "标题不能为空", trigger: "blur" }],
        content: [{ required: true, message: "内容不能为空", trigger: "blur" }]
      },
      submitLoading: false, // 添加或编辑提交状态
      userColumns: [
        {
          type: "index",
          width: 60,
          align: "center"
        },
        {
          title: "用户名",
          key: "username",
          width: 145,
          sortable: true
        },
        {
          title: "头像",
          key: "avatar",
          width: 80,
          align: "center",
          render: (h, params) => {
            return h("Avatar", {
              props: {
                src: params.row.avatar
              }
            });
          }
        },
        {
          title: "所属部门",
          key: "departmentTitle",
          width: 120
        },
        {
          title: "手机",
          key: "mobile",
          width: 115,
          sortable: true
        },
        {
          title: "邮箱",
          key: "email",
          width: 180,
          sortable: true
        },
        {
          title: "性别",
          key: "sex",
          width: 70,
          align: "center",
          render: (h, params) => {
            let re = "";
            if (params.row.sex === 1) {
              re = "男";
            } else if (params.row.sex === 0) {
              re = "女";
            }
            return h("div", re);
          }
        },
        {
          title: "用户类型",
          key: "type",
          align: "center",
          width: 100,
          render: (h, params) => {
            let re = "";
            if (params.row.type === 1) {
              re = "管理员";
            } else if (params.row.type === 0) {
              re = "普通用户";
            }
            return h("div", re);
          }
        },
        {
          title: "状态",
          key: "status",
          align: "center",
          width: 120,
          render: (h, params) => {
            if (params.row.status === 0) {
              return h("div", [
                h(
                  "Tag",
                  {
                    props: {
                      color: "green"
                    }
                  },
                  "正常启用"
                )
              ]);
            } else if (params.row.status === -1) {
              return h("div", [
                h(
                  "Tag",
                  {
                    props: {
                      color: "red"
                    }
                  },
                  "禁用"
                )
              ]);
            }
          }
        },
        {
          title: "创建时间",
          key: "createTime",
          sortable: true,
          sortType: "desc",
          width: 150
        },
        {
          title: "操作",
          key: "action",
          width: 130,
          align: "center",
          fixed: "right",
          render: (h, params) => {
            return h("div", [
              h(
                "Button",
                {
                  props: {
                    type: "info",
                    size: "small"
                  },
                  on: {
                    click: () => {
                      this.chooseUser(params.row);
                    }
                  }
                },
                "添加该用户"
              )
            ]);
          }
        }
      ],
      userData: [],
      totalUser: 0,
      dictMessageType: [],
      dataEdit: "", // 编辑数据
      showHTMLModal: false, // 显示html
      full: false, // html全屏开关
      fullscreenModal: false, // 显示全屏预览
      backRoute: ""
    };
  },
  methods: {
    init() {
      this.getUserDataList();
      this.getDictMessageType();
      this.initEditor();
      this.type = this.$route.query.type;
      this.backRoute = this.$route.query.backRoute;
      if(this.type==1){
        this.form.id = this.$route.query.id;
        this.getData();
      }
    },
    initEditor() {
      let that = this;
      editor = new E(this.$refs.editor);
      editor.customConfig.onchange = html => {
        this.form.content = xss(html);
      };
      editor.customConfig.uploadImgServer = uploadFile;
      editor.customConfig.uploadImgHeaders = {
        accessToken: that.getStore("accessToken")
      };
      editor.customConfig.uploadFileName = "file";
      editor.customConfig.uploadImgHooks = {
        before: function(xhr, editor, files) {
        },
        success: function(xhr, editor, result) {
        },
        fail: function(xhr, editor, result) {
          that.$Message.error("上传图片失败");
        },
        error: function(xhr, editor) {
          that.$Message.error("上传图片出错");
        },
        timeout: function(xhr, editor) {
          that.$Message.error("上传图片超时");
        },
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
      editor.customConfig.fontNames = ["微软雅黑", "宋体", "黑体", "Arial"];
      editor.customConfig.emotions = [
        {
          title: "新浪",
          type: "image",
          content: sina
        }
      ];
      editor.create();
    },
    editHTML() {
      this.dataEdit = this.form.content;
      this.showHTMLModal = true;
    },
    editHTMLOk() {
      editor.txt.html(this.dataEdit);
      this.form.content = xss(this.dataEdit);
      this.showHTMLModal = false;
    },
    getDictMessageType() {
      getDictDataByType("message_type").then(res => {
        if (res.success) {
          this.dictMessageType = res.result;
        }
      });
    },
    getData() {
      this.loading = true;
      getMessageDataById(this.form.id).then(res => {
        this.loading = false;
        if (res.success === true) {
          // 转换null为""
          let v = res.result
          for (let attr in v) {
            if (v[attr] === null) {
              v[attr] = "";
            }
          }
          let str = JSON.stringify(v);
          let data = JSON.parse(str);
          editor.txt.html(data.content);
          this.form = data;
        }
      });
    },
    changeUserPage(v) {
      this.searchUserForm.pageNumber = v;
      this.getUserDataList();
    },
    changeUserPageSize(v) {
      this.searchUserForm.pageSize = v;
      this.getUserDataList();
    },
    getUserDataList() {
      this.userLoading = true;
      getUserListData(this.searchUserForm).then(res => {
        this.userLoading = false;
        if (res.success === true) {
          this.userData = res.result.content;
          this.totalUser = res.result.totalElements;
        }
      });
    },
    handleSearchUser() {
      this.searchUserForm.pageNumber = 1;
      this.searchUserForm.pageSize = 9;
      this.getUserDataList();
    },
    handleResetUser() {
      this.$refs.searchUserForm.resetFields();
      this.searchUserForm.pageNumber = 1;
      this.searchUserForm.pageSize = 9;
      // 重新加载数据
      this.getUserDataList();
    },
    handelSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true;
          if (this.type === 0) {
            // 添加 避免编辑后传入id等数据 记得删除
            delete this.form.id;
            // 用户id数据
            let ids = [];
            this.selectUsers.forEach(e => {
              ids += e.id + ",";
            });
            if (ids.length > 0) {
              ids = ids.substring(0, ids.length - 1);
            }
            this.form.userIds = ids;
            addMessage(this.form).then(res => {
              this.submitLoading = false;
              if (res.success === true) {
                this.$Message.success("操作成功");
                this.closeCurrentPage();
              }
            });
          } else if (this.type === 1) {
            // 编辑
            editMessage(this.form).then(res => {
              this.submitLoading = false;
              if (res.success === true) {
                this.$Message.success("操作成功");
                this.closeCurrentPage();
              }
            });
          }
        }
      });
    },
    changeRange(v) {
      if (v == 0) {
        this.showChooseUser = false;
        this.selectUserCount = this.totalUser;
      } else {
        this.showChooseUser = true;
        this.clearSelectAllUser();
      }
    },
    chooseUser(v) {
      // 去重
      let that = this;
      let flag = true;
      this.selectUsers.forEach(e => {
        if (v.id == e.id) {
          that.$Message.warning("刚才已经选择过啦，请勿重复选择");
          flag = false;
        }
      });
      if (flag) {
        let u = {
          id: v.id,
          username: v.username
        };
        this.selectUsers.push(u);
        this.selectUserCount += 1;
        this.$Message.success("选择成功");
      }
    },
    handleCancelUser(e, id) {
      // 删除所选用户
      let newArray = [];
      this.selectUsers.forEach(e => {
        if (id != e.id) {
          newArray.push(e);
        }
      });
      this.selectUsers = newArray;
      this.selectUserCount = newArray.length;
      this.$Message.success("删除所选用户成功");
    },
    clearSelectAllUser() {
      this.selectUserCount = 0;
      this.selectUsers = [];
    },
    handelCancel(){
      this.closeCurrentPage();
    },
    // 关闭当前页面
    closeCurrentPage() {
      this.$store.commit("removeTag", "add_edit_message");
      localStorage.pageOpenedList = JSON.stringify(
        this.$store.state.app.pageOpenedList
      );
      this.$router.push({
        name: this.backRoute
      });
    }
  },
  mounted() {
    this.init();
  }
};
</script>