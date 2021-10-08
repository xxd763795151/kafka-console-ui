<template>
  <div class="content">
    <a-spin :spinning="loading">
      <div class="body-c">
        <div class="cluster-id">
          <h3>集群ID：{{ clusterId }}</h3>
        </div>

        <a-table :columns="columns" :data-source="data" bordered row-key="name">
          <div slot="addr" slot-scope="text, record">
            {{ record.host }}:{{ record.port }}
          </div>
          <div slot="controller" slot-scope="text">
            <span v-if="text" style="color: red">是</span><span v-else>否</span>
          </div>
        </a-table>
      </div>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi } from "@/utils/api";

export default {
  name: "Topic",
  data() {
    return {
      data: [],
      columns,
      loading: false,
      clusterId: "",
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
