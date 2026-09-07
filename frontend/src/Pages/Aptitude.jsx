import React, { useState, useEffect, useRef } from "react";
import {
    FiPercent,
    FiCpu,
    FiBookOpen,
    FiBarChart2,
    FiArrowRight,
    FiArrowLeft,
    FiCheck,
    FiClock,
    FiHelpCircle,
    FiCheckCircle,
    FiXCircle,
    FiRotateCcw,
    FiAward
} from "react-icons/fi";
import { APTITUDE_TRACKS } from "./aptitudeData";
import "../styles/aptitude.css";

export default function Aptitude() {
    const [selectedTrackId, setSelectedTrackId] = useState(null);
    const [selectedTopicId, setSelectedTopicId] = useState(null);
    const [viewMode, setViewMode] = useState("overview"); // "overview" | "track-detail" | "practice"

    // Practice Sandbox States
    const [currentQuestionIdx, setCurrentQuestionIdx] = useState(0);
    const [selectedOption, setSelectedOption] = useState(null);
    const [answerSubmitted, setAnswerSubmitted] = useState(false);
    const [secondsElapsed, setSecondsElapsed] = useState(0);
    const [timerActive, setTimerActive] = useState(false);

    const gridRef = useRef(null);

    // Get current track & topic
    const currentTrack = APTITUDE_TRACKS.find((t) => t.id === selectedTrackId) || APTITUDE_TRACKS[0];
    const currentTopic = currentTrack?.topics?.find((top) => top.id === selectedTopicId) || currentTrack?.topics?.[0];
    const questions = currentTopic?.questions || [];
    const currentQuestion = questions[currentQuestionIdx] || questions[0];

    // Timer effect for practice mode
    useEffect(() => {
        let interval = null;
        if (viewMode === "practice" && timerActive) {
            interval = setInterval(() => {
                setSecondsElapsed((prev) => prev + 1);
            }, 1000);
        }
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [viewMode, timerActive]);

    const formatTimer = (secs) => {
        const m = Math.floor(secs / 60);
        const s = secs % 60;
        return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    };

    const handleExploreTrack = (trackId) => {
        setSelectedTrackId(trackId);
        setViewMode("track-detail");
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleStartPractice = (topicId) => {
        setSelectedTopicId(topicId);
        setCurrentQuestionIdx(0);
        setSelectedOption(null);
        setAnswerSubmitted(false);
        setSecondsElapsed(0);
        setTimerActive(true);
        setViewMode("practice");
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleBackToTracks = () => {
        setViewMode("overview");
        setSelectedTrackId(null);
        setSelectedTopicId(null);
        setTimerActive(false);
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleBackToCurriculum = () => {
        setViewMode("track-detail");
        setSelectedTopicId(null);
        setTimerActive(false);
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleOptionSelect = (optionId) => {
        if (!answerSubmitted) {
            setSelectedOption(optionId);
        }
    };

    const handleSubmitAnswer = () => {
        if (selectedOption) {
            setAnswerSubmitted(true);
        }
    };

    const handleNextQuestion = () => {
        if (currentQuestionIdx < questions.length - 1) {
            setCurrentQuestionIdx((prev) => prev + 1);
            setSelectedOption(null);
            setAnswerSubmitted(false);
        }
    };

    const handlePrevQuestion = () => {
        if (currentQuestionIdx > 0) {
            setCurrentQuestionIdx((prev) => prev - 1);
            setSelectedOption(null);
            setAnswerSubmitted(false);
        }
    };

    const handleResetPractice = () => {
        setCurrentQuestionIdx(0);
        setSelectedOption(null);
        setAnswerSubmitted(false);
        setSecondsElapsed(0);
        setTimerActive(true);
    };

    const getTrackIcon = (trackId) => {
        switch (trackId) {
            case "quantitative":
                return <FiPercent size={22} />;
            case "logical":
                return <FiCpu size={22} />;
            case "verbal":
                return <FiBookOpen size={22} />;
            case "data_interpretation":
                return <FiBarChart2 size={22} />;
            default:
                return <FiPercent size={22} />;
        }
    };

    return (
        <div className="aptitude-page">
            {/* ====================================================
                VIEW 1: OVERVIEW & 4 TRACKS
            ==================================================== */}
            {viewMode === "overview" && (
                <>
                    <header className="aptitude-header">
                        <div className="aptitude-hero-content">
                            <span className="aptitude-eyebrow">CAMPUS PLACEMENT PREPARATION</span>
                            <h1>Aptitude</h1>
                            <p>
                                Master the 4 critical placement screening tracks: Quantitative Aptitude,
                                Logical Reasoning, Verbal Ability, and Data Interpretation.
                            </p>
                            <div className="aptitude-hero-actions">
                                <button
                                    type="button"
                                    className="aptitude-cta-btn"
                                    onClick={() => {
                                        gridRef.current?.scrollIntoView({ behavior: "smooth" });
                                    }}
                                >
                                    <span>Explore Curriculum</span>
                                    <FiArrowRight size={16} />
                                </button>
                            </div>
                        </div>
                    </header>

                    <div className="aptitude-grid" ref={gridRef}>
                        {APTITUDE_TRACKS.map((track) => (
                            <div key={track.id} className="aptitude-card">
                                <div className="aptitude-card-header">
                                    <div className="aptitude-card-icon">
                                        {getTrackIcon(track.id)}
                                    </div>
                                    <div>
                                        <h2 className="aptitude-card-title">{track.title}</h2>
                                        <div className="aptitude-card-meta">
                                            <span className="meta-badge">{track.badge}</span>
                                            <span className="meta-sub">{track.frequency}</span>
                                        </div>
                                    </div>
                                </div>

                                <p className="aptitude-card-desc">{track.description}</p>

                                <div className="aptitude-card-topics">
                                    <span className="aptitude-topics-label">Curriculum Highlights</span>
                                    <ul className="aptitude-topics-list">
                                        {track.topics.slice(0, 4).map((top, idx) => (
                                            <li key={idx} className="aptitude-topic-item">
                                                {top.title}
                                            </li>
                                        ))}
                                        {track.topics.length > 4 && (
                                            <li className="aptitude-topic-item more-item">
                                                +{track.topics.length - 4} more core topics
                                            </li>
                                        )}
                                    </ul>
                                </div>

                                <div className="aptitude-card-footer">
                                    <button
                                        type="button"
                                        className="aptitude-action-btn"
                                        onClick={() => handleExploreTrack(track.id)}
                                    >
                                        <span>Explore Track</span>
                                        <FiArrowRight size={15} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            )}

            {/* ====================================================
                VIEW 2: TRACK SYLLABUS DETAIL
            ==================================================== */}
            {viewMode === "track-detail" && (
                <div className="track-detail-view">
                    <button
                        type="button"
                        className="aptitude-back-btn"
                        onClick={handleBackToTracks}
                    >
                        <FiArrowLeft size={16} />
                        <span>Back to All Tracks</span>
                    </button>

                    <div className="track-detail-header">
                        <div className="track-header-left">
                            <div className="track-header-icon">
                                {getTrackIcon(currentTrack.id)}
                            </div>
                            <div>
                                <div className="track-badge-row">
                                    <span className="track-badge">{currentTrack.badge}</span>
                                    <span className="track-badge secondary">{currentTrack.frequency}</span>
                                </div>
                                <h1>{currentTrack.title}</h1>
                                <p>{currentTrack.description}</p>
                            </div>
                        </div>
                    </div>

                    <div className="syllabus-section">
                        <div className="syllabus-header-row">
                            <h3>Complete Placement Syllabus</h3>
                            <span className="syllabus-count">
                                {currentTrack.topics.length} Structured Modules
                            </span>
                        </div>

                        <div className="syllabus-grid">
                            {currentTrack.topics.map((topic, index) => (
                                <div key={topic.id} className="syllabus-card">
                                    <div className="syllabus-card-top">
                                        <span className="topic-index">
                                            Topic {String(index + 1).padStart(2, "0")}
                                        </span>
                                        <div className="topic-tags">
                                            <span className={`difficulty-pill ${topic.difficulty.toLowerCase()}`}>
                                                {topic.difficulty}
                                            </span>
                                            <span className="frequency-pill">
                                                {topic.frequency}
                                            </span>
                                        </div>
                                    </div>

                                    <h4 className="topic-title">{topic.title}</h4>
                                    <p className="topic-summary">{topic.summary}</p>

                                    {topic.formula && (
                                        <div className="topic-formula-box">
                                            <span className="formula-label">Core Formula:</span>
                                            <code>{topic.formula}</code>
                                        </div>
                                    )}

                                    <div className="syllabus-card-bottom">
                                        <span className="question-count-badge">
                                            {topic.questions.length} Placement Questions
                                        </span>
                                        <button
                                            type="button"
                                            className="topic-practice-btn"
                                            onClick={() => handleStartPractice(topic.id)}
                                        >
                                            <span>Practice</span>
                                            <FiArrowRight size={14} />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* ====================================================
                VIEW 3: INTERACTIVE QUESTION-PRACTICE SANDBOX
            ==================================================== */}
            {viewMode === "practice" && (
                <div className="practice-sandbox-view">
                    <div className="practice-nav-bar">
                        <button
                            type="button"
                            className="aptitude-back-btn"
                            onClick={handleBackToCurriculum}
                        >
                            <FiArrowLeft size={16} />
                            <span>Back to {currentTrack.shortTitle} Curriculum</span>
                        </button>

                        <div className="practice-timer-card">
                            <FiClock size={16} />
                            <span>{formatTimer(secondsElapsed)}</span>
                        </div>
                    </div>

                    <div className="sandbox-card">
                        {/* SANDBOX HEADER */}
                        <div className="sandbox-header">
                            <div>
                                <span className="sandbox-breadcrumb">
                                    {currentTrack.title} • {currentTopic.title}
                                </span>
                                <h2>
                                    Question {currentQuestionIdx + 1} of {questions.length}
                                </h2>
                            </div>
                            <div className="sandbox-badges">
                                <span className={`difficulty-pill ${currentTopic.difficulty.toLowerCase()}`}>
                                    {currentTopic.difficulty}
                                </span>
                                <span className="frequency-pill">Placement MCQ</span>
                            </div>
                        </div>

                        {/* FORMULA CALLOUT */}
                        {currentTopic.formula && (
                            <div className="sandbox-formula-hint">
                                <FiHelpCircle size={15} />
                                <div>
                                    <strong>Formula Reference: </strong>
                                    <code>{currentTopic.formula}</code>
                                </div>
                            </div>
                        )}

                        {/* QUESTION PROMPT */}
                        <div className="question-prompt-box">
                            <p>{currentQuestion?.prompt}</p>
                        </div>

                        {/* OPTIONS CONTAINER */}
                        <div className="options-container">
                            {currentQuestion?.options.map((option) => {
                                const isSelected = selectedOption === option.id;
                                const isCorrect = currentQuestion.correctOption === option.id;

                                let statusClass = "";
                                if (isSelected) statusClass = "selected";
                                if (answerSubmitted) {
                                    if (isCorrect) statusClass = "correct";
                                    else if (isSelected && !isCorrect) statusClass = "incorrect";
                                }

                                return (
                                    <button
                                        key={option.id}
                                        type="button"
                                        className={`option-card ${statusClass}`}
                                        onClick={() => handleOptionSelect(option.id)}
                                        disabled={answerSubmitted}
                                    >
                                        <div className="option-letter-circle">
                                            {option.id}
                                        </div>
                                        <div className="option-text">
                                            {option.text}
                                        </div>
                                        {answerSubmitted && isCorrect && (
                                            <FiCheckCircle className="option-status-icon correct" size={18} />
                                        )}
                                        {answerSubmitted && isSelected && !isCorrect && (
                                            <FiXCircle className="option-status-icon incorrect" size={18} />
                                        )}
                                    </button>
                                );
                            })}
                        </div>

                        {/* SUBMIT / CHECK ACTIONS */}
                        <div className="sandbox-actions-bar">
                            <div className="action-left">
                                <button
                                    type="button"
                                    className="sandbox-secondary-btn"
                                    onClick={handlePrevQuestion}
                                    disabled={currentQuestionIdx === 0}
                                >
                                    <FiArrowLeft size={15} />
                                    <span>Previous</span>
                                </button>

                                <button
                                    type="button"
                                    className="sandbox-secondary-btn"
                                    onClick={handleNextQuestion}
                                    disabled={currentQuestionIdx === questions.length - 1}
                                >
                                    <span>Next</span>
                                    <FiArrowRight size={15} />
                                </button>
                            </div>

                            <div className="action-right">
                                {!answerSubmitted ? (
                                    <button
                                        type="button"
                                        className="sandbox-primary-btn"
                                        onClick={handleSubmitAnswer}
                                        disabled={!selectedOption}
                                    >
                                        <FiCheck size={16} />
                                        <span>Check Answer</span>
                                    </button>
                                ) : (
                                    <button
                                        type="button"
                                        className="sandbox-reset-btn"
                                        onClick={handleResetPractice}
                                    >
                                        <FiRotateCcw size={15} />
                                        <span>Reset Sandbox</span>
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* STEP-BY-STEP EXPLANATION REVEAL CONTAINER */}
                        {answerSubmitted && (
                            <div
                                className={`explanation-container ${
                                    selectedOption === currentQuestion.correctOption ? "correct-box" : "incorrect-box"
                                }`}
                            >
                                <div className="explanation-header">
                                    {selectedOption === currentQuestion.correctOption ? (
                                        <div className="result-tag success">
                                            <FiCheckCircle size={18} />
                                            <span>Correct Answer!</span>
                                        </div>
                                    ) : (
                                        <div className="result-tag error">
                                            <FiXCircle size={18} />
                                            <span>
                                                Incorrect — Correct is Option {currentQuestion.correctOption}
                                            </span>
                                        </div>
                                    )}
                                </div>

                                <div className="explanation-body">
                                    <h5>Step-by-Step Mathematical Derivation:</h5>
                                    <p>{currentQuestion.explanation}</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

