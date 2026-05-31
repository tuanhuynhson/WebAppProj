document.addEventListener("DOMContentLoaded", () => {
    const cart = new Map();
    const moneyFormatter = new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD"
    });

    const cartItems = document.getElementById("cartItems");
    const cartCount = document.getElementById("cartCount");
    const cartSubtotal = document.getElementById("cartSubtotal");
    const cartTotal = document.getElementById("cartTotal");
    const cartSummaryInput = document.getElementById("cartSummary");
    const orderTotalInput = document.getElementById("orderTotal");
    const checkoutButton = document.getElementById("checkoutButton");
    const paymentForm = document.getElementById("paymentForm");
    const cardFields = document.querySelector("[data-card-fields]");
    const cardInputs = cardFields ? cardFields.querySelectorAll("input") : [];

    function getTotal() {
        return Array.from(cart.values()).reduce((sum, item) => sum + item.price * item.quantity, 0);
    }

    function getItemCount() {
        return Array.from(cart.values()).reduce((sum, item) => sum + item.quantity, 0);
    }

    function updateHiddenFields(total) {
        const summary = Array.from(cart.values())
            .map((item) => `${item.name} x ${item.quantity}`)
            .join(", ");

        cartSummaryInput.value = summary;
        orderTotalInput.value = total.toFixed(2);
        checkoutButton.disabled = total <= 0;
    }

    function renderCart() {
        const total = getTotal();
        const itemCount = getItemCount();
        cartItems.innerHTML = "";
        cartItems.classList.toggle("cart-empty", cart.size === 0);

        if (cart.size === 0) {
            cartItems.textContent = "Your cart is empty.";
        } else {
            cart.forEach((item) => {
                const row = document.createElement("div");
                row.className = "cart-item";

                const detail = document.createElement("div");
                const name = document.createElement("strong");
                const price = document.createElement("span");
                name.textContent = item.name;
                price.textContent = `${moneyFormatter.format(item.price)} each`;
                detail.append(name, price);

                const controls = document.createElement("div");
                controls.className = "qty-controls";

                const decrease = document.createElement("button");
                decrease.className = "qty-button";
                decrease.type = "button";
                decrease.textContent = "-";
                decrease.setAttribute("aria-label", `Remove one ${item.name}`);
                decrease.addEventListener("click", () => changeQuantity(item.name, -1));

                const quantity = document.createElement("strong");
                quantity.textContent = item.quantity;

                const increase = document.createElement("button");
                increase.className = "qty-button";
                increase.type = "button";
                increase.textContent = "+";
                increase.setAttribute("aria-label", `Add one ${item.name}`);
                increase.addEventListener("click", () => changeQuantity(item.name, 1));

                controls.append(decrease, quantity, increase);
                row.append(detail, controls);
                cartItems.append(row);
            });
        }

        cartCount.textContent = itemCount === 1 ? "1 item" : `${itemCount} items`;
        cartSubtotal.textContent = moneyFormatter.format(total);
        cartTotal.textContent = moneyFormatter.format(total);
        updateHiddenFields(total);
    }

    function changeQuantity(name, amount) {
        const item = cart.get(name);
        if (!item) {
            return;
        }

        item.quantity += amount;
        if (item.quantity <= 0) {
            cart.delete(name);
        }

        renderCart();
    }

    function addToCart(button) {
        const name = button.dataset.product;
        const price = Number(button.dataset.price);

        if (!name || Number.isNaN(price)) {
            return;
        }

        const item = cart.get(name) || { name, price, quantity: 0 };
        item.quantity += 1;
        cart.set(name, item);
        renderCart();
    }

    function updatePaymentFields() {
        const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked');
        const cardSelected = selectedMethod && selectedMethod.value === "Credit Card";

        if (!cardFields) {
            return;
        }

        cardFields.classList.toggle("is-hidden", !cardSelected);
        cardInputs.forEach((input) => {
            input.required = Boolean(cardSelected);
        });
    }

    document.querySelectorAll(".btn-buy").forEach((button) => {
        button.addEventListener("click", () => addToCart(button));
    });

    document.querySelectorAll('input[name="paymentMethod"]').forEach((input) => {
        input.addEventListener("change", updatePaymentFields);
    });

    paymentForm.addEventListener("submit", (event) => {
        if (getTotal() <= 0) {
            event.preventDefault();
            renderCart();
        }
    });

    updatePaymentFields();
    renderCart();
});
