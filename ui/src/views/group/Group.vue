<template>
  <div class="content">
    <a-spin :spinning="loading">
      <div class="topic">
        <div id="form-consumer-group-advanced-search">
          <a-form
            class="ant-advanced-search-form"
            :form="form"
            @submit="handleSearch"
          >
            <a-row :gutter="24">
              <a-col :span="8">
                <a-form-item :label="`消费组`">
                  <a-input
                    placeholder="groupId"
                    class="input-w"
                    v-decorator="['groupId']"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="`状态`">
                  <a-checkbox-group
                    v-decorator="['states']"
                    style="width: 100%"
                  >
                    <a-row>
                      <a-col :span="8">
                        <a-checkbox value="Empty"> Empty</a-checkbox>
                      </a-col>
                      <a-col :span="8">
                        <a-checkbox value="PreparingRebalance">
                          PreparingRebalance
                        </a-checkbox>
                      </a-col>
                      <a-col :span="8">
                        <a-checkbox value="CompletingRebalance">
                          CompletingRebalance
                        </a-checkbox>
                      </a-col>
                      <a-col :span="8">
                        <a-checkbox value="Stable"> Stable</a-checkbox>
                      </a-col>
                      <a-col :span="8">
                        <a-checkbox value="Dead"> Dead</a-checkbox>
                      </a-col>
                    </a-row>
                  </a-checkbox-group>
                </a-form-item>
              </a-col>

              <a-col :span="4" :style="{ textAlign: 'right' }">
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
        <a-table
          :columns="columns"
          :data-source="data"
          bordered
          row-key="groupId"
        >
          <div slot="members" slot-scope="text">
            <a href="#">{{ text }} </a>
          </div>

          <div slot="state" slot-scope="text">
            {{ text }}
            <!--          <span v-if="text" style="color: red">是</span><span v-else>否</span>-->
          </div>

          <div slot="operation" slot-scope="record" v-show="!record.internal">
            <a-popconfirm
              :title="'删除消费组: ' + record.groupId + '？'"
              ok-text="确认"
              cancel-text="取消"
              @confirm="deleteGroup(record.groupId)"
            >
              <a-button size="small" href="javascript:;" class="operation-btn"
                >删除
              </a-button>
            </a-popconfirm>
          </div>
        </a-table>
      </div>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaConsumerApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "ConsumerGroup",
  components: {},
  data() {
    return {
      queryParam: {},
      data: [],
      columns,
      selectRow: {},
      form: this.$form.createForm(this, {
        name: "consumer_group_advanced_search",
      }),
      showUpdateUser: false,
      deleteUserConfirm: false,
      selectDetail: {
        resourceName: "",
        resourceType: "",
        username: "",
      },
      loading: false,
    };
  },
  methods: {
    handleSearch(e) {
      e.preventDefault();
      this.getConsumerGroupList();
    },

    handleReset() {
      this.form.resetFields();
    },

    getConsumerGroupList() {
      Object.assign(this.queryParam, this.form.getFieldsValue());
      this.loading = true;
      request({
        url: KafkaConsumerApi.getConsumerGroupList.url,
        method: KafkaConsumerApi.getConsumerGroupList.method,
        data: this.queryParam,
      }).then((res) => {
        this.loading = false;
        this.data = res.data.list;
      });
    },
    deleteGroup(group) {
      this.loading = true;
      request({
        url: KafkaConsumerApi.deleteConsumerGroup.url + "?groupId=" + group,
        method: KafkaConsumerApi.deleteConsumerGroup.method,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.getConsumerGroupList();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
  },
  created() {
    this.getConsumerGroupList();
  },
};

const columns = [
  {
    title: "消费组",
    dataIndex: "groupId",
    key: "groupId",
    width: 300,
  },
  {
    title: "消费端数量",
    dataIndex: "members",
    key: "members",
    slots: { title: "members" },
    scopedSlots: { customRender: "members" },
  },
  {
    title: "当前状态",
    dataIndex: "state",
    key: "state",
    slots: { title: "state" },
    scopedSlots: { customRender: "state" },
  },
  {
    title: "分区分配器",
    dataIndex: "partitionAssignor",
    key: "partitionAssignor",
  },
  {
    title: "协调者节点",
    dataIndex: "coordinator",
    key: "coordinator",
  },
  // {
  //   title: "授权操作数量",
  //   dataIndex: "authorizedOperations",
  //   key: "authorizedOperations",
  // },
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

#form-consumer-group-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#form-consumer-group-advanced-search .search-result-list {
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
