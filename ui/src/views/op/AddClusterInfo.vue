<template>
  <a-modal
    title="增加集群配置"
    :visible="show"
    :width="1000"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :form="form"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @submit="handleSubmit"
        >
          <a-form-item label="集群名称">
            <a-input
              v-decorator="[
                'clusterName',
                {
                  rules: [{ required: true, message: '输入集群名称!' }],
                  initialValue: clusterInfo.clusterName,
                },
              ]"
              placeholder="输入集群名称"
            />
          </a-form-item>
          <a-form-item label="集群地址">
            <a-input
              v-decorator="[
                'address',
                {
                  rules: [{ required: true, message: '输入集群地址!' }],
                  initialValue: clusterInfo.address,
                },
              ]"
              placeholder="输入集群地址"
            />
          </a-form-item>
          <a-form-item label="属性">
            <a-textarea
              rows="5"
              placeholder='可选参数，集群其它属性配置：
request.timeout.ms=10000
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="name" password="password";
'
              v-decorator="[
                'properties',
                { initialValue: clusterInfo.properties },
              ]"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaClusterApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import { getClusterInfo } from "@/utils/local-cache";
import { mapMutations } from "vuex";
import { CLUSTER } from "@/store/mutation-types";

export default {
  name: "AddClusterInfo",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    isModify: {
      type: Boolean,
      default: false,
    },
    clusterInfo: {
      type: Object,
      default: () => defaultInfo,
    },
    closeDialogEvent: {
      type: String,
      default: "closeAddClusterInfoDialog",
    },
  },
  data() {
    return {
      show: this.visible,
      data: [],
      loading: false,
      form: this.$form.createForm(this, { name: "AddClusterInfoForm" }),
    };
  },
  watch: {
    visible(v) {
      this.show = v;
    },
  },
  methods: {
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          this.loading = true;
          const api = this.isModify
            ? KafkaClusterApi.updateClusterInfo
            : KafkaClusterApi.addClusterInfo;
          const data = this.isModify
            ? Object.assign({}, this.clusterInfo, values)
            : Object.assign({}, values);
          request({
            url: api.url,
            method: api.method,
            data: data,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit(this.closeDialogEvent, { refresh: true });
              if (this.isModify) {
                let clusterInfo = getClusterInfo();
                if (
                  clusterInfo &&
                  clusterInfo.id &&
                  clusterInfo.id == this.clusterInfo.id
                ) {
                  //  &&clusterInfo.clusterName != data.clusterName
                  this.switchCluster(data);
                }
              }
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
    handleCancel() {
      this.data = [];
      this.$emit(this.closeDialogEvent, { refresh: false });
    },
    ...mapMutations({
      switchCluster: CLUSTER.SWITCH,
    }),
  },
};
const defaultInfo = { clusterName: "", address: "", properties: "" };
</script>

<style scoped></style>
