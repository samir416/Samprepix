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
    BookOpen,
    Compass
} from "lucide-react";

import { FiShield, FiCreditCard, FiGithub } from "react-icons/fi";

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
import { getCapabilities } from "../../services/subscriptionService";

const SEARCH_CATALOG = [
    { title: "Two Sum", category: "Coding Problem", link: "/coding-arena?problem=two-sum" },
    { title: "Reverse Linked List", category: "Coding Problem", link: "/coding-arena?problem=reverse-linked-list" },
    { title: "Valid Parentheses", category: "Coding Problem", link: "/coding-arena?problem=valid-parentheses" },
    { title: "Binary Search", category: "Coding Problem", link: "/coding-arena?problem=binary-search" },
    { title: "Merge Two Sorted Lists", category: "Coding Problem", link: "/coding-arena?problem=merge-two-sorted-lists" },
    { title: "Longest Substring Without Repeating Characters", category: "Coding Problem", link: "/coding-arena?problem=longest-substring" },
    { title: "AI Mock Interview", category: "Practice", link: "/mock-interview" },
    { title: "Resume Analyzer", category: "Career Tool", link: "/resume-analyzer" },
    { title: "Aptitude Assessments", category: "Practice", link: "/aptitude" },
    { title: "Performance & Analytics", category: "Metrics", link: "/performance" },
    { title: "GitHub Profile Analyzer", category: "Intelligence", link: "/github-analyzer" },
    { title: "Personalized AI Roadmap", category: "Curriculum", link: "/ai-roadmap" },
    { title: "Subscription Plans", category: "Membership", link: "/pricing" }
];

export default function Topbar() {

    /* =========================
       STATES
    ========================= */

    const [darkMode, setDarkMode] = useState(false);

    const [openNotifications, setOpenNotifications] =
        useState(false);

    const [openMobileNotifications, setOpenMobileNotifications] =
        useState(false);

    const [globalSearch, setGlobalSearch] = useState("");
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [searchActiveIndex, setSearchActiveIndex] = useState(-1);

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
    const [premiumBadge, setPremiumBadge] = useState(null);

    /* =========================
       REFS
    ========================= */

    const profileRef = useRef(null);

    const notificationRef = useRef(null);

    const mobileMenuRef = useRef(null);

    const settingsRef = useRef(null);

    const searchInputRef = useRef(null);

    const searchContainerRef = useRef(null);

    const filteredSuggestions = globalSearch.trim()
        ? SEARCH_CATALOG.filter(
              (item) =>
                  item.title.toLowerCase().includes(globalSearch.toLowerCase().trim()) ||
                  item.category.toLowerCase().includes(globalSearch.toLowerCase().trim())
          ).slice(0, 6)
        : [];

    /* =========================
       GLOBAL CTRL+F SHORTCUT
    ========================= */

    useEffect(() => {
        const handleGlobalKeyDown = (e) => {
            if ((e.ctrlKey || e.metaKey) && (e.key === "f" || e.key === "F")) {
                const activeTag = document.activeElement?.tagName?.toLowerCase();
                const isContentEditable = document.activeElement?.isContentEditable;
                if (activeTag !== "input" && activeTag !== "textarea" && !isContentEditable) {
                    e.preventDefault();
                    searchInputRef.current?.focus();
                    searchInputRef.current?.select();
                    setShowSuggestions(true);
                }
            }
        };
        window.addEventListener("keydown", handleGlobalKeyDown);
        return () => window.removeEventListener("keydown", handleGlobalKeyDown);
    }, []);

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

    const handleSearchKeyDown = (e) => {
        if (e.key === "ArrowDown") {
            if (filteredSuggestions.length > 0) {
                e.preventDefault();
                setSearchActiveIndex((prev) => (prev + 1) % filteredSuggestions.length);
            }
        } else if (e.key === "ArrowUp") {
            if (filteredSuggestions.length > 0) {
                e.preventDefault();
                setSearchActiveIndex((prev) => (prev - 1 + filteredSuggestions.length) % filteredSuggestions.length);
            }
        } else if (e.key === "Escape") {
            e.preventDefault();
            setShowSuggestions(false);
            setSearchActiveIndex(-1);
            searchInputRef.current?.blur();
        } else if (e.key === "Enter") {
            e.preventDefault();
            if (searchActiveIndex >= 0 && filteredSuggestions[searchActiveIndex]) {
                navigate(filteredSuggestions[searchActiveIndex].link);
                setGlobalSearch("");
                setShowSuggestions(false);
                setSearchActiveIndex(-1);
            } else if (globalSearch.trim()) {
                navigate(`/coding-arena?search=${encodeURIComponent(globalSearch.trim())}`);
                setGlobalSearch("");
                setShowSuggestions(false);
                setSearchActiveIndex(-1);
            }
        }
    };

    /* =========================
       OUTSIDE CLICK
    ========================= */

    useEffect(() => {

        function handleClickOutside(e) {

            if (
                searchContainerRef.current &&
                !searchContainerRef.current.contains(e.target)
            ) {
                setShowSuggestions(false);
                setSearchActiveIndex(-1);
            }

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

                try {
                    const cap = await getCapabilities();
                    if (cap?.premiumBadge && cap.premiumBadge !== "STARTER") {
                        setPremiumBadge(cap.premiumBadge);
                    }
                } catch (e) {
                    // Silently ignore capability fetch errors
                }
            }
        };

        fetchUnread();

    }, []);

    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        localStorage.removeItem("onboardingCompleted");
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

                        <a href="/github-analyzer">
                            <FiGithub size={18} />
                            GitHub Analyzer
                        </a>

                        <a href="/ai-roadmap">
                            <Compass size={18} />
                            AI Roadmap
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
                        value={globalSearch}
                        onChange={(e) => setGlobalSearch(e.target.value)}
                        onKeyDown={handleSearchKeyDown}
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

                <div className="topbar-left" ref={searchContainerRef}>

                    <input
                        ref={searchInputRef}
                        type="text"
                        placeholder="Search problems, topics... (Ctrl+F)"
                        value={globalSearch}
                        onChange={(e) => {
                            setGlobalSearch(e.target.value);
                            setShowSuggestions(true);
                            setSearchActiveIndex(-1);
                        }}
                        onFocus={() => setShowSuggestions(true)}
                        onKeyDown={handleSearchKeyDown}
                        aria-label="Search problems, topics, and pages"
                    />

                    <span className="search-shortcut-badge">Ctrl+F</span>

                    {showSuggestions && filteredSuggestions.length > 0 && (
                        <div className="search-suggestions-dropdown" role="listbox">
                            {filteredSuggestions.map((item, idx) => (
                                <div
                                    key={idx}
                                    className={`search-suggestion-item ${searchActiveIndex === idx ? "active" : ""}`}
                                    onClick={() => {
                                        navigate(item.link);
                                        setGlobalSearch("");
                                        setShowSuggestions(false);
                                        setSearchActiveIndex(-1);
                                    }}
                                    onMouseEnter={() => setSearchActiveIndex(idx)}
                                    role="option"
                                    aria-selected={searchActiveIndex === idx}
                                >
                                    <span className="search-suggestion-title">{item.title}</span>
                                    <span className="search-suggestion-category">{item.category}</span>
                                </div>
                            ))}
                        </div>
                    )}

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

                                    {premiumBadge && (
                                        <span
                                            style={{
                                                marginLeft: "6px",
                                                padding: "2px 7px",
                                                borderRadius: "999px",
                                                fontSize: "10px",
                                                fontWeight: "800",
                                                letterSpacing: "0.5px",
                                                background: premiumBadge === "ELITE" ? "linear-gradient(135deg, #f59e0b, #d97706)" : "linear-gradient(135deg, #6366f1, #4f46e5)",
                                                color: "#ffffff",
                                                boxShadow: "0 2px 6px rgba(0,0,0,0.15)",
                                                display: "inline-flex",
                                                alignItems: "center"
                                            }}
                                        >
                                            {premiumBadge}
                                        </span>
                                    )}

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
