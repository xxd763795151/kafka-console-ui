import type { App, Directive } from 'vue';
import Store from '@/store';

const action: Directive<HTMLElement, any> = {
  mounted(el, binding) {
    const actionName = binding.arg as string;
    const enableAuth = Store.state.auth.enable;
    const permissions = Store.state.auth.permissions;
    const permsArr = Array.isArray(permissions) ? permissions : (permissions as string);
    if (enableAuth && (!permissions || (permsArr as string[]).indexOf(actionName) < 0)) {
      if (el.parentNode) {
        el.parentNode.removeChild(el);
      } else {
        el.style.display = 'none';
      }
    }
  },
};

export function setupActionDirective(app: App): void {
  app.directive('action', action);
}

export default action;
