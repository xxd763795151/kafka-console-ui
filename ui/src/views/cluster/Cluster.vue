<template>
  <div class="content">
    <a-spin :spinning="loading">
      <div class="body-c">
        <div class="cluster-id">
          <h3>集群ID：{{ clusterId }}</h3>
        </div>

        <a-table :columns="columns" :data-source="data" bordered row-key="id">
          <template #bodyCell="{ column, text, record }">
            <template v-if="column.key === 'addr'">
              {{ record.host }}:{{ record.port }}
            </template>
            <template v-else-if="column.key === 'controller'">
              <span v-if="text" style="color: red">是</span><span v-else>否</span>
            </template>
            <template v-else-if="column.key === 'operation'">
              <div v-show="!record.internal">
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openBrokerConfigDialog(record, false)"
                  v-action:cluster:property-config
                  >属性配置
                </a-button>
                <a-button
                  size="small"
                  href="javascript:;"
                  class="operation-btn"
                  @click="openBrokerConfigDialog(record, true)"
                  v-action:cluster:log-config
                  >日志配置
                </a-button>
              </div>
            </template>
          </template>
        </a-table>
      </div>
      <BrokerConfig
        :open="showBrokerConfigDialog"
        :id="select.idString"
        :is-logger-config="isLoggerConfig"
        @closeBrokerConfigDialog="closeBrokerConfigDialog"
      ></BrokerConfig>
    </a-spin>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import request from "@/utils/request";
import { KafkaClusterApi } from "@/utils/api";
import BrokerConfig from "@/views/cluster/BrokerConfig.vue";
import notification from "ant-design-vue/lib/notification";

interface NodeRecord {
  id: number;
  host: string;
  port: number;
  controller: boolean;
  internal: boolean;
  idString: string;
}

const columns = [
  {
    title: "id",
    dataIndex: "id",
    key: "id",
  },
  {
    title: "地址",
    key: "addr",
  },
  {
    title: "控制器",
    key: "controller",
    dataIndex: "controller",
  },
  {
    title: "操作",
    key: "operation",
  },
];

export default defineComponent({
  name: "Cluster",
  components: { BrokerConfig },
  data() {
    return {
      data: [] as NodeRecord[],
      columns,
      loading: false,
      clusterId: "",
      showBrokerConfigDialog: false,
      select: {} as NodeRecord,
      isLoggerConfig: false,
    };
  },
  methods: {
    getClusterInfo() {
      this.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res: any) => {
        this.loading = false;
        if (res.code == 0) {
          this.data = res.data.nodes;
          this.clusterId = res.data.clusterId;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    openBrokerConfigDialog(record: NodeRecord, isLoggerConfig: boolean) {
      this.select = record;
      this.showBrokerConfigDialog = true;
      this.isLoggerConfig = isLoggerConfig;
    },
    closeBrokerConfigDialog() {
      this.showBrokerConfigDialog = false;
    },
  },
  created() {
    this.getClusterInfo();
  },
});
</script>

<style scoped>
.body-c {
  width: 100%;
  height: 100%;
}

.cluster-id {
  text-align: left;
}

.operation-row-button {
  height: 4%;
  text-align: left;
}

.operation-btn {
  margin-right: 3%;
}
</style>
