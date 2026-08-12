import React, { useEffect, useMemo, useState } from "react";
import {
    FiArrowLeft,
    FiAward,
    FiCheckCircle,
    FiChevronDown,
    FiChevronUp,
    FiClock,
    FiCode,
    FiDownload,
    FiMessageCircle,
    FiTarget,
    FiTrendingUp,
    FiXCircle
} from "react-icons/fi";
import { useNavigate, useSearchParams } from "react-router-dom";
import { getResult } from "../services/interviewService";
import "../styles/InterviewResult.css";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";

export default function InterviewResult() {

    const navigate = useNavigate();

    const [searchParams] = useSearchParams();

    const [result, setResult] = useState(null);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");

    const [isExporting, setIsExporting] = useState(false);

    const [expandedQuestion, setExpandedQuestion] = useState(null);

    const sessionId =
        searchParams.get("sessionId") ||
        localStorage.getItem("lastInterviewSessionId");

    const reportGeneratedAt = useMemo(() => {
        return result?.completedAt
            ? new Date(result.completedAt)
            : new Date();
    }, [result?.completedAt]);

    useEffect(() => {

        const fetchResult = async () => {

            if (!sessionId) {

                setError(
                    "Interview report could not be found."
                );

                setLoading(false);

                return;
            }

            try {

                const response =
                    await getResult(sessionId);

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

    const formatDate = (date) => {

        return date.toLocaleDateString(
            "en-IN",
            {
                day: "2-digit",
                month: "short",
                year: "numeric"
            }
        );

    };

    const formatTime = (date) => {

        return date.toLocaleTimeString(
            "en-IN",
            {
                hour: "2-digit",
                minute: "2-digit",
                hour12: true
            }
        );

    };

    const normalizeList = (value) => {

        if (Array.isArray(value)) {

            return value.filter(Boolean);

        }

        if (typeof value === "string") {

            return value
                .split(/\r?\n|•|;/)
                .map(
                    (item) => item.trim()
                )
                .filter(Boolean);

        }

        return [];

    };

    const handleExport = async () => {

        if (isExporting) {
            return;
        }

        const reportElement =
            document.querySelector(".interview-result-page");

        if (!reportElement) {
            return;
        }

        try {

            setIsExporting(true);

            const canvas = await html2canvas(
                reportElement,
                {
                    scale: 2,
                    useCORS: true,
                    backgroundColor:
                        getComputedStyle(reportElement).backgroundColor,
                    logging: false,
                    windowWidth: reportElement.scrollWidth,
                    windowHeight: reportElement.scrollHeight
                }
            );

            const imageData =
                canvas.toDataURL("image/png");

            const pdf = new jsPDF(
                "p",
                "mm",
                "a4"
            );

            const pageWidth =
                pdf.internal.pageSize.getWidth();

            const pageHeight =
                pdf.internal.pageSize.getHeight();

            const imageWidth = pageWidth;

            const imageHeight =
                (canvas.height * imageWidth) /
                canvas.width;

            let heightLeft = imageHeight;

            let position = 0;

            pdf.addImage(
                imageData,
                "PNG",
                0,
                position,
                imageWidth,
                imageHeight,
                undefined,
                "FAST"
            );

            heightLeft -= pageHeight;

            while (heightLeft > 0) {

                position =
                    heightLeft - imageHeight;

                pdf.addPage();

                pdf.addImage(
                    imageData,
                    "PNG",
                    0,
                    position,
                    imageWidth,
                    imageHeight,
                    undefined,
                    "FAST"
                );

                heightLeft -= pageHeight;

            }

            const role =
                result?.targetRole
                    ?.replace(/[^a-z0-9]+/gi, "_")
                    .replace(/^_+|_+$/g, "")
                || "Interview";

            const date =
                reportGeneratedAt
                    .toISOString()
                    .slice(0, 10);

            pdf.save(
                `${role}_Interview_Report_${date}.pdf`
            );

        } catch (error) {

            console.error(
                "PDF export failed:",
                error
            );

        } finally {

            setIsExporting(false);

        }

    };

    const handleCloseReport = () => {

        if (!sessionId) {

            navigate("/performance");

            return;

        }

        navigate(
            "/mock-interview",
            {
                replace: true,
                state: {
                    openFeedback: true,
                    sessionId: sessionId
                }
            }
        );

    };

    if (loading) {

        return (

            <div className="interview-result-page">

                <div className="result-loading-card">

                    <div className="result-loading-spinner"></div>

                    <h3>
                        Preparing Your Report
                    </h3>

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

                    <h3>
                        Report Unavailable
                    </h3>

                    <p>
                        {
                            error ||
                            "Unable to load the interview report."
                        }
                    </p>

                    <button
                        type="button"
                        onClick={() =>
                            navigate("/performance")
                        }
                    >
                        Back to Performance
                    </button>

                </div>

            </div>

        );

    }

    const answers = Array.isArray(
        result.answers
    )
        ? result.answers
        : [];

    const skills = Array.isArray(
        result.skills
    )
        ? result.skills
        : [];

    const overallScore =
        result.overallScore ?? 0;

    const technicalAccuracy =
        result.technicalAccuracy ?? 0;

    const completeness =
        result.completeness ?? 0;

    const communication =
        result.communication ?? 0;

    return (

        <div className="interview-result-page">

            <div className="result-page-header">

                <button
                    type="button"
                    className="result-back-btn"
                    onClick={handleCloseReport}
                >
                    <FiArrowLeft />
                    Close Report
                </button>

                <div className="result-header-content">

                    <div className="result-header-main">

                        <span className="result-eyebrow">
                            AI INTERVIEW REPORT
                        </span>

                        <h1>
                            Interview Performance
                        </h1>

                        <p>
                            Your complete interview evaluation, answers and improvement insights.
                        </p>

                        <div className="result-generated-meta">

                            <span>

                                <FiClock />

                                {formatDate(
                                    reportGeneratedAt
                                )}

                            </span>

                            <span>

                                {formatTime(
                                    reportGeneratedAt
                                )}

                            </span>

                        </div>

                    </div>

                    <div className="result-header-actions">

                        <button
                            type="button"
                            className="result-export-btn"
                            onClick={handleExport}
                            disabled={isExporting}
                        >

                            <FiDownload />

                            {isExporting
                                ? "Generating PDF..."
                                : "Export Report"}

                        </button>

                        <div className="result-status">

                            <FiCheckCircle />

                            {
                                result.status ||
                                "COMPLETED"
                            }

                        </div>

                    </div>

                </div>

            </div>

            <div className="result-profile-card">

                <div className="result-profile-main">

                    <div className="result-role-icon">

                        <FiTarget />

                    </div>

                    <div>

                        <span>
                            Target Role
                        </span>

                        <h2>
                            {
                                result.targetRole ||
                                "Not specified"
                            }
                        </h2>

                    </div>

                </div>

                <div className="result-profile-item">

                    <span>
                        Experience Level
                    </span>

                    <strong>
                        {
                            result.experienceLevel ||
                            "Not specified"
                        }
                    </strong>

                </div>

                <div className="result-profile-item">

                    <span>
                        Difficulty
                    </span>

                    <strong>
                        {
                            result.difficulty ||
                            "Not specified"
                        }
                    </strong>

                </div>

                <div className="result-profile-item">

                    <span>
                        Questions
                    </span>

                    <strong>
                        {
                            result.questionsAnswered ??
                            answers.length
                        }
                    </strong>

                </div>

            </div>

            <div className="result-skills-card">

                <div className="result-section-heading">

                    <div>

                        <span className="result-section-eyebrow">
                            INTERVIEW FOCUS
                        </span>

                        <h3>
                            Technical Skills
                        </h3>

                    </div>

                    <span className="result-answer-count">

                        {skills.length} Skills

                    </span>

                </div>

                <div className="result-skill-list">

                    {
                        skills.length > 0
                            ? (
                                skills.map(
                                    (
                                        skill,
                                        index
                                    ) => (

                                        <span
                                            className="result-skill-chip"
                                            key={`${skill}-${index}`}
                                        >

                                            <FiCode />

                                            {skill}

                                        </span>

                                    )
                                )
                            )
                            : (

                                <span className="result-empty-text">

                                    No skills recorded.

                                </span>

                            )
                    }

                </div>

            </div>

            <div className="result-score-grid">

                <div className="result-score-card result-score-main">

                    <div className="result-card-icon">

                        <FiAward />

                    </div>

                    <div>

                        <span>
                            Overall Score
                        </span>

                        <strong
                            className={
                                getScoreClass(
                                    overallScore
                                )
                            }
                        >
                            {overallScore}
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

                    <span>
                        Technical Accuracy
                    </span>

                    <strong
                        className={
                            getScoreClass(
                                technicalAccuracy
                            )
                        }
                    >
                        {technicalAccuracy}%
                    </strong>

                </div>

                <div className="result-score-card">

                    <div className="result-card-icon">

                        <FiTrendingUp />

                    </div>

                    <span>
                        Completeness
                    </span>

                    <strong
                        className={
                            getScoreClass(
                                completeness
                            )
                        }
                    >
                        {completeness}%
                    </strong>

                </div>

                <div className="result-score-card">

                    <div className="result-card-icon">

                        <FiMessageCircle />

                    </div>

                    <span>
                        Communication
                    </span>

                    <strong
                        className={
                            getScoreClass(
                                communication
                            )
                        }
                    >
                        {communication}%
                    </strong>

                </div>

            </div>

            <div className="result-focus-card">

                <div className="result-focus-icon">

                    <FiTrendingUp />

                </div>

                <div>

                    <span>
                        Recommended Next Focus
                    </span>

                    <h3>
                        {
                            result.nextFocusSkill ||
                            "Keep strengthening your technical fundamentals."
                        }
                    </h3>

                    <p>
                        Focus on this area during your next preparation session.
                    </p>

                </div>

            </div>

            <div className="result-summary-card">

                <div className="result-section-heading">

                    <div>

                        <span className="result-section-eyebrow">
                            PERFORMANCE SUMMARY
                        </span>

                        <h2>
                            Interview Overview
                        </h2>

                    </div>

                </div>

                <div className="result-summary-grid">

                    <div className="result-summary-item">

                        <span>
                            Questions Answered
                        </span>

                        <strong>
                            {
                                result.questionsAnswered ??
                                answers.length
                            }
                        </strong>

                    </div>

                    <div className="result-summary-item">

                        <span>
                            Overall Performance
                        </span>

                        <strong
                            className={
                                getScoreClass(
                                    overallScore
                                )
                            }
                        >
                            {overallScore}/100
                        </strong>

                    </div>

                    <div className="result-summary-item">

                        <span>
                            Interview Difficulty
                        </span>

                        <strong>
                            {
                                result.difficulty ||
                                "Not specified"
                            }
                        </strong>

                    </div>

                    <div className="result-summary-item">

                        <span>
                            Report Status
                        </span>

                        <strong>
                            {
                                result.status ||
                                "COMPLETED"
                            }
                        </strong>

                    </div>

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

                        <p>
                            Review every answer, AI evaluation and improvement area from this interview.
                        </p>

                    </div>

                    <span className="result-answer-count">

                        {answers.length} Questions

                    </span>

                </div>

                <div className="result-answer-list">

                    {
                        answers.length > 0
                            ? (
                                answers.map(
                                    (
                                        answer,
                                        index
                                    ) => {

                                        const isExpanded =
                                            expandedQuestion === index;

                                        const strengths =
                                            normalizeList(
                                                answer.strengths
                                            );

                                        const missingConcepts =
                                            normalizeList(
                                                answer.missingConcepts
                                            );

                                        return (

                                            <div
                                                className={`result-answer-card ${isExpanded
                                                    ? "result-answer-expanded"
                                                    : ""
                                                    }`}
                                                key={
                                                    answer.questionNumber ||
                                                    answer.id ||
                                                    index
                                                }
                                            >

                                                <button
                                                    type="button"
                                                    className="result-answer-header"
                                                    onClick={() =>
                                                        toggleQuestion(
                                                            index
                                                        )
                                                    }
                                                >

                                                    <div className="result-question-number">

                                                        Q{
                                                            answer.questionNumber ??
                                                            index + 1
                                                        }

                                                    </div>

                                                    <div className="result-question-summary">

                                                        <span>

                                                            {
                                                                answer.difficulty ||
                                                                "Interview Question"
                                                            }

                                                        </span>

                                                        <h3>

                                                            {
                                                                answer.question ||
                                                                "Question unavailable."
                                                            }

                                                        </h3>

                                                    </div>

                                                    <div className="result-answer-score">

                                                        <strong
                                                            className={
                                                                getScoreClass(
                                                                    answer.overallScore ??
                                                                    0
                                                                )
                                                            }
                                                        >
                                                            {
                                                                answer.overallScore ??
                                                                0
                                                            }
                                                        </strong>

                                                        {
                                                            isExpanded
                                                                ? (
                                                                    <FiChevronUp />
                                                                )
                                                                : (
                                                                    <FiChevronDown />
                                                                )
                                                        }

                                                    </div>

                                                </button>

                                                {
                                                    isExpanded && (

                                                        <div className="result-answer-details">

                                                            <div className="result-detail-block">

                                                                <span>
                                                                    Your Answer
                                                                </span>

                                                                <p>
                                                                    {
                                                                        answer.answer ||
                                                                        "No answer provided."
                                                                    }
                                                                </p>

                                                            </div>

                                                            <div className="result-detail-block result-correct-answer">

                                                                <span>
                                                                    Ideal Answer
                                                                </span>

                                                                <p>
                                                                    {
                                                                        answer.idealAnswer ||
                                                                        "Not available."
                                                                    }
                                                                </p>

                                                            </div>

                                                            <div className="result-detail-metrics">

                                                                <div>

                                                                    <span>
                                                                        Technical Accuracy
                                                                    </span>

                                                                    <strong
                                                                        className={
                                                                            getScoreClass(
                                                                                answer.technicalAccuracy ??
                                                                                0
                                                                            )
                                                                        }
                                                                    >
                                                                        {
                                                                            answer.technicalAccuracy ??
                                                                            0
                                                                        }%
                                                                    </strong>

                                                                </div>

                                                                <div>

                                                                    <span>
                                                                        Completeness
                                                                    </span>

                                                                    <strong
                                                                        className={
                                                                            getScoreClass(
                                                                                answer.completeness ??
                                                                                0
                                                                            )
                                                                        }
                                                                    >
                                                                        {
                                                                            answer.completeness ??
                                                                            0
                                                                        }%
                                                                    </strong>

                                                                </div>

                                                                <div>

                                                                    <span>
                                                                        Communication
                                                                    </span>

                                                                    <strong
                                                                        className={
                                                                            getScoreClass(
                                                                                answer.communication ??
                                                                                0
                                                                            )
                                                                        }
                                                                    >
                                                                        {
                                                                            answer.communication ??
                                                                            0
                                                                        }%
                                                                    </strong>

                                                                </div>

                                                                <div>

                                                                    <span>
                                                                        Overall Score
                                                                    </span>

                                                                    <strong
                                                                        className={
                                                                            getScoreClass(
                                                                                answer.overallScore ??
                                                                                0
                                                                            )
                                                                        }
                                                                    >
                                                                        {
                                                                            answer.overallScore ??
                                                                            0
                                                                        }%
                                                                    </strong>

                                                                </div>

                                                            </div>

                                                            <div className="result-detail-block">

                                                                <span>
                                                                    Performance
                                                                </span>

                                                                <strong>
                                                                    {
                                                                        answer.performance ||
                                                                        "Not Evaluated"
                                                                    }
                                                                </strong>

                                                            </div>

                                                            <div className="result-detail-block">

                                                                <span>
                                                                    AI Feedback
                                                                </span>

                                                                <p>
                                                                    {
                                                                        answer.feedback ||
                                                                        "No feedback available."
                                                                    }
                                                                </p>

                                                            </div>

                                                            <div className="result-detail-columns">

                                                                <div>

                                                                    <span>
                                                                        Strengths
                                                                    </span>

                                                                    {
                                                                        strengths.length > 0
                                                                            ? (
                                                                                <ul>

                                                                                    {
                                                                                        strengths.map(
                                                                                            (
                                                                                                strength,
                                                                                                strengthIndex
                                                                                            ) => (

                                                                                                <li
                                                                                                    key={
                                                                                                        strengthIndex
                                                                                                    }
                                                                                                >

                                                                                                    <FiCheckCircle />

                                                                                                    {
                                                                                                        strength
                                                                                                    }

                                                                                                </li>

                                                                                            )
                                                                                        )
                                                                                    }

                                                                                </ul>
                                                                            )
                                                                            : (

                                                                                <p>
                                                                                    No strengths recorded.
                                                                                </p>

                                                                            )
                                                                    }

                                                                </div>

                                                                <div>

                                                                    <span>
                                                                        Missing Concepts
                                                                    </span>

                                                                    {
                                                                        missingConcepts.length > 0
                                                                            ? (
                                                                                <ul>

                                                                                    {
                                                                                        missingConcepts.map(
                                                                                            (
                                                                                                concept,
                                                                                                conceptIndex
                                                                                            ) => (

                                                                                                <li
                                                                                                    key={
                                                                                                        conceptIndex
                                                                                                    }
                                                                                                >

                                                                                                    <FiXCircle />

                                                                                                    {
                                                                                                        concept
                                                                                                    }

                                                                                                </li>

                                                                                            )
                                                                                        )
                                                                                    }

                                                                                </ul>
                                                                            )
                                                                            : (

                                                                                <p>
                                                                                    No missing concepts recorded.
                                                                                </p>

                                                                            )
                                                                    }

                                                                </div>

                                                            </div>

                                                            <div className="result-next-focus-inline">

                                                                <span>
                                                                    Next Focus Skill
                                                                </span>

                                                                <strong>
                                                                    {
                                                                        answer.nextFocusSkill ||
                                                                        result.nextFocusSkill ||
                                                                        "Continue strengthening your fundamentals."
                                                                    }
                                                                </strong>

                                                            </div>

                                                        </div>

                                                    )
                                                }

                                            </div>

                                        );

                                    }
                                )
                            )
                            : (

                                <div className="result-empty-card">

                                    <FiClock />

                                    <h3>
                                        No Answer Details Available
                                    </h3>

                                    <p>
                                        The interview was completed, but detailed answer evaluation is not available.
                                    </p>

                                </div>

                            )
                    }

                </div>

            </div>

            <div className="result-report-footer">

                <div>

                    <span>
                        REPORT GENERATED
                    </span>

                    <strong>
                        {
                            formatDate(
                                reportGeneratedAt
                            )
                        }{" "}
                        at{" "}
                        {
                            formatTime(
                                reportGeneratedAt
                            )
                        }
                    </strong>

                </div>

                <button
                    type="button"
                    className="result-export-btn"
                    onClick={handleExport}
                    disabled={isExporting}
                >

                    <FiDownload />

                    {isExporting
                        ? "Generating PDF..."
                        : "Export Report"}


                </button>

            </div>

        </div>

    );

}