<template>
  <div class="content">
    <a-spin :spinning="loading">
      <div class="body-c">
        <div class="cluster-id">
          <h3>集群ID：{{ clusterId }}</h3>
        </div>

        <a-table :columns="columns" :data-source="data" bordered row-key="id">
          <div slot="addr" slot-scope="text, record">
            {{ record.host }}:{{ record.port }}
          </div>
          <div slot="controller" slot-scope="text">
            <span v-if="text" style="color: red">是</span><span v-else>否</span>
          </div>
          <div slot="operation" slot-scope="record" v-show="!record.internal">
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openBrokerConfigDialog(record)"
              >属性配置
            </a-button>
          </div>
        </a-table>
      </div>
      <BrokerConfig
        :visible="showBrokerConfigDialog"
        :id="this.select.id"
        @closeBrokerConfigDialog="closeBrokerConfigDialog"
      ></BrokerConfig>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi } from "@/utils/api";
import BrokerConfig from "@/views/cluster/BrokerConfig";

export default {
  name: "Topic",
  components: { BrokerConfig },
  data() {
    return {
      data: [],
      columns,
      loading: false,
      clusterId: "",
      showBrokerConfigDialog: false,
      select: {},
    };
  },
  methods: {
    getClusterInfo() {
      this.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res) => {
        this.loading = false;
        this.data = res.data.nodes;
        this.clusterId = res.data.clusterId;
      });
    },
    openBrokerConfigDialog(record) {
      this.select = record;
      this.showBrokerConfigDialog = true;
    },
    closeBrokerConfigDialog() {
      this.showBrokerConfigDialog = false;
    },
  },
  created() {
    this.getClusterInfo();
  },
};

const columns = [
  {
    title: "id",
    dataIndex: "id",
    key: "id",
  },
  {
    title: "地址",
    key: "addr",
    scopedSlots: { customRender: "addr" },
  },
  {
    title: "控制器",
    key: "controller",
    dataIndex: "controller",
    scopedSlots: { customRender: "controller" },
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];
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
