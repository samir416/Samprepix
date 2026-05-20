import {
    Moon,
    Sun,
    Menu
} from "lucide-react";

import {
    useEffect,
    useRef,
    useState
} from "react";

import NotificationDropdown from "./NotificationDropdown";
import ProfileDropdown from "./ProfileDropdown";

export default function Topbar({
    sidebarOpen,
    setSidebarOpen
}) {

    const [darkMode, setDarkMode] = useState(false);

    const [openNotifications, setOpenNotifications] = useState(false);

    const [openProfile, setOpenProfile] = useState(false);

    const profileRef = useRef(null);

    const notificationRef = useRef(null);

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

    /* OUTSIDE CLICK */

    useEffect(() => {

        function handleClickOutside(e) {

            if (
                profileRef.current &&
                !profileRef.current.contains(e.target)
            ) {

                setOpenProfile(false);
            }

            if (
                notificationRef.current &&
                !notificationRef.current.contains(e.target)
            ) {

                setOpenNotifications(false);
            }
        }

        document.addEventListener(
            "mousedown",
            handleClickOutside
        );

        return () => {

            document.removeEventListener(
                "mousedown",
                handleClickOutside
            );
        };

    }, []);

    /* TOGGLE THEME */

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

            {/* LEFT */}

            <div className="topbar-left">

                {/* MOBILE HAMBURGER */}

                <button
                    className="mobile-menu-btn"
                    onClick={() =>
                        setSidebarOpen(!sidebarOpen)
                    }
                >

                    <Menu size={22} />

                </button>

                {/* SEARCH */}

                <input
                    type="text"
                    placeholder="Search problems, topics, companies..."
                />

            </div>

            {/* RIGHT */}

            <div className="topbar-right">

                {/* THEME */}

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

                <div
                    className="notification-wrapper"
                    ref={notificationRef}
                >

                    <button
                        className="notification-btn"
                        onClick={() =>
                            setOpenNotifications(
                                !openNotifications
                            )
                        }
                    >

                        🔔

                        <span className="notification-badge">
                            3
                        </span>

                    </button>

                    {
                        openNotifications &&
                        <NotificationDropdown />
                    }

                </div>

                {/* PROFILE */}

                <div
                    className="profile-wrapper"
                    ref={profileRef}
                >

                    <div
                        className="profile-circle"
                        onClick={() =>
                            setOpenProfile(
                                !openProfile
                            )
                        }
                    >

                        S

                    </div>

                    {
                        openProfile &&
                        <ProfileDropdown />
                    }

                </div>

            </div>

        </div>
    );
}