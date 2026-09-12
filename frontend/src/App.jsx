import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";
import AppLoader from "./Components/Common/AppLoader";
import ErrorBoundary from "./Components/Common/ErrorBoundary";
import { initGA, trackPageView } from "./utils/analytics";
import { updatePageSEO } from "./utils/seo";
import "./styles/mobile.css";

function App() {
    const location = useLocation();
    const [loading, setLoading] = useState(false);

    // Initialize Analytics
    useEffect(() => {
        initGA();
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
        // Hydrate Theme Preference
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
        } else {
            document.body.classList.remove("dark-theme");
        }

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
    }, []);

    return (

        <>

            <AppLoader
                visible={loading}
            />

            <ErrorBoundary>
                <AppRoutes />
            </ErrorBoundary>

        </>

    );

}

export default App;