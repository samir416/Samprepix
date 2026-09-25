import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { getCurrentUser } from "../services/authService";

import {
    getResumeHistory,
    getLatestResumeAnalysis
} from "../services/resumeService";

import {
    getCodingDashboardStats
} from "../services/codingService";

import {
    getCompletedInterviewCount
} from "../services/interviewService";

import {
    getAptitudeAttempts
} from "../services/aptitudeService";

import {
    FiTrendingUp,
    FiTarget,
    FiClock,
    FiAward,
    FiMic,
    FiCode,
    FiFileText,
    FiCheckCircle,
    FiArrowRight
} from "react-icons/fi";

import {
    ResponsiveContainer,
    AreaChart,
    Area,
    Tooltip,
    XAxis,
    YAxis,
    CartesianGrid
} from "recharts";

import "../styles/dashboard.css";

export default function Dashboard() {

    const navigate = useNavigate();

    const [resumeHistory, setResumeHistory] = useState([]);

    const [latestResume, setLatestResume] = useState(null);

    const [user, setUser] = useState(() => {
        try {
            return JSON.parse(localStorage.getItem("user")) || null;
        } catch {
            return null;
        }
    });

    const [codingStats, setCodingStats] = useState(null);

    const [mockInterviewCount, setMockInterviewCount] = useState(0);

    const [aptitudeAttempts, setAptitudeAttempts] = useState([]);

    const [loadingStats, setLoadingStats] = useState(true);
    useEffect(() => {

        const verifyUser = async () => {

            try {

                const data = await getCurrentUser();

                if (data && data.profileCompleted === false) {
                    localStorage.setItem("user", JSON.stringify(data));
                    localStorage.removeItem("onboardingCompleted");
                    navigate("/onboarding", { replace: true });
                    return;
                }

                setUser(data);

                localStorage.setItem(
                    "user",
                    JSON.stringify(data)
                );

            } catch (err) {

                if (err?.response?.status === 401) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("user");
                    localStorage.removeItem("onboardingCompleted");
                    navigate("/login", { replace: true });
                }
            }

        };
        verifyUser();

        const loadHistory =
            async () => {

                try {

                    const data =
                        await getResumeHistory();

                    setResumeHistory(
                        data
                    );

                } catch (error) {

                    console.error(error);
                }
            };

        loadHistory();

        const loadLatestResume = async () => {

            try {

                const data =
                    await getLatestResumeAnalysis();

                setLatestResume(data);

            } catch (error) {

                console.error(error);
            }
        };

        loadLatestResume();

        const loadStats = async () => {
            try {
                const response = await getCodingDashboardStats();
                setCodingStats(response.data);
            } catch (error) {
                console.error("Failed to load coding dashboard stats", error);
            } finally {
                setLoadingStats(false);
            }
        };

        loadStats();

        const loadInterviews = async () => {
            try {
                const res = await getCompletedInterviewCount();
                setMockInterviewCount(typeof res.data === "number" ? res.data : 0);
            } catch (error) {
                console.error("Failed to load interview count", error);
            }
        };

        loadInterviews();

        const loadAptitude = async () => {
            try {
                const attempts = await getAptitudeAttempts();
                setAptitudeAttempts(Array.isArray(attempts) ? attempts : []);
            } catch (error) {
                console.error("Failed to load aptitude attempts", error);
            }
        };

        loadAptitude();

    }, [navigate]);

    const bestAptitudeScore = aptitudeAttempts.length > 0
        ? Math.max(...aptitudeAttempts.map((a) => a.percentage || 0))
        : null;

    const combinedActivities = [
        ...(codingStats?.recentSubmissions || []).map((s) => ({
            id: `coding-${s.problemId}-${s.attemptedAt}`,
            title: s.problemTitle || "Coding Problem",
            subtitle: `${s.language ? s.language.toUpperCase() : "Code"} · ${s.difficulty || "Practice"}`,
            badge: s.completed ? "✓ Solved" : "Attempted",
            badgeClass: s.completed ? "solved" : "attempted",
            dotColor: s.completed ? "#10b981" : "#6366f1",
            timestamp: s.attemptedAt ? new Date(s.attemptedAt).getTime() : 0,
            onClick: () => navigate("/coding-arena")
        })),
        ...(resumeHistory || []).map((r) => ({
            id: `resume-${r.id}`,
            title: "Resume Analysis",
            subtitle: r.analyzedAt ? new Date(r.analyzedAt).toLocaleDateString() : "Recent",
            badge: `ATS ${r.score}`,
            badgeClass: "resume",
            dotColor: "#06b6d4",
            timestamp: r.analyzedAt ? new Date(r.analyzedAt).getTime() : 0,
            onClick: () => navigate("/resume-analyzer")
        })),
        ...(aptitudeAttempts || []).map((a) => ({
            id: `aptitude-${a.id}`,
            title: `${a.trackTitle || "Aptitude"} Test`,
            subtitle: `${a.correctCount}/${a.totalQuestions} correct (${a.percentage}%)`,
            badge: `${a.percentage}%`,
            badgeClass: "aptitude",
            dotColor: "#f59e0b",
            timestamp: a.completedAt ? new Date(a.completedAt).getTime() : 0,
            onClick: () => navigate("/aptitude")
        }))
    ]
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 4);

    return (
        <motion.div
            className="dashboard-home"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.25 }}
        >
            {/* HEADER */}
            <div className="dashboard-header">

                                                                <h1>
                                                                    Welcome back, {user?.username || user?.name || "User"} 👋
                                                                </h1>

                                                                <p>
                                                                    {user?.email || "Track your placement journey and improve every day."}
                                                                </p>

                                                            </div>

                                                            {/* STATS */}

                                                            <div className="stats-grid">

                                                                {/* CARD 1: PROBLEMS SOLVED */}

                                                                <div
                                                                    className="stat-card interactive"
                                                                    onClick={() => navigate("/coding-arena")}
                                                                    title="Open Coding Arena"
                                                                >

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Problems Solved
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiCode />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            {codingStats ? codingStats.problemsSolved : 0}
                                                                        </h2>

                                                                        <span>
                                                                            {codingStats?.acceptanceRate != null ? `${codingStats.acceptanceRate}% Rate` : "0% Rate"}
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 2: RESUME ATS */}

                                                                <div
                                                                    className="stat-card interactive"
                                                                    onClick={() => navigate("/resume-analyzer")}
                                                                    title="Open Resume Analyzer"
                                                                >

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Resume ATS
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiTarget />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            {
                                                                                latestResume
                                                                                    ? latestResume.score
                                                                                    : "--"
                                                                            }
                                                                        </h2>

                                                                        <span>
                                                                            {latestResume ? `${resumeHistory.length} analyses` : "Upload resume"}
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 3: REAL DAY STREAK */}

                                                                <div className="stat-card">

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Day Streak
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiClock />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            {codingStats ? `${codingStats.currentStreak} Day${codingStats.currentStreak === 1 ? "" : "s"}` : "0 Days"}
                                                                        </h2>

                                                                        <span>
                                                                            {codingStats?.currentStreak > 0 ? "Keep it up!" : "Start streak"}
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 4: MOCK INTERVIEWS */}

                                                                <div
                                                                    className="stat-card interactive"
                                                                    onClick={() => navigate("/mock-interview")}
                                                                    title="Start Mock Interview"
                                                                >

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Mock Interviews
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiMic />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            {mockInterviewCount}
                                                                        </h2>

                                                                        <span>
                                                                            {mockInterviewCount > 0 ? "Completed sessions" : "Practice now"}
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 5: APTITUDE TESTS */}

                                                                <div
                                                                    className="stat-card interactive"
                                                                    onClick={() => navigate("/aptitude")}
                                                                    title="Open Aptitude Hub"
                                                                >

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Aptitude Tests
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiAward />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            {aptitudeAttempts.length}
                                                                        </h2>

                                                                        <span>
                                                                            {aptitudeAttempts.length > 0
                                                                                ? `Best: ${bestAptitudeScore}% score`
                                                                                : "22,000+ Questions"}
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 6: REAL TOTAL SUBMISSIONS */}

                                                                <div
                                                                    className="stat-card interactive"
                                                                    onClick={() => navigate("/coding-arena")}
                                                                    title="Open Coding Arena Submissions"
                                                                >

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Total Submissions
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiCheckCircle />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            {codingStats ? codingStats.totalSubmissions || codingStats.problemsAttempted || 0 : 0}
                                                                        </h2>

                                                                        <span>
                                                                            {codingStats?.successfulSubmissions != null
                                                                                ? `${codingStats.successfulSubmissions} passed`
                                                                                : "Active practice"}
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                            </div>

                                                            {/* TOP GRAPH SECTION: CODING PROGRESS + DIFFICULTY BREAKDOWN */}

                                                            <div className="dashboard-bottom">

                                                                {/* GRAPH: REAL CUMULATIVE CODING PROGRESS */}

                                                                <div className="graph-card">

                                                                    <div className="graph-top">

                                                                        <div>

                                                                            <h3>
                                                                                Coding Progress Trend
                                                                            </h3>

                                                                            <p>
                                                                                Cumulative problems solved over time
                                                                            </p>

                                                                        </div>

                                                                        <span>
                                                                            {codingStats?.problemsSolved || 0} Solved
                                                                        </span>

                                                                    </div>

                                                                    <div className="real-graph">

                                                                        <ResponsiveContainer
                                                                            width="100%"
                                                                            height={270}
                                                                        >

                                                                            <AreaChart
                                                                                data={
                                                                                    codingStats?.timeline && codingStats.timeline.length > 0
                                                                                        ? codingStats.timeline
                                                                                        : [
                                                                                            { date: "Day 1", solved: 0 },
                                                                                            { date: "Day 2", solved: 0 },
                                                                                            { date: "Day 3", solved: 0 },
                                                                                            { date: "Day 4", solved: 0 },
                                                                                            { date: "Day 5", solved: 0 },
                                                                                            { date: "Day 6", solved: 0 },
                                                                                            { date: "Day 7", solved: 0 }
                                                                                        ]
                                                                                }
                                                                                margin={{
                                                                                    top: 10,
                                                                                    right: 30,
                                                                                    left: -15,
                                                                                    bottom: 0
                                                                                }}
                                                                            >

                                                                                <defs>

                                                                                    <linearGradient
                                                                                        id="colorProgress"
                                                                                        x1="0"
                                                                                        y1="0"
                                                                                        x2="0"
                                                                                        y2="1"
                                                                                    >

                                                                                        <stop
                                                                                            offset="5%"
                                                                                            stopColor="#6366f1"
                                                                                            stopOpacity={0.32}
                                                                                        />

                                                                                        <stop
                                                                                            offset="95%"
                                                                                            stopColor="#00c2ff"
                                                                                            stopOpacity={0.03}
                                                                                        />

                                                                                    </linearGradient>

                                                                                </defs>

                                                                                <CartesianGrid
                                                                                    strokeDasharray="4 4"
                                                                                    vertical={true}
                                                                                    horizontal={true}
                                                                                />

                                                                                <XAxis
                                                                                    dataKey="date"
                                                                                    tickLine={false}
                                                                                    axisLine={false}
                                                                                />

                                                                                <YAxis
                                                                                    allowDecimals={false}
                                                                                    tickLine={false}
                                                                                    axisLine={false}
                                                                                />

                                                                                <Tooltip
                                                                                    formatter={(value) => [`${value} Problems Solved`, "Cumulative Solved"]}
                                                                                    labelFormatter={(label) => `Milestone: ${label}`}
                                                                                />

                                                                                <Area
                                                                                    type="monotone"
                                                                                    dataKey="solved"
                                                                                    stroke="#6366f1"
                                                                                    strokeWidth={3.5}
                                                                                    fillOpacity={1}
                                                                                    fill="url(#colorProgress)"
                                                                                    dot={{
                                                                                        r: 4,
                                                                                        strokeWidth: 2,
                                                                                        fill: "#6366f1",
                                                                                        stroke: "#ffffff"
                                                                                    }}
                                                                                    activeDot={{
                                                                                        r: 6
                                                                                    }}
                                                                                />

                                                                            </AreaChart>

                                                                        </ResponsiveContainer>

                                                                    </div>

                                                                </div>

                                                                {/* DIFFICULTY BREAKDOWN (REPLACING FAKE DONUT) */}

                                                                <div className="progress-card">

                                                                    <h3>
                                                                        Difficulty Breakdown
                                                                    </h3>

                                                                    <p>
                                                                        DSA & SQL mastery breakdown
                                                                    </p>

                                                                    <div className="difficulty-breakdown">

                                                                        <div className="diff-summary-row">

                                                                            <div className="diff-summary-count">

                                                                                <span className="diff-big-number">
                                                                                    {codingStats?.problemsSolved || 0}
                                                                                </span>

                                                                                <span className="diff-total-label">
                                                                                    / {codingStats?.totalAvailableProblems || 6260} solved
                                                                                </span>

                                                                            </div>

                                                                            <span className="diff-rate-pill">
                                                                                {codingStats?.acceptanceRate != null ? `${codingStats.acceptanceRate}% Rate` : "0% Rate"}
                                                                            </span>

                                                                        </div>

                                                                        <div className="diff-bars">

                                                                            {/* EASY */}

                                                                            <div className="diff-bar-item">

                                                                                <div className="diff-bar-header">

                                                                                    <span className="diff-bar-title">
                                                                                        <span className="diff-dot easy" /> Easy
                                                                                    </span>

                                                                                    <span className="diff-bar-count">
                                                                                        {codingStats?.easySolved || 0}
                                                                                    </span>

                                                                                </div>

                                                                                <div className="diff-progress-track">

                                                                                    <div
                                                                                        className="diff-progress-fill easy"
                                                                                        style={{
                                                                                            width: `${Math.min(100, Math.round(((codingStats?.easySolved || 0) / Math.max(1, codingStats?.problemsSolved || 1)) * 100))}%`
                                                                                        }}
                                                                                    />

                                                                                </div>

                                                                            </div>

                                                                            {/* MEDIUM */}

                                                                            <div className="diff-bar-item">

                                                                                <div className="diff-bar-header">

                                                                                    <span className="diff-bar-title">
                                                                                        <span className="diff-dot medium" /> Medium
                                                                                    </span>

                                                                                    <span className="diff-bar-count">
                                                                                        {codingStats?.mediumSolved || 0}
                                                                                    </span>

                                                                                </div>

                                                                                <div className="diff-progress-track">

                                                                                    <div
                                                                                        className="diff-progress-fill medium"
                                                                                        style={{
                                                                                            width: `${Math.min(100, Math.round(((codingStats?.mediumSolved || 0) / Math.max(1, codingStats?.problemsSolved || 1)) * 100))}%`
                                                                                        }}
                                                                                    />

                                                                                </div>

                                                                            </div>

                                                                            {/* HARD */}

                                                                            <div className="diff-bar-item">

                                                                                <div className="diff-bar-header">

                                                                                    <span className="diff-bar-title">
                                                                                        <span className="diff-dot hard" /> Hard
                                                                                    </span>

                                                                                    <span className="diff-bar-count">
                                                                                        {codingStats?.hardSolved || 0}
                                                                                    </span>

                                                                                </div>

                                                                                <div className="diff-progress-track">

                                                                                    <div
                                                                                        className="diff-progress-fill hard"
                                                                                        style={{
                                                                                            width: `${Math.min(100, Math.round(((codingStats?.hardSolved || 0) / Math.max(1, codingStats?.problemsSolved || 1)) * 100))}%`
                                                                                        }}
                                                                                    />

                                                                                </div>

                                                                            </div>

                                                                        </div>

                                                                        <div className="diff-sub-stats">

                                                                            <div className="diff-sub-stat">

                                                                                <span>DSA</span>

                                                                                <strong>{codingStats?.dsaSolved || 0}</strong>

                                                                            </div>

                                                                            <div className="diff-sub-stat">

                                                                                <span>SQL</span>

                                                                                <strong>{codingStats?.sqlSolved || 0}</strong>

                                                                            </div>

                                                                            <div className="diff-sub-stat">

                                                                                <span>Attempted</span>

                                                                                <strong>{codingStats?.problemsAttempted || 0}</strong>

                                                                            </div>

                                                                        </div>

                                                                    </div>

                                                                </div>

                                                            </div>

                                                            {/* ACTION CARDS & RECENT ACTIVITY */}

                                                            <div className="dashboard-bottom second-bottom">

                                                                {/* ACTION 1 */}

                                                                <div
                                                                    className="action-card"
                                                                    onClick={() => navigate("/mock-interview")}
                                                                    style={{ cursor: "pointer" }}
                                                                >

                                                                    <div className="action-arrow">
                                                                        ↗
                                                                    </div>

                                                                    <div className="action-icon">
                                                                        <FiMic />
                                                                    </div>

                                                                    <h3>
                                                                        Start Mock Interview
                                                                    </h3>

                                                                    <p>
                                                                        5–15 min sessions
                                                                    </p>

                                                                </div>

                                                                {/* ACTION 2 */}

                                                                <div
                                                                    className="action-card"
                                                                    onClick={() => navigate("/coding-arena")}
                                                                    style={{ cursor: "pointer" }}
                                                                >

                                                                    <div className="action-arrow">
                                                                        ↗
                                                                    </div>

                                                                    <div className="action-icon">
                                                                        <FiCode />
                                                                    </div>

                                                                    <h3>
                                                                        Solve a Problem
                                                                    </h3>

                                                                    <p>
                                                                        6,260 problems ready
                                                                    </p>

                                                                </div>

                                                                {/* ACTION 3 */}

                                                                <div
                                                                    className="action-card"
                                                                    onClick={() => navigate("/resume-analyzer")}
                                                                    style={{ cursor: "pointer" }}
                                                                >

                                                                    <div className="action-arrow">
                                                                        ↗
                                                                    </div>

                                                                    <div className="action-icon">
                                                                        <FiFileText />
                                                                    </div>

                                                                    <h3>
                                                                        Analyze Resume
                                                                    </h3>

                                                                    <p>
                                                                        Get instant ATS score
                                                                    </p>

                                                                </div>

                                                                {/* RECENT ACTIVITY */}

                                                                <div className="activity-card">

                                                                    <h3>
                                                                        Recent activity
                                                                    </h3>

                                                                    {combinedActivities.length > 0 ? (

                                                                        combinedActivities.map((item) => (

                                                                            <div
                                                                                className="activity-item"
                                                                                key={item.id}
                                                                                onClick={item.onClick}
                                                                                style={{ cursor: "pointer" }}
                                                                            >

                                                                                <div className="activity-left">

                                                                                    <div
                                                                                        className="activity-dot"
                                                                                        style={{
                                                                                            background: item.dotColor || "#6366f1"
                                                                                        }}
                                                                                    />

                                                                                    <div className="activity-text">

                                                                                        <h4>
                                                                                            {item.title}
                                                                                        </h4>

                                                                                        <p>
                                                                                            {item.subtitle}
                                                                                        </p>

                                                                                    </div>

                                                                                </div>

                                                                                <span className={`activity-badge ${item.badgeClass}`}>
                                                                                    {item.badge}
                                                                                </span>

                                                                            </div>

                                                                        ))

                                                                    ) : (

                                                                        <div className="activity-item empty-activity">
                                                                            No recent activity yet. Solve a problem in Coding Arena or take an Aptitude test to get started!
                                                                        </div>

                                                                    )}

                                                                </div>

                                                            </div>



        </motion.div>
    );
}
