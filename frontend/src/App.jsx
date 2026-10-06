import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";
import AppLoader from "./Components/Common/AppLoader";
import ErrorBoundary from "./Components/Common/ErrorBoundary";
import ReportProblemModal from "./Components/Support/ReportProblemModal";
import AIHelpBot from "./Components/Support/AIHelpBot";
import { getCleanToken } from "./services/authService";
import { initGA, trackPageView } from "./utils/analytics";
import { updatePageSEO } from "./utils/seo";
import "./styles/mobile.css";

// Internal application routes where the AI Assistant is allowed to appear
const LOGGED_IN_APP_ROUTES = [
    "/dashboard",
    "/resume-analyzer",
    "/mock-interview",
    "/interview-result",
    "/coding-arena",
    "/aptitude",
    "/performance",
    "/analytics",
    "/billing-history",
    "/github-analyzer",
    "/ai-roadmap",
    "/profile",
    "/admin"
];

function App() {
    const location = useLocation();
    const [loading, setLoading] = useState(false);
    const [isReportModalOpen, setIsReportModalOpen] = useState(false);
    const [reportModalData, setReportModalData] = useState({});

    // Listen for global open-report-problem events
    useEffect(() => {
        const handleOpenReport = (e) => {
            setReportModalData(e.detail || {});
            setIsReportModalOpen(true);
        };
        window.addEventListener("open-report-problem", handleOpenReport);
        return () => window.removeEventListener("open-report-problem", handleOpenReport);
    }, []);

    // Initialize Analytics
    useEffect(() => {
        initGA();
    }, []);

    // Global theme toggle shortcut: Ctrl + Shift + L
    useEffect(() => {
        const handleKeyDown = (e) => {
            if (e.ctrlKey && e.shiftKey && (e.key === "L" || e.key === "l")) {
                const target = document.activeElement;
                const tagName = target?.tagName?.toLowerCase();
                const isContentEditable = target?.isContentEditable;
                if (
                    tagName === "input" ||
                    tagName === "textarea" ||
                    tagName === "select" ||
                    isContentEditable
                ) {
                    return;
                }
                e.preventDefault();
                const isDark = !document.body.classList.contains("dark-theme");
                if (isDark) {
                    document.body.classList.add("dark-theme");
                    document.body.classList.remove("light-theme");
                } else {
                    document.body.classList.remove("dark-theme");
                    document.body.classList.add("light-theme");
                }
                localStorage.setItem("theme", isDark ? "dark" : "light");
                localStorage.setItem("themePreference", isDark ? "dark" : "light");
                window.dispatchEvent(new Event("themechange"));
                window.dispatchEvent(new Event("themeChanged"));
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, []);

    // Track SPA route changes and manage private vs public indexing
    useEffect(() => {
        const fullPath = location.pathname + location.search;
        trackPageView(fullPath, document.title);

        const privateRoutes = [
            "/dashboard",
            "/coding-arena",
            "/mock-interview",
            "/interview-result",
            "/aptitude",
            "/performance",
            "/analytics",
            "/profile",
            "/onboarding"
        ];
        const isPrivate = privateRoutes.some((pr) => location.pathname.startsWith(pr));
        if (isPrivate) {
            updatePageSEO({
                title: "Candidate Workspace | Samprepix",
                description: "Authenticated candidate dashboard and practice area.",
                canonicalPath: location.pathname,
                noIndex: true
            });
        }
    }, [location]);

    // Centralized route scroll restoration (always start new page navigation from top)
    useEffect(() => {
        if (!location.hash) {
            window.scrollTo({ top: 0, left: 0, behavior: "instant" });
        } else {
            const id = location.hash.replace("#", "");
            const element = document.getElementById(id);
            if (element) {
                element.scrollIntoView({ behavior: "smooth" });
            }
        }
    }, [location.pathname, location.hash]);

    useEffect(() => {
        const applyTheme = () => {
            const themePref = localStorage.getItem("themePreference") || localStorage.getItem("theme") || "system";
            let isDark = false;
            if (themePref === "dark") {
                isDark = true;
            } else if (themePref === "light") {
                isDark = false;
            } else {
                isDark = typeof window !== "undefined" && window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
            }

            if (isDark) {
                document.body.classList.add("dark-theme");
                document.body.classList.remove("light-theme");
            } else {
                document.body.classList.remove("dark-theme");
                document.body.classList.add("light-theme");
            }
        };

        applyTheme();
        window.addEventListener("storage", applyTheme);
        window.addEventListener("themeChanged", applyTheme);
        window.addEventListener("themechange", applyTheme);

        // Hydrate Interface Density
        const density = localStorage.getItem("setting_interface_density") || "comfortable";
        if (density === "compact") {
            document.body.classList.add("density-compact");
        } else {
            document.body.classList.remove("density-compact");
        }

        // Hydrate Reduced Motion Preference
        const reducedMotion = localStorage.getItem("setting_reduced_motion") === "true";
        if (reducedMotion) {
            document.body.classList.add("reduce-motion");
        } else {
            document.body.classList.remove("reduce-motion");
        }

        // Enable smooth theme transitions after initial hydration without load flicker
        const transitionTimer = setTimeout(() => {
            document.body.classList.add("theme-transition-ready");
        }, 50);

        return () => {
            clearTimeout(transitionTimer);
            window.removeEventListener("storage", applyTheme);
            window.removeEventListener("themeChanged", applyTheme);
            window.removeEventListener("themechange", applyTheme);
        };
    }, []);

    // Render AI Assistant ONLY when user is authenticated AND inside logged-in application area
    const isAuthAppRoute = LOGGED_IN_APP_ROUTES.some((route) =>
        location.pathname === route || location.pathname.startsWith(`${route}/`)
    );
    const isAuthenticated = Boolean(getCleanToken());
    const shouldRenderAssistant = isAuthenticated && isAuthAppRoute;

    return (

        <>

            <AppLoader
                visible={loading}
            />

            <ErrorBoundary>
                <AppRoutes />
            </ErrorBoundary>

            <ReportProblemModal
                isOpen={isReportModalOpen}
                onClose={() => setIsReportModalOpen(false)}
                initialData={reportModalData}
            />

            {shouldRenderAssistant && <AIHelpBot />}
        </>
    );

}

export default App;