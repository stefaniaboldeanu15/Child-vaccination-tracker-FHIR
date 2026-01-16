import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuth, type UserRole } from '@/auth/auth'

import LoginView from '@/views/LoginView.vue'
import PatientPortalView from '@/views/PatientPortalView.vue'
import DoctorPortalView from '@/views/DoctorPortalView.vue'
import GuardianPortalView from '@/views/GuardianPortalView.vue'

type Meta = {
  requiresAuth?: boolean
  role?: UserRole
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => {
      const { state } = useAuth()
      if (state.role === 'doctor') return '/doctor'
      if (state.role === 'patient') return '/patient'
      if (state.role === 'guardian') return '/guardian'
      return '/login'
    },
  },
  { path: '/login', name: 'login', component: LoginView },
  {
    path: '/patient',
    name: 'patient',
    component: PatientPortalView,
    meta: { requiresAuth: true, role: 'patient' } satisfies Meta,
  },
  {
    path: '/doctor',
    name: 'doctor',
    component: DoctorPortalView,
    meta: { requiresAuth: true, role: 'doctor' } satisfies Meta,
  },
  {
    path: '/guardian',
    name: 'guardian',
    component: GuardianPortalView,
    meta: { requiresAuth: true, role: 'guardian' } satisfies Meta,
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const { isAuthenticated, state } = useAuth()
  const meta = (to.meta || {}) as Meta

  // If already logged in, keep them out of /login.
  if (to.path === '/login' && isAuthenticated.value) {
    if (state.role === 'doctor') return '/doctor'
    if (state.role === 'patient') return '/patient'
    return '/guardian'
  }

  if (meta.requiresAuth && !isAuthenticated.value) {
    return '/login'
  }

  if (meta.role && state.role && meta.role !== state.role) {
    if (state.role === 'doctor') return '/doctor'
    if (state.role === 'patient') return '/patient'
    return '/guardian'
  }

  return true
})
