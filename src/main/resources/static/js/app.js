/* Date Convertor — plain vanilla JS to talk to the REST API */
(function () {
    "use strict";

    var form = document.querySelector(".date-form");
    var card = form.closest(".card");
    var modeBtns = document.querySelectorAll(".mode-btn");
    var todayBtn = document.querySelector(".today-btn");
    var copyBtn = card.querySelector(".copy-btn");
    var resultDateEl = card.querySelector(".js-result-date");

    var KIND = {
        bs: { endpoint: "/api/convert/bs-to-ad", yMin: 1970, yMax: 2100, dMax: 32, format: "YYYY/MM/DD", resultLabel: "English date (AD)" },
        ad: { endpoint: "/api/convert/ad-to-bs", yMin: 1913, yMax: 2044, dMax: 31, format: "YYYY-MM-DD", resultLabel: "Nepali date (BS)" }
    };

    /* --- mode toggle ------------------------------------------------------ */
    function setMode(mode) {
        var cfg = KIND[mode];
        form.dataset.kind = mode;
        form.dataset.endpoint = cfg.endpoint;
        form.year.min = cfg.yMin;
        form.year.max = cfg.yMax;
        form.month.min = 1;
        form.month.max = 12;
        form.day.min = 1;
        form.day.max = cfg.dMax;
        document.getElementById("format-hint").textContent = "Format: " + cfg.format;
        document.getElementById("result-label").textContent = cfg.resultLabel;
        todayBtn.hidden = mode !== "ad";
        modeBtns.forEach(function (btn) {
            var active = btn.dataset.mode === mode;
            btn.classList.toggle("is-active", active);
            btn.setAttribute("aria-selected", active ? "true" : "false");
        });
        clearFeedback();
        refreshDayMax();
    }

    modeBtns.forEach(function (btn) {
        btn.addEventListener("click", function () { setMode(btn.dataset.mode); });
    });

    /* --- day / month clamping -------------------------------------------- */
    function adMaxDay(year, month) {
        return new Date(year, month, 0).getDate();
    }

    function clampDay(max) {
        if (form.day.value && Number(form.day.value) > max) form.day.value = max;
    }

    function refreshDayMax() {
        var y = parseInt(form.year.value, 10);
        var m = parseInt(form.month.value, 10);
        if (!y || !m || m < 1 || m > 12) return;

        if (form.dataset.kind === "ad") {
            form.day.max = adMaxDay(y, m);
            clampDay(form.day.max);
            return;
        }

        fetch("/api/convert/days-in-month?kind=bs&year=" + y + "&month=" + m)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data && data.days) {
                    form.day.max = data.days;
                    clampDay(data.days);
                }
            })
            .catch(function () { /* keep current max */ });
    }

    form.month.addEventListener("input", function () {
        if (form.month.value && Number(form.month.value) > 12) form.month.value = 12;
        if (form.month.value.length >= 2) form.day.focus();
        refreshDayMax();
    });

    form.year.addEventListener("input", function () {
        if (form.year.value.length >= 4) form.month.focus();
        refreshDayMax();
    });

    form.day.addEventListener("input", function () {
        if (form.day.value && Number(form.day.value) > form.day.max) form.day.value = form.day.max;
    });

    /* "Today" convenience (AD mode only) */
    todayBtn.addEventListener("click", function () {
        var now = new Date();
        form.year.value = now.getFullYear();
        form.month.value = now.getMonth() + 1;
        form.day.value = now.getDate();
        form.day.max = adMaxDay(now.getFullYear(), now.getMonth() + 1);
        clearFeedback();
    });

    /* --- submit / result -------------------------------------------------- */
    form.addEventListener("submit", handleSubmit);

    copyBtn.addEventListener("click", function () {
        var text = resultDateEl.textContent.trim();
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(flashCopied.bind(null, copyBtn));
        } else {
            var ta = document.createElement("textarea");
            ta.value = text;
            document.body.appendChild(ta);
            ta.select();
            try { document.execCommand("copy"); } catch (e) { /* noop */ }
            document.body.removeChild(ta);
            flashCopied(copyBtn);
        }
    });

    function handleSubmit(event) {
        event.preventDefault();
        var kind = form.dataset.kind;
        var cfg = KIND[kind];

        var parsed = { year: Number(form.year.value), month: Number(form.month.value), day: Number(form.day.value) };

        clearFeedback();

        var check = validate(parsed, cfg);
        if (check) {
            showError(check);
            return;
        }

        var date = kind === "ad"
            ? pad(form.year.value) + "-" + pad(form.month.value) + "-" + pad(form.day.value)
            : pad(form.year.value) + "/" + pad(form.month.value) + "/" + pad(form.day.value);

        setLoading(true);

        fetch(form.dataset.endpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ date: date })
        })
            .then(function (res) {
                return res.text().then(function (text) {
                    var data = null;
                    try { data = JSON.parse(text); } catch (e) { /* non-JSON response */ }
                    return { ok: res.ok, data: data };
                });
            })
            .then(function (result) {
                if (result.ok) {
                    if (!result.data || !result.data.bsDate) {
                        showError("Unexpected response from the server.");
                    } else {
                        showResult(kind === "ad" ? result.data.bsDate : result.data.adDate);
                    }
                } else {
                    showError((result.data && result.data.error) || "Conversion failed. Please check the date.");
                }
            })
            .catch(function () {
                showError("Could not reach the server. Is the app running?");
            })
            .finally(function () {
                setLoading(false);
            });
    }

    function validate(d, cfg) {
        if (!Number.isInteger(d.year) || d.year < cfg.yMin || d.year > cfg.yMax) {
            return "Year must be between " + cfg.yMin + " and " + cfg.yMax + ".";
        }
        if (!Number.isInteger(d.month) || d.month < 1 || d.month > 12) {
            return "Month must be between 1 and 12.";
        }
        var maxDay = Number(form.day.max) || cfg.dMax;
        if (!Number.isInteger(d.day) || d.day < 1 || d.day > maxDay) {
            return "Day must be between 1 and " + maxDay + ".";
        }
        return null;
    }

    function showResult(text) {
        resultDateEl.textContent = text;
        card.querySelector(".result").hidden = false;
        copyBtn.classList.remove("copied");
        copyBtn.textContent = "Copy";
    }

    function showError(message) {
        var err = card.querySelector(".error");
        err.textContent = message;
        err.hidden = false;
        card.querySelector(".result").hidden = true;
    }

    function clearFeedback() {
        card.querySelector(".error").hidden = true;
        card.querySelector(".result").hidden = true;
    }

    function setLoading(loading) {
        var btn = form.querySelector(".btn");
        var label = btn.querySelector(".btn-label");
        var spinner = btn.querySelector(".btn-spinner");
        btn.disabled = loading;
        label.hidden = loading;
        spinner.hidden = !loading;
    }

    function flashCopied(btn) {
        btn.classList.add("copied");
        btn.textContent = "Copied!";
        setTimeout(function () {
            btn.classList.remove("copied");
            btn.textContent = "Copy";
        }, 1500);
    }

    function pad(n) {
        n = String(n);
        return n.length === 1 ? "0" + n : n;
    }
})();
