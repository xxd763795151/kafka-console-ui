<template>
  <a-modal
    title="转发消息"
    :visible="show"
    :width="600"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="true"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <div>
          <h4>选择集群</h4>
          <hr />
          <div class="message-detail" id="message-detail">
            <a-form
              :form="form"
              :label-col="{ span: 5 }"
              :wrapper-col="{ span: 18 }"
              @submit="handleSubmit"
            >
              <a-form-item label="集群">
                <a-select
                  class="select-width"
                  @change="clusterChange"
                  v-decorator="[
                    'targetClusterId',
                    {
                      rules: [{ required: true, message: '请选择一个集群!' }],
                    },
                  ]"
                  placeholder="请选择一个集群"
                >
                  <a-select-option
                    v-for="v in clusterList"
                    :key="v.id"
                    :value="v.id"
                  >
                    {{ v.clusterName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="Topic">
                <a-select
                  class="select-width"
                  show-search
                  option-filter-prop="children"
                  v-decorator="[
                    'targetTopic',
                    {
                      rules: [{ required: true, message: '请选择一个topic!' }],
                    },
                  ]"
                  placeholder="请选择一个topic"
                >
                  <a-select-option v-for="v in topicList" :key="v" :value="v">
                    {{ v }}
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="相同分区">
                <a-radio-group
                  v-decorator="[
                    'samePartition',
                    {
                      initialValue: 'false',
                      rules: [{ required: true, message: '请选择!' }],
                    },
                  ]"
                >
                  <a-radio value="false"> 否</a-radio>
                  <a-radio value="true"> 是</a-radio>
                </a-radio-group>
                <span class="mar-left">和原消息保持同一个分区</span>
              </a-form-item>
              <a-form-item>
                <div class="form-footer">
                  <a-button type="primary" html-type="submit"> 提交</a-button>
                </div>
              </a-form-item>
            </a-form>
          </div>
        </div>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi, KafkaMessageApi, KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/lib/notification";
import moment from "moment";

export default {
  name: "ForwardMessage",
  props: {
    record: {},
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: this.visible,
      data: {},
      loading: false,
      showForwardDialog: false,
      targetClusterId: -1,
      clusterList: [],
      partition: -1,
      topicList: [],
      form: this.$form.createForm(this, { name: "ForwardMessageForm" }),
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getClusterList();
      }
    },
  },
  methods: {
    getClusterList() {
      this.loading = true;
      request({
        url: KafkaClusterApi.getClusterInfoList.url,
        method: KafkaClusterApi.getClusterInfoList.method,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.clusterList = res.data;
          this.targetClusterId = this.clusterList[0].id;
        }
      });
    },
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          const params = {
            message: Object.assign({}, this.record),
          };
          this.forward({ ...params, ...values });
        }
      });
    },
    handleCancel() {
      this.$emit("closeForwardDialog", { refresh: false });
    },
    formatTime(time) {
      return time == -1 ? -1 : moment(time).format("YYYY-MM-DD HH:mm:ss:SSS");
    },
    clusterChange(e) {
      this.getTopicNameList(e);
    },
    forward(params) {
      this.loading = true;
      request({
        url: KafkaMessageApi.forward.url,
        method: KafkaMessageApi.forward.method,
        data: params,
      }).then((res) => {
        this.loading = false;
        if (res.code != 0) {
          notification.error({
            message: "error",
            description: res.msg,
          });
        } else {
          this.$message.success(res.msg);
        }
      });
    },
    openForwardDialog() {
      this.showForwardDialog = true;
    },
    closeForwardDialog() {
      this.showForwardDialog = false;
    },
    getTopicNameList(clusterInfoId) {
      this.loading = true;
      request({
        url: KafkaTopicApi.getTopicNameList.url,
        method: KafkaTopicApi.getTopicNameList.method,
        headers: {
          "X-Specific-Cluster-Info-Id": clusterInfoId,
        },
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.topicList = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
  },
};
</script>

<style scoped>
.m-info {
  /*text-decoration: underline;*/
}

.title {
  width: 15%;
  display: inline-block;
  text-align: right;
  margin-right: 2%;
  font-weight: bold;
}

.ant-spin-container #message-detail textarea {
  max-width: 80% !important;
  vertical-align: top !important;
}

.center {
  text-align: center;
}

.mar-left {
  margin-left: 1%;
}

.select-width {
  width: 80%;
}

.form-footer {
  text-align: center;
  margin-top: 3%;
}
</style>
