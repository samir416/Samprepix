import React, { useState, useEffect } from "react";
import {
    FiX,
    FiMoon,
    FiSun,
    FiMonitor,
    FiBell,
    FiVolume2,
    FiUser,
    FiSliders,
    FiCode,
    FiCheck,
    FiExternalLink
} from "react-icons/fi";
import { useNavigate } from "react-router-dom";
import "../../styles/settings.css";

export default function SettingsModal({ isOpen, onClose, user }) {
    const navigate = useNavigate();

    // Appearance State
    const [theme, setTheme] = useState(() => {
        return localStorage.getItem("themePreference") || "system";
    });

    // Notification Preferences
    const [soundEnabled, setSoundEnabled] = useState(() => {
        return localStorage.getItem("setting_sound_enabled") !== "false";
    });
    const [browserNotifs, setBrowserNotifs] = useState(() => {
        return localStorage.getItem("setting_browser_notifs") === "true";
    });
    const [deadlineAlerts, setDeadlineAlerts] = useState(() => {
        return localStorage.getItem("setting_deadline_alerts") !== "false";
    });

    // Application Preferences
    const [editorFontSize, setEditorFontSize] = useState(() => {
        return localStorage.getItem("setting_editor_font_size") || "14";
    });
    const [editorAutocomplete, setEditorAutocomplete] = useState(() => {
        return localStorage.getItem("setting_editor_autocomplete") !== "false";
    });
    const [defaultTrack, setDefaultTrack] = useState(() => {
        return localStorage.getItem("setting_default_track") || "quantitative";
    });

    const [activeTab, setActiveTab] = useState("appearance");
    const [savedNotice, setSavedNotice] = useState(false);

    // Apply theme
    const applyTheme = (selectedTheme) => {
        setTheme(selectedTheme);
        localStorage.setItem("themePreference", selectedTheme);

        let isDark = false;
        if (selectedTheme === "dark") {
            isDark = true;
        } else if (selectedTheme === "light") {
            isDark = false;
        } else {
            // System
            isDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
        }

        if (isDark) {
            document.body.classList.add("dark-theme");
            localStorage.setItem("theme", "dark");
        } else {
            document.body.classList.remove("dark-theme");
            localStorage.setItem("theme", "light");
        }

        triggerNotice();
    };

    const triggerNotice = () => {
        setSavedNotice(true);
        setTimeout(() => setSavedNotice(false), 2000);
    };

    const handleSoundToggle = () => {
        const next = !soundEnabled;
        setSoundEnabled(next);
        localStorage.setItem("setting_sound_enabled", String(next));
        triggerNotice();
    };

    const handleBrowserNotifsToggle = () => {
        const next = !browserNotifs;
        setBrowserNotifs(next);
        localStorage.setItem("setting_browser_notifs", String(next));
        if (next && "Notification" in window && Notification.permission !== "granted") {
            Notification.requestPermission();
        }
        triggerNotice();
    };

    const handleDeadlineAlertsToggle = () => {
        const next = !deadlineAlerts;
        setDeadlineAlerts(next);
        localStorage.setItem("setting_deadline_alerts", String(next));
        triggerNotice();
    };

    const handleFontSizeChange = (val) => {
        setEditorFontSize(val);
        localStorage.setItem("setting_editor_font_size", val);
        triggerNotice();
    };

    const handleAutocompleteToggle = () => {
        const next = !editorAutocomplete;
        setEditorAutocomplete(next);
        localStorage.setItem("setting_editor_autocomplete", String(next));
        triggerNotice();
    };

    const handleDefaultTrackChange = (val) => {
        setDefaultTrack(val);
        localStorage.setItem("setting_default_track", val);
        triggerNotice();
    };

    if (!isOpen) return null;

    return (
        <div className="settings-modal-overlay" onClick={onClose}>
            <div
                className="settings-modal-card"
                onClick={(e) => e.stopPropagation()}
                role="dialog"
                aria-modal="true"
                aria-label="Platform Settings"
            >
                {/* MODAL HEADER */}
                <div className="settings-header">
                    <div className="settings-header-left">
                        <div className="settings-header-icon">
                            <FiSliders size={20} />
                        </div>
                        <div>
                            <h2>Settings</h2>
                            <p>Manage your interface, notifications, and application preferences</p>
                        </div>
                    </div>
                    <button
                        className="settings-close-btn"
                        onClick={onClose}
                        aria-label="Close Settings"
                    >
                        <FiX size={20} />
                    </button>
                </div>

                {/* MODAL BODY */}
                <div className="settings-body">
                    {/* TABS SIDEBAR */}
                    <nav className="settings-tabs">
                        <button
                            className={`settings-tab-btn ${activeTab === "appearance" ? "active" : ""}`}
                            onClick={() => setActiveTab("appearance")}
                        >
                            <FiSun size={17} />
                            <span>Appearance</span>
                        </button>
                        <button
                            className={`settings-tab-btn ${activeTab === "notifications" ? "active" : ""}`}
                            onClick={() => setActiveTab("notifications")}
                        >
                            <FiBell size={17} />
                            <span>Notifications</span>
                        </button>
                        <button
                            className={`settings-tab-btn ${activeTab === "account" ? "active" : ""}`}
                            onClick={() => setActiveTab("account")}
                        >
                            <FiUser size={17} />
                            <span>Account</span>
                        </button>
                        <button
                            className={`settings-tab-btn ${activeTab === "application" ? "active" : ""}`}
                            onClick={() => setActiveTab("application")}
                        >
                            <FiCode size={17} />
                            <span>Application</span>
                        </button>
                    </nav>

                    {/* CONTENT CONTAINER */}
                    <div className="settings-content">
                        {/* APPEARANCE */}
                        {activeTab === "appearance" && (
                            <section className="settings-section">
                                <div className="settings-section-title">
                                    <h3>Appearance & Theme</h3>
                                    <p>Customize the look and feel of your placement environment.</p>
                                </div>

                                <div className="theme-options-grid">
                                    <button
                                        type="button"
                                        className={`theme-option-card ${theme === "light" ? "selected" : ""}`}
                                        onClick={() => applyTheme("light")}
                                    >
                                        <div className="theme-card-preview light-preview">
                                            <FiSun size={24} />
                                        </div>
                                        <div className="theme-card-info">
                                            <h4>Light</h4>
                                            <p>Crisp, clean high-contrast mode</p>
                                        </div>
                                        {theme === "light" && <FiCheck className="theme-check" size={16} />}
                                    </button>

                                    <button
                                        type="button"
                                        className={`theme-option-card ${theme === "dark" ? "selected" : ""}`}
                                        onClick={() => applyTheme("dark")}
                                    >
                                        <div className="theme-card-preview dark-preview">
                                            <FiMoon size={24} />
                                        </div>
                                        <div className="theme-card-info">
                                            <h4>Dark</h4>
                                            <p>Midnight deep slate mode</p>
                                        </div>
                                        {theme === "dark" && <FiCheck className="theme-check" size={16} />}
                                    </button>

                                    <button
                                        type="button"
                                        className={`theme-option-card ${theme === "system" ? "selected" : ""}`}
                                        onClick={() => applyTheme("system")}
                                    >
                                        <div className="theme-card-preview system-preview">
                                            <FiMonitor size={24} />
                                        </div>
                                        <div className="theme-card-info">
                                            <h4>System</h4>
                                            <p>Sync automatically with OS</p>
                                        </div>
                                        {theme === "system" && <FiCheck className="theme-check" size={16} />}
                                    </button>
                                </div>
                            </section>
                        )}

                        {/* NOTIFICATIONS */}
                        {activeTab === "notifications" && (
                            <section className="settings-section">
                                <div className="settings-section-title">
                                    <h3>Notification Preferences</h3>
                                    <p>Configure how and when you receive system and assessment alerts.</p>
                                </div>

                                <div className="settings-toggle-list">
                                    <div className="settings-toggle-row">
                                        <div className="toggle-info">
                                            <div className="toggle-icon-wrap">
                                                <FiVolume2 size={18} />
                                            </div>
                                            <div>
                                                <h4>Sound Alerts</h4>
                                                <p>Play audio feedback when code tests pass or fail.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={soundEnabled}
                                                onChange={handleSoundToggle}
                                            />
                                            <span className="settings-slider" />
                                        </label>
                                    </div>

                                    <div className="settings-toggle-row">
                                        <div className="toggle-info">
                                            <div className="toggle-icon-wrap">
                                                <FiBell size={18} />
                                            </div>
                                            <div>
                                                <h4>Browser Push Notifications</h4>
                                                <p>Receive desktop alerts for mock interview reminders.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={browserNotifs}
                                                onChange={handleBrowserNotifsToggle}
                                            />
                                            <span className="settings-slider" />
                                        </label>
                                    </div>

                                    <div className="settings-toggle-row">
                                        <div className="toggle-info">
                                            <div className="toggle-icon-wrap">
                                                <FiSliders size={18} />
                                            </div>
                                            <div>
                                                <h4>Placement Activity Alerts</h4>
                                                <p>Show notifications for completed assessments and resume reviews.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={deadlineAlerts}
                                                onChange={handleDeadlineAlertsToggle}
                                            />
                                            <span className="settings-slider" />
                                        </label>
                                    </div>
                                </div>
                            </section>
                        )}

                        {/* ACCOUNT */}
                        {activeTab === "account" && (
                            <section className="settings-section">
                                <div className="settings-section-title">
                                    <h3>Account Overview</h3>
                                    <p>Your authenticated identity and profile access.</p>
                                </div>

                                <div className="account-card">
                                    <div className="account-avatar">
                                        {user?.username?.charAt(0)?.toUpperCase() ||
                                            user?.name?.charAt(0)?.toUpperCase() ||
                                            "U"}
                                    </div>
                                    <div className="account-meta">
                                        <h4>{user?.username || user?.name || "Candidate User"}</h4>
                                        <p>{user?.email || "candidate@placement.com"}</p>
                                        <span className="account-role-badge">
                                            {user?.role ? user.role.toUpperCase() : "STUDENT / CANDIDATE"}
                                        </span>
                                    </div>
                                </div>

                                <div className="account-action-card">
                                    <div>
                                        <h4>Full Candidate Profile</h4>
                                        <p>Update your resume, target companies, education, and skills.</p>
                                    </div>
                                    <button
                                        type="button"
                                        className="settings-link-btn"
                                        onClick={() => {
                                            onClose();
                                            navigate("/profile");
                                        }}
                                    >
                                        <span>View Profile</span>
                                        <FiExternalLink size={15} />
                                    </button>
                                </div>
                            </section>
                        )}

                        {/* APPLICATION PREFERENCES */}
                        {activeTab === "application" && (
                            <section className="settings-section">
                                <div className="settings-section-title">
                                    <h3>Application & Editor</h3>
                                    <p>Tune code editor behavior and default platform preferences.</p>
                                </div>

                                <div className="app-pref-group">
                                    <label className="app-pref-label">Coding Arena Editor Font Size</label>
                                    <div className="font-size-selector">
                                        {["13", "14", "15", "16"].map((size) => (
                                            <button
                                                key={size}
                                                type="button"
                                                className={`font-size-btn ${editorFontSize === size ? "active" : ""}`}
                                                onClick={() => handleFontSizeChange(size)}
                                            >
                                                {size}px
                                            </button>
                                        ))}
                                    </div>
                                </div>

                                <div className="settings-toggle-row" style={{ marginTop: "16px" }}>
                                    <div className="toggle-info">
                                        <div className="toggle-icon-wrap">
                                            <FiCode size={18} />
                                        </div>
                                        <div>
                                            <h4>Code Editor Autocomplete</h4>
                                            <p>Enable Monaco IntelliSense suggestions while solving problems.</p>
                                        </div>
                                    </div>
                                    <label className="settings-switch">
                                        <input
                                            type="checkbox"
                                            checked={editorAutocomplete}
                                            onChange={handleAutocompleteToggle}
                                        />
                                        <span className="settings-slider" />
                                    </label>
                                </div>

                                <div className="app-pref-group" style={{ marginTop: "18px" }}>
                                    <label className="app-pref-label">Default Aptitude Track</label>
                                    <select
                                        className="settings-select"
                                        value={defaultTrack}
                                        onChange={(e) => handleDefaultTrackChange(e.target.value)}
                                    >
                                        <option value="quantitative">Quantitative Aptitude</option>
                                        <option value="logical">Logical Reasoning</option>
                                        <option value="verbal">Verbal Ability</option>
                                        <option value="data_interpretation">Data Interpretation</option>
                                    </select>
                                </div>
                            </section>
                        )}
                    </div>
                </div>

                {/* MODAL FOOTER */}
                <div className="settings-footer">
                    <div className="settings-notice-area">
                        {savedNotice && (
                            <span className="settings-saved-badge">
                                <FiCheck size={14} /> Preferences updated
                            </span>
                        )}
                    </div>
                    <button
                        type="button"
                        className="settings-done-btn"
                        onClick={onClose}
                    >
                        Done
                    </button>
                </div>
            </div>
        </div>
    );
}

