<template>
  <a-modal
    title="API版本信息"
    :visible="show"
    :width="600"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      <h3>格式说明</h3>
      <p>请求类型(1)：0 to n(2) [usage: v](3)</p>
      <ol>
        <li>表示客户端发出的请求类型</li>
        <li>该请求在broker中支持的版本号区间</li>
        <li>
          表示当前控制台的kafka客户端使用的是v版本，如果是UNSUPPORTED，说明broker版本太老，无法处理控制台的这些请求，可能影响相关功能的使用
        </li>
      </ol>

      <hr />
      <ol>
        <li v-for="info in versionInfo" v-bind:key="info">{{ info }}</li>
      </ol>
    </div>
  </a-modal>
</template>

<script>
export default {
  name: "APIVersionInfo",
  props: {
    versionInfo: {
      type: Array,
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      show: false,
    };
  },
  watch: {
    visible(v) {
      this.show = v;
    },
  },
  methods: {
    handleCancel() {
      this.$emit("closeApiVersionInfoDialog", {});
    },
  },
};
</script>

<style scoped></style>
