import {
    Moon,
    Sun,
    Menu,
    X,
    Settings
} from "lucide-react";

import {
    useEffect,
    useRef,
    useState
} from "react";

import NotificationDropdown from "./NotificationDropdown";
import ProfileDropdown from "./ProfileDropdown";

export default function Topbar() {

    const [darkMode, setDarkMode] = useState(false);

    const [openNotifications, setOpenNotifications] = useState(false);

    const [openProfile, setOpenProfile] = useState(false);

    const [openMobileMenu, setOpenMobileMenu] = useState(false);

    const [openSettings, setOpenSettings] = useState(false);

    const profileRef = useRef(null);

    const notificationRef = useRef(null);

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

        <>

            {/* MOBILE PREMIUM TOPBAR */}

            <div className="mobile-premium-topbar">

                {/* LEFT MENU */}

                <button
                    className="mobile-menu-btn"
                    onClick={() =>
                        setOpenMobileMenu(
                            !openMobileMenu
                        )
                    }
                >

                    {
                        openMobileMenu
                            ? <X size={22} />
                            : <Menu size={22} />
                    }

                </button>

                {/* SEARCH */}

                <div className="mobile-search-wrap">

                    <input
                        type="text"
                        placeholder="Search problems, topics..."
                    />

                </div>

                {/* SETTINGS */}

                <div className="mobile-settings-wrapper">

                    <button
                        className="mobile-settings-btn"
                        onClick={() =>
                            setOpenSettings(
                                !openSettings
                            )
                        }
                    >

                        <Settings size={20} />

                    </button>

                    {

                        openSettings &&

                        <div className="mobile-settings-dropdown">

                            {/* THEME */}

                            <button
                                className="mobile-setting-item"
                                onClick={toggleTheme}
                            >

                                {
                                    darkMode
                                        ? <Sun size={18} />
                                        : <Moon size={18} />
                                }

                                Theme

                            </button>

                            {/* NOTIFICATION */}

                            <button
                                className="mobile-setting-item"
                                onClick={() =>
                                    setOpenNotifications(
                                        !openNotifications
                                    )
                                }
                            >

                                🔔 Notifications

                            </button>

                            {/* PROFILE */}

                            <button
                                className="mobile-setting-item"
                                onClick={() =>
                                    setOpenProfile(
                                        !openProfile
                                    )
                                }
                            >

                                👤 Profile

                            </button>

                        </div>
                    }

                </div>

            </div>

            {/* MOBILE SLIDE MENU */}

            {

                openMobileMenu &&

                <div className="mobile-slide-menu">

                    <a href="/dashboard">

                        Dashboard

                    </a>

                    <a href="/resume-analyzer">

                        Resume Analyzer

                    </a>

                    <a href="/mock-interview">

                        AI Interview

                    </a>

                    <a href="/coding-arena">

                        Coding Arena

                    </a>

                    <a href="/performance">

                        Performance

                    </a>

                </div>
            }

            {/* DESKTOP TOPBAR */}

            <div className="dashboard-topbar">

                {/* SEARCH */}

                <div className="topbar-left">

                    <input
                        type="text"
                        placeholder="Search problems, topics..."
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

        </>
    );
}