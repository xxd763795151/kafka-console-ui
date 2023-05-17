<template>
  <a-modal
    title="Broker配置"
    :visible="show"
    :width="1400"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div>
          <a-input-search
            placeholder="属性"
            style="width: 200px"
            v-model="search"
            @input="searchData"
            @search="searchData"
          />
          <br /><br />
        </div>

        <a-table
          :columns="columns"
          :data-source="filterData"
          bordered
          :rowKey="(record) => record.name"
        >
          <div slot="operation" slot-scope="record">
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              v-show="!record.readOnly"
              @click="openEditConfigDialog(record)"
              v-action:cluster:edit
              >编辑
            </a-button>
            <a-popconfirm
              :title="'删除配置项: ' + record.name + '？'"
              ok-text="确认"
              cancel-text="取消"
              v-show="isDynamic(record.source)"
              @confirm="deleteBrokerConfig(record)"
            >
              <a-button
                size="small"
                href="javascript:;"
                class="operation-btn"
                v-action:cluster:edit
                >删除
              </a-button>
            </a-popconfirm>
          </div>
        </a-table>
        <EditConfig
          :visible="showEditConfigDialog"
          :record="selectData"
          :broker-id="id"
          :is-logger-config="isLoggerConfig"
          @closeEditConfigDialog="closeEditConfigDialog"
        ></EditConfig>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import EditConfig from "@/views/cluster/EditConfig";

export default {
  name: "BrokerConfig",
  components: { EditConfig },
  props: {
    group: {
      type: String,
      default: "",
    },
    visible: {
      type: Boolean,
      default: false,
    },
    id: {
      type: String,
      default: "",
    },
    isLoggerConfig: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      columns: columns,
      show: this.visible,
      data: [],
      loading: false,
      search: "",
      filterData: [],
      showEditConfigDialog: false,
      selectData: {},
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getBrokerConfig();
      }
    },
  },
  methods: {
    getBrokerConfig() {
      this.loading = true;
      const api = this.isLoggerConfig
        ? KafkaConfigApi.getBrokerLoggerConfig
        : KafkaConfigApi.getBrokerConfig;
      request({
        url: api.url + "?brokerId=" + this.id,
        method: api.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.data = res.data;
          this.searchData();
        }
      });
    },
    deleteBrokerConfig(record) {
      this.selectData = record;
      this.loading = true;
      const api = this.isLoggerConfig
        ? KafkaConfigApi.deleteBrokerLoggerConfig
        : KafkaConfigApi.deleteBrokerConfig;
      request({
        url: api.url,
        method: api.method,
        data: {
          name: record.name,
          value: record.value,
          entity: this.id,
        },
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.getBrokerConfig();
        }
      });
    },
    searchData() {
      this.filterData = this.data.filter(
        (e) => e.name.indexOf(this.search) >= 0
      );
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeBrokerConfigDialog", {});
    },
    openEditConfigDialog(record) {
      this.showEditConfigDialog = true;
      this.selectData = record;
    },
    closeEditConfigDialog(params) {
      this.showEditConfigDialog = false;
      if (params.refresh) {
        this.getBrokerConfig();
      }
    },
    isDynamic(source) {
      return source.startsWith("DYNAMIC_");
    },
  },
};

const columns = [
  {
    title: "属性",
    dataIndex: "name",
    key: "name",
    width: 300,
  },
  {
    title: "值",
    dataIndex: "value",
    key: "value",
  },
  {
    title: "属性源",
    dataIndex: "source",
    key: "source",
    width: 200,
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
    width: 150,
  },
];
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>
