<template>
  <a-modal
    title="位移主题分区"
    :visible="show"
    :width="800"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        {{ group }}提交位移到位移主题的[{{ data }}]分区
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaConsumerApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";

export default {
  name: "OffsetTopicPartition",
  props: {
    group: {
      type: String,
      default: "",
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: this.visible,
      data: [],
      loading: false,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getOffsetPartition();
      }
    },
  },
  methods: {
    getOffsetPartition() {
      this.loading = true;
      request({
        url: KafkaConsumerApi.getOffsetPartition.url + "?groupId=" + this.group,
        method: KafkaConsumerApi.getOffsetPartition.method,
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
      this.$emit("closeOffsetPartitionDialog", {});
    },
  },
};
</script>

<style scoped></style>
