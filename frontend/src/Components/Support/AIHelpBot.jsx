import React, { useState, useEffect, useRef, useCallback } from "react";
import { useLocation } from "react-router-dom";
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
    FiLayers,
    FiCornerDownRight
} from "react-icons/fi";
import { askSupportQuestion, openReportProblemModal } from "../../services/supportService";
import { getCleanToken } from "../../services/authService";
import "../../styles/aiHelpBot.css";

// Internal application routes where the AI Assistant is allowed to appear
const LOGGED_IN_APP_ROUTES = [
    "/dashboard",
    "/resume-analyzer",
    "/mock-interview",
    "/interview-result",
    "/coding-arena",
    "/aptitude",
    "/performance",
    "/analytics",
    "/billing-history",
    "/github-analyzer",
    "/ai-roadmap",
    "/profile",
    "/admin"
];

// 5 Predefined questions with zero-cost instant offline answers
const PREDEFINED_QUESTIONS = [
    {
        id: "roadmap",
        icon: <FiCompass />,
        title: "How does the AI Roadmap work?",
        answer: "The **Samprepix AI Roadmap** generates a step-by-step personalized placement curriculum based on your target role, current skills, and timeline. You can view milestones, mark topics as completed, track overall progress percentages, and launch targeted interview or coding practice for each milestone."
    },
    {
        id: "github",
        icon: <FiLayers />,
        title: "How to use the GitHub Profile Analyzer?",
        answer: "The **GitHub Profile Analyzer** evaluates your public repositories, languages, commit patterns, and code contributions. It generates an objective 0–100 recruiter readiness score, audits your repositories, and generates a tailored profile README to strengthen your engineering portfolio."
    },
    {
        id: "arena",
        icon: <FiCode />,
        title: "What languages are supported in Coding Arena?",
        answer: "The **Coding Arena** supports 8 industry programming languages: Java, Python, C++, C, JavaScript, TypeScript, Go, and Rust. You can write code with syntax highlighting, run code against custom inputs or hidden test cases, get real-time compiler diagnostics, and request AI hints when stuck."
    },
    {
        id: "interviews",
        icon: <FiHelpCircle />,
        title: "How do I practice Mock Interviews?",
        answer: "To practice **AI Mock Interviews**, select your engineering track (such as Java Full Stack, Frontend, or Core CS) and difficulty level. You can answer using voice speech-to-text recognition or text. The AI provides real-time follow-ups and delivers an instant performance scorecard with improvement tips."
    },
    {
        id: "metrics",
        icon: <FiAward />,
        title: "Where can I see my performance metrics?",
        answer: "You can track your comprehensive placement analytics in the **Performance Hub** and **Analytics** dashboard. It monitors your coding accuracy, mock interview scores over time, topic breakdowns, streak metrics, and placement readiness index."
    }
];

const CONTEXTUAL_QUESTIONS = {
    roadmap: [
        { id: "ctx-rm1", title: "Can I customize the roadmap target role or timeline?" },
        { id: "ctx-rm2", title: "Where can I see my performance metrics?" }
    ],
    github: [
        { id: "ctx-gh1", title: "How does the README generator customize for Java Full Stack?" },
        { id: "ctx-gh2", title: "What factors reduce my GitHub recruiter score?" }
    ],
    arena: [
        { id: "ctx-ar1", title: "How do AI hints work in the Coding Arena?" },
        { id: "ctx-ar2", title: "Can I run custom test inputs before submitting code?" }
    ],
    interviews: [
        { id: "ctx-in1", title: "What evaluation criteria are scored in mock interviews?" },
        { id: "ctx-in2", title: "Where can I see my performance metrics?" }
    ],
    metrics: [
        { id: "ctx-me1", title: "How is the placement readiness score calculated?" },
        { id: "ctx-me2", title: "How do I practice Mock Interviews?" }
    ]
};

const CONTEXTUAL_ANSWERS = {
    "ctx-rm1": "Yes! In the **AI Roadmap**, you can customize your target role, track (Java Full Stack, Frontend, Core CS), and target preparation timeline (30, 60, or 90 days) to automatically balance weekly milestones.",
    "ctx-rm2": "You can view your detailed analytics in the **Performance Hub** and **Analytics** dashboard. It monitors your coding accuracy, mock interview scores over time, topic breakdowns, streak metrics, and placement readiness index.",
    "ctx-gh1": "The **GitHub Profile Analyzer** automatically detects public repositories, verifies Java/Spring Boot frameworks and MySQL/REST APIs, and enriches your README with recruiter-friendly metrics and technical highlights.",
    "ctx-gh2": "Factors that reduce your GitHub recruiter score include missing repository descriptions, absence of a profile README (`username/username`), unpinned projects, and lack of recent commit activity.",
    "ctx-ar1": "In **Coding Arena**, clicking 'Ask AI Hint' analyzes your current code logic and algorithmic complexity to provide progressive architectural guidance without spoiling the full solution.",
    "ctx-ar2": "Yes! The Coding Arena test console includes a 'Custom Input' tab where you can enter custom edge cases and inspect execution output and memory diagnostics before submitting.",
    "ctx-in1": "AI Mock Interviews evaluate candidates on 4 core dimensions: Technical Accuracy, Architectural Depth, Communication Clarity, and Problem-Solving Strategy, accompanied by an instant scorecard.",
    "ctx-in2": "Detailed scorecards with question-by-question transcripts, audio confidence analysis, and suggested improvements are saved directly in your **Performance Hub**.",
    "ctx-me1": "The placement readiness index is calculated deterministically from your Coding Arena problem completion, AI Mock Interview scores, ATS resume rating, and roadmap milestones.",
    "ctx-me2": "To practice **AI Mock Interviews**, select your engineering track (such as Java Full Stack, Frontend, or Core CS) and difficulty level. You can answer using voice speech-to-text recognition or text."
};

export default function AIHelpBot() {
    const location = useLocation();
    const [isOpen, setIsOpen] = useState(false);
    const [isEdgeCollapsed, setIsEdgeCollapsed] = useState(() => {
        return localStorage.getItem("samprepix_bot_collapsed") === "true";
    });
    const [showGreeting, setShowGreeting] = useState(false);
    const [messages, setMessages] = useState([
        {
            sender: "bot",
            text: "Hi! 👋 I'm your Samprepix AI Assistant. How can I help you with your placement preparation today?"
        }
    ]);
    const [inputVal, setInputVal] = useState("");
    const [loading, setLoading] = useState(false);
    const [isTyping, setIsTyping] = useState(false);
    const [activeFollowups, setActiveFollowups] = useState(null);
    const messagesEndRef = useRef(null);
    const typingTimerRef = useRef(null);

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

    // Cleanup timer on unmount
    useEffect(() => {
        return () => {
            if (typingTimerRef.current) {
                clearInterval(typingTimerRef.current);
            }
        };
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

    const scrollToBottom = useCallback(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, []);

    useEffect(() => {
        if (isOpen) {
            scrollToBottom();
        }
    }, [messages, isTyping, isOpen, scrollToBottom]);

    // Natural word-by-word streaming effect
    const streamBotResponse = useCallback((fullText) => {
        return new Promise((resolve) => {
            // 1. Show animated typing indicator first (thinking state)
            setIsTyping(true);

            setTimeout(() => {
                setIsTyping(false);

                // 2. Add new empty bot message bubble
                setMessages((prev) => [...prev, { sender: "bot", text: "" }]);

                // 3. Tokenize by words and spaces
                const tokens = fullText.split(/(\s+)/);
                let currentAccumulated = "";
                let tokenIdx = 0;

                if (typingTimerRef.current) {
                    clearInterval(typingTimerRef.current);
                }

                typingTimerRef.current = setInterval(() => {
                    if (tokenIdx < tokens.length) {
                        currentAccumulated += tokens[tokenIdx];
                        tokenIdx++;

                        setMessages((prev) => {
                            const copy = [...prev];
                            copy[copy.length - 1] = {
                                sender: "bot",
                                text: currentAccumulated
                            };
                            return copy;
                        });
                        scrollToBottom();
                    } else {
                        clearInterval(typingTimerRef.current);
                        typingTimerRef.current = null;
                        resolve();
                    }
                }, 16);
            }, 180);
        });
    }, [scrollToBottom]);

    const handlePredefinedClick = async (q) => {
        if (loading || isTyping) return;

        // Immediately append user question
        setMessages((prev) => [...prev, { sender: "user", text: q.title }]);
        setLoading(true);

        try {
            await streamBotResponse(q.answer);
            if (CONTEXTUAL_QUESTIONS[q.id]) {
                setActiveFollowups(CONTEXTUAL_QUESTIONS[q.id]);
            }
        } finally {
            setLoading(false);
            setIsTyping(false);
        }
    };

    const handleContextualClick = async (q) => {
        if (loading || isTyping) return;

        setMessages((prev) => [...prev, { sender: "user", text: q.title }]);
        setLoading(true);

        const answerText = CONTEXTUAL_ANSWERS[q.id] || "You can explore this directly in the relevant module or ask me any question about your placement preparation!";
        try {
            await streamBotResponse(answerText);
        } finally {
            setLoading(false);
            setIsTyping(false);
        }
    };

    const handleAsk5QuestionsAgain = () => {
        if (loading || isTyping) return;
        // Clean non-recursive reset back to starter questions
        setActiveFollowups(null);
        scrollToBottom();
    };

    const handleSendMessage = async (e) => {
        e?.preventDefault();
        const text = inputVal.trim();
        if (!text || loading || isTyping) return;

        // Show user message immediately and clear input
        setInputVal("");
        setMessages((prev) => [...prev, { sender: "user", text }]);
        setLoading(true);
        setIsTyping(true);

        try {
            const data = await askSupportQuestion(text);
            const answerText = data?.answer || "I'm here to assist with all Samprepix placement features — including your AI Roadmap, Coding Arena, Mock Interviews, Resume Analyzer, and GitHub Profiler. How can I guide you?";
            await streamBotResponse(answerText);
        } catch (error) {
            console.error("AI Help Bot query failed:", error);
            await streamBotResponse("I'm having trouble connecting to the assistant service right now. You can check the quick questions above or report an issue directly to our team!");
        } finally {
            setLoading(false);
            setIsTyping(false);
        }
    };

    const token = getCleanToken();
    const isAllowedRoute = LOGGED_IN_APP_ROUTES.some(
        (route) => location.pathname === route || location.pathname.startsWith(`${route}/`)
    );

    // Strictly render nothing on public pages or unauthenticated sessions
    if (!token || !isAllowedRoute) {
        return null;
    }

    // Edge-collapsed view: Sleek vertical dock on right viewport edge (Desktop) / compact circular trigger (Mobile)
    if (isEdgeCollapsed) {
        return (
            <>
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
                <div className="ai-bot-mobile-collapsed-trigger">
                    <button
                        className="ai-bot-trigger-btn"
                        onClick={() => {
                            setIsEdgeCollapsed(false);
                            localStorage.setItem("samprepix_bot_collapsed", "false");
                            setIsOpen(true);
                        }}
                        aria-label="Open AI Product Assistant"
                        title="Samprepix Product Assistant"
                    >
                        <span className="trigger-icon">🤖</span>
                    </button>
                </div>
            </>
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
                        {/* QUICK QUESTIONS SECTION (COLLAPSIBLE / CLEAN) */}
                        <div className="ai-bot-quick-section">
                            <span className="quick-section-title">Quick Platform Answers:</span>
                            <div className="ai-bot-quick-list">
                                {PREDEFINED_QUESTIONS.map((q) => (
                                    <button
                                        key={q.id}
                                        className="ai-bot-quick-chip"
                                        onClick={() => handlePredefinedClick(q)}
                                        disabled={loading || isTyping}
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

                            {/* ANIMATED TYPING INDICATOR (AI THINKING STATE) */}
                            {isTyping && (
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

                        {/* RE-SUGGESTION CHIPS AFTER MESSAGES (WHEN NOT STREAMING) */}
                        {!loading && !isTyping && messages.length > 1 && (
                            <div className="ai-bot-followup-suggestions">
                                <span className="followup-title">
                                    <FiCornerDownRight size={12} /> {activeFollowups ? "Related follow-ups:" : "Platform starter questions:"}
                                </span>
                                <div className="followup-chips-row">
                                    {activeFollowups ? (
                                        <>
                                            <button
                                                type="button"
                                                className="ai-bot-followup-chip reset-questions-chip"
                                                onClick={handleAsk5QuestionsAgain}
                                                title="Reset back to the 5 starter questions"
                                            >
                                                🔄 Ask the 5 starter questions again
                                            </button>
                                            {activeFollowups.map((q) => (
                                                <button
                                                    key={q.id}
                                                    type="button"
                                                    className="ai-bot-followup-chip"
                                                    onClick={() => handleContextualClick(q)}
                                                >
                                                    {q.title}
                                                </button>
                                            ))}
                                        </>
                                    ) : (
                                        PREDEFINED_QUESTIONS.map((q) => (
                                            <button
                                                key={`follow-${q.id}`}
                                                type="button"
                                                className="ai-bot-followup-chip"
                                                onClick={() => handlePredefinedClick(q)}
                                            >
                                                {q.title}
                                            </button>
                                        ))
                                    )}
                                </div>
                            </div>
                        )}
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
                            placeholder={loading || isTyping ? "AI is responding..." : "Ask a question about Samprepix..."}
                            value={inputVal}
                            onChange={(e) => setInputVal(e.target.value)}
                            disabled={loading || isTyping}
                        />
                        <button
                            type="submit"
                            className="ai-bot-send-btn"
                            disabled={!inputVal.trim() || loading || isTyping}
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
