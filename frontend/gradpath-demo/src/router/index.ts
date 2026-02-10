import { createRouter, createWebHistory } from 'vue-router'

import MyApplicationPage from '../pages/student/MyApplicationPage.vue'
import ApplicationListPage from '../pages/reviewer/ApplicationListPage.vue'
import ReviewDetailPage from '../pages/reviewer/ReviewDetailPage.vue'
import SystemOverviewPage from '../pages/admin/SystemOverviewPage.vue'

const routes = [
  { path: '/student/application/:id', component: MyApplicationPage },
  { path: '/reviewer/applications', component: ApplicationListPage },
  { path: '/reviewer/application/:id', component: ReviewDetailPage },
  { path: '/admin/overview', component: SystemOverviewPage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
