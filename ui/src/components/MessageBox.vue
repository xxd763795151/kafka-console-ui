<template>
  <a-modal
    title="消息提示"
    v-model:open="visible"
    :width="400"
    :mask="false"
    :destroyOnClose="true"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div>
      {{ message }}
    </div>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'MessageBox',
  props: {
    message: {
      type: String,
      default: '',
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['closeMessageBox', 'update:visible'],
  methods: {
    handleCancel() {
      this.$emit('update:visible', false);
      this.$emit('closeMessageBox', {});
    },
  },
});
</script>

<style scoped></style>
