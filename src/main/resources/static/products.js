
function toggleDrop() {
    document.getElementById('dropMenu').classList.toggle('open');
}
document.addEventListener('click', function (e) {
    if (!e.target.closest('.profile-wrap')) {
        document.getElementById('dropMenu').classList.remove('open');
    }
});

let currentCat    = 'All';
let currentPrice  = 0;
let currentSearch = '';

function filterCat(btn, cat) {
    document.querySelectorAll('.cat-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    currentCat = cat;
    document.getElementById('searchCatSelect').value = cat;
    applyFilters();
}

function filterPrice(max) {
    currentPrice = max;
    applyFilters();
}

function doSearch() {
    currentSearch = document.getElementById('searchInput').value.trim().toLowerCase();
    currentCat    = document.getElementById('searchCatSelect').value;
    document.querySelectorAll('.cat-btn').forEach(b => {
        const txt = b.textContent.trim();
        if (currentCat === 'All') {
            b.classList.toggle('active', txt.includes('All'));
        } else {
            b.classList.toggle('active', txt.includes(currentCat));
        }
    });
    applyFilters();
}

document.getElementById('searchInput').addEventListener('input', function () {
    currentSearch = this.value.trim().toLowerCase();
    applyFilters();
});

document.getElementById('searchInput').addEventListener('keydown', function (e) {
    if (e.key === 'Enter') doSearch();
});

function applyFilters() {
    const cards = document.querySelectorAll('.product-card');
    let visible = 0;

    cards.forEach(card => {
        const cat    = (card.getAttribute('data-category') || '').trim();
        const price  = parseFloat(card.getAttribute('data-price')) || 0;
        const name   = (card.getAttribute('data-name') || '').toLowerCase();

        const catOk    = currentCat === 'All' || cat === currentCat;
        const priceOk  = currentPrice === 0   || price <= currentPrice;
        const searchOk = currentSearch === ''  || name.includes(currentSearch);

        const show = catOk && priceOk && searchOk;
        card.style.display = show ? '' : 'none';
        if (show) visible++;
    });

    document.getElementById('resultsCount').textContent =
        visible === 0
            ? 'No products found'
            : `Showing ${visible} product${visible !== 1 ? 's' : ''}`;

    const noResults = document.getElementById('noResults');
    const grid      = document.getElementById('productsGrid');
    noResults.style.display = visible === 0 ? 'block' : 'none';
    grid.style.display      = visible === 0 ? 'none'  : 'grid';
}

function sortProducts() {
    const val   = document.getElementById('sortSelect').value;
    const grid  = document.getElementById('productsGrid');
    const cards = Array.from(grid.querySelectorAll('.product-card'));

    cards.sort((a, b) => {
        if (val === 'price-low')  return parseFloat(a.dataset.price) - parseFloat(b.dataset.price);
        if (val === 'price-high') return parseFloat(b.dataset.price) - parseFloat(a.dataset.price);
        if (val === 'name')       return (a.dataset.name || '').localeCompare(b.dataset.name || '');
        return 0;
    });

    cards.forEach(card => grid.appendChild(card));
}
let cart = [];

function openCart() {
    document.getElementById('cartPanel').classList.add('open');
    document.getElementById('cartOverlay').classList.add('open');
}

function closeCart() {
    document.getElementById('cartPanel').classList.remove('open');
    document.getElementById('cartOverlay').classList.remove('open');
}

function addToCart(id, name, price, img) {
    const existing = cart.find(i => i.id === id);
    if (existing) {
        existing.qty++;
    } else {
        cart.push({ id, name, price, img, qty: 1 });
    }
    renderCart();
    openCart();
}

function changeQty(id, delta) {
    const item = cart.find(i => i.id === id);
    if (!item) return;
    item.qty += delta;
    if (item.qty <= 0) cart = cart.filter(i => i.id !== id);
    renderCart();
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

document.addEventListener('DOMContentLoaded', function () {
    document.querySelector('.checkout-btn').addEventListener('click', function () {
        if (cart.length === 0) {
            alert('Your cart is empty!');
            return;
        }
        sessionStorage.setItem('retailhub_cart', JSON.stringify(cart));
        window.location.href = '/api/checkout';
    });
});