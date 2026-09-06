import Sidebar from "../Components/Dashboard/Sidebar";
import Topbar from "../Components/Dashboard/Topbar";
import { useLocation } from "react-router-dom";
import ResumeAnalyzer from "./ResumeAnalyzer";
import MockInterview from "./MockInterview";
import CodingArena from "./CodingArena";
import Performance from "./Performance";
import Profile from "./Profile";
import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
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

    const location = useLocation();

    const [resumeHistory, setResumeHistory] = useState([]);

    const [latestResume, setLatestResume] = useState(null);

    const [user, setUser] = useState(null);

    const [codingStats, setCodingStats] = useState(null);

    const [mockInterviewCount, setMockInterviewCount] = useState(0);

    const [loadingStats, setLoadingStats] = useState(true);
    useEffect(() => {

        const verifyUser = async () => {

            try {

                const data = await getCurrentUser();

                setUser(data);

                localStorage.setItem(
                    "user",
                    JSON.stringify(data)
                );

            } catch {

                localStorage.removeItem("token");
                localStorage.removeItem("user");

                navigate("/login");
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

                    console.log(error);
                }
            };

        loadHistory();

        const loadLatestResume = async () => {

            try {

                const data =
                    await getLatestResumeAnalysis();

                setLatestResume(data);

            } catch (error) {

                console.log(error);
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

    }, [navigate]);

    return (

        <section className="dashboard-layout">

            {/* SIDEBAR */}

            <Sidebar />

            {/* MAIN */}

            <div className="dashboard-main">

                {/* TOPBAR */}

                <Topbar />

                {/* CONTENT */}

                <div className="dashboard-content">
                    <AnimatePresence

                        mode="wait"

                    >

                        <motion.div

                            key={location.pathname}

                            initial={{

                                opacity: 0,

                                y: 14,

                                scale: .985

                            }}

                            animate={{

                                opacity: 1,

                                y: 0,

                                scale: 1

                            }}

                            exit={{

                                opacity: 0,

                                y: -8,

                                scale: .992

                            }}

                            transition={{

                                duration: .26,

                                ease: [0.22, 1, 0.36, 1]

                            }}

                        >

                            {

                                /* =========================
                                RESUME ANALYZER
                                ========================= */

                                location.pathname === "/resume-analyzer"

                                    ? (

                                        <ResumeAnalyzer />

                                    )

                                    /* =========================
                                    MOCK INTERVIEW
                                    ========================= */

                                    : location.pathname === "/mock-interview"

                                        ? (

                                            <MockInterview />

                                        )

                                        /* =========================
                                        CODING ARENA
                                        ========================= */

                                        : location.pathname === "/coding-arena"

                                            ? (

                                                <CodingArena />

                                            )

                                            /* =========================
                                            PERFORMANCE
                                            ========================= */

                                            : location.pathname === "/performance"

                                                ? (

                                                    <Performance />

                                                )

                                                 /* =========================
                                                    Profile
                                                    ========================= */

                                                : location.pathname === "/profile"

                                                    ? (

                                                        <Profile />

                                                    )

                                                    /* =========================
                                                    DASHBOARD HOME
                                                    ========================= */

                                                    : (

                                                        <>

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

                                                                        {codingStats?.timeline && codingStats.timeline.length > 0 ? (

                                                                            <ResponsiveContainer
                                                                                width="100%"
                                                                                height={270}
                                                                            >

                                                                                <AreaChart
                                                                                    data={codingStats.timeline}
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

                                                                        ) : (

                                                                            <div className="coding-empty-graph">

                                                                                <div className="coding-empty-icon">

                                                                                    <FiCode />

                                                                                </div>

                                                                                <h4>No Solved Problems Yet</h4>

                                                                                <p>
                                                                                    Your coding progress chart will populate automatically as you pass test cases in Coding Arena.
                                                                                </p>

                                                                                <button
                                                                                    type="button"
                                                                                    className="coding-empty-cta"
                                                                                    onClick={() => navigate("/coding-arena")}
                                                                                >
                                                                                    Jump into Coding Arena <FiArrowRight />
                                                                                </button>

                                                                            </div>

                                                                        )}

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

                                                                    {codingStats?.recentSubmissions && codingStats.recentSubmissions.length > 0 ? (

                                                                        codingStats.recentSubmissions.slice(0, 4).map((item) => (

                                                                            <div
                                                                                className="activity-item"
                                                                                key={`${item.problemId}-${item.attemptedAt}`}
                                                                                onClick={() => navigate("/coding-arena")}
                                                                                style={{ cursor: "pointer" }}
                                                                            >

                                                                                <div className="activity-left">

                                                                                    <div
                                                                                        className="activity-dot"
                                                                                        style={{
                                                                                            background: item.completed ? "#10b981" : "#6366f1"
                                                                                        }}
                                                                                    />

                                                                                    <div className="activity-text">

                                                                                        <h4>
                                                                                            {item.problemTitle}
                                                                                        </h4>

                                                                                        <p>
                                                                                            {item.language ? item.language.toUpperCase() : "Code"} · {item.difficulty}
                                                                                        </p>

                                                                                    </div>

                                                                                </div>

                                                                                <span className={`activity-badge ${item.completed ? "solved" : "attempted"}`}>
                                                                                    {item.completed ? "✓ Solved" : "Attempted"}
                                                                                </span>

                                                                            </div>

                                                                        ))

                                                                    ) : resumeHistory && resumeHistory.length > 0 ? (

                                                                        resumeHistory.slice(0, 4).map((item) => (

                                                                            <div
                                                                                className="activity-item"
                                                                                key={item.id}
                                                                                onClick={() => navigate("/resume-analyzer")}
                                                                                style={{ cursor: "pointer" }}
                                                                            >

                                                                                <div className="activity-left">

                                                                                    <div className="activity-dot" />

                                                                                    <div className="activity-text">

                                                                                        <h4>
                                                                                            Resume Analysis
                                                                                        </h4>

                                                                                        <p>
                                                                                            {item.analyzedAt ? item.analyzedAt.split("T")[0] : "Recent"}
                                                                                        </p>

                                                                                    </div>

                                                                                </div>

                                                                                <span className="activity-badge resume">
                                                                                    ATS {item.score}
                                                                                </span>

                                                                            </div>

                                                                        ))

                                                                    ) : (

                                                                        <div className="activity-item empty-activity">
                                                                            No recent activity yet. Solve a problem in Coding Arena to get started!
                                                                        </div>

                                                                    )}

                                                                </div>

                                                            </div>



                                                        </>

                                                    )

                            }

                        </motion.div>

                    </AnimatePresence>


                </div>


            </div>


        </section>
    );
}