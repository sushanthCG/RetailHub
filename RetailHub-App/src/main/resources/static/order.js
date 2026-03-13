/* ══════════════════════════════════════
   LOAD CART FROM sessionStorage
══════════════════════════════════════ */
let cart = [];

window.addEventListener('DOMContentLoaded', function () {
    const saved = sessionStorage.getItem('retailhub_cart');
    if (saved) {
        cart = JSON.parse(saved);
    }

    if (cart.length === 0) {
        alert('Your cart is empty!');
        window.location.href = '/api/products-page';
        return;
    }

    renderSummary();
    setupPaymentToggle();
});

/* ══════════════════════════════════════
   RENDER ORDER SUMMARY
══════════════════════════════════════ */
function renderSummary() {
    const container = document.getElementById('summaryItems');
    const total     = cart.reduce((s, i) => s + i.price * i.qty, 0);

    container.innerHTML = cart.map(item => `
        <div class="summary-item">
            ${item.img
                ? `<img class="sum-img" src="${item.img}" alt="${item.name}">`
                : `<div class="sum-no-img">🛍️</div>`
            }
            <div class="sum-info">
                <div class="sum-name">${item.name}</div>
                <div class="sum-qty">Qty: ${item.qty}</div>
            </div>
            <div class="sum-price">₹${(item.price * item.qty).toLocaleString('en-IN')}</div>
        </div>
    `).join('');

    const fmt = total.toLocaleString('en-IN');
    document.getElementById('summarySubtotal').textContent = '₹' + fmt;
    document.getElementById('summaryTotal').textContent    = '₹' + fmt;
}

/* ══════════════════════════════════════
   PAYMENT METHOD TOGGLE
══════════════════════════════════════ */
function setupPaymentToggle() {
    document.querySelectorAll('.pay-option').forEach(opt => {
        opt.addEventListener('click', function () {
            document.querySelectorAll('.pay-option').forEach(o => o.classList.remove('selected'));
            this.classList.add('selected');
            this.querySelector('input[type="radio"]').checked = true;

            const method = this.querySelector('input').value;
            const btn    = document.getElementById('placeOrderBtn');
            btn.textContent = method === 'cod'
                ? '📦 Place Order (Cash on Delivery)'
                : '🔒 Place Order & Pay';
        });
    });
}

/* ══════════════════════════════════════
   VALIDATE ADDRESS FORM
══════════════════════════════════════ */
function validateForm() {
    const fields = ['fullName', 'phone', 'address', 'city', 'state', 'pincode'];
    for (const id of fields) {
        const el = document.getElementById(id);
        if (!el.value.trim()) {
            el.focus();
            el.style.borderColor = '#d32f2f';
            setTimeout(() => el.style.borderColor = '', 2000);
            return false;
        }
    }
    return true;
}

/* ══════════════════════════════════════
   PLACE ORDER
══════════════════════════════════════ */
async function placeOrder() {
    if (!validateForm()) return;

    const method = document.querySelector('input[name="payment"]:checked').value;
    const total  = cart.reduce((s, i) => s + i.price * i.qty, 0);

    if (method === 'cod') {
        await saveOrderToBackend(total, 'COD', null);
        return;
    }

    // ── Razorpay flow ──
    try {
        const btn = document.getElementById('placeOrderBtn');
        btn.disabled    = true;
        btn.textContent = '⏳ Processing...';

        const res  = await fetch('/api/create-razorpay-order', {
            method:      'POST',
            credentials: 'include',          // ← ADDED
            headers:     { 'Content-Type': 'application/json' },
            body:        JSON.stringify({ amount: total })
        });
        const data = await res.json();

        if (data.error) {
            alert('Error: ' + data.error);
            btn.disabled    = false;
            btn.textContent = '🔒 Place Order & Pay';
            return;
        }

        const rzpKey = document.getElementById('rzpKeyHolder').getAttribute('data-key');

        const options = {
            key:         rzpKey,
            amount:      data.amount,
            currency:    data.currency,
            order_id:    data.razorpay_order_id,
            name:        'RetailHub',
            description: 'Order Payment',
            image:       '',
            prefill: {
                name:    document.getElementById('fullName').value,
                contact: document.getElementById('phone').value
            },
            theme: { color: '#2874f0' },

            handler: async function (response) {
                await saveOrderToBackend(total, 'RAZORPAY', response.razorpay_payment_id);
            },

            modal: {
                ondismiss: function () {
                    btn.disabled    = false;
                    btn.textContent = '🔒 Place Order & Pay';
                }
            }
        };

        const rzp = new Razorpay(options);
        rzp.open();

    } catch (err) {
        alert('Something went wrong: ' + err.message);
        const btn = document.getElementById('placeOrderBtn');
        btn.disabled    = false;
        btn.textContent = '🔒 Place Order & Pay';
    }
}

/* ══════════════════════════════════════
   SAVE ORDER TO BACKEND
══════════════════════════════════════ */
async function saveOrderToBackend(total, paymentMethod, paymentId) {
    try {
        const res = await fetch('/api/save-order', {
            method:      'POST',
            credentials: 'include',          // ← ADDED
            headers:     { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                totalAmount:   total,
                paymentMethod: paymentMethod,
                paymentId:     paymentId,
                items: cart.map(i => ({
                    id:    i.id,
                    qty:   i.qty,
                    price: i.price
                })),
                address: {
                    fullName: document.getElementById('fullName').value,
                    phone:    document.getElementById('phone').value,
                    address:  document.getElementById('address').value,
                    city:     document.getElementById('city').value,
                    state:    document.getElementById('state').value,
                    pincode:  document.getElementById('pincode').value
                }
            })
        });

        const data = await res.json();

        if (data.error) {
            alert('Error saving order: ' + data.error);
            return;
        }

        // Clear cart
        sessionStorage.removeItem('retailhub_cart');
        cart = [];

        // Show success overlay
        document.getElementById('successOrderId').textContent = 'Order ID: ' + data.order_id;
        document.getElementById('successOverlay').classList.add('show');

    } catch (err) {
        alert('Failed to save order: ' + err.message);
    }
}