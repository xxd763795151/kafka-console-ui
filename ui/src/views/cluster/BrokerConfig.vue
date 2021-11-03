<template>
  <a-modal
    title="Broker配置"
    :visible="show"
    :width="1200"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="data"
          bordered
          :rowKey="(record) => record.memberId"
        >
          <ul slot="partitions" slot-scope="text">
            <ol v-for="i in text" :key="i.topic + i.partition">
              {{
                i.topic
              }}:
              {{
                i.partition
              }}
            </ol>
          </ul>
        </a-table>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaConfigApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "BrokerConfig",
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
  },
  data() {
    return {
      columns: columns,
      show: this.visible,
      data: [],
      loading: false,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getPartitionInfo();
      }
    },
  },
  methods: {
    getPartitionInfo() {
      this.loading = true;
      request({
        url: KafkaConfigApi.getBrokerConfig.url + "?brokerId=" + this.id,
        method: KafkaConfigApi.getBrokerConfig.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.data = res.data;
        }
      });
    },
    handleCancel() {
      this.data = [];
      this.$emit("closeBrokerConfigDialog", {});
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
];
</script>

<style scoped></style>
