import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuth } from '@/auth/auth'

import LoginView from '@/views/LoginView.vue'
import DoctorPortalView from '@/views/DoctorPortalView.vue'
import DebugView from '@/views/DebugView.vue'

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        redirect: () => {
            const { state } = useAuth()
            return state.role === 'doctor' ? '/doctor' : '/login'
        },
    },
{ path: '/login', name: 'login', component: LoginView },
{ path: '/doctor', name: 'doctor', component: DoctorPortalView, meta: { requiresAuth: true } },
{ path: '/debug', name: 'debug', component: DebugView, meta: { requiresAuth: true } },
{ path: '/:pathMatch(.*)*', redirect: '/' },
]

export const router = createRouter({
    history: createWebHistory(),
                                   routes,
})

router.beforeEach((to) => {
    const { isAuthenticated } = useAuth()

    if (to.path === '/login' && isAuthenticated.value) return '/doctor'
        if (to.meta.requiresAuth && !isAuthenticated.value) return '/login'
            return true
})
