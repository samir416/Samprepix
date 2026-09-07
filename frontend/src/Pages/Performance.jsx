import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  FaChartLine,
  FaCode,
  FaBrain,
  FaFire,
  FaCheckCircle,
  FaLaptopCode,
  FaArrowRight,
  FaSyncAlt,
  FaExclamationTriangle,
  FaFileAlt
} from "react-icons/fa";
import {
  MdOutlineInsights,
  MdTrackChanges,
  MdLeaderboard
} from "react-icons/md";
import { getPerformanceAnalytics } from "../services/performanceService";
import "../styles/Performance.css";

export default function Performance() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [aiMode, setAiMode] = useState(false);
  const [reportOpen, setReportOpen] = useState(false);
  const [selectedActivity, setSelectedActivity] = useState("");

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getPerformanceAnalytics();
      setAnalytics(data);
    } catch (err) {
      console.error("Failed to load performance analytics:", err);
      setError(err.response?.data?.message || err.message || "Failed to load performance metrics");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading) {
    return (
      <div className="performance-page">
        <div className="performance-loading-wrapper">
          <div className="performance-loading-spinner" />
          <p>Aggregating your placement performance analytics...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="performance-page">
        <div className="performance-error-banner">
          <FaExclamationTriangle size={24} />
          <div>
            <h3>Unable to Load Performance Data</h3>
            <p>{error}</p>
          </div>
          <button className="performance-retry-btn" onClick={loadData}>
            <FaSyncAlt /> Retry
          </button>
        </div>
      </div>
    );
  }

  const {
    placementReadinessScore = 0,
    readinessStatus = "Needs Practice",
    technicalScore,
    problemSolvingScore,
    resumeAtsScore,
    problemsSolved = 0,
    problemsAttempted = 0,
    acceptanceRate = 0,
    easySolved = 0,
    mediumSolved = 0,
    hardSolved = 0,
    dsaSolved = 0,
    sqlSolved = 0,
    currentStreak = 0,
    skillMetrics = [],
    hasInterviewData = false,
    totalInterviews = 0,
    avgInterviewScore,
    hasResumeData = false,
    latestResumeScore,
    recentActivities = []
  } = analytics || {};

  const topStats = [
    {
      title: "Problems Solved",
      value: problemsSolved,
      icon: <FaCode />,
      subtitle: `${dsaSolved} DSA · ${sqlSolved} SQL`,
      accent: "#6366f1"
    },
    {
      title: "Mock Interviews",
      value: totalInterviews,
      icon: <FaBrain />,
      subtitle: hasInterviewData ? `Avg Score: ${avgInterviewScore}%` : "No mocks completed",
      accent: "#06b6d4"
    },
    {
      title: "Resume ATS Score",
      value: hasResumeData ? `${latestResumeScore}%` : "N/A",
      icon: <FaFileAlt />,
      subtitle: hasResumeData ? "Latest uploaded resume" : "No resume analyzed",
      accent: "#10b981"
    },
    {
      title: "Daily Coding Streak",
      value: `${currentStreak} ${currentStreak === 1 ? "Day" : "Days"}`,
      icon: <FaFire />,
      subtitle: currentStreak > 0 ? "Consecutive active days" : "Start your streak today",
      accent: "#f59e0b"
    }
  ];

  return (
    <div className="performance-page">
      {/* HEADER */}
      <div className="performance-header">
        <div>
          <h1>Performance Analytics</h1>
          <p>Real-time analytics and placement readiness tracking based on your actual activity.</p>
        </div>

        <div className="performance-header-right">
          <button
            className={`performance-ai-btn ${aiMode ? "active-ai" : ""}`}
            onClick={() => setAiMode(!aiMode)}
          >
            <MdOutlineInsights />
            {aiMode ? "AI Analysis Active" : "AI Insights"}
          </button>
        </div>
      </div>

      {/* AI INSIGHT PANEL */}
      {aiMode && (
        <div className="ai-insight-panel glass-card">
          <div className="ai-insight-top">
            <h3>AI Recommendation Engine</h3>
            <span className="live-badge">REAL-TIME INSIGHTS</span>
          </div>

          <div className="ai-insight-grid">
            <div className="ai-recommend-card">
              <h4>DSA Growth</h4>
              <p>
                {dsaSolved > 0
                  ? `You've solved ${dsaSolved} DSA problems. Focus on Medium Tree and Dynamic Programming challenges next.`
                  : "Start solving DSA problems in the Coding Arena to build algorithmic fluency."}
              </p>
            </div>

            <div className="ai-recommend-card">
              <h4>Interview Readiness</h4>
              <p>
                {hasInterviewData
                  ? `Your average interview score is ${avgInterviewScore}%. Practice behavioral and system design mocks to push past 85%.`
                  : "Complete a mock interview to evaluate your technical accuracy, communication, and response speed."}
              </p>
            </div>

            <div className="ai-recommend-card">
              <h4>Resume Optimization</h4>
              <p>
                {hasResumeData
                  ? `Your current ATS score is ${latestResumeScore}%. Review keyword suggestions to maximize candidate screening match.`
                  : "Upload your resume in Resume Analyzer to view quantified keyword matching and ATS scoring."}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* TOP STATS GRID */}
      <div className="performance-top-grid">
        {topStats.map((item, index) => (
          <div
            className={`performance-stat-card ${selectedActivity === item.title ? "active-stat-card" : ""}`}
            key={index}
            onClick={() => setSelectedActivity(item.title)}
          >
            <div className="performance-stat-top">
              <div className="performance-stat-icon" style={{ color: item.accent }}>
                {item.icon}
              </div>
              <span className="performance-stat-tag">{item.title}</span>
            </div>

            <h2>{item.value}</h2>
            <p>{item.subtitle}</p>
          </div>
        ))}
      </div>

      {/* SELECTED ACTIVITY FILTER BANNER */}
      {selectedActivity && (
        <div className="selected-activity-box glass-card">
          <div>
            <h3>Active Focus: {selectedActivity}</h3>
            <p>
              Displaying consolidated metric breakdown for {selectedActivity.toLowerCase()} computed from verified database records.
            </p>
          </div>
          <button className="clear-focus-btn" onClick={() => setSelectedActivity("")}>
            Clear Filter
          </button>
        </div>
      )}

      {/* MIDDLE SECTION: SKILL PROGRESS + PLACEMENT READINESS */}
      <div className="performance-middle-grid">
        {/* SKILL PROGRESS */}
        <div className="performance-chart-card glass-card">
          <div className="performance-card-header">
            <div>
              <h3>Verified Skill Progress</h3>
              <p>Cumulative problem solving by curriculum track</p>
            </div>

            <div className="performance-mini-badge tracking-active">
              <MdTrackChanges /> Live Sync
            </div>
          </div>

          <div className="performance-skill-wrapper">
            {skillMetrics && skillMetrics.length > 0 ? (
              skillMetrics.map((skill, index) => (
                <div className="performance-skill-item" key={index}>
                  <div className="performance-skill-top">
                    <span className="skill-name">{skill.name}</span>
                    <span className="skill-stats">
                      <strong>{skill.solved}</strong> / {skill.total} ({skill.progress}%)
                    </span>
                  </div>

                  <div className="performance-progress-bar">
                    <div
                      className="performance-progress-fill"
                      style={{
                        width: `${Math.max(skill.progress, skill.solved > 0 ? 3 : 0)}%`,
                        background: skill.color || "#6366f1"
                      }}
                    />
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-skill-state">
                <FaCode size={28} />
                <p>No problems solved yet. Visit Coding Arena to begin practicing.</p>
              </div>
            )}
          </div>
        </div>

        {/* PLACEMENT READINESS CARD */}
        <div className="performance-right-panel">
          <div className="performance-score-card glass-card">
            <div className="score-ring">
              <div className="score-ring-inner">
                <h2>{placementReadinessScore}%</h2>
                <span>Readiness</span>
              </div>
            </div>

            <div className="performance-score-content">
              <h3>{readinessStatus}</h3>
              <p>
                Composite score based on your real coding solutions ({problemsSolved} solved), interview sessions ({totalInterviews} completed), and resume review.
              </p>

              <button onClick={() => setReportOpen(!reportOpen)}>
                {reportOpen ? "Hide Breakdown" : "View Breakdown"}
              </button>
            </div>
          </div>

          {reportOpen && (
            <div className="report-preview glass-card">
              <div className="report-line">
                <span>Technical Accuracy</span>
                <strong>{technicalScore != null ? `${technicalScore}%` : "Needs Practice"}</strong>
              </div>

              <div className="report-line">
                <span>Problem Solving</span>
                <strong>{problemSolvingScore != null ? `${problemSolvingScore}%` : "No problems attempted"}</strong>
              </div>

              <div className="report-line">
                <span>Resume ATS Match</span>
                <strong>{resumeAtsScore != null ? `${resumeAtsScore}%` : "No resume"}</strong>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* BOTTOM SECTION: RECENT VERIFIED ACTIVITY */}
      <div className="performance-recent-section glass-card">
        <div className="performance-card-header">
          <div>
            <h3>Recent Verified Activity</h3>
            <p>Authentic submissions, coding attempts, and evaluation logs</p>
          </div>
          <button className="view-all-btn" onClick={() => navigate("/coding-arena")}>
            Open Coding Arena <FaArrowRight size={12} />
          </button>
        </div>

        <div className="performance-activity-list">
          {recentActivities && recentActivities.length > 0 ? (
            recentActivities.map((item, index) => (
              <div className="performance-activity-item" key={index}>
                <div className="activity-left">
                  <div className="activity-icon">
                    {item.type === "CODING" ? (
                      <FaCode />
                    ) : item.type === "INTERVIEW" ? (
                      <FaBrain />
                    ) : (
                      <FaFileAlt />
                    )}
                  </div>
                  <div>
                    <h4>{item.title}</h4>
                    <span className="activity-timestamp">{item.timestamp}</span>
                  </div>
                </div>

                <div className="activity-score-badge">
                  <span className={`status-tag status-${item.status.toLowerCase()}`}>
                    {item.status}
                  </span>
                  <span className="score-text">{item.score}</span>
                </div>
              </div>
            ))
          ) : (
            <div className="empty-activity-state">
              <FaLaptopCode size={36} color="#6366f1" />
              <h4>No Activity Records Logged Yet</h4>
              <p>Your accepted solutions, coding attempts, and interview sessions will automatically appear here.</p>
              <button className="performance-empty-action-btn" onClick={() => navigate("/coding-arena")}>
                Start Coding Now
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
