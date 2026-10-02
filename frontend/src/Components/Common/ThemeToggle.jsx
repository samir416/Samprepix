import {
    Moon,
    Sun
} from "lucide-react";

import {
    useEffect,
    useState
} from "react";

export default function ThemeToggle() {

    const [darkMode, setDarkMode] = useState(false);

    /* LOAD SAVED THEME */

    useEffect(() => {

        const savedTheme =
            localStorage.getItem("theme");

        if (savedTheme === "dark") {

            document.body.classList.add(
                "dark-theme"
            );

            setDarkMode(true);
        }

        const handleThemeChange = () => {
            setDarkMode(document.body.classList.contains("dark-theme"));
        };
        window.addEventListener("themechange", handleThemeChange);
        return () => window.removeEventListener("themechange", handleThemeChange);

    }, []);

    /* TOGGLE */

    const toggleTheme = () => {

        document.body.classList.toggle(
            "dark-theme"
        );

        const isDark =
            document.body.classList.contains(
                "dark-theme"
            );

        setDarkMode(isDark);

        localStorage.setItem(
            "theme",
            isDark ? "dark" : "light"
        );

        window.dispatchEvent(new Event("themechange"));
    };

    return (

        <button
            className="theme-toggle-btn"
            onClick={toggleTheme}
            title="Toggle theme · Ctrl + Shift + L"
            aria-label={
                darkMode
                    ? "Switch to light mode"
                    : "Switch to dark mode"
            }
            aria-pressed={darkMode}
        >

            <span className="theme-toggle-glow"></span>

            <span className="theme-toggle-icon">

                {
                    darkMode
                        ? <Sun size={18} />
                        : <Moon size={18} />
                }

            </span>

        </button>
    );
}