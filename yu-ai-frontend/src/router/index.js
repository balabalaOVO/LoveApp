import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoveChatView from '../views/LoveChatView.vue'
import ManusChatView from '../views/ManusChatView.vue'
import AuthView from '../views/AuthView.vue'
import { isAuthenticated } from '../store/auth'


const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/love-chat',
      name: 'love-chat',
      component: LoveChatView,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/manus-chat',
      name: 'manus-chat',
      component: ManusChatView,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/login',
      name: 'login',
      component: AuthView
    }
  ]
})

router.beforeEach((to) => {
  const loggedIn = isAuthenticated()

  if (to.meta.requiresAuth && !loggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (to.path === '/login' && loggedIn) {
    return {
      path: '/'
    }
  }

  return true
})

export default router
