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
    FiTrendingUp,
    FiTarget,
    FiClock,
    FiAward,
    FiMic,
    FiCode,
    FiFileText
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

    const graphData = resumeHistory.map(
        (item, index) => ({
            day: `R${index + 1}`,
            score: item.score
        })
    );
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

                                                                {/* CARD 1 */}

                                                                <div className="stat-card">

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Interview Score
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiTrendingUp />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            87
                                                                        </h2>

                                                                        <span>
                                                                            +12%
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 2 */}

                                                                <div className="stat-card">

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
                                                                            +5%
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 3 */}

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
                                                                            14
                                                                        </h2>

                                                                        <span>
                                                                            Keep going!
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                                {/* CARD 4 */}

                                                                <div className="stat-card">

                                                                    <div className="stat-top">

                                                                        <p>
                                                                            Rank
                                                                        </p>

                                                                        <div className="stat-icon">

                                                                            <FiAward />

                                                                        </div>

                                                                    </div>

                                                                    <div className="stat-bottom">

                                                                        <h2>
                                                                            #284
                                                                        </h2>

                                                                        <span>
                                                                            Top 3%
                                                                        </span>

                                                                    </div>

                                                                </div>

                                                            </div>

                                                            {/* TOP GRAPH SECTION */}

                                                            <div className="dashboard-bottom">

                                                                {/* GRAPH */}

                                                                <div className="graph-card">

                                                                    <div className="graph-top">

                                                                        <div>

                                                                            <h3>
                                                                                ATS Score Trend
                                                                            </h3>

                                                                            <p>
                                                                                Resume History
                                                                            </p>

                                                                        </div>

                                                                        <span>
                                                                            {resumeHistory.length} Records
                                                                        </span>

                                                                    </div>

                                                                    <div className="real-graph">

                                                                        <ResponsiveContainer
                                                                            width="100%"
                                                                            height={270}
                                                                        >

                                                                            <AreaChart

                                                                                data={graphData}

                                                                                margin={{
                                                                                    top: 10,
                                                                                    right: 30,
                                                                                    left: -15,
                                                                                    bottom: 0
                                                                                }}
                                                                            >

                                                                                <defs>

                                                                                    <linearGradient
                                                                                        id="colorScore"
                                                                                        x1="0"
                                                                                        y1="0"
                                                                                        x2="0"
                                                                                        y2="1"
                                                                                    >

                                                                                        <stop
                                                                                            offset="5%"
                                                                                            stopColor="#6366f1"
                                                                                            stopOpacity={0.28}
                                                                                        />

                                                                                        <stop
                                                                                            offset="95%"
                                                                                            stopColor="#6366f1"
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
                                                                                    dataKey="day"
                                                                                    tickLine={false}
                                                                                    axisLine={false}
                                                                                />

                                                                                <YAxis
                                                                                    tickLine={false}
                                                                                    axisLine={false}
                                                                                />

                                                                                <Tooltip />

                                                                                <Area
                                                                                    type="monotone"
                                                                                    dataKey="score"
                                                                                    stroke="#6366f1"
                                                                                    strokeWidth={4}
                                                                                    fillOpacity={1}
                                                                                    fill="url(#colorScore)"
                                                                                    dot={{
                                                                                        r: 5,
                                                                                        strokeWidth: 4,
                                                                                        fill: "#6366f1",
                                                                                        stroke: "#ffffff"
                                                                                    }}
                                                                                    activeDot={{
                                                                                        r: 7
                                                                                    }}
                                                                                />

                                                                            </AreaChart>

                                                                        </ResponsiveContainer>

                                                                    </div>

                                                                </div>

                                                                {/* DONUT */}

                                                                <div className="progress-card">

                                                                    <h3>
                                                                        Coding Progress
                                                                    </h3>

                                                                    <p>
                                                                        DSA mastery
                                                                    </p>

                                                                    <div className="donut-chart">

                                                                        <div className="donut-inner">

                                                                            <h2>
                                                                                73%
                                                                            </h2>

                                                                            <span>
                                                                                142 / 195 solved
                                                                            </span>

                                                                        </div>

                                                                    </div>

                                                                </div>

                                                            </div>

                                                            {/* NEW BOTTOM UI */}

                                                            <div className="dashboard-bottom second-bottom">

                                                                {/* CARD 1 */}

                                                                <div className="action-card">

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

                                                                {/* CARD 2 */}

                                                                <div className="action-card">

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
                                                                        Daily challenge ready
                                                                    </p>

                                                                </div>

                                                                {/* CARD 3 */}

                                                                <div className="action-card">

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



                                                                    {
                                                                        resumeHistory.map(
                                                                            (item) => (

                                                                                <div
                                                                                    className="activity-item"
                                                                                    key={item.id}
                                                                                >

                                                                                    <div className="activity-left">

                                                                                        <div className="activity-dot"></div>

                                                                                        <div className="activity-text">

                                                                                            <h4>
                                                                                                Resume Analysis
                                                                                            </h4>

                                                                                            <p>
                                                                                                {
                                                                                                    item.analyzedAt
                                                                                                        .split("T")[0]
                                                                                                }
                                                                                            </p>

                                                                                        </div>

                                                                                    </div>

                                                                                    <span className="activity-score">

                                                                                        ATS {item.score}

                                                                                    </span>

                                                                                </div>
                                                                            )
                                                                        )
                                                                    }
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