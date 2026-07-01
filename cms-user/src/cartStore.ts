import { defineStore } from 'pinia'
import { cartService } from '@/service/CartService'
import { authService } from '@/service/AuthService'

export const useCartStore = defineStore('cartStore', {
    state: () => ({
        cartCount: 0,
    }),
    actions: {
        async fetchCartCount() {
            if (authService.isAuthenticated()) {
                try {
                    const response = await cartService.getCartItems({})
                    const cart = response.data.data
                    if (cart && cart.products) {
                        this.cartCount = cart.products.reduce((sum: number, item: any) => sum + item.quantity, 0)
                    } else {
                        this.cartCount = 0
                    }
                } catch (err) {
                    console.error("Fetch cart count error", err)
                    this.cartCount = 0
                }
            } else {
                this.cartCount = 0
            }
        },
        setCartCount(count: number) {
            this.cartCount = count
        }
    }
})
