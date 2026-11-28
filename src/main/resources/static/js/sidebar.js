document.addEventListener("DOMContentLoaded", function () {
    const toggleBtn = document.getElementById("ccSidebarToggle");
    const sidebar = document.getElementById("ccSidebar");
    const backdrop = document.getElementById("ccSidebarBackdrop");
    const closeBtn = document.getElementById("ccSidebarClose");

    function normalizePath(path) {
        if (!path || path === "/") {
            return "/";
        }
        return path.endsWith("/") ? path.slice(0, -1) : path;
    }

    if (sidebar) {
        const currentPath = normalizePath(window.location.pathname);
        const links = sidebar.querySelectorAll(".cc-sidebar-link");

        links.forEach(function (link) {
            let linkPath;
            try {
                linkPath = normalizePath(new URL(link.href, window.location.origin).pathname);
            } catch (ignored) {
                return;
            }

            const isMatch = linkPath === "/"
                ? currentPath === "/"
                : (currentPath === linkPath || currentPath.startsWith(linkPath + "/"));

            if (isMatch) {
                link.classList.add("is-active");
                link.setAttribute("aria-current", "page");
                const parentGroup = link.closest("details.cc-sidebar-group");
                if (parentGroup) {
                    parentGroup.open = true;
                }
            }
        });
    }

    if (!toggleBtn || !sidebar || !backdrop) {
        return;
    }

    function openSidebar() {
        sidebar.classList.add("is-open");
        backdrop.classList.add("is-open");
        sidebar.setAttribute("aria-hidden", "false");
        backdrop.setAttribute("aria-hidden", "false");
        toggleBtn.setAttribute("aria-expanded", "true");
        document.body.classList.add("cc-scroll-locked");

        if (closeBtn) {
            closeBtn.focus({ preventScroll: true });
        }
    }

    function closeSidebar(restoreFocus) {
        sidebar.classList.remove("is-open");
        backdrop.classList.remove("is-open");
        sidebar.setAttribute("aria-hidden", "true");
        backdrop.setAttribute("aria-hidden", "true");
        toggleBtn.setAttribute("aria-expanded", "false");
        document.body.classList.remove("cc-scroll-locked");

        if (restoreFocus) {
            toggleBtn.focus({ preventScroll: true });
        }
    }

    toggleBtn.addEventListener("click", function () {
        if (sidebar.classList.contains("is-open")) {
            closeSidebar(false);
        } else {
            openSidebar();
        }
    });

    if (closeBtn) {
        closeBtn.addEventListener("click", function () {
            closeSidebar(true);
        });
    }

    backdrop.addEventListener("click", function () {
        closeSidebar(false);
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && sidebar.classList.contains("is-open")) {
            closeSidebar(true);
        }
    });
});
