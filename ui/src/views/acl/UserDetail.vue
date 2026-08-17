<template>
  <a-modal
    title="用户详情"
    :open="show"
    :width="800"
    :mask="false"
    :destroy-on-close="true"
    :footer="null"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <a-form :model="formState" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="用户名">
          <span>{{ user.username }}</span>
        </a-form-item>
        <a-form-item label="密码">
          <span>{{ user.password }}</span>
        </a-form-item>
        <a-form-item label="凭证信息">
          <span>{{ user.credentialInfos }}</span>
        </a-form-item>
        <a-form-item label="数据一致性说明">
          <strong>{{ user.consistencyDescription }}</strong>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { KafkaAclApi } from "@/utils/api";
import request from "@/utils/request";

export default defineComponent({
  name: "UserDetail",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    username: {
      type: String,
    },
  },
  setup(props, { emit }) {
    const show = ref(props.visible)
    const formState = reactive({})
    const user = reactive<any>({})
    const loading = ref(false)

    watch(() => props.visible, (n, o) => {
      show.value = n;
      if (n != o && show.value) {
        getUserDetail();
      }
    })

    function handleCancel() {
      emit("userDetailDialog", {});
    }

    function getUserDetail() {
      const api = KafkaAclApi.getKafkaUserDetail;
      loading.value = true;
      request({
        url: api.url,
        method: api.method,
        params: { username: props.username },
      }).then((res: any) => {
        loading.value = false;
        if (res.code != 0) {
          message.error(res.msg);
        } else {
          Object.assign(user, res.data);
        }
      });
    }

    return {
      show,
      formState,
      user,
      loading,
      handleCancel,
      getUserDetail,
    }
  }
});
</script>

<style scoped></style>
