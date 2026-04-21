import { createRouter, createWebHistory } from 'vue-router'
import { useSession } from '@/auth/session'

import LandingView from '@/views/LandingView.vue'
import AuthCallbackView from '@/views/AuthCallbackView.vue'
import PractitionerDashboardView from '@/views/PractitionerDashboardView.vue'
import RelatedPersonDashboardView from '@/views/RelatedPersonDashboardView.vue'
import PractitionerRegisterView from '@/views/PractitionerRegisterView.vue'
import RelatedPersonRegisterView from '@/views/RelatedPersonRegisterView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: LandingView, meta: { guestOnly: true } },
    {
      path: '/register/practitioner',
      name: 'practitioner-register',
      component: PractitionerRegisterView,
      meta: { guestOnly: true },
    },
    {
      path: '/register/related-person',
      name: 'related-person-register',
      component: RelatedPersonRegisterView,
      meta: { guestOnly: true },
    },
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

  if (isAuthenticated.value && to.meta.guestOnly) {
    return state.role === 'practitioner' ? '/practitioner' : '/related-person'
  }

  if (!to.meta.requiresAuth) return true

  if (!isAuthenticated.value) return '/'
  if (to.meta.role && to.meta.role !== state.role) {
    return state.role === 'practitioner' ? '/practitioner' : '/related-person'
  }

  return true
})
