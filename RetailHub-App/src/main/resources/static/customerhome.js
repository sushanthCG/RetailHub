/* ══════════════════════════════
   PROFILE DROPDOWN
══════════════════════════════ */
function toggleDrop() {
    document.getElementById('dropMenu').classList.toggle('open');
}
document.addEventListener('click', function (e) {
    if (!e.target.closest('.profile-wrap')) {
        document.getElementById('dropMenu').classList.remove('open');
    }
});


/* ══════════════════════════════
   FILTER STATE
══════════════════════════════ */
let currentCat    = 'All';
let currentSearch = '';

function filterCat(btn, cat) {
    document.querySelectorAll('.cat-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    currentCat = cat;
    applyFilters();
}

function applyFilters() {
    const cards = document.querySelectorAll('.product-card');

    cards.forEach(card => {
        const cat  = (card.getAttribute('data-category') || '').trim();
        const name = (card.getAttribute('data-name') || '').toLowerCase();

        const catOk    = currentCat === 'All' || cat === currentCat;
        const searchOk = currentSearch === '' || name.includes(currentSearch);

        card.style.display = (catOk && searchOk) ? '' : 'none';
    });
}

/* ══════════════════════════════
   CART
══════════════════════════════ */
let cart = [];

// Load cart from DB when page loads
window.addEventListener('DOMContentLoaded', async function () {
    await loadCartFromDB();

    // Checkout button — only ONE listener here
    document.querySelector('.checkout-btn').addEventListener('click', function () {
        if (cart.length === 0) {
            alert('Your cart is empty!');
            return;
        }
        sessionStorage.setItem('retailhub_cart', JSON.stringify(cart));
        window.location.href = '/api/checkout';
    });
});

async function loadCartFromDB() {
    try {
        const res  = await fetch('/api/cart', {
            credentials: 'include'          // ← ADDED: sends authToken cookie
        });
        const data = await res.json();
        if (Array.isArray(data)) {
            cart = data;
            renderCart();
        }
    } catch (e) {
        console.log('Cart load error:', e);
    }
}

function openCart() {
    document.getElementById('cartPanel').classList.add('open');
    document.getElementById('cartOverlay').classList.add('open');
}

function closeCart() {
    document.getElementById('cartPanel').classList.remove('open');
    document.getElementById('cartOverlay').classList.remove('open');
}

async function addToCart(id, name, price, img) {
    try {
        await fetch('/api/cart/add', {
            method:      'POST',
            credentials: 'include',          // ← ADDED: sends authToken cookie
            headers:     { 'Content-Type': 'application/json' },
            body:        JSON.stringify({ productId: id, qty: 1 })
        });

        const existing = cart.find(i => i.id === id);
        if (existing) {
            existing.qty++;
        } else {
            cart.push({ id, name, price, img, qty: 1 });
        }

        renderCart();
        openCart();
    } catch (e) {
        console.error('Add to cart error:', e);
    }
}

async function changeQty(id, delta) {
    const item = cart.find(i => i.id === id);
    if (!item) return;

    const newQty = item.qty + delta;

    try {
        if (newQty <= 0) {
            await fetch(`/api/cart/remove/${id}`, {
                method:      'DELETE',
                credentials: 'include'       // ← ADDED: sends authToken cookie
            });
            cart = cart.filter(i => i.id !== id);
        } else {
            await fetch('/api/cart/update', {
                method:      'POST',
                credentials: 'include',      // ← ADDED: sends authToken cookie
                headers:     { 'Content-Type': 'application/json' },
                body:        JSON.stringify({ productId: id, qty: newQty })
            });
            item.qty = newQty;
        }
        renderCart();
    } catch (e) {
        console.error('Update cart error:', e);
    }
}

function renderCart() {
    const body  = document.getElementById('cartBody');
    const badge = document.getElementById('cartBadge');
    const total = document.getElementById('cartTotal');

    const totalQty   = cart.reduce((s, i) => s + i.qty, 0);
    const totalPrice = cart.reduce((s, i) => s + i.price * i.qty, 0);

    badge.textContent = totalQty;
    total.textContent = '₹' + totalPrice.toLocaleString('en-IN');

    if (cart.length === 0) {
        body.innerHTML = '<div class="cart-empty"><div>🛒</div>Your cart is empty</div>';
        return;
    }

    body.innerHTML = cart.map(item => `
        <div class="cart-item">
            ${item.img
                ? `<img class="ci-img" src="${item.img}" alt="${item.name}">`
                : `<div class="ci-no-img">🛍️</div>`
            }
            <div class="ci-info">
                <div class="ci-name">${item.name}</div>
                <div class="ci-price">₹${(item.price * item.qty).toLocaleString('en-IN')}</div>
                <div class="qty-row">
                    <button class="qty-btn" onclick="changeQty(${item.id}, -1)">−</button>
                    <span class="qty-num">${item.qty}</span>
                    <button class="qty-btn" onclick="changeQty(${item.id}, +1)">+</button>
                </div>
            </div>
        </div>
    `).join('');
}