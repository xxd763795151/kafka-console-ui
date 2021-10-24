<template>
  <a-modal
    title="同步消费位点"
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
          <a-form-item label="消费组">
            <a-select
              show-search
              option-filter-prop="children"
              v-decorator="[
                'groupId',
                { rules: [{ required: true, message: '请选择一个消费组!' }] },
              ]"
              placeholder="请选择一个消费组"
            >
              <a-select-option v-for="v in groupIdList" :key="v" :value="v">
                {{ v }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="topic">
            <a-select
              show-search
              option-filter-prop="children"
              v-decorator="[
                'topic',
                { rules: [{ required: true, message: '请选择一个topic!' }] },
              ]"
              placeholder="请选择一个topic"
            >
              <a-select-option v-for="v in topicList" :key="v" :value="v">
                {{ v }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="kafka地址">
            <a-input
              v-decorator="[
                'address',
                {
                  rules: [{ required: true, message: '输入待同步kafka地址!' }],
                },
              ]"
              placeholder="输入待同步kafka地址"
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
import { KafkaConsumerApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
export default {
  name: "SyncConsumerOffset",
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
      topicList: [],
      groupIdList: [],
    };
  },
  watch: {
    visible(v) {
      this.show = v;
      if (this.show) {
        this.getGroupIdList();
      }
    },
  },
  methods: {
    getGroupIdList() {
      request({
        url: KafkaConsumerApi.getGroupIdList.url,
        method: KafkaConsumerApi.getGroupIdList.method,
      }).then((res) => {
        if (res.code == 0) {
          this.groupIdList = res.data;
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFields((err, values) => {
        if (!err) {
          this.loading = true;
          request({
            url: KafkaConsumerApi.addSubscription.url,
            method: KafkaConsumerApi.addSubscription.method,
            data: values,
          }).then((res) => {
            this.loading = false;
            if (res.code == 0) {
              this.$message.success(res.msg);
              this.$emit("closeSyncConsumerOffsetDialog", { refresh: true });
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
      this.$emit("closeSyncConsumerOffsetDialog", { refresh: false });
    },
  },
};
</script>

<style scoped></style>
