import Sidebar from "../Components/Dashboard/Sidebar";
import Topbar from "../Components/Dashboard/Topbar";

import {
    FiTrendingUp,
    FiTarget,
    FiClock,
    FiAward
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

                    {/* BOTTOM SECTION */}

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

                </div>

            </div>

        </section>
    );
}