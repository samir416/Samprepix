import React, { useEffect, useMemo, useState } from "react";
import Logo from "../assets/Logo.png";
import {
    FiArrowLeft,
    FiCheckCircle,
    FiChevronDown,
    FiChevronUp,
    FiClock,
    FiCode,
    FiDownload,
    FiFileText,
    FiMessageCircle,
    FiTarget,
    FiTrendingUp,
    FiXCircle
} from "react-icons/fi";
import { useNavigate, useSearchParams } from "react-router-dom";
import { jsPDF } from "jspdf";
import { getResult } from "../services/interviewService";
import "../styles/InterviewResult.css";

const InterviewResult = () => {

    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [expandedQuestion, setExpandedQuestion] = useState(null);
    const [isExporting, setIsExporting] = useState(false);

    const sessionId =
        searchParams.get("sessionId") ||
        localStorage.getItem("lastInterviewSessionId");

    const generatedAt = useMemo(
        () => new Date(),
        []
    );

    useEffect(() => {

        const loadResult = async () => {

            if (!sessionId) {
                setError("Interview result could not be found.");
                setLoading(false);
                return;
            }

            try {

                const response = await getResult(sessionId);

                setResult(response.data);

            } catch (err) {

                setError(
                    err?.response?.data?.message ||
                    "Unable to load interview result."
                );

            } finally {

                setLoading(false);

            }

        };

        loadResult();

    }, [sessionId]);

    const answers = Array.isArray(result?.answers)
        ? result.answers
        : [];

    const skills = Array.isArray(result?.skills)
        ? result.skills
        : [];

    const overallScore = Number(
        result?.overallScore || 0
    );

    const technicalAccuracy = Number(
        result?.technicalAccuracy || 0
    );

    const completeness = Number(
        result?.completeness || 0
    );

    const communication = Number(
        result?.communication || 0
    );

    const targetRole =
        result?.targetRole ||
        result?.domain ||
        "Not specified";

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

    const getPerformanceLabel = (score) => {

        if (score >= 80) {
            return "Excellent Performance";
        }

        if (score >= 60) {
            return "Good Performance";
        }

        if (score >= 40) {
            return "Average Performance";
        }

        return "Needs Improvement";

    };

    const normalizeList = (value) => {

        if (Array.isArray(value)) {
            return value.filter(Boolean);
        }

        if (typeof value === "string") {

            return value
                .split(/\r?\n|•|;/)
                .map((item) => item.trim())
                .filter(Boolean);

        }

        return [];

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

    const toggleQuestion = (index) => {

        setExpandedQuestion(
            expandedQuestion === index
                ? null
                : index
        );

    };

    const addPdfText = (
        pdf,
        text,
        x,
        y,
        width,
        lineHeight = 5
    ) => {

        const safeText =
            text === null ||
            text === undefined ||
            text === ""
                ? "Not available."
                : String(text);

        const lines =
            pdf.splitTextToSize(
                safeText,
                width
            );

        pdf.text(
            lines,
            x,
            y
        );

        return y + (
            lines.length *
            lineHeight
        );

    };

  const drawPdfBrand = (
    pdf,
    pageWidth
) => {

    const headerX = 14;
    const headerY = 12;
    const headerWidth = pageWidth - 28;
    const headerHeight = 27;

    pdf.setFillColor(
        248,
        247,
        255
    );

    pdf.roundedRect(
        headerX,
        headerY,
        headerWidth,
        headerHeight,
        5,
        5,
        "F"
    );

    pdf.setDrawColor(
        221,
        222,
        244
    );

    pdf.setLineWidth(
        0.35
    );

    pdf.roundedRect(
        headerX,
        headerY,
        headerWidth,
        headerHeight,
        5,
        5,
        "S"
    );

    const logoImage =
        new Image();

    logoImage.src = Logo;

 pdf.addImage(
    logoImage,
    "PNG",
    12,
    13,
    35,
    25
);
    pdf.setTextColor(
        24,
        32,
        54
    );

    pdf.setFont(
        "helvetica",
        "bold"
    );

    pdf.setFontSize(
        14
    );

    pdf.text(
        "Samprepix",
        37,
        24
    );

    pdf.setFont(
        "helvetica",
        "normal"
    );

    pdf.setFontSize(
        7.5
    );

    pdf.setTextColor(
        105,
        115,
        136
    );

    pdf.text(
        "AI Interview & Placement Preparation Platform",
        37,
        29
    );

};  

    const drawPdfFooter = (
        pdf,
        pageWidth,
        pageHeight
    ) => {

        pdf.setDrawColor(
            224,
            228,
            237
        );

        pdf.setLineWidth(
            0.25
        );

        pdf.line(
            18,
            pageHeight - 13,
            pageWidth - 18,
            pageHeight - 13
        );

        pdf.setFont(
            "helvetica",
            "normal"
        );

        pdf.setFontSize(
            7
        );

        pdf.setTextColor(
            135,
            144,
            160
        );

        pdf.text(
            "Samprepix",
            18,
            pageHeight - 7
        );

        pdf.text(
            "Interview Questions & Answers",
            pageWidth - 18,
            pageHeight - 7,
            {
                align: "right"
            }
        );

    };

    const addNewPdfPage = (
        pdf,
        pageWidth,
        pageHeight
    ) => {

        pdf.addPage();

        drawPdfBrand(
            pdf,
            pageWidth
        );

        drawPdfFooter(
            pdf,
            pageWidth,
            pageHeight
        );

        return 49;

    };

    const handleExportPdf = () => {

        if (
            isExporting ||
            !result
        ) {
            return;
        }

        try {

            setIsExporting(true);

            const pdf = new jsPDF(
                "p",
                "mm",
                "a4"
            );

            const pageWidth =
                pdf.internal.pageSize.getWidth();

            const pageHeight =
                pdf.internal.pageSize.getHeight();

            const margin = 18;

            const contentWidth =
                pageWidth -
                margin * 2;

            const bottomLimit =
                pageHeight - 21;

            drawPdfBrand(
                pdf,
                pageWidth
            );

            drawPdfFooter(
                pdf,
                pageWidth,
                pageHeight
            );

            let y = 49;

            pdf.setFont(
                "helvetica",
                "bold"
            );

            pdf.setFontSize(
                18
            );

            pdf.setTextColor(
                25,
                35,
                55
            );

            pdf.text(
                "Interview Questions & Answers",
                margin,
                y
            );

            y += 9;

            pdf.setFont(
                "helvetica",
                "normal"
            );

            pdf.setFontSize(
                9
            );

            pdf.setTextColor(
                92,
                103,
                122
            );

            const details = [
                `Domain / Target Role: ${targetRole}`,
                `Experience Level: ${
                    result.experienceLevel ||
                    "Not specified"
                }`,
                `Difficulty: ${
                    result.difficulty ||
                    "Not specified"
                }`,
                `Questions Answered: ${
                    result.questionsAnswered ??
                    answers.length
                }`,
                `Overall Score: ${overallScore}/100`
            ];

            details.forEach(
                (detail) => {

                    y = addPdfText(
                        pdf,
                        detail,
                        margin,
                        y,
                        contentWidth,
                        5
                    );

                    y += 1;

                }
            );

            y += 6;

            answers.forEach(
                (answer, index) => {

                    const questionNumber =
                        answer.questionNumber ??
                        index + 1;

                    const question =
                        answer.question ||
                        "Question unavailable.";

                    const userAnswer =
                        answer.answer ||
                        "No answer provided.";

                    const rightAnswer =
                        answer.idealAnswer ||
                        answer.rightAnswer ||
                        answer.correctAnswer ||
                        answer.modelAnswer ||
                        "Right answer is not available.";

                    const score =
                        Number(
                            answer.overallScore || 0
                        );

                    const questionLines =
                        pdf.splitTextToSize(
                            question,
                            contentWidth - 10
                        );

                    const answerLines =
                        pdf.splitTextToSize(
                            userAnswer,
                            contentWidth - 10
                        );

                    const rightAnswerLines =
                        pdf.splitTextToSize(
                            rightAnswer,
                            contentWidth - 10
                        );

                    const estimatedHeight =
                        12 +
                        questionLines.length * 5 +
                        12 +
                        answerLines.length * 5 +
                        12 +
                        rightAnswerLines.length * 5 +
                        13;

                    if (
                        y + estimatedHeight >
                        bottomLimit
                    ) {

                        y = addNewPdfPage(
                            pdf,
                            pageWidth,
                            pageHeight
                        );

                    }

                    pdf.setFillColor(
                        244,
                        246,
                        251
                    );

                    pdf.roundedRect(
                        margin,
                        y - 5,
                        contentWidth,
                        8,
                        2,
                        2,
                        "F"
                    );

                    pdf.setFont(
                        "helvetica",
                        "bold"
                    );

                    pdf.setFontSize(
                        10
                    );

                    pdf.setTextColor(
                        55,
                        69,
                        94
                    );

                    pdf.text(
                        `Question ${questionNumber}`,
                        margin + 4,
                        y
                    );

                    y += 9;

                    pdf.setFont(
                        "helvetica",
                        "bold"
                    );

                    pdf.setFontSize(
                        8
                    );

                    pdf.setTextColor(
                        78,
                        91,
                        112
                    );

                    pdf.text(
                        "QUESTION",
                        margin,
                        y
                    );

                    y += 5;

                    pdf.setFont(
                        "helvetica",
                        "normal"
                    );

                    pdf.setFontSize(
                        9
                    );

                    pdf.setTextColor(
                        42,
                        52,
                        70
                    );

                    y = addPdfText(
                        pdf,
                        question,
                        margin,
                        y,
                        contentWidth - 4,
                        5
                    );

                    y += 5;

                    pdf.setFillColor(
                        238,
                        248,
                        243
                    );

                    const answerBoxStart =
                        y - 4;

                    const answerBoxHeight =
                        12 +
                        answerLines.length * 5;

                    pdf.roundedRect(
                        margin,
                        answerBoxStart,
                        contentWidth,
                        answerBoxHeight,
                        2.5,
                        2.5,
                        "F"
                    );

                    pdf.setTextColor(
                        39,
                        132,
                        89
                    );

                    pdf.setFont(
                        "helvetica",
                        "bold"
                    );

                    pdf.setFontSize(
                        8
                    );

                    pdf.text(
                        "YOUR ANSWER",
                        margin + 4,
                        y + 2
                    );

                    y += 7;

                    pdf.setTextColor(
                        57,
                        73,
                        88
                    );

                    pdf.setFont(
                        "helvetica",
                        "normal"
                    );

                    pdf.setFontSize(
                        9
                    );

                    y = addPdfText(
                        pdf,
                        userAnswer,
                        margin + 4,
                        y,
                        contentWidth - 8,
                        5
                    );

                    y += 6;

                    pdf.setFillColor(
                        239,
                        244,
                        255
                    );

                    const rightBoxStart =
                        y - 4;

                    const rightBoxHeight =
                        12 +
                        rightAnswerLines.length * 5;

                    pdf.roundedRect(
                        margin,
                        rightBoxStart,
                        contentWidth,
                        rightBoxHeight,
                        2.5,
                        2.5,
                        "F"
                    );

                    pdf.setTextColor(
                        70,
                        94,
                        190
                    );

                    pdf.setFont(
                        "helvetica",
                        "bold"
                    );

                    pdf.setFontSize(
                        8
                    );

                    pdf.text(
                        "RIGHT ANSWER",
                        margin + 4,
                        y + 2
                    );

                    y += 7;

                    pdf.setTextColor(
                        55,
                        68,
                        92
                    );

                    pdf.setFont(
                        "helvetica",
                        "normal"
                    );

                    pdf.setFontSize(
                        9
                    );

                    y = addPdfText(
                        pdf,
                        rightAnswer,
                        margin + 4,
                        y,
                        contentWidth - 8,
                        5
                    );

                    y += 6;

                    pdf.setFont(
                        "helvetica",
                        "bold"
                    );

                    pdf.setFontSize(
                        9
                    );

                    pdf.setTextColor(
                        43,
                        137,
                        91
                    );

                    pdf.text(
                        `Question Score: ${score}/100`,
                        margin,
                        y
                    );

                    y += 8;

                    pdf.setDrawColor(
                        218,
                        224,
                        233
                    );

                    pdf.setLineWidth(
                        0.25
                    );

                    pdf.line(
                        margin,
                        y,
                        pageWidth - margin,
                        y
                    );

                    y += 8;

                }
            );

            const fileRole =
                String(targetRole)
                    .replace(
                        /[^a-z0-9]+/gi,
                        "_"
                    )
                    .replace(
                        /^_+|_+$/g,
                        ""
                    ) ||
                "Interview";

            const fileDate =
                generatedAt
                    .toISOString()
                    .slice(
                        0,
                        10
                    );

            pdf.save(
                `Samprepix_${fileRole}_Interview_${fileDate}.pdf`
            );

        } catch (err) {

            console.error(
                "PDF generation failed:",
                err
            );

        } finally {

            setIsExporting(false);

        }

    };

    const handleContinueFeedback = () => {

        navigate(
            "/mock-interview",
            {
                replace: true,
                state: {
                    openFeedback: true,
                    fromResult: true,
                    sessionId
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
                        Preparing Your Result
                    </h3>

                    <p>
                        Evaluating your interview performance...
                    </p>

                </div>

            </div>

        );

    }

    if (
        error ||
        !result
    ) {

        return (

            <div className="interview-result-page">

                <div className="result-error-card">

                    <FiXCircle size={46} />

                    <h3>
                        Result Unavailable
                    </h3>

                    <p>
                        {
                            error ||
                            "Unable to load the interview result."
                        }
                    </p>

                    <button
                        type="button"
                        onClick={() =>
                            navigate(
                                "/performance"
                            )
                        }
                    >
                        Back to Performance
                    </button>

                </div>

            </div>

        );

    }

    return (

        <div className="interview-result-page">

            <header className="result-page-header">

                <button
                    type="button"
                    className="result-back-btn"
                    onClick={() =>
                        navigate(
                            "/mock-interview"
                        )
                    }
                >
                    <FiArrowLeft />
                    Back to Interview
                </button>

                <div className="result-header-content">

                    <div className="result-header-main">

                        <span className="result-eyebrow">
                            AI INTERVIEW COMPLETED
                        </span>

                        <h1>
                            Interview Result
                        </h1>

                        <p>
                            Your complete interview performance is summarized below.
                            Review every answer, score and evaluation in one place.
                        </p>

                        <div className="result-generated-meta">

                            <span>
                                <FiClock />
                                {formatDate(
                                    generatedAt
                                )}
                            </span>

                            <span>
                                {formatTime(
                                    generatedAt
                                )}
                            </span>

                        </div>

                    </div>

                    <div className="result-header-actions">

                        <button
                            type="button"
                            className="result-export-btn"
                            onClick={handleExportPdf}
                            disabled={isExporting}
                        >
                            <FiDownload />

                            {
                                isExporting
                                    ? "Creating PDF..."
                                    : "Export PDF"
                            }

                        </button>

                        <div className="result-status">
                            <FiCheckCircle />
                            COMPLETED
                        </div>

                    </div>

                </div>

            </header>

            <section className="result-overview">

                <div
                    className="result-overall-card"
                    style={{
                        "--score":
                            Math.max(
                                0,
                                Math.min(
                                    100,
                                    overallScore
                                )
                            )
                    }}
                >

                    <div className="result-overall-ring">

                        <div>

                            <strong
                                className={
                                    getScoreClass(
                                        overallScore
                                    )
                                }
                            >
                                {overallScore}
                            </strong>

                            <span>
                                / 100
                            </span>

                        </div>

                    </div>

                    <div className="result-overall-content">

                        <span>
                            OVERALL PERFORMANCE
                        </span>

                        <h2>
                            {getPerformanceLabel(
                                overallScore
                            )}
                        </h2>

                        <p>
                            Your performance is calculated from technical accuracy,
                            completeness and communication across the interview.
                        </p>

                        <div className="result-completed-badge">
                            <FiCheckCircle />
                            Interview Completed
                        </div>

                    </div>

                </div>

                <div className="result-quick-stats">

                    <div className="result-quick-stat">

                        <FiTarget />

                        <div>

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

                    </div>

                    <div className="result-quick-stat">

                        <FiTrendingUp />

                        <div>

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

                    </div>

                    <div className="result-quick-stat">

                        <FiMessageCircle />

                        <div>

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

                    <div className="result-quick-stat">

                        <FiFileText />

                        <div>

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

                    </div>

                </div>

            </section>

            <section className="result-profile-card">

                <div className="result-profile-main">

                    <div className="result-role-icon">
                        <FiTarget />
                    </div>

                    <div>

                        <span>
                            DOMAIN / TARGET ROLE
                        </span>

                        <h2>
                            {targetRole}
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

            </section>

            <section className="result-skills-card">

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
                            ? skills.map(
                                (
                                    skill,
                                    index
                                ) => (

                                    <span
                                        className="result-skill-chip"
                                        key={
                                            `${skill}-${index}`
                                        }
                                    >
                                        <FiCode />
                                        {skill}
                                    </span>

                                )
                            )
                            : (
                                <span className="result-empty-text">
                                    No skills recorded.
                                </span>
                            )
                    }

                </div>

            </section>

            <section className="result-focus-card">

                <div className="result-focus-icon">
                    <FiTrendingUp />
                </div>

                <div>

                    <span>
                        RECOMMENDED NEXT FOCUS
                    </span>

                    <h3>
                        {
                            result.nextFocusSkill ||
                            "Continue strengthening your technical fundamentals."
                        }
                    </h3>

                    <p>
                        Use this recommendation as your next preparation target.
                    </p>

                </div>

            </section>

            <section className="result-answers-section">

                <div className="result-section-heading">

                    <div>

                        <span className="result-section-eyebrow">
                            DETAILED EVALUATION
                        </span>

                        <h2>
                            Questions & Answers
                        </h2>

                        <p>
                            Review every question, your response and the evaluation behind your score.
                        </p>

                    </div>

                    <span className="result-answer-count">
                        {answers.length} Questions
                    </span>

                </div>

                <div className="result-answer-list">

                    {
                        answers.length > 0
                            ? answers.map(
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

                                        <article
                                            className={
                                                `result-answer-card ${
                                                    isExpanded
                                                        ? "result-answer-expanded"
                                                        : ""
                                                }`
                                            }
                                            key={
                                                answer.id ||
                                                answer.questionNumber ||
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
                                                            "INTERVIEW QUESTION"
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
                                                                Number(
                                                                    answer.overallScore ||
                                                                    0
                                                                )
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
                                                            ? <FiChevronUp />
                                                            : <FiChevronDown />
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
                                                                Right Answer
                                                            </span>

                                                            <p>
                                                                {
                                                                    answer.idealAnswer ||
                                                                    answer.rightAnswer ||
                                                                    answer.correctAnswer ||
                                                                    answer.modelAnswer ||
                                                                    "Right answer is not available."
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
                                                                            Number(
                                                                                answer.technicalAccuracy ||
                                                                                0
                                                                            )
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
                                                                            Number(
                                                                                answer.completeness ||
                                                                                0
                                                                            )
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
                                                                            Number(
                                                                                answer.communication ||
                                                                                0
                                                                            )
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
                                                                    Overall
                                                                </span>

                                                                <strong
                                                                    className={
                                                                        getScoreClass(
                                                                            Number(
                                                                                answer.overallScore ||
                                                                                0
                                                                            )
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
                                                                AI Feedback
                                                            </span>

                                                            <p>
                                                                {
                                                                    answer.feedback ||
                                                                    "No detailed feedback available."
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

                                        </article>

                                    );

                                }
                            )
                            : (

                                <div className="result-empty-card">

                                    <FiFileText />

                                    <h3>
                                        No Answer Details Available
                                    </h3>

                                    <p>
                                        Detailed answer evaluation is not available for this interview.
                                    </p>

                                </div>

                            )
                    }

                </div>

            </section>

            <section className="result-report-footer">

                <div>

                    <span>
                        REPORT GENERATED
                    </span>

                    <strong>
                        {formatDate(
                            generatedAt
                        )}{" "}
                        at{" "}
                        {formatTime(
                            generatedAt
                        )}
                    </strong>

                </div>

                <button
                    type="button"
                    className="result-export-btn"
                    onClick={handleExportPdf}
                    disabled={isExporting}
                >
                    <FiDownload />

                    {
                        isExporting
                            ? "Creating PDF..."
                            : "Export Questions PDF"
                    }

                </button>

            </section>

            <section className="result-finish-card">

                <div>

                    <FiCheckCircle />

                    <div>

                        <span>
                            INTERVIEW REPORT COMPLETE
                        </span>

                        <h3>
                            Ready to continue your placement preparation?
                        </h3>

                        <p>
                            Your result is saved. Continue to feedback and move to your next preparation activity.
                        </p>

                    </div>

                </div>

                <button
                    type="button"
                    onClick={
                        handleContinueFeedback
                    }
                >
                    Continue to Feedback
                    <FiArrowLeft className="result-arrow-right" />
                </button>

            </section>

        </div>

    );

};

export default InterviewResult;