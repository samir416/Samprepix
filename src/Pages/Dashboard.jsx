import Sidebar from "../Components/Dashboard/Sidebar";
import Topbar from "../Components/Dashboard/Topbar";

import {
    FiTrendingUp,
    FiTarget,
    FiClock,
    FiAward,
    FiMic,
    FiCode,
    FiFileText
} from "react-icons/fi";

import "../styles/dashboard.css";

export default function Dashboard() {

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
                                    92
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

                            <div className="fake-graph">

                                <div className="graph-line"></div>

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

                            {/* ITEM */}

                            <div className="activity-item">

                                <div className="activity-left">

                                    <div className="activity-dot"></div>

                                    <div className="activity-text">

                                        <h4>
                                            Completed mock: Behavioral
                                        </h4>

                                        <p>
                                            2h ago
                                        </p>

                                    </div>

                                </div>

                                <span className="activity-score">
                                    Score 87
                                </span>

                            </div>

                            {/* ITEM */}

                            <div className="activity-item">

                                <div className="activity-left">

                                    <div className="activity-dot"></div>

                                    <div className="activity-text">

                                        <h4>
                                            Solved: Two Sum II
                                        </h4>

                                        <p>
                                            5h ago
                                        </p>

                                    </div>

                                </div>

                                <span className="activity-score">
                                    +10 XP
                                </span>

                            </div>

                            {/* ITEM */}

                            <div className="activity-item">

                                <div className="activity-left">

                                    <div className="activity-dot"></div>

                                    <div className="activity-text">

                                        <h4>
                                            Resume v3 uploaded
                                        </h4>

                                        <p>
                                            Yesterday
                                        </p>

                                    </div>

                                </div>

                                <span className="activity-score">
                                    ATS 92
                                </span>

                            </div>

                            {/* ITEM */}

                            <div className="activity-item">

                                <div className="activity-left">

                                    <div className="activity-dot"></div>

                                    <div className="activity-text">

                                        <h4>
                                            Joined cohort: Google SDE
                                        </h4>

                                        <p>
                                            2d ago
                                        </p>

                                    </div>

                                </div>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </section>
    );
}