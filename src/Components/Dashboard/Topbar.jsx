import {
    Moon,
    Sun
} from "lucide-react";

import {
    useEffect,
    useState
} from "react";

export default function Topbar() {

    const [darkMode, setDarkMode] =
        useState(false);

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
    };

    return (

        <div className="dashboard-topbar">

            <input
                type="text"
                placeholder="Search problems, topics, companies..."
            />

            <div className="topbar-right">

                {/* THEME TOGGLE */}

                <button
                    className="dashboard-theme-toggle"
                    onClick={toggleTheme}
                >

                    {
                        darkMode
                            ? <Sun size={18} />
                            : <Moon size={18} />
                    }

                </button>

                {/* NOTIFICATION */}

                <button>
                    🔔
                </button>

                {/* PROFILE */}

                <div className="profile-circle">

                </div>

            </div>

        </div>
    );
}