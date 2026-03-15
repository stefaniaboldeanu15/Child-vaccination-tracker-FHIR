import { createRouter, createWebHistory } from 'vue-router'
import { useSession } from '@/auth/session'

import LandingView from '@/views/LandingView.vue'
import AuthCallbackView from '@/views/AuthCallbackView.vue'
import PractitionerDashboardView from '@/views/PractitionerDashboardView.vue'
import RelatedPersonDashboardView from '@/views/RelatedPersonDashboardView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: LandingView },
    { path: '/auth/callback', name: 'auth-callback', component: AuthCallbackView },
    {
      path: '/practitioner',
      name: 'practitioner',
      component: PractitionerDashboardView,
      meta: { requiresAuth: true, role: 'practitioner' },
    },
    {
      path: '/related-person',
      name: 'related-person',
      component: RelatedPersonDashboardView,
      meta: { requiresAuth: true, role: 'related-person' },
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const { state, isAuthenticated } = useSession()

  if (!to.meta.requiresAuth) {
    if (to.path === '/' && isAuthenticated.value) {
      return state.role === 'practitioner' ? '/practitioner' : '/related-person'
    }
    return true
  }

  if (!isAuthenticated.value) return '/'
  if (to.meta.role && to.meta.role !== state.role) {
    return state.role === 'practitioner' ? '/practitioner' : '/related-person'
  }

  return true
})
