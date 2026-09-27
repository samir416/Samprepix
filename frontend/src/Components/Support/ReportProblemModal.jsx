import React, { useState, useEffect } from "react";
import { FiAlertTriangle, FiX, FiCheckCircle, FiSend, FiLock } from "react-icons/fi";
import { toast } from "react-toastify";
import { submitProblemReport } from "../../services/supportService";
import "../../styles/reportProblemModal.css";

const FEATURES = [
    "General / Other",
    "AI Roadmap",
    "GitHub Profile Analyzer",
    "Coding Arena",
    "Mock Interview",
    "Resume Analyzer",
    "Aptitude Hub",
    "Performance & Analytics",
    "Billing & Subscription",
    "Login & Authentication"
];

export default function ReportProblemModal({ isOpen, onClose, initialData = {} }) {
    const [feature, setFeature] = useState("General / Other");
    const [actionAttempted, setActionAttempted] = useState("");
    const [description, setDescription] = useState("");
    const [internalDiagnostic, setInternalDiagnostic] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [statusMessage, setStatusMessage] = useState(null); // { type: 'success'|'error', text: '' }

    useEffect(() => {
        if (isOpen) {
            // Populate feature category if provided
            if (initialData.feature) {
                const matched = FEATURES.find(f => f.toLowerCase().includes(initialData.feature.toLowerCase()));
                setFeature(matched || initialData.feature || "General / Other");
            }
            if (initialData.actionAttempted) {
                setActionAttempted(initialData.actionAttempted);
            }
            // Technical diagnostic kept strictly internal for backend logs, never auto-filled in description
            setInternalDiagnostic(initialData.technicalDiagnostic || "");
            
            // Description always starts empty for the user to describe what happened
            setDescription("");
            setStatusMessage(null);
        }
    }, [isOpen, initialData]);

    if (!isOpen) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        const trimmed = description.trim();
        if (!trimmed) {
            setStatusMessage({ type: "error", text: "Please describe what went wrong." });
            return;
        }
        if (trimmed.length < 10) {
            setStatusMessage({ type: "error", text: "Please provide at least 10 characters describing the issue." });
            return;
        }

        setIsSubmitting(true);
        setStatusMessage(null);

        let userEmail = "";
        try {
            const storedUser = JSON.parse(localStorage.getItem("user") || "{}");
            userEmail = storedUser.email || "";
        } catch (err) {}

        try {
            await submitProblemReport({
                feature,
                actionAttempted: actionAttempted.trim(),
                description: trimmed,
                reporterEmail: userEmail || "support-reporter@samprepix.com",
                pageUrl: initialData.pageUrl || window.location.pathname,
                timestamp: new Date().toISOString(),
                errorDiagnostic: internalDiagnostic || null
            });

            toast.success("Thank you! Your report has been dispatched to our engineering team.");
            setDescription("");
            setActionAttempted("");
            setStatusMessage(null);
            onClose();
        } catch (error) {
            console.error("Problem report submission error:", error);
            setStatusMessage({
                type: "error",
                text: "Unable to submit your report at this time. Please try again later."
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="problem-modal-backdrop" onClick={onClose} role="dialog" aria-modal="true">
            <div className="problem-modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="problem-modal-header">
                    <div className="problem-modal-title-group">
                        <div className="problem-modal-icon">
                            <FiAlertTriangle />
                        </div>
                        <div>
                            <h3 className="problem-modal-title">Report a Problem</h3>
                            <p className="problem-modal-subtitle">Encountered an issue or glitch? Let us know so we can fix it promptly.</p>
                        </div>
                    </div>
                    <button className="problem-modal-close" onClick={onClose} aria-label="Close modal">
                        <FiX />
                    </button>
                </div>

                {statusMessage && (
                    <div className={`problem-modal-alert ${statusMessage.type}`}>
                        {statusMessage.type === "success" ? <FiCheckCircle /> : <FiAlertTriangle />}
                        <span>{statusMessage.text}</span>
                    </div>
                )}

                <form onSubmit={handleSubmit} className="problem-modal-form">
                    <div className="problem-form-group">
                        <label htmlFor="problem-feature" className="problem-label">Feature / Section</label>
                        <select
                            id="problem-feature"
                            className="problem-select"
                            value={feature}
                            onChange={(e) => setFeature(e.target.value)}
                            disabled={isSubmitting}
                        >
                            {FEATURES.map((feat) => (
                                <option key={feat} value={feat}>{feat}</option>
                            ))}
                        </select>
                    </div>

                    <div className="problem-form-group">
                        <label htmlFor="problem-action" className="problem-label">What were you trying to do?</label>
                        <input
                            id="problem-action"
                            type="text"
                            className="problem-input"
                            placeholder="e.g. Submitting code in C++, running test cases, generating roadmap"
                            value={actionAttempted}
                            onChange={(e) => setActionAttempted(e.target.value)}
                            disabled={isSubmitting}
                            maxLength={150}
                        />
                    </div>

                    <div className="problem-form-group">
                        <label htmlFor="problem-desc" className="problem-label">
                            What happened? (Problem Details) <span className="required-star">*</span>
                        </label>
                        <textarea
                            id="problem-desc"
                            className="problem-textarea"
                            rows={4}
                            placeholder="Describe what went wrong..."
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            disabled={isSubmitting}
                            minLength={10}
                            maxLength={3000}
                            required
                        />
                        <div className="problem-char-count">{description.length} / 3000 (min 10 characters)</div>
                    </div>

                    <div className="problem-form-group">
                        <label htmlFor="problem-user-email" className="problem-label" style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                            Your Email (for updates) <FiLock size={12} style={{ color: "var(--text-secondary, #94a3b8)" }} />
                        </label>
                        <input
                            id="problem-user-email"
                            type="email"
                            className="problem-input"
                            value={(() => {
                                try {
                                    const u = JSON.parse(localStorage.getItem("user") || "{}");
                                    return u.email || "";
                                } catch (_) {
                                    return "";
                                }
                            })()}
                            placeholder="user@samprepix.com"
                            readOnly
                            disabled
                            style={{ opacity: 0.85, cursor: "not-allowed" }}
                        />
                    </div>

                    <div className="problem-modal-actions">
                        <button
                            type="button"
                            className="problem-btn-secondary"
                            onClick={onClose}
                            disabled={isSubmitting}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="problem-btn-primary"
                            disabled={isSubmitting || !description.trim()}
                        >
                            {isSubmitting ? (
                                <>
                                    <span className="problem-spinner" /> Submitting...
                                </>
                            ) : (
                                <>
                                    <FiSend /> Send Problem Report
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
