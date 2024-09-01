<template>
  <div class="home">
    <a-card title="控制台默认配置" class="card-style">
      <p v-for="(v, k) in config" :key="k">{{ k }}={{ v }}</p>
    </a-card>
    <p></p>
    <hr />
    <h3>kafka API 版本兼容性</h3>
    <div class="green">
      broker版本说明:
      该值并不保证broker实际版本一定是该值(大概是这个版本范围)，broker使用不同的模式(如kraft)可能显示不同的值
    </div>
    <a-spin :spinning="apiVersionInfoLoading">
      <a-table
        :columns="columns"
        :data-source="brokerApiVersionInfo"
        bordered
        row-key="brokerId"
      >
        <div slot="operation" slot-scope="record">
          <a-button
            size="small"
            href="javascript:;"
            class="operation-btn"
            @click="openApiVersionInfoDialog(record)"
            >详情
          </a-button>
        </div>
      </a-table>
    </a-spin>
    <VersionInfo
      :version-info="apiVersionInfo"
      :visible="showApiVersionInfoDialog"
      @closeApiVersionInfoDialog="closeApiVersionInfoDialog"
    >
    </VersionInfo>
  </div>
</template>

<script>
// @ is an alias to /src
import request from "@/utils/request";
import { KafkaConfigApi, KafkaClusterApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import VersionInfo from "@/views/home/VersionInfo";
export default {
  name: "Home",
  components: { VersionInfo },
  data() {
    return {
      config: {},
      columns,
      brokerApiVersionInfo: [],
      showApiVersionInfoDialog: false,
      apiVersionInfo: [],
      apiVersionInfoLoading: false,
    };
  },
  methods: {
    openApiVersionInfoDialog(record) {
      this.apiVersionInfo = record.versionInfo;
      this.showApiVersionInfoDialog = true;
    },
    closeApiVersionInfoDialog() {
      this.showApiVersionInfoDialog = false;
    },
  },

  created() {
    request({
      url: KafkaConfigApi.getConfig.url,
      method: KafkaConfigApi.getConfig.method,
    }).then((res) => {
      if (res.code == 0) {
        this.config = res.data;
      } else {
        notification.error({
          message: "error",
          description: res.msg,
        });
      }
    });
    this.apiVersionInfoLoading = true;
    request({
      url: KafkaClusterApi.getBrokerApiVersionInfo.url,
      method: KafkaClusterApi.getBrokerApiVersionInfo.method,
    }).then((res) => {
      this.apiVersionInfoLoading = false;
      if (res.code == 0) {
        this.brokerApiVersionInfo = res.data;
      } else {
        notification.error({
          message: "error",
          description: res.msg,
        });
      }
    });
  },
};
const columns = [
  {
    title: "id",
    dataIndex: "brokerId",
    key: "brokerId",
  },
  {
    title: "地址",
    dataIndex: "host",
    key: "host",
  },
  {
    title: "broker版本",
    dataIndex: "brokerVersion",
    key: "brokerVersion",
  },
  {
    title: "支持的api数量",
    dataIndex: "supportNums",
    key: "supportNums",
  },
  {
    title: "不支持的api数量",
    dataIndex: "unSupportNums",
    key: "unSupportNums",
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
  },
];
</script>
<style scoped>
.card-style {
  width: 100%;
}
.green {
  color: green;
}
</style>
