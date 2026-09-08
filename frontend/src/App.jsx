import { useState, useEffect } from "react";
import AppRoutes from "./routes/AppRoutes";
import AppLoader from "./Components/Common/AppLoader";
import "./styles/mobile.css";

function App() {

    const [loading, setLoading] = useState(true);

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

    useEffect(() => {

        const timer = setTimeout(() => {

            setLoading(false);

        }, 2500);

        return () => clearTimeout(timer);

    }, []);

    return (

        <>

            <AppLoader
                visible={loading}
            />

            <AppRoutes />

        </>

    );

}

export default App;