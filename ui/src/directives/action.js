import Vue from "vue";
import Store from "@/store";

const action = Vue.directive("action", {
  inserted: function (el, binding) {
    const actionName = binding.arg;
    const enableAuth = Store.state.auth.enable;
    const permissions = Store.state.auth.permissions;
    if (enableAuth && (!permissions || permissions.indexOf(actionName) < 0)) {
      (el.parentNode && el.parentNode.removeChild(el)) ||
        (el.style.display = "none");
    }
  },
});

export default action;
