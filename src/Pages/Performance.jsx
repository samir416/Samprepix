import React, { useState } from "react";

import {
  FaChartLine,
  FaCode,
  FaBrain,
  FaTrophy,
  FaFire,
  FaClock,
  FaArrowUp,
  FaCheckCircle,
  FaLaptopCode,
} from "react-icons/fa";

import {
  MdLeaderboard,
  MdOutlineInsights,
  MdTrackChanges,
} from "react-icons/md";

import "../styles/Performance.css";

const skillData = [
  {
    name: "DSA",
    progress: 82,
    color: "#8b5cf6",
  },
  {
    name: "Frontend",
    progress: 74,
    color: "#06b6d4",
  },
  {
    name: "Backend",
    progress: 63,
    color: "#10b981",
  },
  {
    name: "Aptitude",
    progress: 69,
    color: "#f59e0b",
  },
];

const activityData = [
  {
    title: "Solved Coding Problems",
    value: "248",
    icon: <FaCode />,
    growth: "+18%",
  },
  {
    title: "Mock Interviews",
    value: "37",
    icon: <FaBrain />,
    growth: "+12%",
  },
  {
    title: "Resume Score",
    value: "91%",
    icon: <FaCheckCircle />,
    growth: "+6%",
  },
  {
    title: "Daily Streak",
    value: "26 Days",
    icon: <FaFire />,
    growth: "+8%",
  },
];

const recentPerformance = [
  {
    title: "React Interview Round",
    score: "92%",
    status: "Excellent",
  },
  {
    title: "Java DSA Contest",
    score: "84%",
    status: "Strong",
  },
  {
    title: "Resume Analysis",
    score: "91%",
    status: "Optimized",
  },
  {
    title: "SQL Mock Test",
    score: "79%",
    status: "Improving",
  },
];

const Performance = () => {

  const [aiMode, setAiMode] = useState(false);

  const [reportOpen, setReportOpen] = useState(false);

  const [liveTracking, setLiveTracking] = useState(true);

  const [selectedActivity, setSelectedActivity] = useState("");

  const [selectedDailyBox, setSelectedDailyBox] = useState("");

  return (

    <div className="performance-page">

      {/* HEADER */}

      <div className="performance-header">

        <div>

          <h1>
            Performance Analytics
          </h1>

          <p>
            Track your placement preparation journey with AI-powered insights.
          </p>

        </div>

        <div className="performance-header-right">

          <button
            className={`performance-ai-btn ${aiMode ? "active-ai" : ""}`}
            onClick={() => setAiMode(!aiMode)}
          >

            <MdOutlineInsights />

            {
              aiMode
                ? "AI Analysis Enabled"
                : "Enable AI Insights"
            }

          </button>

        </div>

      </div>

      {/* AI PANEL */}

      {

        aiMode && (

          <div className="ai-insight-panel glass-card">

            <div className="ai-insight-top">

              <h3>
                AI Recommendation Engine
              </h3>

              <span>
                LIVE
              </span>

            </div>

            <div className="ai-insight-grid">

              <div className="ai-recommend-card">

                <h4>
                  DSA Improvement
                </h4>

                <p>
                  Solve 3 medium graph problems daily to improve coding rounds.
                </p>

              </div>

              <div className="ai-recommend-card">

                <h4>
                  Interview Readiness
                </h4>

                <p>
                  Practice HR mock interviews to improve confidence score.
                </p>

              </div>

              <div className="ai-recommend-card">

                <h4>
                  Resume Optimization
                </h4>

                <p>
                  Add measurable achievements to improve ATS ranking.
                </p>

              </div>

            </div>

          </div>

        )

      }

      {/* TOP STATS */}

      <div className="performance-top-grid">

        {

          activityData.map((item, index) => (

            <div
              className={`performance-stat-card ${
                selectedActivity === item.title
                  ? "active-stat-card"
                  : ""
              }`}
              key={index}
              onClick={() => setSelectedActivity(item.title)}
            >

              <div className="performance-stat-top">

                <div className="performance-stat-icon">
                  {item.icon}
                </div>

                <div className="performance-growth">

                  <FaArrowUp />

                  {item.growth}

                </div>

              </div>

              <h2>
                {item.value}
              </h2>

              <p>
                {item.title}
              </p>

            </div>

          ))

        }

      </div>

      {/* SELECTED ACTIVITY */}

      {

        selectedActivity && (

          <div className="selected-activity-box glass-card">

            <h3>
              {selectedActivity}
            </h3>

            <p>
              AI is now analyzing your {selectedActivity.toLowerCase()} performance and generating optimization insights.
            </p>

          </div>

        )

      }

      {/* MIDDLE */}

      <div className="performance-middle-grid">

        {/* SKILLS */}

        <div className="performance-chart-card glass-card">

          <div className="performance-card-header">

            <div>

              <h3>
                Skill Progress
              </h3>

              <p>
                AI tracked growth metrics
              </p>

            </div>

            <div
              className={`performance-mini-badge ${
                liveTracking ? "tracking-active" : ""
              }`}
              onClick={() => setLiveTracking(!liveTracking)}
            >

              <MdTrackChanges />

              {
                liveTracking
                  ? "Tracking Enabled"
                  : "Tracking Disabled"
              }

            </div>

          </div>

          <div className="performance-skill-wrapper">

            {

              skillData.map((skill, index) => (

                <div
                  className="performance-skill-item"
                  key={index}
                >

                  <div className="performance-skill-top">

                    <span>
                      {skill.name}
                    </span>

                    <span>
                      {skill.progress}%
                    </span>

                  </div>

                  <div className="performance-progress-bar">

                    <div
                      className="performance-progress-fill"
                      style={{
                        width: `${skill.progress}%`,
                        background: skill.color,
                      }}
                    ></div>

                  </div>

                </div>

              ))

            }

          </div>

        </div>

        {/* RIGHT */}

        <div className="performance-right-panel">

          <div className="performance-score-card glass-card">

            <div className="score-ring">

              <div className="score-ring-inner">

                <h2>
                  87%
                </h2>

                <span>
                  Overall
                </span>

              </div>

            </div>

            <div className="performance-score-content">

              <h3>
                Placement Readiness
              </h3>

              <p>
                Your profile is performing better than 78% of students this month.
              </p>

              <button
                onClick={() => setReportOpen(!reportOpen)}
              >
                {
                  reportOpen
                    ? "Hide Report"
                    : "View Detailed Report"
                }
              </button>

            </div>

          </div>

          {

            reportOpen && (

              <div className="report-preview glass-card">

                <div className="report-line">
                  <span>Technical Score</span>
                  <strong>89%</strong>
                </div>

                <div className="report-line">
                  <span>Communication</span>
                  <strong>81%</strong>
                </div>

                <div className="report-line">
                  <span>Problem Solving</span>
                  <strong>93%</strong>
                </div>

                <div className="report-line">
                  <span>Resume ATS</span>
                  <strong>91%</strong>
                </div>

              </div>

            )

          }

          {/* RANK */}

          <div className="performance-rank-card glass-card">

            <div className="rank-icon">

              <MdLeaderboard />

            </div>

            <div>

              <h3>
                #148
              </h3>

              <p>
                Global Rank
              </p>

            </div>

          </div>

        </div>

      </div>

      {/* BOTTOM */}

      <div className="performance-bottom-grid">

        {/* RECENT */}

        <div className="performance-activity-card glass-card">

          <div className="performance-card-header">

            <div>

              <h3>
                Recent Performance
              </h3>

              <p>
                Latest activity insights
              </p>

            </div>

            <button
              className="view-all-btn"
            >
              Performance Logs
            </button>

          </div>

          <div className="performance-activity-list">

            {

              recentPerformance.map((item, index) => (

                <div
                  className="performance-activity-item"
                  key={index}
                >

                  <div className="activity-left">

                    <div className="activity-icon">

                      <FaLaptopCode />

                    </div>

                    <div>

                      <h4>
                        {item.title}
                      </h4>

                      <span>
                        {item.status}
                      </span>

                    </div>

                  </div>

                  <div className="activity-score">
                    {item.score}
                  </div>

                </div>

              ))

            }

          </div>

        </div>

        {/* DAILY */}

        <div className="performance-side-card glass-card">

          <div className="performance-card-header">

            <div>

              <h3>
                Daily Analytics
              </h3>

              <p>
                Consistency & activity
              </p>

            </div>

          </div>

          <div className="daily-analytics-grid">

            <div
              className={`daily-box ${
                selectedDailyBox === "study"
                  ? "daily-box-active"
                  : ""
              }`}
              onClick={() => setSelectedDailyBox("study")}
            >

              <FaClock />

              <h2>
                5.8h
              </h2>

              <p>
                Study Time
              </p>

            </div>

            <div
              className={`daily-box ${
                selectedDailyBox === "achievement"
                  ? "daily-box-active"
                  : ""
              }`}
              onClick={() => setSelectedDailyBox("achievement")}
            >

              <FaTrophy />

              <h2>
                12
              </h2>

              <p>
                Achievements
              </p>

            </div>

            <div
              className={`daily-box ${
                selectedDailyBox === "growth"
                  ? "daily-box-active"
                  : ""
              }`}
              onClick={() => setSelectedDailyBox("growth")}
            >

              <FaChartLine />

              <h2>
                91%
              </h2>

              <p>
                Growth Rate
              </p>

            </div>

            <div
              className={`daily-box ${
                selectedDailyBox === "streak"
                  ? "daily-box-active"
                  : ""
              }`}
              onClick={() => setSelectedDailyBox("streak")}
            >

              <FaFire />

              <h2>
                26
              </h2>

              <p>
                Streak
              </p>

            </div>

          </div>

          {

            selectedDailyBox && (

              <div className="daily-preview-box">

                <h4>
                  {
                    selectedDailyBox === "study"
                      ? "Study Insights"

                      : selectedDailyBox === "achievement"
                      ? "Achievements"

                      : selectedDailyBox === "growth"
                      ? "Growth Analysis"

                      : "Streak Analytics"
                  }
                </h4>

                <p>
                  AI generated analytics and insights are being displayed for this section.
                </p>

              </div>

            )

          }

        </div>

      </div>

    </div>
  );
};

export default Performance;