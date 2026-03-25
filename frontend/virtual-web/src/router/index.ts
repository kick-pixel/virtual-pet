import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import i18n from '@/lang'

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        component: () => import('@/components/layout/AppLayout.vue'),
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('@/views/Home.vue'),
                meta: { titleKey: 'route.home' }
            },
            // ===== Generate =====
            {
                path: 'generate/create',
                name: 'GenerateCreate',
                component: () => import('@/views/generate/Create.vue'),
                meta: { titleKey: 'route.generateCreate', requiresAuth: true }
            },
            {
                path: 'generate/progress/:taskId',
                name: 'GenerateProgress',
                component: () => import('@/views/generate/Progress.vue'),
                meta: { titleKey: 'route.generateProgress', requiresAuth: true }
            },
            // ===== Recharges (accessible via Profile modal) =====
            {
                path: 'credits/recharge',
                name: 'CreditsRecharge',
                component: () => import('@/views/credits/Recharge.vue'),
                meta: { titleKey: 'route.creditsRecharge', requiresAuth: true }
            },
            // ===== Showcase =====
            {
                path: 'showcase',
                name: 'Showcase',
                component: () => import('@/views/showcase/List.vue'),
                meta: { titleKey: 'route.showcase' }
            },
            {
                path: 'showcase/post/:taskId',
                name: 'ShowcaseDetail',
                component: () => import('@/views/showcase/Detail.vue'),
                meta: { titleKey: 'route.showcaseDetail' }
            },
            // ===== User =====
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/user/Profile.vue'),
                meta: { titleKey: 'route.profile', requiresAuth: true }
            },
            // ===== About =====
            {
                path: 'about',
                name: 'About',
                component: () => import('@/views/About.vue'),
                meta: { titleKey: 'route.about' }
            }
        ]
    },
    // ===== OAuth 回调（独立页面，不含 Header/Footer） =====
    {
        path: '/auth/callback',
        name: 'AuthCallback',
        component: () => import('@/views/auth/OAuthCallback.vue'),
        meta: { title: 'OAuth Login' }
    },
    // ===== 404 =====
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/NotFound.vue'),
        meta: { titleKey: 'route.notFound' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to, _from, next) => {
    // Translate page title using i18n
    const titleKey = to.meta.titleKey as string
    const title = titleKey ? i18n.global.t(titleKey) : ''
    document.title = title ? `${title} - Pet AI` : 'Pet AI'

    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
        return next({ name: 'Home' })
    }

    if (userStore.isLoggedIn() && !userStore.userId) {
        await userStore.fetchProfile()
    }

    next()
})

export default router