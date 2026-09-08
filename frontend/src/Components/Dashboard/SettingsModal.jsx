import React, { useState, useEffect, useCallback } from "react";
import {
    FiX,
    FiMoon,
    FiSun,
    FiMonitor,
    FiBell,
    FiVolume2,
    FiSliders,
    FiCode,
    FiCheck,
    FiPercent,
    FiCpu,
    FiBookOpen,
    FiBarChart2,
    FiFastForward
} from "react-icons/fi";
import audioService from "../../services/audioService";
import "../../styles/settings.css";

const APTITUDE_TRACK_OPTIONS = [
    {
        id: "quantitative",
        label: "Quantitative Aptitude",
        short: "Quant & Arithmetic",
        count: "7,500 Qs",
        icon: FiPercent
    },
    {
        id: "logical",
        label: "Logical Reasoning",
        short: "Deductions & Patterns",
        count: "5,760 Qs",
        icon: FiCpu
    },
    {
        id: "verbal",
        label: "Verbal Ability",
        short: "Grammar & Vocab",
        count: "5,200 Qs",
        icon: FiBookOpen
    },
    {
        id: "data_interpretation",
        label: "Data Interpretation",
        short: "Charts & Caselets",
        count: "3,600 Qs",
        icon: FiBarChart2
    }
];

const CODING_LANGUAGES = [
    { id: "python", label: "Python (3.10)" },
    { id: "java", label: "Java (OpenJDK 17)" },
    { id: "cpp", label: "C++ (GCC 12)" },
    { id: "javascript", label: "JavaScript (Node.js)" },
    { id: "typescript", label: "TypeScript (5.0)" }
];

const getPersistedSettings = () => ({
    themePreference: localStorage.getItem("themePreference") || localStorage.getItem("theme") || "system",
    interfaceDensity: localStorage.getItem("setting_interface_density") || "comfortable",
    reducedMotion: localStorage.getItem("setting_reduced_motion") === "true",
    soundEnabled: localStorage.getItem("setting_sound_enabled") !== "false",
    codingNotifs: localStorage.getItem("setting_notif_coding") !== "false",
    aptitudeNotifs: localStorage.getItem("setting_notif_aptitude") !== "false",
    interviewNotifs: localStorage.getItem("setting_notif_interviews") !== "false",
    browserNotifs: localStorage.getItem("setting_browser_notifs") === "true",
    editorFontSize: localStorage.getItem("setting_editor_font_size") || "14",
    editorAutocomplete: localStorage.getItem("setting_editor_autocomplete") !== "false",
    defaultCodingLang: localStorage.getItem("setting_default_language") || "python",
    defaultTrack: localStorage.getItem("setting_default_track") || "quantitative"
});

const applyDomPreferences = (themePref, densityPref, motionPref) => {
    // Theme
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

    // Density
    if (densityPref === "compact") {
        document.body.classList.add("density-compact");
    } else {
        document.body.classList.remove("density-compact");
    }

    // Motion
    if (motionPref) {
        document.body.classList.add("reduce-motion");
    } else {
        document.body.classList.remove("reduce-motion");
    }
};

export default function SettingsModal({ isOpen, onClose, user }) {
    const [persistedSettings, setPersistedSettings] = useState(getPersistedSettings);
    const [draftSettings, setDraftSettings] = useState(getPersistedSettings);
    const [activeTab, setActiveTab] = useState("appearance");
    const [isTestingSound, setIsTestingSound] = useState(false);

    // Sync draft with persisted when modal is opened
    useEffect(() => {
        if (isOpen) {
            const current = getPersistedSettings();
            setPersistedSettings(current);
            setDraftSettings({ ...current });
        }
    }, [isOpen]);

    // Appearance Handlers (mutates draft only, with live preview for theme/density/motion)
    const handleThemeSelect = (selectedTheme) => {
        setDraftSettings((prev) => ({ ...prev, themePreference: selectedTheme }));
        applyDomPreferences(selectedTheme, draftSettings.interfaceDensity, draftSettings.reducedMotion);
    };

    const handleDensityChange = (density) => {
        setDraftSettings((prev) => ({ ...prev, interfaceDensity: density }));
        applyDomPreferences(draftSettings.themePreference, density, draftSettings.reducedMotion);
    };

    const handleReducedMotionToggle = () => {
        const next = !draftSettings.reducedMotion;
        setDraftSettings((prev) => ({ ...prev, reducedMotion: next }));
        applyDomPreferences(draftSettings.themePreference, draftSettings.interfaceDensity, next);
    };

    // Notification Handlers (mutates draft only)
    const handleSoundToggle = () => {
        const next = !draftSettings.soundEnabled;
        setDraftSettings((prev) => ({ ...prev, soundEnabled: next }));
        if (next) {
            audioService.previewSound(true);
        }
    };

    const handleTestSound = () => {
        if (!draftSettings.soundEnabled || isTestingSound) return;
        setIsTestingSound(true);
        audioService.previewSound(true);
        setTimeout(() => setIsTestingSound(false), 900);
    };

    const handleCodingNotifsToggle = () => {
        setDraftSettings((prev) => ({ ...prev, codingNotifs: !prev.codingNotifs }));
    };

    const handleAptitudeNotifsToggle = () => {
        setDraftSettings((prev) => ({ ...prev, aptitudeNotifs: !prev.aptitudeNotifs }));
    };

    const handleInterviewNotifsToggle = () => {
        setDraftSettings((prev) => ({ ...prev, interviewNotifs: !prev.interviewNotifs }));
    };

    const handleBrowserNotifsToggle = () => {
        const next = !draftSettings.browserNotifs;
        setDraftSettings((prev) => ({ ...prev, browserNotifs: next }));
        if (next && typeof window !== "undefined" && "Notification" in window && Notification.permission !== "granted") {
            Notification.requestPermission();
        }
    };

    // Application Preferences Handlers (mutates draft only)
    const handleFontSizeChange = (val) => {
        setDraftSettings((prev) => ({ ...prev, editorFontSize: val }));
    };

    const handleAutocompleteToggle = () => {
        setDraftSettings((prev) => ({ ...prev, editorAutocomplete: !prev.editorAutocomplete }));
    };

    const handleDefaultLanguageSelect = (langId) => {
        setDraftSettings((prev) => ({ ...prev, defaultCodingLang: langId }));
    };

    const handleDefaultTrackSelect = (trackId) => {
        setDraftSettings((prev) => ({ ...prev, defaultTrack: trackId }));
    };

    // Roll back draft changes to persisted state and close
    const handleCancel = useCallback(() => {
        applyDomPreferences(
            persistedSettings.themePreference,
            persistedSettings.interfaceDensity,
            persistedSettings.reducedMotion
        );
        audioService.setSoundEnabled(persistedSettings.soundEnabled);
        setDraftSettings({ ...persistedSettings });
        onClose();
    }, [persistedSettings, onClose]);

    // Commit draft changes to localStorage, apply DOM/Audio, dispatch event, and close
    const handleDone = useCallback(() => {
        localStorage.setItem("themePreference", draftSettings.themePreference);
        const resolvedTheme = draftSettings.themePreference === "system"
            ? (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light")
            : draftSettings.themePreference;
        localStorage.setItem("theme", resolvedTheme);
        localStorage.setItem("setting_interface_density", draftSettings.interfaceDensity);
        localStorage.setItem("setting_reduced_motion", String(draftSettings.reducedMotion));
        localStorage.setItem("setting_sound_enabled", String(draftSettings.soundEnabled));
        localStorage.setItem("setting_notif_coding", String(draftSettings.codingNotifs));
        localStorage.setItem("setting_notif_aptitude", String(draftSettings.aptitudeNotifs));
        localStorage.setItem("setting_notif_interviews", String(draftSettings.interviewNotifs));
        localStorage.setItem("setting_browser_notifs", String(draftSettings.browserNotifs));
        localStorage.setItem("setting_editor_font_size", draftSettings.editorFontSize);
        localStorage.setItem("setting_editor_autocomplete", String(draftSettings.editorAutocomplete));
        localStorage.setItem("setting_default_language", draftSettings.defaultCodingLang);
        localStorage.setItem("setting_default_track", draftSettings.defaultTrack);

        applyDomPreferences(
            draftSettings.themePreference,
            draftSettings.interfaceDensity,
            draftSettings.reducedMotion
        );
        audioService.setSoundEnabled(draftSettings.soundEnabled);

        setPersistedSettings({ ...draftSettings });
        window.dispatchEvent(new Event("settingsUpdated"));
        onClose();
    }, [draftSettings, onClose]);

    // Handle Escape key to cancel
    useEffect(() => {
        const handleKeyDown = (e) => {
            if (e.key === "Escape" && isOpen) {
                handleCancel();
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [isOpen, handleCancel]);

    if (!isOpen) return null;

    const hasChanges = JSON.stringify(persistedSettings) !== JSON.stringify(draftSettings);

    return (
        <div className="settings-modal-overlay" onClick={handleCancel}>
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
                            <p>Configure interface, notification triggers, and learning preferences</p>
                        </div>
                    </div>
                    <button
                        className="settings-close-btn"
                        onClick={handleCancel}
                        aria-label="Close Settings"
                    >
                        <FiX size={20} />
                    </button>
                </div>

                {/* MODAL BODY */}
                <div className="settings-body">
                    {/* TABS SIDEBAR (STRICTLY 3 SETTINGS TABS) */}
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
                            className={`settings-tab-btn ${activeTab === "application" ? "active" : ""}`}
                            onClick={() => setActiveTab("application")}
                        >
                            <FiCode size={17} />
                            <span>Application</span>
                        </button>
                    </nav>

                    {/* CONTENT CONTAINER */}
                    <div className="settings-content">
                        {/* TAB 1: APPEARANCE */}
                        {activeTab === "appearance" && (
                            <section className="settings-section">
                                <div className="settings-section-title">
                                    <h3>Appearance & Theme</h3>
                                    <p>Customize the look, visual density, and motion in your placement environment.</p>
                                </div>

                                <div className="theme-options-grid">
                                    <button
                                        type="button"
                                        className={`theme-option-card ${draftSettings.themePreference === "light" ? "selected" : ""}`}
                                        onClick={() => handleThemeSelect("light")}
                                    >
                                        <div className="theme-card-preview light-preview">
                                            <FiSun size={24} />
                                        </div>
                                        <div className="theme-card-info">
                                            <h4>Light</h4>
                                            <p>Crisp, clean high-contrast mode</p>
                                        </div>
                                        {draftSettings.themePreference === "light" && <FiCheck className="theme-check" size={16} />}
                                    </button>

                                    <button
                                        type="button"
                                        className={`theme-option-card ${draftSettings.themePreference === "dark" ? "selected" : ""}`}
                                        onClick={() => handleThemeSelect("dark")}
                                    >
                                        <div className="theme-card-preview dark-preview">
                                            <FiMoon size={24} />
                                        </div>
                                        <div className="theme-card-info">
                                            <h4>Dark</h4>
                                            <p>Midnight deep slate mode</p>
                                        </div>
                                        {draftSettings.themePreference === "dark" && <FiCheck className="theme-check" size={16} />}
                                    </button>

                                    <button
                                        type="button"
                                        className={`theme-option-card ${draftSettings.themePreference === "system" ? "selected" : ""}`}
                                        onClick={() => handleThemeSelect("system")}
                                    >
                                        <div className="theme-card-preview system-preview">
                                            <FiMonitor size={24} />
                                        </div>
                                        <div className="theme-card-info">
                                            <h4>System</h4>
                                            <p>Sync automatically with OS</p>
                                        </div>
                                        {draftSettings.themePreference === "system" && <FiCheck className="theme-check" size={16} />}
                                    </button>
                                </div>

                                {/* INTERFACE DENSITY */}
                                <div className="app-pref-group" style={{ marginTop: "22px" }}>
                                    <label className="app-pref-label">Interface Density</label>
                                    <p className="app-pref-hint">Adjust padding and spacing across tables, cards, and dashboards.</p>
                                    <div className="font-size-selector">
                                        <button
                                            type="button"
                                            className={`font-size-btn ${draftSettings.interfaceDensity === "comfortable" ? "active" : ""}`}
                                            onClick={() => handleDensityChange("comfortable")}
                                        >
                                            Comfortable
                                        </button>
                                        <button
                                            type="button"
                                            className={`font-size-btn ${draftSettings.interfaceDensity === "compact" ? "active" : ""}`}
                                            onClick={() => handleDensityChange("compact")}
                                        >
                                            Compact
                                        </button>
                                    </div>
                                </div>

                                {/* REDUCED MOTION */}
                                <div className="settings-toggle-row" style={{ marginTop: "16px" }}>
                                    <div className="toggle-info">
                                        <div className="toggle-icon-wrap">
                                            <FiFastForward size={18} />
                                        </div>
                                        <div>
                                            <h4>Reduced Motion</h4>
                                            <p>Minimize UI transitions and smooth scroll animations.</p>
                                        </div>
                                    </div>
                                    <label className="settings-switch">
                                        <input
                                            type="checkbox"
                                            checked={draftSettings.reducedMotion}
                                            onChange={handleReducedMotionToggle}
                                        />
                                        <span className="settings-slider" />
                                    </label>
                                </div>
                            </section>
                        )}

                        {/* TAB 2: NOTIFICATIONS */}
                        {activeTab === "notifications" && (
                            <section className="settings-section">
                                <div className="settings-section-title">
                                    <h3>Notification Preferences</h3>
                                    <p>Configure audio cues and real event alerts across placement modules.</p>
                                </div>

                                <div className="settings-toggle-list">
                                    <div className="settings-toggle-row">
                                        <div className="toggle-info">
                                            <div className="toggle-icon-wrap">
                                                <FiVolume2 size={18} />
                                            </div>
                                            <div>
                                                <h4>Sound & Audio Feedback</h4>
                                                <p>Play audio cues for test passes, countdown warnings, and question speech.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={draftSettings.soundEnabled}
                                                onChange={handleSoundToggle}
                                                aria-label="Toggle Sound Effects"
                                            />
                                            <span className="settings-slider" />
                                        </label>
                                    </div>

                                    <div className="settings-sound-test-row">
                                        <button
                                            type="button"
                                            className="test-sound-btn"
                                            onClick={handleTestSound}
                                            disabled={!draftSettings.soundEnabled || isTestingSound}
                                            title={!draftSettings.soundEnabled ? "Enable sound to test audio preview" : "Click to play procedural Web Audio chime"}
                                        >
                                            <FiVolume2 size={15} />
                                            <span>{isTestingSound ? "Playing..." : "Test Sound"}</span>
                                        </button>
                                        <span className="test-sound-hint">
                                            {draftSettings.soundEnabled ? "Plays synthesized Web Audio chime" : "Enable master sound to test preview"}
                                        </span>
                                    </div>

                                    <div className="settings-toggle-row">
                                        <div className="toggle-info">
                                            <div className="toggle-icon-wrap">
                                                <FiCode size={18} />
                                            </div>
                                            <div>
                                                <h4>Coding Completion Alerts</h4>
                                                <p>Receive notifications when problem solutions pass all test cases.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={draftSettings.codingNotifs}
                                                onChange={handleCodingNotifsToggle}
                                            />
                                            <span className="settings-slider" />
                                        </label>
                                    </div>

                                    <div className="settings-toggle-row">
                                        <div className="toggle-info">
                                            <div className="toggle-icon-wrap">
                                                <FiPercent size={18} />
                                            </div>
                                            <div>
                                                <h4>Aptitude Assessment Alerts</h4>
                                                <p>Notify when aptitude tests are evaluated and scores are ready.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={draftSettings.aptitudeNotifs}
                                                onChange={handleAptitudeNotifsToggle}
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
                                                <h4>Mock Interview & Resume Alerts</h4>
                                                <p>Updates for AI mock interview scoring and resume ATS analyses.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={draftSettings.interviewNotifs}
                                                onChange={handleInterviewNotifsToggle}
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
                                                <p>Receive system notifications outside the active browser tab.</p>
                                            </div>
                                        </div>
                                        <label className="settings-switch">
                                            <input
                                                type="checkbox"
                                                checked={draftSettings.browserNotifs}
                                                onChange={handleBrowserNotifsToggle}
                                            />
                                            <span className="settings-slider" />
                                        </label>
                                    </div>
                                </div>
                            </section>
                        )}

                        {/* TAB 3: APPLICATION PREFERENCES */}
                        {activeTab === "application" && (
                            <section className="settings-section">
                                <div className="settings-section-title">
                                    <h3>Application & Editor</h3>
                                    <p>Configure Monaco code editor behavior and default learning tracks.</p>
                                </div>

                                <div className="app-pref-group">
                                    <label className="app-pref-label">Coding Arena Editor Font Size</label>
                                    <div className="font-size-selector">
                                        {["13", "14", "15", "16"].map((size) => (
                                            <button
                                                key={size}
                                                type="button"
                                                className={`font-size-btn ${draftSettings.editorFontSize === size ? "active" : ""}`}
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
                                            checked={draftSettings.editorAutocomplete}
                                            onChange={handleAutocompleteToggle}
                                        />
                                        <span className="settings-slider" />
                                    </label>
                                </div>

                                {/* DEFAULT CODING LANGUAGE SELECTOR */}
                                <div className="app-pref-group" style={{ marginTop: "20px" }}>
                                    <label className="app-pref-label">Default Coding Language</label>
                                    <p className="app-pref-hint">Initial starter runtime loaded when opening problems in Coding Arena.</p>
                                    <div className="font-size-selector">
                                        {CODING_LANGUAGES.map((lang) => (
                                            <button
                                                key={lang.id}
                                                type="button"
                                                className={`font-size-btn ${draftSettings.defaultCodingLang === lang.id ? "active" : ""}`}
                                                onClick={() => handleDefaultLanguageSelect(lang.id)}
                                            >
                                                {lang.label}
                                            </button>
                                        ))}
                                    </div>
                                </div>

                                {/* MODERN CUSTOM DEFAULT APTITUDE TRACK SELECTOR */}
                                <div className="app-pref-group" style={{ marginTop: "20px" }}>
                                    <label className="app-pref-label">Default Aptitude Track</label>
                                    <p className="app-pref-hint">Select which curriculum track opens automatically when exploring Aptitude.</p>
                                    <div className="custom-track-grid">
                                        {APTITUDE_TRACK_OPTIONS.map((track) => {
                                            const IconComp = track.icon;
                                            const isSelected = draftSettings.defaultTrack === track.id;
                                            return (
                                                <button
                                                    key={track.id}
                                                    type="button"
                                                    className={`custom-track-card ${isSelected ? "selected" : ""}`}
                                                    onClick={() => handleDefaultTrackSelect(track.id)}
                                                    onKeyDown={(e) => {
                                                        if (e.key === "Enter" || e.key === " ") {
                                                            e.preventDefault();
                                                            handleDefaultTrackSelect(track.id);
                                                        }
                                                    }}
                                                >
                                                    <div className="track-card-left">
                                                        <div className="track-icon-badge">
                                                            <IconComp size={16} />
                                                        </div>
                                                        <div className="track-text">
                                                            <div style={{ display: "flex", alignItems: "center" }}>
                                                                <span className="track-name">{track.label}</span>
                                                                <span className="track-count-badge">{track.count}</span>
                                                            </div>
                                                            <span className="track-sub">{track.short}</span>
                                                        </div>
                                                    </div>
                                                    {isSelected && (
                                                        <div className="track-check-pill">
                                                            <FiCheck size={14} />
                                                        </div>
                                                    )}
                                                </button>
                                            );
                                        })}
                                    </div>
                                </div>
                            </section>
                        )}
                    </div>
                </div>

                {/* MODAL FOOTER WITH GUARANTEED GENEROUS BOTTOM PADDING */}
                <div className="settings-footer">
                    <div className="settings-notice-area">
                        {hasChanges && (
                            <span className="settings-unsaved-badge">
                                ● Unsaved changes
                            </span>
                        )}
                    </div>
                    <div className="settings-footer-actions">
                        <button
                            type="button"
                            className="settings-cancel-btn"
                            onClick={handleCancel}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="settings-done-btn"
                            onClick={handleDone}
                        >
                            Done
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
