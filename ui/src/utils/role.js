export function isManager() {
    return 'manager' === localStorage.getItem("role");
}