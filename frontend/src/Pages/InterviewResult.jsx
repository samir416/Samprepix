import React, { useEffect, useState } from "react";
import {
    FiArrowLeft,
    FiAward,
    FiCheckCircle,
    FiChevronDown,
    FiChevronUp,
    FiClock,
    FiCode,
    FiMessageCircle,
    FiTarget,
    FiTrendingUp,
    FiXCircle
} from "react-icons/fi";
import { useNavigate, useSearchParams } from "react-router-dom";
import { getResult } from "../services/interviewService";
import "../styles/InterviewResult.css";

export default function InterviewResult() {

    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [expandedQuestion, setExpandedQuestion] = useState(null);

    const sessionId =
        searchParams.get("sessionId") ||
        localStorage.getItem("lastInterviewSessionId");

    useEffect(() => {

        const fetchResult = async () => {

            if (!sessionId) {

                setError("Interview report could not be found.");
                setLoading(false);
                return;

            }

            try {

                const response = await getResult(sessionId);

                setResult(response.data);
            } catch (err) {

                setError(
                    err?.response?.data?.message ||
                    "Unable to load interview report."
                );

            } finally {

                setLoading(false);

            }

        };

        fetchResult();

    }, [sessionId]);

    const toggleQuestion = (index) => {

        setExpandedQuestion(
            expandedQuestion === index
                ? null
                : index
        );

    };

    const getScoreClass = (score) => {

        if (score >= 80) {
            return "result-score-excellent";
        }

        if (score >= 60) {
            return "result-score-good";
        }

        if (score >= 40) {
            return "result-score-average";
        }

        return "result-score-low";

    };

    if (loading) {

        return (
            <div className="interview-result-page">
                <div className="result-loading-card">
                    <div className="result-loading-spinner"></div>
                    <h3>Preparing Your Report</h3>
                    <p>
                        Analyzing your interview performance...
                    </p>
                </div>
            </div>
        );

    }

    if (error || !result) {

        return (
            <div className="interview-result-page">
                <div className="result-error-card">
                    <FiXCircle size={46} />
                    <h3>Report Unavailable</h3>
                    <p>
                        {error || "Unable to load the interview report."}
                    </p>
                    <button
                        type="button"
                        onClick={() => navigate("/performance")}
                    >
                        Back to Performance
                    </button>
                </div>
            </div>
        );

    }

    return (
        <div className="interview-result-page">

            <div className="result-page-header">

                <button
                    type="button"
                    className="result-back-btn"
                    onClick={() => navigate("/performance")}
                >
                    <FiArrowLeft />
                    Back to Performance
                </button>

                <div className="result-header-content">

                    <div>
                        <span className="result-eyebrow">
                            AI INTERVIEW REPORT
                        </span>

                        <h1>
                            Interview Performance
                        </h1>

                        <p>
                            Your detailed interview evaluation and improvement insights.
                        </p>
                    </div>

                    <div className="result-status">
                        <FiCheckCircle />
                        {result.status || "COMPLETED"}
                    </div>

                </div>

            </div>

            <div className="result-profile-card">

                <div className="result-profile-main">

                    <div className="result-role-icon">
                        <FiTarget />
                    </div>

                    <div>
                        <span>Target Role</span>
                        <h2>
                            {result.targetRole || "Not specified"}
                        </h2>
                    </div>

                </div>

                <div className="result-profile-item">

                    <span>Experience Level</span>

                    <strong>
                        {result.experienceLevel || "Not specified"}
                    </strong>

                </div>

                <div className="result-profile-item">

                    <span>Difficulty</span>

                    <strong>
                        {result.difficulty || "Not specified"}
                    </strong>

                </div>

                <div className="result-profile-item">

                    <span>Questions</span>

                    <strong>
                        {result.questionsAnswered ?? 0}
                    </strong>

                </div>

            </div>

            <div className="result-skills-card">

                <div className="result-section-heading">
                    <div>
                        <span className="result-section-eyebrow">
                            INTERVIEW FOCUS
                        </span>
                        <h3>Technical Skills</h3>
                    </div>
                </div>

                <div className="result-skill-list">

                    {(result.skills || []).map((skill, index) => (
                        <span
                            className="result-skill-chip"
                            key={`${skill}-${index}`}
                        >
                            <FiCode />
                            {skill}
                        </span>
                    ))}

                </div>

            </div>

            <div className="result-score-grid">

                <div className="result-score-card result-score-main">

                    <div className="result-card-icon">
                        <FiAward />
                    </div>

                    <div>

                        <span>Overall Score</span>

                        <strong
                            className={getScoreClass(result.overallScore)}
                        >
                            {result.overallScore ?? 0}
                        </strong>

                        <small>
                            out of 100
                        </small>

                    </div>

                </div>

                <div className="result-score-card">

                    <div className="result-card-icon">
                        <FiTarget />
                    </div>

                    <span>Technical Accuracy</span>

                    <strong>
                        {result.technicalAccuracy ?? 0}%
                    </strong>

                </div>

                <div className="result-score-card">

                    <div className="result-card-icon">
                        <FiTrendingUp />
                    </div>

                    <span>Completeness</span>

                    <strong>
                        {result.completeness ?? 0}%
                    </strong>

                </div>

                <div className="result-score-card">

                    <div className="result-card-icon">
                        <FiMessageCircle />
                    </div>

                    <span>Communication</span>

                    <strong>
                        {result.communication ?? 0}%
                    </strong>

                </div>

            </div>

            <div className="result-focus-card">

                <div className="result-focus-icon">
                    <FiTrendingUp />
                </div>

                <div>

                    <span>Recommended Next Focus</span>

                    <h3>
                        {result.nextFocusSkill || "Keep strengthening your technical fundamentals."}
                    </h3>

                    <p>
                        Focus on this area during your next preparation session.
                    </p>

                </div>

            </div>

            <div className="result-answers-section">

                <div className="result-section-heading">

                    <div>
                        <span className="result-section-eyebrow">
                            QUESTION REVIEW
                        </span>

                        <h2>
                            Interview Answers
                        </h2>
                    </div>

                    <span className="result-answer-count">
                        {(result.answers || []).length} Questions
                    </span>

                </div>

                <div className="result-answer-list">

                    {(result.answers || []).map((answer, index) => {

                        const isExpanded =
                            expandedQuestion === index;

                        return (
                            <div
                                className={`result-answer-card ${isExpanded
                                        ? "result-answer-expanded"
                                        : ""
                                    }`}
                                key={answer.questionNumber || index}
                            >

                                <button
                                    type="button"
                                    className="result-answer-header"
                                    onClick={() =>
                                        toggleQuestion(index)
                                    }
                                >

                                    <div className="result-question-number">
                                        Q{answer.questionNumber}
                                    </div>

                                    <div className="result-question-summary">

                                        <span>
                                            {answer.difficulty || "Interview Question"}
                                        </span>

                                        <h3>
                                            {answer.question}
                                        </h3>

                                    </div>

                                    <div className="result-answer-score">

                                        <strong>
                                            {answer.overallScore ?? 0}
                                        </strong>

                                        {isExpanded
                                            ? <FiChevronUp />
                                            : <FiChevronDown />
                                        }

                                    </div>

                                </button>

                                {isExpanded && (

                                    <div className="result-answer-details">

                                        <div className="result-detail-block">

                                            <span>Your Answer</span>

                                            <p>
                                                {answer.answer || "No answer provided."}
                                            </p>

                                        </div>

                                        <div className="result-detail-metrics">

                                            <div>
                                                <span>Technical Accuracy</span>
                                                <strong>
                                                    {answer.technicalAccuracy ?? 0}%
                                                </strong>
                                            </div>

                                            <div>
                                                <span>Completeness</span>
                                                <strong>
                                                    {answer.completeness ?? 0}%
                                                </strong>
                                            </div>

                                            <div>
                                                <span>Communication</span>
                                                <strong>
                                                    {answer.communication ?? 0}%
                                                </strong>
                                            </div>

                                        </div>

                                        <div className="result-detail-block">

                                            <span>Performance</span>

                                            <strong>
                                                {answer.performance || "Not Evaluated"}
                                            </strong>

                                        </div>

                                        <div className="result-detail-block">

                                            <span>AI Feedback</span>

                                            <p>
                                                {answer.feedback || "No feedback available."}
                                            </p>

                                        </div>

                                        <div className="result-detail-block">

                                            <span>Ideal Answer</span>

                                            <p>
                                                {answer.idealAnswer || "Not available."}
                                            </p>

                                        </div>

                                        <div className="result-detail-columns">

                                            <div>

                                                <span>Strengths</span>

                                                {(answer.strengths || []).length > 0 ? (

                                                    <ul>

                                                        {answer.strengths.map(
                                                            (strength, strengthIndex) => (
                                                                <li key={strengthIndex}>
                                                                    <FiCheckCircle />
                                                                    {strength}
                                                                </li>
                                                            )
                                                        )}

                                                    </ul>

                                                ) : (
                                                    <p>No strengths recorded.</p>
                                                )}

                                            </div>

                                            <div>

                                                <span>Missing Concepts</span>

                                                {(answer.missingConcepts || []).length > 0 ? (

                                                    <ul>

                                                        {answer.missingConcepts.map(
                                                            (concept, conceptIndex) => (
                                                                <li key={conceptIndex}>
                                                                    <FiXCircle />
                                                                    {concept}
                                                                </li>
                                                            )
                                                        )}

                                                    </ul>

                                                ) : (
                                                    <p>No missing concepts recorded.</p>
                                                )}

                                            </div>

                                        </div>

                                    </div>

                                )}

                            </div>
                        );

                    })}

                </div>

            </div>

        </div>
    );
}