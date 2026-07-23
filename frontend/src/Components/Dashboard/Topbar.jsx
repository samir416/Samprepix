import {
    Moon,
    Sun,
    Menu,
    X,
    Settings,
    LayoutDashboard,
    FileText,
    Mic,
    Code2,
    BarChart3,
    Bell,
    User,
    ArrowLeft
} from "lucide-react";

import {
    useEffect,
    useRef,
    useState
} from "react";

import NotificationDropdown from "./NotificationDropdown";
import ProfileDropdown from "./ProfileDropdown";
import { useNavigate } from "react-router-dom";

export default function Topbar() {

    /* =========================
       STATES
    ========================= */

    const [darkMode, setDarkMode] = useState(false);

    const [openNotifications, setOpenNotifications] =
        useState(false);

    const [openMobileProfile, setOpenMobileProfile] =
        useState(false);

    const [openDesktopProfile, setOpenDesktopProfile] =
        useState(false);

    const [openMobileMenu, setOpenMobileMenu] =
        useState(false);

    const [openSettings, setOpenSettings] =
        useState(false);

    const [user, setUser] = useState(null);

    /* =========================
       REFS
    ========================= */

    const profileRef = useRef(null);

    const notificationRef = useRef(null);

    const mobileMenuRef = useRef(null);

    const settingsRef = useRef(null);

    /* =========================
       LOAD THEME
    ========================= */

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



    /* =========================
       OUTSIDE CLICK
    ========================= */

    useEffect(() => {

        function handleClickOutside(e) {

            if (
                profileRef.current &&
                !profileRef.current.contains(
                    e.target
                )
            ) {

                setOpenMobileProfile(false);
                setOpenDesktopProfile(false);
            }

            if (
                notificationRef.current &&
                !notificationRef.current.contains(
                    e.target
                )
            ) {

                setOpenNotifications(false);
            }

            if (
                mobileMenuRef.current &&
                !mobileMenuRef.current.contains(
                    e.target
                )
            ) {

                setOpenMobileMenu(false);
            }

            if (
                settingsRef.current &&
                !settingsRef.current.contains(
                    e.target
                )
            ) {

                setOpenSettings(false);
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

    /* =========================
       TOGGLE THEME
    ========================= */

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

    useEffect(() => {

        const storedUser = localStorage.getItem("user");

        if (storedUser) {

            setUser(
                JSON.parse(storedUser)
            );

        }

    }, []);

    const navigate = useNavigate();

    const handleLogout = () => {

        localStorage.removeItem("token");
        localStorage.removeItem("user");

        navigate("/login");

    };

    return (

        <>

            {/* =========================
                MOBILE PREMIUM TOPBAR
            ========================= */}

            <div className="mobile-premium-topbar">

                {/* HAMBURGER */}

                <div
                    className="mobile-menu-wrapper"
                    ref={mobileMenuRef}
                >

                    <button
                        className={
                            openMobileMenu
                                ? "mobile-menu-btn active-menu"
                                : "mobile-menu-btn"
                        }
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

                    {/* MOBILE SIDEBAR */}

                    <div
                        className={
                            openMobileMenu
                                ? "mobile-slide-menu active"
                                : "mobile-slide-menu"
                        }
                    >

                        <a href="/dashboard">

                            <LayoutDashboard size={18} />

                            Dashboard

                        </a>

                        <a href="/resume-analyzer">

                            <FileText size={18} />

                            Resume Analyzer

                        </a>

                        <a href="/mock-interview">

                            <Mic size={18} />

                            AI Interview

                        </a>

                        <a href="/coding-arena">

                            <Code2 size={18} />

                            Coding Arena

                        </a>

                        <a href="/performance">

                            <BarChart3 size={18} />

                            Performance

                        </a>

                    </div>

                </div>

                {/* SEARCH */}

                <div className="mobile-search-wrap">

                    <input
                        type="text"
                        placeholder="Search problems, topics..."
                    />

                </div>

                {/* SETTINGS */}

                <div
                    className="mobile-settings-wrapper"
                    ref={settingsRef}
                >

                    <button
                        className={
                            openSettings
                                ? "mobile-settings-btn active-settings"
                                : "mobile-settings-btn"
                        }
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
                                onClick={() => {

                                    setOpenNotifications(true);

                                    setOpenMobileProfile(false);
                                    setOpenDesktopProfile(false);

                                    setOpenSettings(false);
                                }}
                            >

                                <Bell size={18} />

                                Notifications

                            </button>

                            {/* PROFILE */}

                            <button
                                className="mobile-setting-item"
                                onClick={() => {

                                    setOpenMobileProfile(true);
                                    setOpenNotifications(false);

                                    setOpenSettings(false);
                                }}
                            >

                                <User size={18} />

                                Profile

                            </button>

                        </div>
                    }

                </div>

            </div>

            {/* =========================
                MOBILE NOTIFICATION
            ========================= */}

            {

                openNotifications &&

                <div
                    className="mobile-popup-dropdown"
                    ref={notificationRef}
                >

                    <div className="mobile-popup-header">

                        <button
                            className="mobile-back-btn"
                            onClick={() =>
                                setOpenNotifications(false)
                            }
                        >

                            <ArrowLeft size={18} />

                        </button>

                        <h3>

                            Notifications

                        </h3>

                    </div>

                    <NotificationDropdown />

                </div>
            }

            {/* =========================
                MOBILE PROFILE
            ========================= */}

            {

                openMobileProfile &&

                <div
                    className="mobile-popup-dropdown"
                    ref={profileRef}
                >

                    <div className="mobile-popup-header">

                        <button
                            className="mobile-back-btn"
                            onClick={() =>
                                setOpenMobileProfile(false)
                            }
                        >

                            <ArrowLeft size={18} />

                        </button>

                        <h3>

                            Profile

                        </h3>

                    </div>

                    <ProfileDropdown
                        user={user}
                        onLogout={handleLogout}
                    />
                </div>
            }

            {/* MOBILE OVERLAY */}

            {

                openMobileMenu &&

                <div
                    className="mobile-overlay"
                    onClick={() =>
                        setOpenMobileMenu(false)
                    }
                />
            }

            {/* =========================
                DESKTOP TOPBAR
            ========================= */}

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
                                setOpenDesktopProfile(
                                    !openDesktopProfile
                                )
                            }
                        >

                            {
                                user?.username?.charAt(0)?.toUpperCase()
                                ||
                                user?.name?.charAt(0)?.toUpperCase()
                                ||
                                "U"
                            }

                        </div>

                        {
                            openDesktopProfile &&

                            <ProfileDropdown
                                user={user}
                                onLogout={handleLogout}
                            />
                        }

                    </div>

                </div>

            </div>

        </>
    );
}