import Vue from "vue";

const action = Vue.directive("action", {
  inserted: function (el, binding) {
    const actionName = binding.arg;
    if (actionName != "action") {
      (el.parentNode && el.parentNode.removeChild(el)) ||
        (el.style.display = "none");
    }
  },
});

export default action;
