import React, { useState, useEffect } from "react";
import { FiAlertTriangle, FiX, FiCheckCircle, FiSend } from "react-icons/fi";
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
    const [reporterEmail, setReporterEmail] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [statusMessage, setStatusMessage] = useState(null); // { type: 'success'|'error', text: '' }

    useEffect(() => {
        if (isOpen) {
            // Populate pre-filled data
            if (initialData.feature) {
                const matched = FEATURES.find(f => f.toLowerCase().includes(initialData.feature.toLowerCase()));
                setFeature(matched || initialData.feature || "General / Other");
            }
            if (initialData.actionAttempted) {
                setActionAttempted(initialData.actionAttempted);
            }
            if (initialData.description) {
                setDescription(initialData.description);
            }

            // Populate user email if logged in
            try {
                const storedUser = JSON.parse(localStorage.getItem("user") || "{}");
                if (storedUser.email) {
                    setReporterEmail(storedUser.email);
                }
            } catch (e) {
                // Ignore parse errors
            }

            setStatusMessage(null);
        }
    }, [isOpen, initialData]);

    if (!isOpen) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!description.trim()) {
            setStatusMessage({ type: "error", text: "Please provide a brief description of the issue." });
            return;
        }

        setIsSubmitting(true);
        setStatusMessage(null);

        try {
            await submitProblemReport({
                feature,
                actionAttempted: actionAttempted.trim(),
                description: description.trim(),
                reporterEmail: reporterEmail.trim(),
                pageUrl: initialData.pageUrl || window.location.pathname
            });

            setStatusMessage({
                type: "success",
                text: "Thank you! Your report has been dispatched to our engineering team."
            });

            // Auto-close after 2 seconds
            setTimeout(() => {
                setDescription("");
                setActionAttempted("");
                setStatusMessage(null);
                onClose();
            }, 2000);
        } catch (error) {
            console.error("Failed to submit problem report:", error);
            setStatusMessage({
                type: "error",
                text: error?.response?.data?.message || "Failed to submit report. Please try again later."
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
                            placeholder="Explain the error or unexpected behavior in detail..."
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            disabled={isSubmitting}
                            maxLength={3000}
                            required
                        />
                        <div className="problem-char-count">{description.length} / 3000</div>
                    </div>

                    <div className="problem-form-group">
                        <label htmlFor="problem-email" className="problem-label">Your Email (for updates)</label>
                        <input
                            id="problem-email"
                            type="email"
                            className="problem-input"
                            placeholder="your.email@example.com"
                            value={reporterEmail}
                            onChange={(e) => setReporterEmail(e.target.value)}
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="problem-page-hint">
                        <span>Current Route:</span> <code>{window.location.pathname}</code>
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
