<template>
  <a-modal
    title="重置消费位点"
    :visible="show"
    :width="600"
    :mask="false"
    :destroyOnClose="true"
    :maskClosable="false"
    @cancel="handleCancel"
    @ok="resetOffset"
    okText="提交"
    cancelText="取消"
  >
    <div>
      <a-spin :spinning="loading">
        <a-form
          :form="resetOffsetForm"
          :label-col="{ span: 8 }"
          :wrapper-col="{ span: 12 }"
        >
          <a-form-item label="重置消费位点到">
            <a-date-picker
              show-time
              placeholder="选择重置到哪个时间"
              :locale="locale"
              v-decorator="[
                'dateTime',
                {
                  rules: [{ required: true, message: '输入消费位点!' }],
                },
              ]"
            />
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

import locale from "ant-design-vue/lib/date-picker/locale/zh_CN";

import moment from "moment";
export default {
  name: "ResetOffsetByTime",
  props: {
    group: {
      type: String,
      default: "",
    },
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
      locale,
      show: this.visible,
      loading: false,
      resetOffsetForm: this.$form.createForm(this, {
        name: "resetOffsetForm",
      }),
    };
  },
  watch: {
    visible(v) {
      this.show = v;
    },
  },
  methods: {
    handleCancel() {
      this.$emit("closeResetOffsetByTimeDialog", {});
    },
    resetOffset() {
      this.resetOffsetForm.validateFields((e, v) => {
        if (e) {
          return;
        }
        v.dateStr = moment(v.dateTime).format("YYYY-MM-DD HH:mm:ss");
        const data = { dateStr: v.dateStr };
        data.groupId = this.group;
        data.topic = this.topic;
        data.level = 1;
        data.type = 3;
        this.loading = true;
        request({
          url: KafkaConsumerApi.resetOffset.url,
          method: KafkaConsumerApi.resetOffset.method,
          data: data,
        }).then((res) => {
          this.loading = false;
          if (res.code != 0) {
            notification.error({
              message: "error",
              description: res.msg,
            });
          } else {
            this.$emit("closeResetOffsetByTimeDialog", { refresh: true });
          }
        });
      });
    },
  },
};
</script>

<style scoped></style>
