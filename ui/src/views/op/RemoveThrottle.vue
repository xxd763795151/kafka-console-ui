<template>
  <a-modal
    title="解除限流"
    :visible="show"
    :width="1000"
    :mask="false"
    :maskClosable="false"
    okText="确认"
    cancelText="取消"
    :destroyOnClose="true"
    @cancel="handleCancel"
    @ok="ok"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :form="form"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
        >
          <a-form-item label="Broker">
            <a-select
              mode="multiple"
              option-filter-prop="children"
              v-decorator="[
                'brokerList',
                {
                  initialValue: brokers,
                  rules: [{ required: true, message: '请选择一个broker!' }],
                },
              ]"
              placeholder="请选择一个broker"
            >
              <a-select-option v-for="v in brokers" :key="v" :value="v">
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
        <hr />
        <h4>如何检查是否配置的有限流速率：</h4>
        kafka的限流速率是通过下面这两项配置的：
        <ul>
          <li>leader.replication.throttled.rate</li>
          <li>follower.replication.throttled.rate</li>
        </ul>
        只需通过
        <strong>集群->属性配置</strong>
        查看是否存在这两项配置，如果不存在，便是没有配置限流速率。如果未配置限流速率，即使指定某个topic的分区副本进行限流，没有速率也不限流。
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi, KafkaOpApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "RemoveThrottle",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: this.visible,
      loading: false,
      form: this.$form.createForm(this, { name: "RemoveThrottleForm" }),
      brokers: [],
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getClusterInfo();
      }
    },
  },
  methods: {
    handleCancel() {
      this.$emit("closeRemoveThrottleDialog", { refresh: false });
    },
    getClusterInfo() {
      this.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfo.url,
        method: KafkaClusterApi.getClusterInfo.method,
      }).then((res) => {
        this.loading = false;
        this.brokers = [];
        res.data.nodes.forEach((node) => this.brokers.push(node.id));
      });
    },
    ok() {
      this.form.validateFields((err, values) => {
        if (!err) {
          const data = Object.assign({}, values);
          this.loading = true;
          request({
            url: KafkaOpApi.removeThrottle.url,
            method: KafkaOpApi.removeThrottle.method,
            data: data,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeRemoveThrottleDialog", { refresh: false });
            } else {
              notification.error({
                message: "error",
                description: res.msg,
              });
            }
          });
        }
      });
    },
  },
};
</script>

<style scoped></style>
