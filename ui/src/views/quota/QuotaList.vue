<template>
  <div>
    <a-spin :spinning="loading">
      <a-table
          :columns="columns"
          :data-source="data"
          bordered
          :row-key="
        (record, index) => {
          return index;
        }
      "
          @change="handleChange"
      >
        <div slot="client" slot-scope="text">
          <span v-if="text">{{ text }}</span><span v-else style="color: red">默认配置</span>
        </div>
        <div slot="user" slot-scope="text">
          <span v-if="text">{{ text }}</span><span v-else style="color: red">默认配置</span>
        </div>

        <div slot="operation" slot-scope="record">
          <a-popconfirm
              :title="'删除当前配置？'"
              ok-text="确认"
              cancel-text="取消"
              @confirm="deleteConfig(record)"
          >
            <a-button size="small" href="javascript:;" class="operation-btn"
            >删除
            </a-button>
          </a-popconfirm>
          <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openUpdateDialog(record)"
          >修改
          </a-button>
        </div>
      </a-table>
      <UpdateQuotaConfig :type="type" :record="selectRow" :visible="showUpdateDialog"
                         @closeUpdateQuotaDialog="closeUpdateQuotaDialog"></UpdateQuotaConfig>
    </a-spin>
  </div>
</template>

<script>
import {KafkaClientQuotaApi} from "@/utils/api";
import request from "@/utils/request";
import notification from "ant-design-vue/lib/notification";
import UpdateQuotaConfig from "@/views/quota/UpdateQuotaConfig.vue";

export default {
  name: "QuotaList",
  components: {UpdateQuotaConfig},
  props: {
    columns: {
      type: Array,
    },
    data: {
      type: Array,
    },
    type: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      record: {},
      sortedInfo: null,
      loading: false,
      selectRow: {},
      showUpdateDialog: false,
    };
  },
  methods: {
    openDetailDialog(record) {
      this.record = record;
      this.showDetailDialog = true;
    },
    closeDetailDialog() {
      this.showDetailDialog = false;
    },
    handleChange() {
      this.sortedInfo = arguments[2];
    },
    deleteConfig(record) {
      this.loading = true;
      const params = {type: this.type};
      params.types = [];
      if (this.type == "user") {
        params.types.push("user");
        if (record.user) {
          params.names = [record.user.trim()];
        } else {
          params.names = [""];
        }
      } else if (this.type == "client-id") {
        params.types.push("client-id");
        if (record.client) {
          params.names = [record.client.trim()];
        } else {
          params.names = [""];
        }
      }
      if (this.type == "ip") {
        params.types.push("ip");
        if (record.ip) {
          params.names = [record.ip.trim()];
        } else {
          params.names = [""];
        }
      }
      request({
        url: KafkaClientQuotaApi.deleteClientQuotaConfigs.url,
        method: KafkaClientQuotaApi.deleteClientQuotaConfigs.method,
        data: params,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.$emit("refreshQuotaList");
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    openUpdateDialog(record) {
      this.selectRow = record;
      this.showUpdateDialog = true;
    },
    closeUpdateQuotaDialog(event) {
      this.selectRow = {};
      this.showUpdateDialog = false;
      if (event.refresh) {
        this.$emit("refreshQuotaList");
      }
    },
  },
  created() {
    this.columns.push({
      title: "操作",
      key: "operation",
      scopedSlots: {customRender: "operation"},
    });
  },
};
</script>

<style scoped>
.operation-btn {
  margin-right: 3%;
}
</style>
