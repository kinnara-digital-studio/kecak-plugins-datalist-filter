<#--
  Odoo-style Multi-Field Datalist Filter
  =======================================
  - Renders a text input with a dropdown showing "Search [ColLabel] for: [term]" per column
  - Selected tokens are stored as hidden <option> elements in a <select multiple>
  - No AJAX / API calls — purely client-side interaction
  - On selection the form auto-submits to trigger the datalist reload
-->

<style>
.mf-filter-wrapper {
    position: relative;
    display: inline-block;
    min-width: 320px;
    font-family: inherit;
}

/* ---- Chip + input row ---- */
.mf-input-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
    background: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 4px 8px;
    cursor: text;
    min-height: 34px;
}
.mf-input-row:focus-within {
    border-color: #714B67;
    box-shadow: 0 0 0 2px rgba(113,75,103,.2);
}

/* ---- Chips ---- */
.mf-chip {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    background: #F3EEF1;
    color: #714B67;
    border: 1px solid #c8a8bc;
    border-radius: 12px;
    padding: 2px 8px;
    font-size: 12px;
    white-space: nowrap;
}
.mf-chip-remove {
    cursor: pointer;
    font-weight: bold;
    line-height: 1;
    color: #714B67;
}
.mf-chip-remove:hover { color: #a00; }

/* ---- Text input ---- */
.mf-text-input {
    border: none;
    outline: none;
    flex: 1;
    min-width: 100px;
    font-size: 14px;
    background: transparent;
    padding: 2px 4px;
}

/* ---- Dropdown ---- */
.mf-dropdown {
    display: none;
    position: absolute;
    top: calc(100% + 2px);
    left: 0;
    right: 0;
    background: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-shadow: 0 4px 12px rgba(0,0,0,.12);
    z-index: 9999;
    max-height: 260px;
    overflow-y: auto;
}
.mf-dropdown.open { display: block; }

.mf-dropdown-item {
    padding: 8px 14px;
    font-size: 13px;
    color: #444;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
    border-bottom: 1px solid #f0f0f0;
}
.mf-dropdown-item:last-child { border-bottom: none; }
.mf-dropdown-item:hover,
.mf-dropdown-item.active {
    background: #f8f3f7;
    color: #714B67;
}
.mf-dropdown-item .mf-fi-label { font-weight: 600; }
.mf-dropdown-item .mf-fi-term  { font-style: italic; color: #714B67; }
.mf-dropdown-item .mf-fi-icon  { color: #bbb; font-size: 11px; }
</style>

<div class="mf-filter-wrapper" id="mfWrapper_${name}">

    <#-- Hidden select that carries filter values to the server -->
    <select id="mfSelect_${name}" name="${name}" multiple style="display:none;">
        <#list selectedChips! as chip>
            <option value="${chip.value?html}" selected>${chip.label?html}</option>
        </#list>
        <#-- Dummy empty option so browsers always submit the field -->
        <#if !(selectedChips?has_content)>
            <option value="" selected></option>
        </#if>
    </select>

    <#-- Visible row: chips + text input -->
    <div class="mf-input-row" id="mfInputRow_${name}">
        <#list selectedChips! as chip>
            <span class="mf-chip" data-value="${chip.value?html}">
                ${chip.label?html}
                <span class="mf-chip-remove" title="Remove">×</span>
            </span>
        </#list>
        <input
            type="text"
            class="mf-text-input"
            id="mfTextInput_${name}"
            placeholder="${placeholder!}"
            autocomplete="off"
        />
    </div>

    <#-- Dropdown list -->
    <div class="mf-dropdown" id="mfDropdown_${name}">
        <#list columns! as col>
            <div class="mf-dropdown-item"
                 data-col="${col.columnName?html}"
                 data-col-label="${col.columnLabel?html}">
                <span class="mf-fi-icon">▶</span>
                Search <span class="mf-fi-label">&nbsp;${col.columnLabel?html}&nbsp;</span> for:
                <span class="mf-fi-term"></span>
            </div>
        </#list>
    </div>
</div>

<script>
(function () {
    var wrapperId   = 'mfWrapper_${name}';
    var selectId    = 'mfSelect_${name}';
    var inputRowId  = 'mfInputRow_${name}';
    var textInputId = 'mfTextInput_${name}';
    var dropdownId  = 'mfDropdown_${name}';

    var wrapper   = document.getElementById(wrapperId);
    var select    = document.getElementById(selectId);
    var inputRow  = document.getElementById(inputRowId);
    var textInput = document.getElementById(textInputId);
    var dropdown  = document.getElementById(dropdownId);

    // Click on input-row focusses the hidden text field
    inputRow.addEventListener('click', function (e) {
        if (!e.target.classList.contains('mf-chip-remove')) {
            textInput.focus();
        }
    });

    // Typing: update term spans + show/hide dropdown
    textInput.addEventListener('input', function () {
        var term = this.value.trim();
        dropdown.querySelectorAll('.mf-fi-term').forEach(function (el) {
            el.textContent = term;
        });
        if (term.length > 0) {
            dropdown.classList.add('open');
        } else {
            dropdown.classList.remove('open');
        }
    });

    // Click on a dropdown item → add chip only (user clicks Show button to apply)
    dropdown.addEventListener('click', function (e) {
        var item = e.target.closest('.mf-dropdown-item');
        if (!item) return;

        var colName  = item.dataset.col;
        var colLabel = item.dataset.colLabel;
        var term     = textInput.value.trim();
        if (!term) return;

        var tokenValue = colName + ':' + term;
        var tokenLabel = colLabel + ': ' + term;

        addChip(tokenValue, tokenLabel);
        textInput.value = '';
        dropdown.classList.remove('open');
    });

    // Remove chip (user clicks Show button to re-apply)
    inputRow.addEventListener('click', function (e) {
        if (!e.target.classList.contains('mf-chip-remove')) return;
        var chip = e.target.closest('.mf-chip');
        if (!chip) return;
        var val = chip.dataset.value;
        removeChip(chip, val);
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function (e) {
        if (!wrapper.contains(e.target)) {
            dropdown.classList.remove('open');
        }
    });

    // Enter key: if dropdown has one visible item, select it; else free search
    textInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            var term = this.value.trim();
            if (!term) return;
            var items = dropdown.querySelectorAll('.mf-dropdown-item');
            if (items.length === 1) {
                items[0].click();
            } else {
                // free search – empty colName means search across all
                addChip('*:' + term, 'All: ' + term);
                this.value = '';
                dropdown.classList.remove('open');
                submitForm();
            }
        }
    });

    // ---- helpers ----

    function addChip(value, label) {
        // Remove dummy empty option if present
        var dummy = select.querySelector('option[value=""]');
        if (dummy) dummy.remove();

        // Avoid duplicates
        var existing = select.querySelector('option[value="' + CSS.escape(value) + '"]');
        if (existing) return;

        // Add to hidden select
        var opt = document.createElement('option');
        opt.value = value;
        opt.selected = true;
        opt.textContent = label;
        select.appendChild(opt);

        // Add visual chip
        var chip = document.createElement('span');
        chip.className = 'mf-chip';
        chip.dataset.value = value;
        chip.innerHTML = escapeHtml(label) + ' <span class="mf-chip-remove" title="Remove">\u00d7</span>';
        inputRow.insertBefore(chip, textInput);
    }

    function removeChip(chipEl, value) {
        chipEl.remove();
        var opt = select.querySelector('option[value="' + CSS.escape(value) + '"]');
        if (opt) opt.remove();
        // Re-add dummy if no tokens left
        if (!select.querySelector('option')) {
            var dummy = document.createElement('option');
            dummy.value = '';
            dummy.selected = true;
            select.appendChild(dummy);
        }
    }

    function submitForm() {
        var form = select.closest('form');
        if (form) form.submit();
    }

    function escapeHtml(str) {
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }
})();
</script>