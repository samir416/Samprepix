import React, { useState, useEffect, useRef } from "react";
import {
    FiMessageSquare,
    FiX,
    FiSend,
    FiMinimize2,
    FiChevronRight,
    FiAlertCircle,
    FiHelpCircle,
    FiCompass,
    FiCode,
    FiAward,
    FiLayers
} from "react-icons/fi";
import { askSupportQuestion, openReportProblemModal } from "../../services/supportService";
import "../../styles/aiHelpBot.css";

// 5 Predefined questions with zero-cost instant offline answers
const PREDEFINED_QUESTIONS = [
    {
        id: "roadmap",
        icon: <FiCompass />,
        title: "How does the AI Roadmap work?",
        answer: "The **Samprepix AI Roadmap** generates a step-by-step personalized placement preparation curriculum based on your target role, current skills, and timeline. You can view milestones, mark topics as completed, track overall progress percentages, and access targeted study materials for each phase."
    },
    {
        id: "github",
        icon: <FiLayers />,
        title: "How to use the GitHub Profile Analyzer?",
        answer: "The **GitHub Profile Analyzer** connects with your public repositories, languages, commit patterns, and code contributions. It generates an industry readiness score, identifies your top technical skills, and recommends enhancements to strengthen your engineering portfolio for recruiters."
    },
    {
        id: "arena",
        icon: <FiCode />,
        title: "What languages are supported in Coding Arena?",
        answer: "The **Coding Arena** supports 8 industry programming languages: Java, Python, C++, C, JavaScript, TypeScript, Go, and Rust. You can write code with syntax highlighting, run code against custom inputs or hidden test cases, get real-time compiler diagnostics, and request AI hints when stuck."
    },
    {
        id: "tiers",
        icon: <FiAward />,
        title: "What are the PRO vs ELITE differences?",
        answer: "**Samprepix Membership Tiers**:\n• **PRO (₹1 test price)**: Unlocks full Coding Arena access, AI Roadmap generator, Resume Analyzer, and core aptitude practice.\n• **ELITE (₹2 test price)**: Everything in Pro plus unlimited AI Mock Interviews with video/audio feedback, advanced performance telemetry, and priority AI hints."
    },
    {
        id: "interviews",
        icon: <FiHelpCircle />,
        title: "How do AI Mock Interviews work?",
        answer: "**AI Mock Interviews** simulate real technical and HR interviews with speech-to-text recognition, real-time AI follow-up questions, and comprehensive scoring on clarity, accuracy, and depth. Detailed evaluation reports are stored in your Performance Hub."
    }
];

export default function AIHelpBot() {
    const [isOpen, setIsOpen] = useState(false);
    const [isEdgeCollapsed, setIsEdgeCollapsed] = useState(() => {
        return localStorage.getItem("samprepix_bot_collapsed") === "true";
    });
    const [showGreeting, setShowGreeting] = useState(false);
    const [messages, setMessages] = useState([
        {
            sender: "bot",
            text: "Hi! 👋 I'm your Samprepix AI Assistant. How can I help you?"
        }
    ]);
    const [inputVal, setInputVal] = useState("");
    const [loading, setLoading] = useState(false);
    const messagesEndRef = useRef(null);

    // Welcome greeting for first-time visitor or new login
    useEffect(() => {
        const greeted = localStorage.getItem("samprepix_bot_greeted");
        if (!greeted) {
            const timer = setTimeout(() => {
                setShowGreeting(true);
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, []);

    const dismissGreeting = () => {
        setShowGreeting(false);
        localStorage.setItem("samprepix_bot_greeted", "true");
    };

    const handleOpenFromGreeting = () => {
        dismissGreeting();
        setIsOpen(true);
        setIsEdgeCollapsed(false);
    };

    const toggleCollapseToEdge = () => {
        const nextState = !isEdgeCollapsed;
        setIsEdgeCollapsed(nextState);
        localStorage.setItem("samprepix_bot_collapsed", String(nextState));
        if (nextState) {
            setIsOpen(false);
        }
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (isOpen) {
            scrollToBottom();
        }
    }, [messages, isOpen]);

    const handlePredefinedClick = (q) => {
        // Zero-cost instant local response
        setMessages((prev) => [
            ...prev,
            { sender: "user", text: q.title },
            { sender: "bot", text: q.answer, isPredefined: true }
        ]);
    };

    const handleSendMessage = async (e) => {
        e?.preventDefault();
        const text = inputVal.trim();
        if (!text || loading) return;

        setInputVal("");
        setMessages((prev) => [...prev, { sender: "user", text }]);
        setLoading(true);

        try {
            const data = await askSupportQuestion(text);
            setMessages((prev) => [
                ...prev,
                { sender: "bot", text: data?.answer || "I could not find an answer to that question. Please try asking about our platform features or reporting an issue." }
            ]);
        } catch (error) {
            console.error("AI Help Bot query failed:", error);
            setMessages((prev) => [
                ...prev,
                {
                    sender: "bot",
                    text: "I'm having trouble reaching the assistant service right now. You can check the quick questions above or report an issue directly to our team!"
                }
            ]);
        } finally {
            setLoading(false);
        }
    };

    // Edge-collapsed view: Sleek vertical dock on right viewport edge
    if (isEdgeCollapsed) {
        return (
            <div
                className="ai-bot-edge-handle"
                onClick={() => {
                    setIsEdgeCollapsed(false);
                    localStorage.setItem("samprepix_bot_collapsed", "false");
                    setIsOpen(true);
                }}
                title="Expand Samprepix Support Assistant"
            >
                <span className="edge-dot">•</span>
                <span className="edge-dot">•</span>
                <span className="edge-dot">•</span>
                <span className="edge-label">HELP</span>
            </div>
        );
    }

    return (
        <div className="ai-bot-wrapper">
            {/* FIRST TIME GREETING POPUP */}
            {showGreeting && !isOpen && (
                <div className="ai-bot-greeting-bubble" role="alert">
                    <button
                        className="greeting-close-btn"
                        onClick={dismissGreeting}
                        aria-label="Close welcome notice"
                    >
                        <FiX />
                    </button>
                    <div className="greeting-avatar">🤖</div>
                    <div className="greeting-content">
                        <strong>Welcome to Samprepix!</strong>
                        <p>Need help navigating AI Roadmap, GitHub Analyzer, or Coding Arena? Tap to chat.</p>
                        <button className="greeting-cta" onClick={handleOpenFromGreeting}>
                            Ask Assistant <FiChevronRight />
                        </button>
                    </div>
                </div>
            )}

            {/* CHAT WINDOW */}
            {isOpen && (
                <div className="ai-bot-window" role="dialog" aria-label="Support Assistant Chat">
                    {/* HEADER */}
                    <div className="ai-bot-header">
                        <div className="ai-bot-title-group">
                            <div className="ai-bot-avatar-badge">🤖</div>
                            <div>
                                <h4 className="ai-bot-title">Samprepix Assistant</h4>
                                <span className="ai-bot-status">
                                    <span className="status-indicator-dot" /> Online &bull; Instant Support
                                </span>
                            </div>
                        </div>
                        <div className="ai-bot-header-controls">
                            <button
                                className="ai-bot-ctrl-btn"
                                onClick={toggleCollapseToEdge}
                                title="Dock to edge handle (• • •)"
                                aria-label="Dock to edge handle"
                            >
                                <FiMinimize2 />
                            </button>
                            <button
                                className="ai-bot-ctrl-btn"
                                onClick={() => setIsOpen(false)}
                                title="Close chat window"
                                aria-label="Close chat window"
                            >
                                <FiX />
                            </button>
                        </div>
                    </div>

                    {/* MESSAGES VIEW */}
                    <div className="ai-bot-body">
                        {/* QUICK QUESTIONS PILL LIST */}
                        <div className="ai-bot-quick-section">
                            <span className="quick-section-title">Quick Platform Answers:</span>
                            <div className="ai-bot-quick-list">
                                {PREDEFINED_QUESTIONS.map((q) => (
                                    <button
                                        key={q.id}
                                        className="ai-bot-quick-chip"
                                        onClick={() => handlePredefinedClick(q)}
                                    >
                                        <span className="quick-chip-icon">{q.icon}</span>
                                        <span>{q.title}</span>
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* MESSAGE THREAD */}
                        <div className="ai-bot-messages">
                            {messages.map((m, idx) => (
                                <div key={idx} className={`ai-bot-msg-row ${m.sender}`}>
                                    {m.sender === "bot" && <div className="msg-bot-icon">🤖</div>}
                                    <div className={`ai-bot-bubble ${m.sender}`}>
                                        <div
                                            className="msg-content"
                                            dangerouslySetInnerHTML={{
                                                __html: m.text
                                                    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
                                                    .replace(/\n/g, "<br />")
                                            }}
                                        />
                                    </div>
                                </div>
                            ))}
                            {loading && (
                                <div className="ai-bot-msg-row bot">
                                    <div className="msg-bot-icon">🤖</div>
                                    <div className="ai-bot-bubble bot typing">
                                        <span className="typing-dot" />
                                        <span className="typing-dot" />
                                        <span className="typing-dot" />
                                    </div>
                                </div>
                            )}
                            <div ref={messagesEndRef} />
                        </div>
                    </div>

                    {/* REPORT ISSUE FOOTER BAR */}
                    <div className="ai-bot-quick-report-bar">
                        <button
                            type="button"
                            className="ai-bot-report-trigger-btn"
                            onClick={() => {
                                setIsOpen(false);
                                openReportProblemModal({ feature: "General" });
                            }}
                        >
                            <FiAlertCircle /> Report an Issue or Bug to Team
                        </button>
                    </div>

                    {/* INPUT FORM */}
                    <form className="ai-bot-input-form" onSubmit={handleSendMessage}>
                        <input
                            type="text"
                            className="ai-bot-input"
                            placeholder="Ask a question about Samprepix..."
                            value={inputVal}
                            onChange={(e) => setInputVal(e.target.value)}
                            disabled={loading}
                        />
                        <button
                            type="submit"
                            className="ai-bot-send-btn"
                            disabled={!inputVal.trim() || loading}
                            aria-label="Send query"
                        >
                            <FiSend />
                        </button>
                    </form>
                </div>
            )}

            {/* FLOATING CIRCULAR TRIGGER BUTTON */}
            {!isOpen && (
                <button
                    className="ai-bot-trigger-btn"
                    onClick={() => setIsOpen(true)}
                    aria-label="Open AI Product Assistant"
                    title="Samprepix Product Assistant"
                >
                    <span className="trigger-icon">🤖</span>
                    <span className="trigger-pulse" />
                </button>
            )}
        </div>
    );
}
