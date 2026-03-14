import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuth, useRelatedPersonAuth } from '@/auth/auth'

import LoginView from '@/views/LoginView.vue'
import DoctorPortalView from '@/views/DoctorPortalView.vue'
import RelatedPersonView from '@/views/RelatedPersonView.vue'

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        redirect: () => {
            const { isAuthenticated } = useAuth()
            const { isAuthenticated: isRelatedPersonAuthenticated } = useRelatedPersonAuth()

            if (isAuthenticated.value) return '/doctor'
                if (isRelatedPersonAuthenticated.value) return '/related-person'
                    return '/login'
        },
    },
{ path: '/login', name: 'login', component: LoginView },
{ path: '/doctor', name: 'doctor', component: DoctorPortalView, meta: { requiresAuth: true } },
{ path: '/related-person', name: 'related-person', component: RelatedPersonView },
{ path: '/:pathMatch(.*)*', redirect: '/' },
]

export const router = createRouter({
    history: createWebHistory(),
                                   routes,
})

router.beforeEach((to) => {
    const { isAuthenticated } = useAuth()
    const { isAuthenticated: isRelatedPersonAuthenticated } = useRelatedPersonAuth()

    if (to.path === '/login' && isAuthenticated.value) return '/doctor'
        if (to.path === '/login' && isRelatedPersonAuthenticated.value) return '/related-person'
            if (to.meta.requiresAuth && !isAuthenticated.value) return '/login'
                return true
})
