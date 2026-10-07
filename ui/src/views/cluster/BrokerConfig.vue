<template>
  <a-modal
    title="Broker配置"
    :open="show"
    :width="1400"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div>
          <a-input-search
            placeholder="属性"
            style="width: 200px"
            v-model:value="search"
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
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'operation'">
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
                  type="primary"
                  danger
                  v-action:cluster:edit
                  >删除
                </a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
        <EditConfig
          :open="showEditConfigDialog"
          :record="selectData"
          :broker-id="id"
          :is-logger-config="isLoggerConfig"
          @closeEditConfigDialog="closeEditConfigDialog"
        ></EditConfig>
      </a-spin>
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import EditConfig from "@/views/cluster/EditConfig.vue";

interface ConfigRecord {
  name: string;
  value: string;
  source: string;
  readOnly: boolean;
}

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
    width: 150,
  },
];

export default defineComponent({
  name: "BrokerConfig",
  components: { EditConfig },
  props: {
    group: {
      type: String,
      default: "",
    },
    open: {
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
      show: this.open,
      data: [] as ConfigRecord[],
      loading: false,
      search: "",
      filterData: [] as ConfigRecord[],
      showEditConfigDialog: false,
      selectData: {} as ConfigRecord,
    };
  },
  watch: {
    open(v: boolean) {
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
      }).then((res: any) => {
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
    deleteBrokerConfig(record: ConfigRecord) {
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
      }).then((res: any) => {
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
    openEditConfigDialog(record: ConfigRecord) {
      this.showEditConfigDialog = true;
      this.selectData = record;
    },
    closeEditConfigDialog(params: { refresh: boolean }) {
      this.showEditConfigDialog = false;
      if (params.refresh) {
        this.getBrokerConfig();
      }
    },
    isDynamic(source: string) {
      return source.startsWith("DYNAMIC_");
    },
  },
});
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>
