<template>
  <a-modal
    :title="topic + '限流'"
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
          <a-form-item label="选择分区">
            <a-select
              mode="multiple"
              option-filter-prop="children"
              v-decorator="[
                'partitions',
                {
                  initialValue: [-1],
                  rules: [{ required: true, message: '请选择一个分区!' }],
                },
              ]"
              placeholder="请选择一个分区"
            >
              <a-select-option v-for="v in partitions" :key="v" :value="v">
                <span v-if="v == -1">全部</span> <span v-else>{{ v }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="操作">
            <a-radio-group
              v-decorator="[
                'operation',
                {
                  initialValue: 'ON',
                  rules: [{ required: true, message: '请选择一个操作!' }],
                },
              ]"
            >
              <a-radio value="ON"> 开启限流配置 </a-radio>
              <a-radio value="OFF"> 移除限流配置 </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>
        <hr />
        <h4>说明：</h4>
        该限流表示topic的副本的在不同broker之间数据同步占用带宽的限制，该配置是一个topic级别的配置项。如未配置速率，即使配置了这个限流也不会进行实际的限流操作。配置速率在
        <span style="color: red">运维->配置限流</span> 处进行操作.
        <h4>如何检查是否对哪些分区启用限流：</h4>
        topic的限流是通过下面这两项配置的：
        <ul>
          <li>leader.replication.throttled.replicas</li>
          <li>follower.replication.throttled.replicas</li>
        </ul>
        只需通过
        <strong>属性配置</strong>
        查看这两项配置的值，格式为："0:0,1:0"，左侧为分区，右侧为broker
        id。示例表示：[分区0的副本：在broker 0上，分区1的副本：在broker 0上]。
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "ConfigTopicThrottle",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    topic: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      show: this.visible,
      loading: false,
      form: this.$form.createForm(this, { name: "RemoveThrottleForm" }),
      partitions: [],
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
    handleCancel() {
      this.$emit("closeThrottleDialog", { refresh: false });
    },
    getPartitionInfo() {
      this.loading = true;
      request({
        url: KafkaTopicApi.getPartitionInfo.url + "?topic=" + this.topic,
        method: KafkaTopicApi.getPartitionInfo.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.partitions = res.data.map((e) => e.partition);
          this.partitions.splice(0, 0, -1);
        }
      });
    },
    ok() {
      this.form.validateFields((err, values) => {
        if (!err) {
          const data = Object.assign({}, values, {topic: this.topic});
          this.loading = true;
          request({
            url: KafkaTopicApi.configThrottle.url,
            method: KafkaTopicApi.configThrottle.method,
            data: data,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeThrottleDialog", { refresh: false });
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
