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
            <hr class="hr" />
            <a-row :gutter="24">
              <a-col :span="24">
                <a-form-item label="过滤消费组">
                  <a-input
                    placeholder="groupId 模糊过滤"
                    class="input-w"
                    v-decorator="['filterGroupId']"
                    @change="onFilterGroupIdUpdate"
                  />
                  <span>
                    仅过滤当前已查出来的消费组，如果要查询服务端最新消费组，请点击查询按钮</span
                  >
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>
        <div class="operation-row-button">
          <a-button
            type="primary"
            @click="openAddSubscriptionDialog"
            v-action:group:add
            >新增订阅</a-button
          >
        </div>
        <a-table
          :columns="columns"
          :data-source="filteredData"
          bordered
          row-key="groupId"
        >
          <div slot="members" slot-scope="text, record">
            <a href="#" @click="openConsumerMemberDialog(record.groupId)"
              >{{ text }}
            </a>
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
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                v-action:group:del
                >删除
              </a-button>
            </a-popconfirm>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openConsumerMemberDialog(record.groupId)"
              v-action:group:client
              >消费端
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openConsumerDetailDialog(record.groupId)"
              v-action:group:consumer-detail
              >消费详情
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openOffsetPartitionDialog(record.groupId)"
              v-action:group:offset-partition
              >位移分区
            </a-button>
          </div>
        </a-table>
        <Member
          :visible="showConsumerGroupDialog"
          :group="selectDetail.resourceName"
          @closeConsumerMemberDialog="closeConsumerDialog"
        ></Member>
        <ConsumerDetail
          :visible="showConsumerDetailDialog"
          :group="selectDetail.resourceName"
          @closeConsumerDetailDialog="closeConsumerDetailDialog"
        >
        </ConsumerDetail>
        <AddSupscription
          :visible="showAddSubscriptionDialog"
          @closeAddSubscriptionDialog="closeAddSubscriptionDialog"
        >
        </AddSupscription>
        <OffsetTopicPartition
          :visible="showOffsetPartitionDialog"
          :group="selectDetail.resourceName"
          @closeOffsetPartitionDialog="closeOffsetPartitionDialog"
        ></OffsetTopicPartition>
      </div>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaConsumerApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import Member from "@/views/group/Member";
import ConsumerDetail from "@/views/group/ConsumerDetail";
import AddSupscription from "@/views/group/AddSupscription";
import OffsetTopicPartition from "@/views/group/OffsetTopicPartition";
import { isAuthorized } from "@/utils/auth";

export default {
  name: "ConsumerGroup",
  components: { Member, ConsumerDetail, AddSupscription, OffsetTopicPartition },
  data() {
    return {
      queryParam: {},
      data: [],
      filteredData: [],
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
      showConsumerGroupDialog: false,
      showConsumerDetailDialog: false,
      showAddSubscriptionDialog: false,
      showOffsetPartitionDialog: false,
      filterGroupId: "",
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
        if (res.code == 0) {
          this.data = res.data.list;
          this.filter();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
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
    openConsumerMemberDialog(groupId) {
      if (!isAuthorized("group:client")) {
        return;
      }
      this.showConsumerGroupDialog = true;
      this.selectDetail.resourceName = groupId;
    },
    closeConsumerDialog() {
      this.showConsumerGroupDialog = false;
    },
    openConsumerDetailDialog(groupId) {
      this.showConsumerDetailDialog = true;
      this.selectDetail.resourceName = groupId;
    },
    closeConsumerDetailDialog() {
      this.showConsumerDetailDialog = false;
    },
    openAddSubscriptionDialog() {
      this.showAddSubscriptionDialog = true;
    },
    closeAddSubscriptionDialog(res) {
      this.showAddSubscriptionDialog = false;
      if (res.refresh) {
        this.getConsumerGroupList();
      }
    },
    openOffsetPartitionDialog(groupId) {
      this.showOffsetPartitionDialog = true;
      this.selectDetail.resourceName = groupId;
    },
    closeOffsetPartitionDialog() {
      this.showOffsetPartitionDialog = false;
    },
    onFilterGroupIdUpdate(input) {
      this.filterGroupId = input.target.value;
      this.filter();
    },
    filter() {
      if (this.filterGroupId) {
        this.filteredData = this.data.filter(
          (e) => e.groupId.indexOf(this.filterGroupId) != -1
        );
      } else {
        this.filteredData = this.data;
      }
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
  margin-bottom: 8px;
}

.operation-btn {
  margin-right: 3%;
}

.type-select {
  width: 200px !important;
}
.hr {
  height: 1px;
  border: none;
  border-top: 1px dashed #0066cc;
}
</style>
