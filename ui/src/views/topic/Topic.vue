<template>
  <div class="content">
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
        <a-button type="primary" @click="handleReset">新增/更新</a-button>
      </div>
      <a-table :columns="columns" :data-source="data" bordered>
        <div slot="partitions" slot-scope="text">
          <a href="#">{{ text }} </a>
        </div>

        <div slot="internal" slot-scope="text">
          <span v-if="text">是</span><span v-else>否</span>
        </div>

        <div
          slot="operation"
          slot-scope="record"
          v-show="!record.user || record.user.role != 'admin'"
        >
          <a-popconfirm
            :title="'删除用户: ' + record.username + '及相关权限？'"
            ok-text="确认"
            cancel-text="取消"
            @confirm="handleReset(record)"
          >
            <a-button size="small" href="javascript:;" class="operation-btn"
              >删除</a-button
            >
          </a-popconfirm>
        </div>
      </a-table>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
export default {
  name: "Topic",
  components: {},
  data() {
    return {
      queryParam: {},
      data: [],
      columns,
      selectRow: {},
      form: this.$form.createForm(this, { name: "advanced_search" }),
      showUpdateUser: false,
      deleteUserConfirm: false,
      selectDetail: {
        resourceName: "",
        resourceType: "",
        username: "",
      },
    };
  },
  methods: {
    handleSearch(e) {
      e.preventDefault();
      this.form.validateFields((error, values) => {
        let queryParam = {};
        if (values.username) {
          queryParam.username = values.username;
        }
        if (values.topic) {
          queryParam.resourceType = "TOPIC";
          queryParam.resourceName = values.topic;
        } else if (values.groupId) {
          queryParam.resourceType = "GROUP";
          queryParam.resourceName = values.groupId;
        }
        Object.assign(this.queryParam, queryParam);
      });
    },

    handleReset() {
      this.form.resetFields();
    },

    getTopicList() {
      request({
        url: KafkaTopicApi.getTopicList.url,
        method: KafkaTopicApi.getTopicList.method,
      }).then((res) => {
        this.data = res.data;
      });
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
</style>
