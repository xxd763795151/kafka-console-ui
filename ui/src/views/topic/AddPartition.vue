<template>
  <a-modal
    title="增加分区"
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
        <a-form
          :form="form"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 12 }"
          @submit="handleSubmit"
        >
          <a-form-item label="Topic名称">
            <a-input
              :disabled="true"
              v-decorator="['topic', { initialValue: topic }]"
              placeholder="topic"
            />
          </a-form-item>
          <a-form-item label="增加分区数">
            <a-input-number
              :min="1"
              :max="32"
              v-decorator="[
                'addNum',
                {
                  initialValue: 1,
                  rules: [{ required: true, message: '输入分区数!' }],
                },
              ]"
            />
            <span class="ant-form-text"> 个分区 </span>
          </a-form-item>
          <a-form-item label="副本">
            <a-textarea
              rows="5"
              placeholder="可选参数，指定新增分区的副本，格式示例如下：
1=1,2
2=2,3"
              v-decorator="['assignment']"
            />
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
            <a-button type="primary" html-type="submit"> 提交 </a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </a-modal>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "AddPartition",
  props: {
    topic: {
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
      form: this.$form.createForm(this, { name: "coordinated" }),
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
      this.loading = false;
    },
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          if (values.assignment) {
            const assignment = {};
            values.assignment.split("\n").forEach((e) => {
              const c = e.split("=");
              if (c.length > 1) {
                let k = c[0];
                let v = c[1];
                let arr = v.split(",");
                if (arr.length > 0) {
                  assignment[k] = arr;
                }
              }
            });
            values.assignment = assignment;
          }
          this.loading = true;
          request({
            url: KafkaTopicApi.addPartition.url,
            method: KafkaTopicApi.addPartition.method,
            data: values,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeAddPartitionDialog", { refresh: true });
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
      this.$emit("closeAddPartitionDialog", { refresh: false });
    },
  },
};
</script>

<style scoped></style>
