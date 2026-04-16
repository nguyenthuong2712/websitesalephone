// src/models/Cart.ts

export interface ProductInCart {
    idCartItem: string,
    productId: string;
    productName: string;
    quantity: number;
    ram: string;
    color: string;
    ops: string;
    image: string;
    price: number; // dùng number cho frontend, parse BigDecimal từ backend nếu cần
}

export interface CartResponse {
    products: ProductInCart[];
    totalQuantity: number;
    total: number;
}

export class ProductInCartModel implements ProductInCart {
    idCartItem: "";
    productId = "";
    productName = "";
    quantity = 0;
    ram = "";
    color = "";
    ops = "";
    image = "";
    price = 0;

    constructor(init?: Partial<ProductInCartModel>) {
        Object.assign(this, init);
    }
}

export class CartResponseModel implements CartResponse {
    products: ProductInCartModel[] = [];
    totalQuantity = 0;
    total = 0;

    constructor(init?: Partial<CartResponseModel>) {
        Object.assign(this, init);
    }
}
