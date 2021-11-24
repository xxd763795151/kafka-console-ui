<template>
  <a-modal
    title="限流配置"
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
          <a-form-item label="带宽">
            <a-input-number
              :min="1"
              :max="1024"
              v-decorator="[
                'throttle',
                {
                  initialValue: 1,
                  rules: [{ required: true, message: '输入带宽!' }],
                },
              ]"
            />
            <a-select default-value="MB" v-model="unit" style="width: 100px">
              <a-select-option value="MB"> MB/s </a-select-option>
              <a-select-option value="KB"> KB/s </a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
        <hr />
        <div><h4>注意：</h4></div>
        <ul>
          <li>该限速带宽，指的是broker之间副本进行同步时占用的带宽</li>
          <li>该配置是broker级别配置，是针对broker上topic的副本</li>
          <li>
            在当前页面对指定broker限流配置后，并不是说设置后该broker上的所有topic副本同步就被限制为当前流速了。这仅仅是速率设置，如果需要对某topic的副本同步进行限流，还需要去
            Topic->限流 处操作，只有进行限流操作的topic，该限速才会对其生效
          </li>
          <li>
            上面这句话的意思就是，这里只配置topic副本同步的速率，要使这个配置真正在某个topic上生效，还要开启这个topic的限流
          </li>
        </ul>
        <h4>如何检查限流配置是否成功：</h4>
        kafka的限流速率是通过下面这两项配置的：
        <ul>
          <li>leader.replication.throttled.rate</li>
          <li>follower.replication.throttled.rate</li>
        </ul>
        只需通过
        <strong>集群->属性配置</strong>
        查看是否存在这两项配置，如果存在便是配置的有限流，值的大小就是速率，单位：kb/s
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi, KafkaOpApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";

export default {
  name: "ConfigThrottle",
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
      form: this.$form.createForm(this, { name: "ConfigThrottleForm" }),
      brokers: [],
      unit: "MB",
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
      this.$emit("closeConfigThrottleDialog", { refresh: false });
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
          const data = Object.assign({}, values, { unit: this.unit });
          this.loading = true;
          request({
            url: KafkaOpApi.configThrottle.url,
            method: KafkaOpApi.configThrottle.method,
            data: data,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeConfigThrottleDialog", { refresh: false });
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
