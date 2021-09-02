<template>
  <a-modal
    title="授予生产权限"
    :visible="show"
    :confirm-loading="confirmLoading"
    :width="800"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="提交"
    cancelText="取消"
    :mask="false"
  >
    <a-form :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
      <a-form-item label="用户名">
        <a-input
          v-decorator="['username', { initialValue: record.username }]"
          disabled="disabled"
        />
      </a-form-item>
      <a-form-item label="topic">
        <a-input
          v-decorator="[
            'topic',
            { rules: [{ required: true, message: '请输入topic!' }] },
          ]"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
export default {
  name: "AddProducerAuth",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    record: {
      default: {},
    },
  },
  data() {
    return {
      formLayout: "horizontal",
      form: this.$form.createForm(this, { name: "AddProducerAuthForm" }),
      confirmLoading: false,
      show: this.visible,
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
          console.log("Received values of form: ", values);
        }
      });
    },
    handleSelectChange(value) {
      console.log(value);
      this.form.setFieldsValue({
        note: `Hi, ${value === "male" ? "man" : "lady"}!`,
      });
    },
    handleOk() {
      console.log(this.record);
      this.$emit("addProducerAuthDialog", {});
    },
    handleCancel() {
      this.$emit("addProducerAuthDialog", {});
    },
  },
};
</script>

<style scoped></style>
