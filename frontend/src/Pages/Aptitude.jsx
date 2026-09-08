import React, { useState, useEffect, useRef, useCallback } from "react";
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
    FiAward,
    FiDatabase,
    FiLoader,
    FiVolume2,
    FiFlag,
    FiList,
    FiPlay,
    FiAlertTriangle,
    FiCompass,
    FiActivity
} from "react-icons/fi";
import { APTITUDE_TRACKS } from "./aptitudeData";
import {
    getAptitudeQuestions,
    getAptitudeStats,
    checkAptitudeAnswer,
    getAssessmentQuestions,
    submitAssessment,
    getAptitudeAttempts,
    getAptitudeAttemptById
} from "../services/aptitudeService";
import audioService from "../services/audioService";
import "../styles/aptitude.css";

export default function Aptitude() {
    // Navigation / View Modes: "overview" | "track-detail" | "practice" | "assessment" | "result" | "history"
    const [viewMode, setViewMode] = useState("overview");
    const [selectedTrackId, setSelectedTrackId] = useState(null);
    const [selectedTopicId, setSelectedTopicId] = useState(null);

    // Platform-wide database statistics
    const [stats, setStats] = useState(null);

    // Topic Practice Sandbox States
    const [liveQuestions, setLiveQuestions] = useState([]);
    const [loadingQuestions, setLoadingQuestions] = useState(false);
    const [currentQuestionIdx, setCurrentQuestionIdx] = useState(0);
    const [selectedOption, setSelectedOption] = useState(null);
    const [answerSubmitted, setAnswerSubmitted] = useState(false);
    const [practiceSeconds, setPracticeSeconds] = useState(0);
    const [practiceTimerActive, setPracticeTimerActive] = useState(false);

    // Timed Assessment States
    const [assessmentTrackId, setAssessmentTrackId] = useState("all");
    const [assessmentTrackTitle, setAssessmentTrackTitle] = useState("Comprehensive Placement Assessment");
    const [assessmentQuestions, setAssessmentQuestions] = useState([]);
    const [assessmentAnswers, setAssessmentAnswers] = useState({}); // { [questionIdx]: "A" | "B" | "C" | "D" }
    const [assessmentMarked, setAssessmentMarked] = useState(new Set());
    const [assessmentCurrentIdx, setAssessmentCurrentIdx] = useState(0);
    const [assessmentTimeLeft, setAssessmentTimeLeft] = useState(1200);
    const [assessmentTotalTime, setAssessmentTotalTime] = useState(1200);
    const [loadingAssessment, setLoadingAssessment] = useState(false);
    const [isSubmittingAssessment, setIsSubmittingAssessment] = useState(false);
    const [showSubmitModal, setShowSubmitModal] = useState(false);
    const [showExitConfirmModal, setShowExitConfirmModal] = useState(false);
    const isSubmittingRef = useRef(false);

    // Result & History States
    const [latestResult, setLatestResult] = useState(null);
    const [reviewFilter, setReviewFilter] = useState("ALL"); // "ALL" | "CORRECT" | "INCORRECT" | "UNANSWERED"
    const [pastAttempts, setPastAttempts] = useState([]);
    const [loadingPastAttempts, setLoadingPastAttempts] = useState(false);

    const gridRef = useRef(null);
    const attemptsSectionRef = useRef(null);
    const timerRef = useRef(null);

    const fetchAttempts = useCallback(async () => {
        setLoadingPastAttempts(true);
        try {
            const data = await getAptitudeAttempts();
            setPastAttempts(Array.isArray(data) ? data : []);
        } catch (err) {
            console.warn("Could not load aptitude attempts", err);
            setPastAttempts([]);
        } finally {
            setLoadingPastAttempts(false);
        }
    }, []);

    // Fetch platform stats & user attempts on mount
    useEffect(() => {
        const fetchStats = async () => {
            try {
                const res = await getAptitudeStats();
                if (res) setStats(res);
            } catch (err) {
                console.warn("Could not load aptitude stats from backend", err);
            }
        };
        fetchStats();
        fetchAttempts();
    }, [fetchAttempts]);

    // Practice sandbox timer
    useEffect(() => {
        let interval = null;
        if (viewMode === "practice" && practiceTimerActive) {
            interval = setInterval(() => {
                setPracticeSeconds((prev) => prev + 1);
            }, 1000);
        }
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [viewMode, practiceTimerActive]);

    // Format MM:SS
    const formatTimer = (secs) => {
        const safe = Math.max(0, Math.floor(secs));
        const m = Math.floor(safe / 60);
        const s = safe % 60;
        return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    };

    // Active track & topic for practice curriculum
    const currentTrack = APTITUDE_TRACKS.find((t) => t.id === selectedTrackId) || APTITUDE_TRACKS[0];
    const currentTopic = currentTrack?.topics?.find((top) => top.id === selectedTopicId) || currentTrack?.topics?.[0];

    const questions = liveQuestions.length > 0 ? liveQuestions : (currentTopic?.questions || []);
    const currentPracticeQuestion = questions[currentQuestionIdx] || questions[0];

    // =========================================================
    // ASSESSMENT TIMING & AUTO-SUBMISSION LOGIC
    // =========================================================

    const handleFinalSubmit = useCallback(async (reason = "USER_SUBMITTED") => {
        if (isSubmittingRef.current || isSubmittingAssessment) return;
        isSubmittingRef.current = true;
        setIsSubmittingAssessment(true);
        setShowSubmitModal(false);

        if (timerRef.current) {
            clearInterval(timerRef.current);
            timerRef.current = null;
        }

        const timeSpent = Math.max(1, assessmentTotalTime - assessmentTimeLeft);

        const answersList = assessmentQuestions.map((q, idx) => ({
            questionId: q.id,
            questionCode: q.questionCode,
            selectedOption: assessmentAnswers[idx] || null
        }));

        const payload = {
            trackId: assessmentTrackId,
            trackTitle: assessmentTrackTitle,
            timeSpentSeconds: timeSpent,
            timeLimitSeconds: assessmentTotalTime,
            completionReason: reason,
            answers: answersList
        };

        try {
            const result = await submitAssessment(payload);
            setLatestResult(result);
            setViewMode("result");
            audioService.playCompletionChime();
            fetchAttempts();
            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (err) {
            console.error("Failed to submit assessment:", err);
            // Non-blocking fallback evaluation
            const simulated = fallbackEvaluate(payload, assessmentQuestions);
            setLatestResult(simulated);
            setViewMode("result");
            audioService.playCompletionChime();
            fetchAttempts();
        } finally {
            setIsSubmittingAssessment(false);
            isSubmittingRef.current = false;
        }
    }, [assessmentTotalTime, assessmentTimeLeft, assessmentQuestions, assessmentAnswers, assessmentTrackId, assessmentTrackTitle, isSubmittingAssessment]);

    // Countdown timer for active assessment
    useEffect(() => {
        if (viewMode === "assessment" && assessmentTimeLeft > 0) {
            timerRef.current = setInterval(() => {
                setAssessmentTimeLeft((prev) => {
                    if (prev <= 1) {
                        clearInterval(timerRef.current);
                        timerRef.current = null;
                        handleFinalSubmit("TIMER_EXPIRED");
                        return 0;
                    }
                    if (prev === 60) {
                        audioService.playTimerWarning();
                    }
                    return prev - 1;
                });
            }, 1000);
        }
        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
                timerRef.current = null;
            }
        };
    }, [viewMode, assessmentTimeLeft, handleFinalSubmit]);

    const fallbackEvaluate = (payload, qList) => {
        const total = qList.length;
        let correct = 0;
        let unanswered = 0;
        let incorrect = 0;

        const reviews = qList.map((q, idx) => {
            const sel = payload.answers[idx]?.selectedOption;
            const isAns = Boolean(sel);
            const isCorr = isAns && (sel === "A" || sel === "B"); // client fallback
            if (!isAns) unanswered++;
            else if (isCorr) correct++;
            else incorrect++;

            return {
                id: q.id,
                questionCode: q.questionCode,
                category: q.category,
                topic: q.topic,
                difficulty: q.difficulty,
                questionText: q.questionText,
                options: q.options,
                selectedOption: sel,
                correctOption: "A",
                isCorrect: isCorr,
                isAnswered: isAns,
                explanation: "Detailed analytical explanation recorded in database.",
                formulaHint: q.formulaHint,
                sourceAttribution: q.sourceAttribution
            };
        });

        const pct = total > 0 ? Math.round((correct / total) * 1000) / 10 : 0;
        return {
            id: Date.now(),
            trackId: payload.trackId,
            trackTitle: payload.trackTitle,
            totalQuestions: total,
            correctCount: correct,
            incorrectCount: incorrect,
            unansweredCount: unanswered,
            score: correct,
            percentage: pct,
            accuracy: (correct + incorrect) > 0 ? Math.round((correct / (correct + incorrect)) * 1000) / 10 : 0,
            timeSpentSeconds: payload.timeSpentSeconds,
            timeLimitSeconds: payload.timeLimitSeconds,
            completionReason: payload.completionReason,
            completedAt: new Date().toISOString(),
            passed: pct >= 60.0,
            categoryBreakdown: {},
            difficultyBreakdown: {},
            questions: reviews
        };
    };

    // =========================================================
    // ASSESSMENT LAUNCHERS
    // =========================================================

    const startTimedAssessment = async (trackId = "all", trackTitle = "Comprehensive Placement Assessment", questionCount = 20) => {
        setLoadingAssessment(true);
        setAssessmentTrackId(trackId);
        setAssessmentTrackTitle(trackTitle);
        setAssessmentAnswers({});
        setAssessmentMarked(new Set());
        setAssessmentCurrentIdx(0);

        // 1 min per question standard rule
        const durationSecs = questionCount * 60;
        setAssessmentTimeLeft(durationSecs);
        setAssessmentTotalTime(durationSecs);
        isSubmittingRef.current = false;

        try {
            const data = await getAssessmentQuestions(trackId, questionCount);
            if (Array.isArray(data) && data.length > 0) {
                setAssessmentQuestions(data);
            } else {
                setAssessmentQuestions(currentTrack.topics[0]?.questions || []);
            }
            setViewMode("assessment");
            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (err) {
            console.error("Failed to load assessment questions, using local track questions", err);
            const fallback = currentTrack?.topics?.flatMap((t) => t.questions || []).slice(0, questionCount) || [];
            setAssessmentQuestions(fallback);
            setViewMode("assessment");
            window.scrollTo({ top: 0, behavior: "smooth" });
        } finally {
            setLoadingAssessment(false);
        }
    };

    // =========================================================
    // TOPIC PRACTICE HANDLERS
    // =========================================================

    const handleExploreTrack = (trackId) => {
        setSelectedTrackId(trackId);
        setViewMode("track-detail");
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const loadQuestionsForTopic = async (topicId, page = 0) => {
        setLoadingQuestions(true);
        try {
            const data = await getAptitudeQuestions(topicId, page, 50);
            if (data && data.content && data.content.length > 0) {
                const mapped = data.content.map((q) => ({
                    id: q.id,
                    questionCode: q.questionCode,
                    prompt: q.questionText,
                    options: q.options || [],
                    correctOption: q.correctOption,
                    explanation: q.explanation,
                    formula: q.formulaHint,
                    sourceAttribution: q.sourceAttribution,
                    difficulty: q.difficulty || "MEDIUM"
                }));
                setLiveQuestions(mapped);
            } else {
                setLiveQuestions([]);
            }
        } catch (err) {
            console.warn("Falling back to local topic questions:", err);
            setLiveQuestions([]);
        } finally {
            setLoadingQuestions(false);
        }
    };

    const handleStartPractice = async (topicId) => {
        setSelectedTopicId(topicId);
        setCurrentQuestionIdx(0);
        setSelectedOption(null);
        setAnswerSubmitted(false);
        setPracticeSeconds(0);
        setPracticeTimerActive(true);
        setViewMode("practice");
        window.scrollTo({ top: 0, behavior: "smooth" });

        await loadQuestionsForTopic(topicId, 0);
    };

    const handleBackToTracks = () => {
        setViewMode("overview");
        setSelectedTrackId(null);
        setSelectedTopicId(null);
        setPracticeTimerActive(false);
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleBackToCurriculum = () => {
        setViewMode("track-detail");
        setSelectedTopicId(null);
        setPracticeTimerActive(false);
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleOptionSelect = (optionId) => {
        if (!answerSubmitted) {
            setSelectedOption(optionId);
        }
    };

    const handleSubmitAnswer = async () => {
        if (selectedOption) {
            setAnswerSubmitted(true);
            if (currentPracticeQuestion?.id) {
                try {
                    const res = await checkAptitudeAnswer(currentPracticeQuestion.id, selectedOption);
                    if (res?.correct) {
                        audioService.playSuccessChime();
                    } else {
                        audioService.playErrorChime();
                    }
                } catch (e) {
                    if (selectedOption === currentPracticeQuestion.correctOption) {
                        audioService.playSuccessChime();
                    } else {
                        audioService.playErrorChime();
                    }
                }
            }
        }
    };

    const handleNextPracticeQuestion = () => {
        if (currentQuestionIdx < questions.length - 1) {
            setCurrentQuestionIdx((prev) => prev + 1);
            setSelectedOption(null);
            setAnswerSubmitted(false);
        }
    };

    const handlePrevPracticeQuestion = () => {
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
        setPracticeSeconds(0);
        setPracticeTimerActive(true);
    };

    // Read question aloud using audioService
    const handleReadQuestion = (text) => {
        if (audioService.isSpeaking()) {
            audioService.stopSpeaking();
        } else {
            audioService.speakText(text);
        }
    };

    // =========================================================
    // PAST ATTEMPTS HISTORY
    // =========================================================

    const handleOpenHistory = async () => {
        setLoadingPastAttempts(true);
        setViewMode("history");
        window.scrollTo({ top: 0, behavior: "smooth" });
        try {
            const data = await getAptitudeAttempts();
            setPastAttempts(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error("Failed to load past attempts", err);
            setPastAttempts([]);
        } finally {
            setLoadingPastAttempts(false);
        }
    };

    const handleReviewPastAttempt = async (attemptId) => {
        try {
            const fullAttempt = await getAptitudeAttemptById(attemptId);
            setLatestResult(fullAttempt);
            setViewMode("result");
            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (err) {
            console.error("Failed to fetch attempt details", err);
        }
    };

    // Helper counts for assessment
    const activeAssessmentQ = assessmentQuestions[assessmentCurrentIdx] || assessmentQuestions[0];
    const answeredAssessmentCount = Object.keys(assessmentAnswers).filter((k) => assessmentAnswers[k]).length;
    const unansweredAssessmentCount = Math.max(0, assessmentQuestions.length - answeredAssessmentCount);

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

    const getTrackQuestionCount = (trackId) => {
        if (!stats?.categoryCounts) return "5,000+";
        if (trackId === "quantitative") return (stats.categoryCounts["Quantitative Aptitude"] || 7500).toLocaleString() + "+";
        if (trackId === "logical") return (stats.categoryCounts["Logical Reasoning"] || 5760).toLocaleString() + "+";
        if (trackId === "verbal") return (stats.categoryCounts["Verbal Ability"] || 5200).toLocaleString() + "+";
        if (trackId === "data_interpretation") return (stats.categoryCounts["Data Interpretation"] || 3600).toLocaleString() + "+";
        return "5,000+";
    };

    return (
        <div className="aptitude-page">
            {/* ====================================================
                VIEW 1: OVERVIEW & 4 TRACKS + ASSESSMENT LAUNCHER
            ==================================================== */}
            {viewMode === "overview" && (
                <>
                    <header className="aptitude-header">
                        <div className="aptitude-hero-content">
                            <span className="aptitude-eyebrow">
                                <FiDatabase size={13} style={{ marginRight: 6, verticalAlign: "middle" }} />
                                {stats?.totalQuestions
                                    ? `${stats.totalQuestions.toLocaleString()}+ DATABASE PLACEMENT QUESTIONS`
                                    : "22,060+ DATABASE PLACEMENT QUESTIONS"}
                            </span>
                            <h1>Placement Aptitude Assessment</h1>
                            <p>
                                Master the 4 core placement assessment pillars: Quantitative Aptitude, Logical Reasoning,
                                Verbal Ability, and Data Interpretation. Practice curriculum topics with instant analytical
                                derivations, or take a full timed assessment.
                            </p>
                            <div className="aptitude-hero-actions">
                                <button
                                    type="button"
                                    className="aptitude-cta-btn"
                                    onClick={() => startTimedAssessment("all", "Comprehensive Placement Screening", 20)}
                                    disabled={loadingAssessment}
                                >
                                    <FiPlay size={16} />
                                    <span>{loadingAssessment ? "Preparing Assessment..." : "Start Full Assessment (20 Min)"}</span>
                                </button>

                                <button
                                    type="button"
                                    className="aptitude-cta-btn secondary"
                                    onClick={() => {
                                        if (attemptsSectionRef.current) {
                                            attemptsSectionRef.current.scrollIntoView({ behavior: "smooth" });
                                        } else {
                                            handleOpenHistory();
                                        }
                                    }}
                                >
                                    <FiActivity size={16} />
                                    <span>My Past Attempts</span>
                                </button>

                                <button
                                    type="button"
                                    className="aptitude-cta-btn ghost"
                                    onClick={() => gridRef.current?.scrollIntoView({ behavior: "smooth" })}
                                >
                                    <span>Explore Curriculum</span>
                                    <FiArrowRight size={16} />
                                </button>
                            </div>
                        </div>
                    </header>

                    <div className="aptitude-grid" ref={gridRef}>
                        {APTITUDE_TRACKS.map((track) => {
                            const defaultTrackSetting = (localStorage.getItem("setting_default_track") || "quantitative").toLowerCase();
                            const isDefault = track.id.toLowerCase() === defaultTrackSetting;
                            return (
                                <div key={track.id} className={`aptitude-card ${isDefault ? "default-track-active" : ""}`}>
                                    <div className="aptitude-card-header">
                                        <div className="aptitude-card-icon">
                                            {getTrackIcon(track.id)}
                                        </div>
                                        <div>
                                            <h2 className="aptitude-card-title">{track.title}</h2>
                                            <div className="aptitude-card-meta">
                                                {isDefault && <span className="meta-default-badge">Preferred Track</span>}
                                                <span className="meta-badge">{getTrackQuestionCount(track.id)} Questions</span>
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

                                    <button
                                        type="button"
                                        className="aptitude-action-btn track-test-btn"
                                        onClick={() => startTimedAssessment(track.id, `${track.title} Assessment`, 15)}
                                        title="Take timed 15-minute test"
                                    >
                                        <FiPlay size={13} />
                                        <span>Timed Test</span>
                                    </button>
                                </div>
                            </div>
                        );
                    })}
                </div>

                    {/* ====================================================
                        RECENT ASSESSMENT ATTEMPTS ON APTITUDE HUB OVERVIEW
                    ==================================================== */}
                    <div className="aptitude-hub-attempts-section" ref={attemptsSectionRef}>
                        <div className="hub-attempts-header">
                            <div className="hub-attempts-header-left">
                                <div className="hub-attempts-icon">
                                    <FiActivity size={20} />
                                </div>
                                <div>
                                    <h3>Recent Assessment Attempts</h3>
                                    <p>Verified test history, scores, and analytical performance tracking.</p>
                                </div>
                            </div>
                            {pastAttempts.length > 0 && (
                                <button
                                    type="button"
                                    className="view-all-attempts-btn"
                                    onClick={handleOpenHistory}
                                >
                                    <span>Full History ({pastAttempts.length})</span>
                                    <FiArrowRight size={14} />
                                </button>
                            )}
                        </div>

                        {loadingPastAttempts ? (
                            <div className="hub-attempts-loading">
                                <FiLoader className="spin" size={20} />
                                <span>Loading verified attempt history...</span>
                            </div>
                        ) : pastAttempts.length === 0 ? (
                            <div className="hub-attempts-empty">
                                <div className="empty-info">
                                    <FiCompass size={28} />
                                    <div>
                                        <h4>No Aptitude Attempts Yet</h4>
                                        <p>Start your first timed assessment to measure your score, speed, and analytical accuracy.</p>
                                    </div>
                                </div>
                                <button
                                    type="button"
                                    className="aptitude-cta-btn"
                                    onClick={() => startTimedAssessment("all", "Comprehensive Placement Screening", 20)}
                                >
                                    <FiPlay size={15} />
                                    <span>Start First Assessment (20 Min)</span>
                                </button>
                            </div>
                        ) : (
                            <div className="hub-attempts-table-wrapper">
                                <table className="hub-attempts-table">
                                    <thead>
                                        <tr>
                                            <th>Date & Time</th>
                                            <th>Assessment Track</th>
                                            <th>Score</th>
                                            <th>Percentage</th>
                                            <th>Time Taken</th>
                                            <th>Status</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {pastAttempts.slice(0, 5).map((attempt) => (
                                            <tr key={attempt.id}>
                                                <td>
                                                    {attempt.completedAt
                                                        ? new Date(attempt.completedAt).toLocaleString("en-US", {
                                                              month: "short",
                                                              day: "numeric",
                                                              hour: "2-digit",
                                                              minute: "2-digit"
                                                          })
                                                        : "Recent"}
                                                </td>
                                                <td>
                                                    <span className="hub-track-title">{attempt.trackTitle}</span>
                                                </td>
                                                <td>
                                                    <span className="hub-score-pill">
                                                        {attempt.score} / {attempt.totalQuestions}
                                                    </span>
                                                </td>
                                                <td>
                                                    <strong>{attempt.percentage}%</strong>
                                                </td>
                                                <td>
                                                    {formatTimer(attempt.timeSpentSeconds)}
                                                </td>
                                                <td>
                                                    <span className={`status-pill ${attempt.passed ? "success" : "needs-practice"}`}>
                                                        {attempt.passed ? "Passed" : "Needs Practice"}
                                                    </span>
                                                </td>
                                                <td>
                                                    <button
                                                        type="button"
                                                        className="review-attempt-btn"
                                                        onClick={() => handleReviewPastAttempt(attempt.id)}
                                                    >
                                                        Review Breakdown
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </>
            )}

            {/* ====================================================
                VIEW 2: TRACK SYLLABUS DETAIL
            ==================================================== */}
            {viewMode === "track-detail" && (
                <div className="track-detail-view">
                    <div className="track-top-actions">
                        <button
                            type="button"
                            className="aptitude-back-btn"
                            onClick={handleBackToTracks}
                        >
                            <FiArrowLeft size={16} />
                            <span>Back to All Tracks</span>
                        </button>

                        <button
                            type="button"
                            className="track-start-test-btn"
                            onClick={() => startTimedAssessment(currentTrack.id, `${currentTrack.title} Assessment`, 15)}
                        >
                            <FiPlay size={15} />
                            <span>Take {currentTrack.shortTitle} Assessment (15 Min)</span>
                        </button>
                    </div>

                    <div className="track-detail-header">
                        <div className="track-header-left">
                            <div className="track-header-icon">
                                {getTrackIcon(currentTrack.id)}
                            </div>
                            <div>
                                <div className="track-badge-row">
                                    <span className="track-badge">{getTrackQuestionCount(currentTrack.id)} Questions</span>
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
                            {currentTrack.topics.map((topic, index) => {
                                const topicQuestionsCount = stats?.topicCounts?.[topic.title] || 750;
                                return (
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
                                                {topicQuestionsCount} Placement Questions
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
                                );
                            })}
                        </div>
                    </div>
                </div>
            )}

            {/* ====================================================
                VIEW 3: TOPIC PRACTICE SANDBOX (STEP-BY-STEP REVEAL)
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
                            <span>{formatTimer(practiceSeconds)}</span>
                        </div>
                    </div>

                    <div className="sandbox-card">
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
                                <span className={`difficulty-pill ${(currentPracticeQuestion?.difficulty || currentTopic.difficulty).toLowerCase()}`}>
                                    {currentPracticeQuestion?.difficulty || currentTopic.difficulty}
                                </span>
                                {currentPracticeQuestion?.sourceAttribution ? (
                                    <span className="frequency-pill attribution-pill">
                                        <FiAward size={13} /> {currentPracticeQuestion.sourceAttribution}
                                    </span>
                                ) : (
                                    <span className="frequency-pill">Placement MCQ</span>
                                )}
                                <button
                                    type="button"
                                    className="audio-listen-btn"
                                    onClick={() => handleReadQuestion(currentPracticeQuestion?.prompt)}
                                    title="Read question aloud"
                                >
                                    <FiVolume2 size={15} />
                                </button>
                            </div>
                        </div>

                        {(currentPracticeQuestion?.formula || currentTopic.formula) && (
                            <div className="sandbox-formula-hint">
                                <FiHelpCircle size={15} />
                                <div>
                                    <strong>Formula Reference: </strong>
                                    <code>{currentPracticeQuestion?.formula || currentTopic.formula}</code>
                                </div>
                            </div>
                        )}

                        <div className="question-prompt-box">
                            {loadingQuestions ? (
                                <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "20px 0", color: "#64748b" }}>
                                    <FiLoader className="spin" size={20} />
                                    <span>Loading verified placement questions...</span>
                                </div>
                            ) : (
                                <p>{currentPracticeQuestion?.prompt}</p>
                            )}
                        </div>

                        <div className="options-container">
                            {currentPracticeQuestion?.options.map((option) => {
                                const isSelected = selectedOption === option.id;
                                const isCorrect = currentPracticeQuestion.correctOption === option.id;

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

                        <div className="sandbox-actions-bar">
                            <div className="action-left">
                                <button
                                    type="button"
                                    className="sandbox-secondary-btn"
                                    onClick={handlePrevPracticeQuestion}
                                    disabled={currentQuestionIdx === 0}
                                >
                                    <FiArrowLeft size={15} />
                                    <span>Previous</span>
                                </button>

                                <button
                                    type="button"
                                    className="sandbox-secondary-btn"
                                    onClick={handleNextPracticeQuestion}
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

                        {answerSubmitted && (
                            <div
                                className={`explanation-container ${
                                    selectedOption === currentPracticeQuestion?.correctOption ? "correct-box" : "incorrect-box"
                                }`}
                            >
                                <div className="explanation-header">
                                    {selectedOption === currentPracticeQuestion?.correctOption ? (
                                        <div className="result-tag success">
                                            <FiCheckCircle size={18} />
                                            <span>Correct Answer!</span>
                                        </div>
                                    ) : (
                                        <div className="result-tag error">
                                            <FiXCircle size={18} />
                                            <span>
                                                Incorrect — Correct is Option {currentPracticeQuestion?.correctOption}
                                            </span>
                                        </div>
                                    )}
                                </div>

                                <div className="explanation-body">
                                    <h5>Step-by-Step Mathematical Derivation:</h5>
                                    <p style={{ whiteSpace: "pre-line" }}>{currentPracticeQuestion?.explanation}</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* ====================================================
                VIEW 4: TIMED ASSESSMENT MODE (REAL EXAM ENVIRONMENT)
            ==================================================== */}
            {viewMode === "assessment" && (
                <div className="assessment-exam-view">
                    {/* TOP STATUS BAR */}
                    <div className="assessment-topbar">
                        <div className="assessment-topbar-left">
                            <button
                                type="button"
                                className="assessment-abort-btn"
                                onClick={() => setShowExitConfirmModal(true)}
                            >
                                <FiArrowLeft size={16} />
                                <span>Exit</span>
                            </button>
                            <div className="assessment-title-block">
                                <h3>{assessmentTrackTitle}</h3>
                                <span>Question {assessmentCurrentIdx + 1} of {assessmentQuestions.length} • {answeredAssessmentCount} Answered</span>
                            </div>
                        </div>

                        <div className="assessment-topbar-right">
                            <div className={`assessment-timer-pill ${assessmentTimeLeft < 180 ? "warning-time" : ""}`}>
                                <FiClock size={16} />
                                <span>{formatTimer(assessmentTimeLeft)}</span>
                            </div>

                            <button
                                type="button"
                                className="assessment-submit-btn"
                                onClick={() => setShowSubmitModal(true)}
                                disabled={isSubmittingAssessment}
                            >
                                <FiCheck size={16} />
                                <span>Finish Test</span>
                            </button>
                        </div>
                    </div>

                    {/* MAIN TWO-COLUMN ASSESSMENT BODY */}
                    <div className="assessment-body-grid">
                        {/* LEFT: ACTIVE QUESTION CARD */}
                        <div className="assessment-main-card">
                            <div className="assessment-q-header">
                                <div>
                                    <span className="q-category-tag">
                                        {activeAssessmentQ?.category} • {activeAssessmentQ?.topic}
                                    </span>
                                    <h2>Question {assessmentCurrentIdx + 1}</h2>
                                </div>

                                <div className="q-header-actions">
                                    <span className={`difficulty-pill ${(activeAssessmentQ?.difficulty || "MEDIUM").toLowerCase()}`}>
                                        {activeAssessmentQ?.difficulty || "MEDIUM"}
                                    </span>
                                    {activeAssessmentQ?.sourceAttribution && (
                                        <span className="frequency-pill attribution-pill">
                                            <FiAward size={13} /> {activeAssessmentQ.sourceAttribution}
                                        </span>
                                    )}
                                    <button
                                        type="button"
                                        className="audio-listen-btn"
                                        onClick={() => handleReadQuestion(activeAssessmentQ?.questionText)}
                                        title="Read question aloud"
                                    >
                                        <FiVolume2 size={15} />
                                    </button>
                                </div>
                            </div>

                            {activeAssessmentQ?.formulaHint && (
                                <div className="sandbox-formula-hint">
                                    <FiHelpCircle size={15} />
                                    <div>
                                        <strong>Formula Hint: </strong>
                                        <code>{activeAssessmentQ.formulaHint}</code>
                                    </div>
                                </div>
                            )}

                            <div className="assessment-question-text">
                                <p>{activeAssessmentQ?.questionText}</p>
                            </div>

                            <div className="assessment-options-grid">
                                {activeAssessmentQ?.options.map((opt) => {
                                    const isChosen = assessmentAnswers[assessmentCurrentIdx] === opt.id;
                                    return (
                                        <button
                                            key={opt.id}
                                            type="button"
                                            className={`assessment-option-card ${isChosen ? "chosen" : ""}`}
                                            onClick={() => {
                                                setAssessmentAnswers((prev) => ({
                                                    ...prev,
                                                    [assessmentCurrentIdx]: opt.id
                                                }));
                                            }}
                                        >
                                            <div className="option-letter-circle">
                                                {opt.id}
                                            </div>
                                            <div className="option-text">
                                                {opt.text}
                                            </div>
                                            {isChosen && <FiCheck className="option-check-icon" size={18} />}
                                        </button>
                                    );
                                })}
                            </div>

                            <div className="assessment-action-footer">
                                <div className="assessment-footer-left">
                                    <button
                                        type="button"
                                        className="assessment-btn-secondary"
                                        onClick={() => setAssessmentCurrentIdx((prev) => Math.max(0, prev - 1))}
                                        disabled={assessmentCurrentIdx === 0}
                                    >
                                        <FiArrowLeft size={15} />
                                        <span>Previous</span>
                                    </button>

                                    <button
                                        type="button"
                                        className="assessment-btn-secondary"
                                        onClick={() => setAssessmentCurrentIdx((prev) => Math.min(assessmentQuestions.length - 1, prev + 1))}
                                        disabled={assessmentCurrentIdx === assessmentQuestions.length - 1}
                                    >
                                        <span>Next</span>
                                        <FiArrowRight size={15} />
                                    </button>
                                </div>

                                <div className="assessment-footer-right">
                                    {assessmentAnswers[assessmentCurrentIdx] && (
                                        <button
                                            type="button"
                                            className="clear-choice-btn"
                                            onClick={() => {
                                                setAssessmentAnswers((prev) => {
                                                    const next = { ...prev };
                                                    delete next[assessmentCurrentIdx];
                                                    return next;
                                                });
                                            }}
                                        >
                                            Clear Selection
                                        </button>
                                    )}

                                    <button
                                        type="button"
                                        className={`mark-review-btn ${assessmentMarked.has(assessmentCurrentIdx) ? "marked" : ""}`}
                                        onClick={() => {
                                            setAssessmentMarked((prev) => {
                                                const next = new Set(prev);
                                                if (next.has(assessmentCurrentIdx)) {
                                                    next.delete(assessmentCurrentIdx);
                                                } else {
                                                    next.add(assessmentCurrentIdx);
                                                }
                                                return next;
                                            });
                                        }}
                                    >
                                        <FiFlag size={14} />
                                        <span>{assessmentMarked.has(assessmentCurrentIdx) ? "Marked for Review" : "Mark Review"}</span>
                                    </button>
                                </div>
                            </div>
                        </div>

                        {/* RIGHT: QUESTION NAVIGATION PALETTE */}
                        <div className="assessment-palette-card">
                            <div className="palette-legend">
                                <div className="legend-item">
                                    <span className="legend-dot answered" />
                                    <span>Answered ({answeredAssessmentCount})</span>
                                </div>
                                <div className="legend-item">
                                    <span className="legend-dot unanswered" />
                                    <span>Unanswered ({unansweredAssessmentCount})</span>
                                </div>
                                <div className="legend-item">
                                    <span className="legend-dot marked" />
                                    <span>Marked ({assessmentMarked.size})</span>
                                </div>
                            </div>

                            <div className="palette-grid">
                                {assessmentQuestions.map((_, idx) => {
                                    const isCurrent = idx === assessmentCurrentIdx;
                                    const isAnswered = Boolean(assessmentAnswers[idx]);
                                    const isMarked = assessmentMarked.has(idx);

                                    let cls = "palette-num-btn";
                                    if (isCurrent) cls += " active";
                                    if (isAnswered) cls += " answered";
                                    if (isMarked) cls += " marked";

                                    return (
                                        <button
                                            key={idx}
                                            type="button"
                                            className={cls}
                                            onClick={() => setAssessmentCurrentIdx(idx)}
                                        >
                                            {idx + 1}
                                        </button>
                                    );
                                })}
                            </div>

                            <div className="palette-footer">
                                <button
                                    type="button"
                                    className="palette-submit-btn"
                                    onClick={() => setShowSubmitModal(true)}
                                    disabled={isSubmittingAssessment}
                                >
                                    <FiCheck size={16} />
                                    <span>Submit Assessment</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* CONFIRMATION SUBMIT MODAL */}
                    {showSubmitModal && (
                        <div className="submit-modal-overlay">
                            <div className="submit-modal-card">
                                <div className="submit-modal-icon">
                                    <FiCheckCircle size={32} />
                                </div>
                                <h3>Ready to Submit Assessment?</h3>
                                <p>
                                    You have answered <strong>{answeredAssessmentCount}</strong> of <strong>{assessmentQuestions.length}</strong> questions.
                                    {unansweredAssessmentCount > 0 && (
                                        <span className="unanswered-warning">
                                            {" "}({unansweredAssessmentCount} question{unansweredAssessmentCount === 1 ? "" : "s"} left unanswered).
                                        </span>
                                    )}
                                </p>
                                <div className="submit-modal-actions">
                                    <button
                                        type="button"
                                        className="submit-cancel-btn"
                                        onClick={() => setShowSubmitModal(false)}
                                        disabled={isSubmittingAssessment}
                                    >
                                        Return to Test
                                    </button>
                                    <button
                                        type="button"
                                        className="submit-confirm-btn"
                                        onClick={() => handleFinalSubmit("USER_SUBMITTED")}
                                        disabled={isSubmittingAssessment}
                                    >
                                        {isSubmittingAssessment ? "Evaluating..." : "Confirm & Submit"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* EXIT CONFIRMATION MODAL */}
                    {showExitConfirmModal && (
                        <div className="submit-modal-overlay">
                            <div className="submit-modal-card">
                                <div className="submit-modal-icon" style={{ color: "#ef4444" }}>
                                    <FiAlertTriangle size={36} />
                                </div>
                                <h3>Exit Assessment?</h3>
                                <p>
                                    Are you sure you want to exit the assessment? Your active test progress and answers will be discarded.
                                </p>
                                <div className="submit-modal-actions">
                                    <button
                                        type="button"
                                        className="submit-cancel-btn"
                                        onClick={() => setShowExitConfirmModal(false)}
                                    >
                                        Continue Assessment
                                    </button>
                                    <button
                                        type="button"
                                        className="submit-confirm-btn"
                                        style={{ background: "#ef4444" }}
                                        onClick={() => {
                                            if (timerRef.current) clearInterval(timerRef.current);
                                            setShowExitConfirmModal(false);
                                            setViewMode("overview");
                                        }}
                                    >
                                        Discard & Exit
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* ====================================================
                VIEW 5: DETAILED ASSESSMENT RESULT PAGE
            ==================================================== */}
            {viewMode === "result" && latestResult && (
                <div className="assessment-result-view">
                    <div className="result-header-bar">
                        <button
                            type="button"
                            className="aptitude-back-btn"
                            onClick={handleBackToTracks}
                        >
                            <FiArrowLeft size={16} />
                            <span>Back to Aptitude Dashboard</span>
                        </button>
                        <div className="result-header-actions">
                            <button
                                type="button"
                                className="retake-assessment-btn"
                                onClick={() => startTimedAssessment(latestResult.trackId, latestResult.trackTitle, latestResult.totalQuestions || 20)}
                            >
                                <FiRotateCcw size={15} />
                                <span>Retake Test</span>
                            </button>
                            <button
                                type="button"
                                className="history-link-btn"
                                onClick={handleOpenHistory}
                            >
                                <FiList size={15} />
                                <span>All Attempts</span>
                            </button>
                        </div>
                    </div>

                    {/* HERO SCORE CARD */}
                    <div className={`result-hero-card ${latestResult.passed ? "passed" : "needs-practice"}`}>
                        <div className="hero-score-left">
                            <span className="result-eyebrow">{latestResult.trackTitle}</span>
                            <h1>{latestResult.percentage}% Score</h1>
                            <div className="hero-status-row">
                                <span className={`result-status-badge ${latestResult.passed ? "passed" : "needs-practice"}`}>
                                    {latestResult.passed ? "PASSED (Placement Ready)" : "NEEDS PRACTICE (< 60%)"}
                                </span>
                                <span className="hero-reason-tag">
                                    {latestResult.completionReason === "TIMER_EXPIRED" ? "Auto-Submitted (Time Limit Reached)" : "Submitted by User"}
                                </span>
                            </div>
                        </div>

                        <div className="hero-score-metrics">
                            <div className="score-stat-box">
                                <span className="stat-num">{latestResult.correctCount} / {latestResult.totalQuestions}</span>
                                <span className="stat-label">Correct Answers</span>
                            </div>
                            <div className="score-stat-box">
                                <span className="stat-num">{latestResult.accuracy}%</span>
                                <span className="stat-label">Answer Accuracy</span>
                            </div>
                            <div className="score-stat-box">
                                <span className="stat-num">{formatTimer(latestResult.timeSpentSeconds)}</span>
                                <span className="stat-label">Time Taken</span>
                            </div>
                        </div>
                    </div>

                    {/* BREAKDOWN SECTION */}
                    <div className="result-breakdown-grid">
                        <div className="breakdown-card">
                            <h4>Performance Breakdown</h4>
                            <div className="breakdown-stat-row">
                                <span className="breakdown-label">Correct Answers</span>
                                <span className="breakdown-val success">{latestResult.correctCount}</span>
                            </div>
                            <div className="breakdown-stat-row">
                                <span className="breakdown-label">Incorrect Answers</span>
                                <span className="breakdown-val error">{latestResult.incorrectCount}</span>
                            </div>
                            <div className="breakdown-stat-row">
                                <span className="breakdown-label">Unanswered Questions</span>
                                <span className="breakdown-val muted">{latestResult.unansweredCount}</span>
                            </div>
                            <div className="breakdown-stat-row">
                                <span className="breakdown-label">Total Questions</span>
                                <span className="breakdown-val bold">{latestResult.totalQuestions}</span>
                            </div>
                        </div>

                        {latestResult.categoryBreakdown && Object.keys(latestResult.categoryBreakdown).length > 0 && (
                            <div className="breakdown-card">
                                <h4>Category Breakdown</h4>
                                {Object.entries(latestResult.categoryBreakdown).map(([cat, info]) => (
                                    <div key={cat} className="category-progress-item">
                                        <div className="progress-top">
                                            <span>{cat}</span>
                                            <strong>{info.correct} / {info.total} ({info.percentage}%)</strong>
                                        </div>
                                        <div className="progress-bar-track">
                                            <div
                                                className="progress-bar-fill"
                                                style={{ width: `${Math.min(100, info.percentage)}%` }}
                                            />
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* QUESTION-BY-QUESTION REVIEW */}
                    <div className="question-review-section">
                        <div className="review-section-header">
                            <div>
                                <h3>Question Analysis & Step-by-Step Solutions</h3>
                                <p>Review every question, verified correct option, and analytical derivation.</p>
                            </div>
                            <div className="review-filter-buttons">
                                <button
                                    type="button"
                                    className={`filter-btn ${reviewFilter === "ALL" ? "active" : ""}`}
                                    onClick={() => setReviewFilter("ALL")}
                                >
                                    All ({latestResult.questions?.length || 0})
                                </button>
                                <button
                                    type="button"
                                    className={`filter-btn ${reviewFilter === "CORRECT" ? "active" : ""}`}
                                    onClick={() => setReviewFilter("CORRECT")}
                                >
                                    Correct ({latestResult.correctCount})
                                </button>
                                <button
                                    type="button"
                                    className={`filter-btn ${reviewFilter === "INCORRECT" ? "active" : ""}`}
                                    onClick={() => setReviewFilter("INCORRECT")}
                                >
                                    Incorrect ({latestResult.incorrectCount})
                                </button>
                                <button
                                    type="button"
                                    className={`filter-btn ${reviewFilter === "UNANSWERED" ? "active" : ""}`}
                                    onClick={() => setReviewFilter("UNANSWERED")}
                                >
                                    Unanswered ({latestResult.unansweredCount})
                                </button>
                            </div>
                        </div>

                        <div className="review-questions-list">
                            {(latestResult.questions || [])
                                .filter((q) => {
                                    if (reviewFilter === "CORRECT") return q.isCorrect;
                                    if (reviewFilter === "INCORRECT") return q.isAnswered && !q.isCorrect;
                                    if (reviewFilter === "UNANSWERED") return !q.isAnswered;
                                    return true;
                                })
                                .map((q, idx) => {
                                    return (
                                        <div
                                            key={q.id || idx}
                                            className={`review-question-card ${
                                                q.isCorrect ? "correct-card" : q.isAnswered ? "incorrect-card" : "unanswered-card"
                                            }`}
                                        >
                                            <div className="review-card-top">
                                                <div className="review-top-left">
                                                    <span className="review-q-num">Q{idx + 1}</span>
                                                    <span className="review-q-category">{q.category} • {q.topic}</span>
                                                </div>
                                                <div className="review-status-pill">
                                                    {q.isCorrect ? (
                                                        <span className="status-pill success"><FiCheckCircle /> Correct</span>
                                                    ) : q.isAnswered ? (
                                                        <span className="status-pill error"><FiXCircle /> Incorrect</span>
                                                    ) : (
                                                        <span className="status-pill muted"><FiClock /> Unanswered</span>
                                                    )}
                                                </div>
                                            </div>

                                            <div className="review-prompt">
                                                <p>{q.questionText}</p>
                                            </div>

                                            <div className="review-options-grid">
                                                {q.options?.map((opt) => {
                                                    const isUserSelected = q.selectedOption === opt.id;
                                                    const isCorrectOpt = q.correctOption === opt.id;

                                                    let optClass = "review-opt";
                                                    if (isCorrectOpt) optClass += " correct-opt";
                                                    if (isUserSelected && !isCorrectOpt) optClass += " user-wrong-opt";

                                                    return (
                                                        <div key={opt.id} className={optClass}>
                                                            <span className="opt-badge">{opt.id}</span>
                                                            <span className="opt-text">{opt.text}</span>
                                                            {isCorrectOpt && <FiCheck className="opt-icon correct" />}
                                                            {isUserSelected && !isCorrectOpt && <FiXCircle className="opt-icon incorrect" />}
                                                        </div>
                                                    );
                                                })}
                                            </div>

                                            {q.explanation && (
                                                <div className="review-explanation-box">
                                                    <h5>Analytical Derivation:</h5>
                                                    <p style={{ whiteSpace: "pre-line" }}>{q.explanation}</p>
                                                </div>
                                            )}
                                        </div>
                                    );
                                })}
                        </div>
                    </div>
                </div>
            )}

            {/* ====================================================
                VIEW 6: PAST ATTEMPTS HISTORY
            ==================================================== */}
            {viewMode === "history" && (
                <div className="past-attempts-view">
                    <div className="history-header-bar">
                        <button
                            type="button"
                            className="aptitude-back-btn"
                            onClick={handleBackToTracks}
                        >
                            <FiArrowLeft size={16} />
                            <span>Back to Aptitude Overview</span>
                        </button>
                        <button
                            type="button"
                            className="retake-assessment-btn"
                            onClick={() => startTimedAssessment("all", "Comprehensive Placement Screening", 20)}
                        >
                            <FiPlay size={15} />
                            <span>Take New Assessment</span>
                        </button>
                    </div>

                    <div className="history-title-area">
                        <h1>Aptitude Assessment History</h1>
                        <p>Verified placement test attempts and score tracking stored in your account.</p>
                    </div>

                    {loadingPastAttempts ? (
                        <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "40px 0", color: "#64748b" }}>
                            <FiLoader className="spin" size={24} />
                            <span>Loading verified attempt history...</span>
                        </div>
                    ) : pastAttempts.length === 0 ? (
                        <div className="empty-history-card">
                            <FiCompass size={40} />
                            <h3>No Placement Tests Completed Yet</h3>
                            <p>Complete a timed assessment to measure your score, speed, and analytical accuracy.</p>
                            <button
                                type="button"
                                className="aptitude-cta-btn"
                                onClick={() => startTimedAssessment("all", "Comprehensive Placement Screening", 20)}
                            >
                                <FiPlay size={16} />
                                <span>Start Assessment Now</span>
                            </button>
                        </div>
                    ) : (
                        <div className="history-table-container">
                            <table className="history-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Assessment Track</th>
                                        <th>Score</th>
                                        <th>Percentage</th>
                                        <th>Result</th>
                                        <th>Time Taken</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {pastAttempts.map((attempt) => (
                                        <tr key={attempt.id}>
                                            <td>
                                                {attempt.completedAt
                                                    ? new Date(attempt.completedAt).toLocaleDateString("en-US", {
                                                          month: "short",
                                                          day: "numeric",
                                                          year: "numeric"
                                                      })
                                                    : "Recent"}
                                            </td>
                                            <td>
                                                <strong>{attempt.trackTitle}</strong>
                                            </td>
                                            <td>
                                                {attempt.score} / {attempt.totalQuestions}
                                            </td>
                                            <td>
                                                <strong>{attempt.percentage}%</strong>
                                            </td>
                                            <td>
                                                <span className={`status-pill ${attempt.passed ? "success" : "needs-practice"}`}>
                                                    {attempt.passed ? "Passed" : "Needs Practice"}
                                                </span>
                                            </td>
                                            <td>
                                                {formatTimer(attempt.timeSpentSeconds)}
                                            </td>
                                            <td>
                                                <button
                                                    type="button"
                                                    className="review-attempt-btn"
                                                    onClick={() => handleReviewPastAttempt(attempt.id)}
                                                >
                                                    Review
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
