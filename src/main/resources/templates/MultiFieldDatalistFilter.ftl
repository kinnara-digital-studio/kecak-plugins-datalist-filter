<style>
    :root {
        --odoo-purple: #714B67;
        --border-color: #ced4da;
        --hover-bg: #f8f9fa;
    }

    body {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        background-color: #f0f2f5;
        padding: 50px;
    }

    .search-container {
        position: relative;
        width: 500px;
        margin: 0 auto;
    }

    /* Input Box */
    .search-box {
        display: flex;
        align-items: center;
        background: white;
        border: 1px solid var(--border-color);
        border-radius: 4px 4px 0 0;
        padding: 5px 10px;
    }

    .search-box input {
        border: none;
        outline: none;
        width: 100%;
        padding: 8px;
        font-size: 14px;
    }

    .search-icon { color: #666; margin-right: 5px; }

    /* Dropdown Menu */
    .dropdown-menu {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background: white;
        border: 1px solid var(--border-color);
        border-top: none;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 1000;
        display: none; /* Sembunyikan default */
    }

    .dropdown-item {
        padding: 8px 20px;
        cursor: pointer;
        font-size: 14px;
        color: #444;
        display: flex;
        align-items: center;
    }

    .dropdown-item:hover {
        background-color: var(--hover-bg);
    }

    .dropdown-item b {
        margin: 0 4px;
        color: #333;
    }

    .dropdown-item i {
        font-style: italic;
        color: var(--odoo-purple);
        font-weight: bold;
    }

    /* Icon panah kecil di kiri item tertentu */
    .has-arrow::before {
        content: '▶';
        font-size: 8px;
        margin-right: 10px;
        color: #888;
    }

    .no-arrow { padding-left: 36px; }

    /* Tampilkan saat input fokus */
    .search-container:focus-within .dropdown-menu {
        display: block;
    }
</style>

<div class="search-container">
    <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="text" id="searchInput" placeholder="Search..." value="">
        <span style="font-size: 10px; color: #666;">▼</span>
    </div>

    <div class="dropdown-menu" id="dropdownMenu">
        <div class="dropdown-item no-arrow">Search <b>Employee</b> for: <i class="term"></i></div>
        <div class="dropdown-item has-arrow">Search <b>Manager</b> for: <i class="term"></i></div>
        <div class="dropdown-item has-arrow">Search <b>Job Position</b> for: <i class="term"></i></div>
        <div class="dropdown-item no-arrow">Search <b>Skills</b> for: <i class="term"></i></div>
        <div class="dropdown-item no-arrow">Search <b>Resume</b> for: <i class="term"></i></div>
        <div class="dropdown-item has-arrow">Search <b>Coach</b> for: <i class="term"></i></div>
        <div class="dropdown-item no-arrow">Search <b>Tags</b> for: <i class="term"></i></div>
        <div class="dropdown-item no-arrow">Search <b>Private Car Plate</b> for: <i class="term"></i></div>
    </div>
</div>

<script>
    // Script sederhana untuk mengupdate teks saat mengetik
    const input = document.getElementById('searchInput');
    const terms = document.querySelectorAll('.term');

    input.addEventListener('input', (e) => {
        const val = e.target.value;
        terms.forEach(el => {
            el.textContent = val;
        });
    });
</script>