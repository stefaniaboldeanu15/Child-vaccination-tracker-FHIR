import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import DashboardView from "../views/DashboardView.vue";
import PatientView from "../views/PatientView.vue";

const routes = [
    { path: "/", component: LoginView },
    { path: "/dashboard", component: DashboardView },
    { path: "/patient/:id", component: PatientView },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
