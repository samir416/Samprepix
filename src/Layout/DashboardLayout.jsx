import { useLocation } from "react-router-dom";
import ResumeAnalyzer from "../Pages/ResumeAnalyzer";
import MockInterview from "../Pages/MockInterview";
import CodingArena from "../Pages/CodingArena";
import Performance from "../Pages/Performance";


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

    const location = useLocation();

    const graphData = [
        { day: "Mon", score: 62 },
        { day: "Tue", score: 68 },
        { day: "Wed", score: 71 },
        { day: "Thu", score: 65 },
        { day: "Fri", score: 78 },
        { day: "Sat", score: 82 },
        { day: "Sun", score: 87 }
    ];

    return (

        <>

            {

                location.pathname === "/resume-analyzer"

                    ? <ResumeAnalyzer />

                    : location.pathname === "/mock-interview"

                        ? <MockInterview />

                        : location.pathname === "/coding-arena"

                            ? <CodingArena />

                            : location.pathname === "/performance"

                                ? <Performance />

                                : (

                                    <>

                                        {/* HEADER */}

                                        <div className="dashboard-header">

                                            <h1>
                                                Welcome back, Aarav 👋
                                            </h1>

                                            <p>
                                                Here's how your placement prep is going this week.
                                            </p>

                                        </div>

                                        {/* STATS */}

                                        <div className="stats-grid">

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
                                                        92
                                                    </h2>

                                                    <span>
                                                        +5%
                                                    </span>

                                                </div>

                                            </div>

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

                                        {/* GRAPH */}

                                        <div className="dashboard-bottom">

                                            <div className="graph-card">

                                                <div className="graph-top">

                                                    <div>

                                                        <h3>
                                                            Interview Score Trend
                                                        </h3>

                                                        <p>
                                                            Last 7 days
                                                        </p>

                                                    </div>

                                                    <span>
                                                        +25 pts
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

                                    </>

                                )

            }

        </>

    );
}