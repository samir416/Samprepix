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
    ArrowLeft,
    BookOpen
} from "lucide-react";

import { FiShield, FiCreditCard } from "react-icons/fi";

import {
    useEffect,
    useRef,
    useState
} from "react";

import NotificationDropdown from "./NotificationDropdown";
import ProfileDropdown from "./ProfileDropdown";
import SettingsModal from "./SettingsModal";
import { useNavigate } from "react-router-dom";
import { getUnreadNotificationCount } from "../../services/notificationService";

export default function Topbar() {

    /* =========================
       STATES
    ========================= */

    const [darkMode, setDarkMode] = useState(false);

    const [openNotifications, setOpenNotifications] =
        useState(false);

    const [openMobileNotifications, setOpenMobileNotifications] =
        useState(false);

    const [unreadCount, setUnreadCount] = useState(0);

    const [openMobileProfile, setOpenMobileProfile] =
        useState(false);

    const [openDesktopProfile, setOpenDesktopProfile] =
        useState(false);

    const [openMobileMenu, setOpenMobileMenu] =
        useState(false);

    const [openSettings, setOpenSettings] =
        useState(false);

    const [openSettingsModal, setOpenSettingsModal] =
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
                setOpenMobileNotifications(false);
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

        const fetchUnread = async () => {
            const token = localStorage.getItem("token");
            if (token) {
                try {
                    const res = await getUnreadNotificationCount();
                    if (res && typeof res.unreadCount === "number") {
                        setUnreadCount(res.unreadCount);
                    }
                } catch (e) {
                    console.error("Failed to fetch initial unread notification count:", e);
                }
            }
        };

        fetchUnread();

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

                        <a href="/aptitude">
                            <BookOpen size={18} />
                            Aptitude
                        </a>

                        <a href="/performance">
                            <BarChart3 size={18} />
                            Performance
                        </a>

                        <a href="/subscription">
                            <FiCreditCard size={18} />
                            Subscription
                        </a>

                        {user?.role === "ADMIN" && (
                            <a href="/admin">
                                <FiShield size={18} />
                                Admin
                            </a>
                        )}

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

                            {/* SETTINGS MODAL */}

                            <button
                                className="mobile-setting-item"
                                onClick={() => {
                                    setOpenSettingsModal(true);
                                    setOpenSettings(false);
                                }}
                            >
                                <Settings size={18} />
                                Settings
                            </button>

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

                                    setOpenMobileNotifications(true);

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
                                    setOpenMobileNotifications(false);

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

                openMobileNotifications &&

                <div
                    className="mobile-popup-dropdown"
                    ref={notificationRef}
                >

                    <div className="mobile-popup-header">

                        <button
                            className="mobile-back-btn"
                            onClick={() =>
                                setOpenMobileNotifications(false)
                            }
                        >

                            <ArrowLeft size={18} />

                        </button>

                        <h3>

                            Notifications

                        </h3>

                    </div>

                    <NotificationDropdown
                        onCountChange={(count) => setUnreadCount(count)}
                        onClose={() => setOpenNotifications(false)}
                    />

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
                        onClose={() => setOpenMobileProfile(false)}
                        onOpenSettings={() => {
                            setOpenMobileProfile(false);
                            setOpenSettingsModal(true);
                        }}
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
                        aria-label="Toggle Theme"
                    >

                        {
                            darkMode
                                ? <Sun size={18} />
                                : <Moon size={18} />
                        }

                    </button>

                    {/* SETTINGS */}

                    <button
                        className="dashboard-settings-toggle"
                        onClick={() => setOpenSettingsModal(true)}
                        title="Platform Settings"
                        aria-label="Platform Settings"
                    >
                        <Settings size={19} />
                    </button>

                    {/* NOTIFICATION */}

                    <div
                        className="notification-wrapper"
                        ref={notificationRef}
                    >

                        <button
                            className="notification-btn"
                            aria-label="Notifications"
                            onClick={() =>
                                setOpenNotifications(
                                    !openNotifications
                                )
                            }
                        >

                            <Bell size={20} />

                            {unreadCount > 0 && (
                                <span className="notification-badge">
                                    {unreadCount > 9 ? "9+" : unreadCount}
                                </span>
                            )}

                        </button>

                        {

                            openNotifications &&

                            <NotificationDropdown
                                onCountChange={(count) => setUnreadCount(count)}
                                onClose={() => setOpenNotifications(false)}
                            />
                        }

                    </div>

                    {/* PROFILE */}

                    <div
                        className="profile-wrapper"
                        ref={profileRef}
                    >

                        <div
                            className="profile-card"
                            onClick={() =>
                                setOpenDesktopProfile(
                                    !openDesktopProfile
                                )
                            }
                        >

                            <div className="profile-circle">

                                {
                                    user?.username?.charAt(0)?.toUpperCase()
                                    ||
                                    user?.name?.charAt(0)?.toUpperCase()
                                    ||
                                    "U"
                                }

                            </div>

                            <div className="profile-info">

                                <h4>

                                    {
                                        user?.username
                                        ||
                                        user?.name
                                        ||
                                        "User"
                                    }

                                </h4>

                                <span>

                                    {
                                        user?.email
                                        ||
                                        "No Email"
                                    }

                                </span>

                            </div>

                        </div>
                        {
                            openDesktopProfile &&

                            <ProfileDropdown
                                user={user}
                                onLogout={handleLogout}
                                onClose={() => setOpenDesktopProfile(false)}
                                onOpenSettings={() => {
                                    setOpenDesktopProfile(false);
                                    setOpenSettingsModal(true);
                                }}
                            />
                        }

                    </div>

                </div>

            </div>

            {/* SETTINGS MODAL */}
            <SettingsModal
                isOpen={openSettingsModal}
                onClose={() => setOpenSettingsModal(false)}
                user={user}
            />

        </>
    );
}
