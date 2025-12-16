import { createRouter, createWebHistory } from 'vue-router'
import { authService } from "./service/AuthService.ts";

// Customer pages
import DetailProductHome from './pages/home/DetailProductHome.vue'
import Cart from './pages/home/Cart.vue'
import HomeProduct from './pages/home/HomeProduct.vue'

// Admin pages
import AdminLayout from './layout/AdminLayout.vue'
import AdminPage from './pages/dashboard.vue'
import User from './pages/user/User.vue'
import UserDetailPage from './pages/user/UserDetailPage.vue'
import Product from './pages/product/Product.vue'
import Order from './pages/order/Order.vue'
import OrderDetailPage from './pages/order/OrderDetailPage.vue'
import HomePage from "./pages/home/HomePage.vue";
import OrderOfUSer from "./pages/home/OrderOfUSer.vue";
import OrderDetailByUser from "./pages/home/OrderDetailByUser.vue";
import LoginPageCustomer from "./pages/LoginPage.vue";
import RegisterPage from "./pages/RegisterPage.vue";
import ProductDetailPage from "./pages/product/ProductDetailPage.vue";
import UserProfile from "./pages/home/UserProfile.vue";
import PageNodePermission from "./pages/PageNodePermission.vue";

const router = createRouter({
    history: createWebHistory(),
    scrollBehavior(to, from, savedPosition) {
        return savedPosition ? savedPosition : { left: 0, top: 0 }
    },
    routes: [
        {
            path: '/',
            redirect: '/customer/home' // Add a proper redirect for root path
        },
        {
            path: '/login',
            name: 'LoginPageCustomer',
            component: LoginPageCustomer,
            meta: { requiresAuth: false } // Login should NOT require auth
        },
        {
            path: '/register',
            name: 'RegisterPage',
            component: RegisterPage,
            meta: { requiresAuth: false } // Register should NOT require auth
        },
        {
            path: '/customer',
            redirect: '/customer/home',
            children: [
                {
                    path: 'home',
                    name: 'home',
                    component: HomePage,
                    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
                },
                {
                    path: 'detail-product/:id',
                    name: 'DetailProductHome',
                    component: DetailProductHome,
                    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
                },
                {
                    path: 'cart',
                    name: 'cart',
                    component: Cart,
                    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
                },
                {
                    path: 'product-home',
                    name: 'HomeProduct',
                    component: HomeProduct,
                    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
                },
                {
                    path: 'order-by-user',
                    name: 'OrderOfUSer',
                    component: OrderOfUSer,
                    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
                },
                {
                    path: 'order-detail/:id',
                    name: 'OrderDetailByUser',
                    component: OrderDetailByUser,
                    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
                },
                {
                    path: 'user-profile',
                    name: 'UserProfile',
                    component: UserProfile,
                    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
                }
            ]
        },
        {
            path: '/admin',
            component: AdminLayout,
            redirect: '/admin/dashboard',
            children: [
                {
                    path: 'dashboard',
                    name: 'dashboard',
                    component: AdminPage,
                    meta: { requiresAuth: true, roles: ['ADMIN'] }
                },
                {
                    path: 'user',
                    name: 'user',
                    component: User,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                },
                {
                    path: 'user-detail/:id',
                    name: 'UserDetailPage',
                    component: UserDetailPage,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                },
                {
                    path: 'create-user',
                    name: 'CreateUserPage',
                    component: UserDetailPage,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                },
                {
                    path: 'product',
                    name: 'product',
                    component: Product,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                },
                {
                    path: 'product-detail/:id',
                    name: 'ProductDetailPage',
                    component: ProductDetailPage,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                },
                {
                    path: 'create-product',
                    name: 'CreateProduct',
                    component: ProductDetailPage,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                },
                {
                    path: 'order',
                    name: 'order',
                    component: Order,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                },
                {
                    path: 'orders-detail/:id',
                    name: 'OrderDetailPage',
                    component: OrderDetailPage,
                    meta: { requiresAuth: true, roles: ['STAFF', 'ADMIN'] }
                }
            ]
        },
        {
            path: '/403',
            name: 'routes.403',
            component: PageNodePermission,
            meta: { requiresAuth: false },
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/customer/home' // Handle 404
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isAuth = authService.isAuthenticated();
    const role = authService.getRole(); // ADMIN | STAFF | CUSTOMER

    // Skip auth check for public routes
    if (!to.meta.requiresAuth) {
        return next();
    }

    if (isAuth) {
        // If authenticated but trying to access login/register
        if (to.name === 'LoginPageCustomer' || to.name === 'RegisterPage') {
            // Redirect based on role
            if (role === 'CUSTOMER') {
                return next({ path: '/customer/home' });
            } else {
                return next({ path: '/admin/dashboard' });
            }
        }

        // Check role permissions
        if (to.meta.roles && !to.meta.roles.includes(role)) {
            return next({ path: '/403' });
        }

        // All checks passed
        return next();
    } else {
        // Not authenticated and trying to access protected route
        if (to.meta.requiresAuth) {
            return next({ name: 'LoginPageCustomer' });
        }

        // For non-protected routes, allow access
        return next();
    }
});

router.afterEach((to) => {
    document.title = to.meta.title ?? 'App'
})

export default router