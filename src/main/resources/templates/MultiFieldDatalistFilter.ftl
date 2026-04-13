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
    padding: 0;  /* ← biarkan child yang atur */
    overflow: hidden; /* agar border-radius chip terapply ke child */
    font-size: 12px;
    white-space: nowrap;
}
.mf-chip-remove {
    cursor: pointer;
    font-weight: bold;
    line-height: 1;
    color: #714B67;
    padding: 2px 6px 2px 0;
}
.mf-chip-remove:hover { color: #a00; }

.mf-chip-gear {
    cursor: pointer;
    font-weight: bold;
    line-height: 1;
    color: #714B67;
    padding: 2px 6px;
    background: #e8dce5;
    border-right: 1px solid #c8a8bc;
}
.mf-chip-gear:hover { color: #a00; }

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

    <input type="hidden" id="${name}-jsonForm" value="${jsonForm!}"/>
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
                <span class="mf-chip-gear" title="Settings">⚙</span>
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

    var jsonForm = JSON.parse(document.getElementById('${name}-jsonForm').value || '{}');
    var nonce = '${nonce!}';

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
        var chip = e.target.closest('.mf-chip');
        if (!chip) return;
        var val = chip.dataset.value;
        if(e.target.classList.contains('mf-chip-gear')) {
            var parts = val.split(':');
            var colName = parts[0];
            var term = parts[1];
            var data = {
                field_name: colName,
                search_value: term
            };
            var appId = "${appId!''}";
            var appVersion = "${appVersion!''}";
            popupForm(wrapperId, appId, appVersion, jsonForm, nonce, {}, data, 800, 900);
            return;
        }
        if(e.target.classList.contains('mf-chip-remove')) {
            removeChip(chip, val);
        }
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
        chip.innerHTML = '<span class="mf-chip-gear" title="Settings">⚙</span>' + escapeHtml(label) + ' <span class="mf-chip-remove" title="Remove">\u00d7</span>';
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

    function popupForm(elementId, appId, appVersion, jsonForm, nonce, args, data, height, width) {
        <#--  let isEditable = ${editable?c};  -->
        let label = 'Submit';
        let formUrl = '${request.contextPath}/web/app/' + appId + '/' + appVersion + '/form/embed?_submitButtonLabel=' + label;
        let frameId = args.frameId = 'Frame_' + elementId;

        if (data) {
          for (var key in data) {
              if (data.hasOwnProperty(key) && data[key]) {
                  if (formUrl.indexOf("?") !== -1) {
                      formUrl += "&";
                  } else {
                      formUrl += "?";
                  }
                  formUrl += encodeURIComponent(key) + "=" + encodeURIComponent(data[key]);
              }
          }
        }
        formUrl += UI.userviewThemeParams();

        var params = {
          _json : JSON.stringify(jsonForm ? jsonForm : {}),
          _callback : 'onSubmitted',
          _setting : JSON.stringify(args ? args : {}).replace(/"/g, "'"),
          _jsonrow : JSON.stringify(data ? data : {}),
          _nonce : nonce
        };
        JPopup.show(frameId, formUrl, params, "", width, height);
    }

    function onSubmitted(args) {
        let result = JSON.parse(args.result);
        let frameId = args.frameId;

        if (JPopup) {
            JPopup.hide(frameId);
        }

        if (result && result.id) {
            var updatedId = result.id;
            var updatedLabel = result[labelField] || "(no label)";
            var updatedStatus = result[statusField] || "";

            var rawCanMove = result[canMoveField];
            canMoveMap[updatedId] = (rawCanMove === "false" || rawCanMove === false) ? false : true;

            var itemData = {
                id: updatedId,
                title: createItemHtml(updatedLabel)
            };

            kanbanBoard.removeElement(updatedId);
            if (updatedStatus) {
                kanbanBoard.addElement(updatedStatus, itemData);

                setTimeout(function() {
                  var el = document.querySelector('.kanban-item[data-eid="' + updatedId + '"]');
                  if (el) {
                    var canMove = canMoveMap[updatedId];
                    if (canMove === false) {
                      el.style.cursor = 'default';
                      el.title = 'Item cannot be moved';
                    } else {
                      el.style.cursor = 'grab';
                      el.removeAttribute('title');
                    }
                  }
                }, 0);
            }
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