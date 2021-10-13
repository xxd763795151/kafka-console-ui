<template>
  <div class="content">
    <a-spin :spinning="loading">
      <div class="topic">
        <div id="components-form-topic-advanced-search">
          <a-form
            class="ant-advanced-search-form"
            :form="form"
            @submit="handleSearch"
          >
            <a-row :gutter="24">
              <a-col :span="8">
                <a-form-item :label="`topic`">
                  <a-input
                    placeholder="topic"
                    class="input-w"
                    v-decorator="['topic']"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="`类型`">
                  <a-select
                    class="type-select"
                    v-decorator="['type', { initialValue: 'normal' }]"
                    placeholder="Please select a country"
                  >
                    <a-select-option value="all"> 所有</a-select-option>
                    <a-select-option value="normal"> 普通</a-select-option>
                    <a-select-option value="system"> 系统</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="8" :style="{ textAlign: 'right' }">
                <a-form-item>
                  <a-button type="primary" html-type="submit"> 搜索</a-button>
                  <a-button :style="{ marginLeft: '8px' }" @click="handleReset">
                    重置
                  </a-button>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>
        <div class="operation-row-button">
          <a-button type="primary" @click="openCreateTopicDialog"
            >新增</a-button
          >
        </div>
        <a-table :columns="columns" :data-source="data" bordered row-key="name">
          <div slot="partitions" slot-scope="text, record">
            <a href="#" @click="openPartitionInfoDialog(record.name)"
              >{{ text }}
            </a>
          </div>

          <div slot="internal" slot-scope="text">
            <span v-if="text" style="color: red">是</span><span v-else>否</span>
          </div>

          <div slot="operation" slot-scope="record" v-show="!record.internal">
            <a-popconfirm
              :title="'删除topic: ' + record.name + '？'"
              ok-text="确认"
              cancel-text="取消"
              @confirm="deleteTopic(record.name)"
            >
              <a-button size="small" href="javascript:;" class="operation-btn"
                >删除
              </a-button>
            </a-popconfirm>
          </div>
        </a-table>
        <PartitionInfo
          :topic="selectDetail.resourceName"
          :visible="showPartitionInfo"
          @closePartitionInfoDialog="closePartitionInfoDialog"
        ></PartitionInfo>
        <CreateTopic
          :visible="showCreateTopic"
          @closeCreateTopicDialog="closeCreateTopicDialog"
        >
        </CreateTopic>
      </div>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import PartitionInfo from "@/views/topic/PartitionInfo";
import CreateTopic from "@/views/topic/CreateTopic";

export default {
  name: "Topic",
  components: { PartitionInfo, CreateTopic },
  data() {
    return {
      queryParam: { type: "normal" },
      data: [],
      columns,
      selectRow: {},
      form: this.$form.createForm(this, { name: "topic_advanced_search" }),
      showUpdateUser: false,
      deleteUserConfirm: false,
      selectDetail: {
        resourceName: "",
        resourceType: "",
        username: "",
      },
      showPartitionInfo: false,
      loading: false,
      showCreateTopic: false,
    };
  },
  methods: {
    handleSearch(e) {
      e.preventDefault();
      this.getTopicList();
    },

    handleReset() {
      this.form.resetFields();
    },

    getTopicList() {
      Object.assign(this.queryParam, this.form.getFieldsValue());
      this.loading = true;
      request({
        url: KafkaTopicApi.getTopicList.url,
        method: KafkaTopicApi.getTopicList.method,
        params: this.queryParam,
      }).then((res) => {
        this.loading = false;
        this.data = res.data;
      });
    },
    deleteTopic(topic) {
      request({
        url: KafkaTopicApi.deleteTopic.url + "?topic=" + topic,
        method: KafkaTopicApi.deleteTopic.method,
      }).then((res) => {
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.getTopicList();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    openPartitionInfoDialog(topic) {
      this.selectDetail.resourceName = topic;
      this.showPartitionInfo = true;
    },
    closePartitionInfoDialog() {
      this.showPartitionInfo = false;
    },
    openCreateTopicDialog() {
      this.showCreateTopic = true;
    },
    closeCreateTopicDialog(res) {
      this.showCreateTopic = false;
      if (res.refresh) {
        this.getTopicList();
      }
    },
  },
  created() {
    this.getTopicList();
  },
};

const columns = [
  {
    title: "topic",
    dataIndex: "name",
    key: "name",
    width: 300,
  },
  {
    title: "分区数",
    dataIndex: "partitions",
    key: "partitions",
    slots: { title: "partitions" },
    scopedSlots: { customRender: "partitions" },
  },
  {
    title: "内部topic",
    dataIndex: "internal",
    key: "internal",
    slots: { title: "internal" },
    scopedSlots: { customRender: "internal" },
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
    width: 500,
  },
];
</script>

<style scoped>
.topic {
  width: 100%;
  height: 100%;
}

.ant-advanced-search-form {
  padding: 24px;
  background: #fbfbfb;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.ant-advanced-search-form .ant-form-item {
  display: flex;
}

.ant-advanced-search-form .ant-form-item-control-wrapper {
  flex: 1;
}

#components-form-topic-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#components-form-topic-advanced-search .search-result-list {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}

.input-w {
  width: 400px;
}

.operation-row-button {
  height: 4%;
  text-align: left;
}

.operation-btn {
  margin-right: 3%;
}

.type-select {
  width: 200px !important;
}
</style>
