document.addEventListener("DOMContentLoaded", function () {
    const seasonCards = document.querySelectorAll(".f7-season-card");

    seasonCards.forEach(function (card) {
        const toggleBtn = card.querySelector(".f7-season-toggle");
        if (!toggleBtn) {
            return;
        }
        toggleBtn.addEventListener("click", function () {
            const isCollapsed = card.classList.toggle("is-collapsed");
            toggleBtn.setAttribute("aria-expanded", String(!isCollapsed));
        });
    });

    const expandAllBtn = document.querySelector("[data-f7-expand-all]");
    if (expandAllBtn) {
        expandAllBtn.addEventListener("click", function () {
            seasonCards.forEach(function (card) {
                card.classList.remove("is-collapsed");
                const toggleBtn = card.querySelector(".f7-season-toggle");
                if (toggleBtn) {
                    toggleBtn.setAttribute("aria-expanded", "true");
                }
            });
        });
    }

    const collapseAllBtn = document.querySelector("[data-f7-collapse-all]");
    if (collapseAllBtn) {
        collapseAllBtn.addEventListener("click", function () {
            seasonCards.forEach(function (card) {
                card.classList.add("is-collapsed");
                const toggleBtn = card.querySelector(".f7-season-toggle");
                if (toggleBtn) {
                    toggleBtn.setAttribute("aria-expanded", "false");
                }
            });
        });
    }

    document.addEventListener("submit", function (event) {
        const form = event.target;
        if (!(form instanceof HTMLFormElement)) {
            return;
        }
        const submitterConfirm = event.submitter
            ? event.submitter.getAttribute("data-confirm")
            : null;
        const formConfirm = form.getAttribute("data-confirm");
        const childConfirmEl = form.querySelector("[data-confirm]");
        const confirmMessage = submitterConfirm
            || formConfirm
            || (childConfirmEl ? childConfirmEl.getAttribute("data-confirm") : null);

        if (confirmMessage && !window.confirm(confirmMessage)) {
            event.preventDefault();
        }
    });
});
