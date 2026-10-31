import brainImage from "../assets/brain.png";
import "../styles/home.css";
import Logo from "../assets/Logo.png";
import AuthModal from "../Components/Auth/AuthModal";
import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import samImage from "../assets//users/Sam.jpeg";
import adiImage from "../assets//users/Adi.jpeg";
import vanshImage from "../assets//users/Vansh.jpeg";

export default function Home() {
  const navigate = useNavigate();

  useEffect(() => {
    updatePageSEO({
      title: "Samprepix | AI Placement Platform for Computer Science Students",
      description: "Crack your dream software placement with AI-powered mock interviews, real-time coding arena, aptitude practice, and ATS resume analytics built for Computer Science students.",
      canonicalPath: "/"
    });
  }, []);
  return (
    <>
      <Navbar />


      {/* HERO SECTION */}

      <section className="hero-section">

        <div className="hero-content">

          <div className="hero-badge">
            ✦ Now with AI voice interviews · v2.0
          </div>

          <h1 className="hero-title">
            Crack your dream
            <br />
            <span className="gradient-text">placement</span> with AI
          </h1>

          <p className="hero-description">
            Practice realistic mock interviews, get instant resume
            feedback, and master coding rounds — all in one beautifully
            designed workspace built for Computer Science students.
          </p>

          <div className="hero-buttons">

            <button className="start-btn" onClick={() => navigate("/auth")}>
              Start preparing free →
            </button>

            <button className="demo-btn">
              Live demo
            </button>

          </div>

          <div className="hero-stats">
            <span>⭐⭐⭐⭐⭐</span>
            <span>Built for CS students</span>
            <span>Secure platform</span>
          </div>

        </div>

      </section>



      {/* AI SECTION */}

      <section className="ai-section">

        <div className="ai-wrapper">

          <div className="ai-container">

            {/* LEFT IMAGE */}

            <div className="ai-left">

              <div className="brain-box">

                <img
                  className="brain-image"
                  src={brainImage}
                  alt="AI Brain"
                />

              </div>

            </div>

            {/* RIGHT CARDS */}

            <div className="ai-right">

              <div className="ai-card">

                <div className="card-icon">🎤</div>

                <h3>AI Interviewer</h3>

                <p className="single-line">
                  Tell me about a time you led a project under pressure.
                </p>

                <div className="card-badge">
                  Listening • 0:42
                </div>

              </div>

              <div className="ai-card">

                <div className="card-icon">📄</div>

                <h3>ATS Score</h3>

                <p>
                  Resume scored 87/100
                </p>

                <div className="card-badge">
                  Improved from last scan
                </div>

              </div>

              <div className="ai-card">

                <div className="card-icon">{"</>"}</div>

                <h3>DSA Streak</h3>

                <p>
                  42 problems · 7 day streak
                </p>

                <div className="card-badge">
                  On fire
                </div>

              </div>

            </div>

          </div>

        </div>

      </section>

      {/* TRUSTED SECTION */}

      <section className="trusted-section">

        <p className="trusted-title">
          TOOLS AND WORKFLOWS DEVELOPERS LOVE
        </p>

        <div className="trusted-companies">

          <span>GitHub</span>
          <span>Vercel</span>
          <span>Figma</span>
          <span>Notion</span>
          <span>Linear</span>
          <span>Stripe</span>
          <span>Framer</span>
          <span>Raycast</span>

        </div>

      </section>
      {/* FEATURES SECTION */}

      <section className="features-section">

        <div className="features-heading">

          <h2>
            Everything you need. Nothing
            <br />
            you don’t.
          </h2>

          <p>
            A focused suite that turns prep time into placement offers.
          </p>

        </div>

        <div className="features-grid">

          {/* CARD 1 */}

          <div className="feature-card reveal-card">

            <div className="feature-icon">
              🎤
            </div>

            <h3>
              AI Mock Interviews
            </h3>

            <p>
              Voice-driven realistic interviews with instant scoring on clarity,
              content & confidence.
            </p>

          </div>

          {/* CARD 2 */}

          <div className="feature-card reveal-card">

            <div className="feature-icon">
              📄
            </div>

            <h3>
              Resume Analyzer
            </h3>

            <p>
              Get an ATS score, skill gaps, and rewrite suggestions in seconds.
            </p>

          </div>

          {/* CARD 3 */}

          <div className="feature-card reveal-card">

            <div className="feature-icon">
              {"</>"}
            </div>

            <h3>
              Coding Arena
            </h3>

            <p>
              LeetCode-style editor with curated problem sets and AI hints when stuck.
            </p>

          </div>

          {/* CARD 4 */}

          <div className="feature-card reveal-card">

            <div className="feature-icon">
              📉
            </div>

            <h3>
              Progress Analytics
            </h3>

            <p>
              Track interview scores, weak topics, and streaks over time.
            </p>

          </div>

          {/* CARD 5 */}

          <div className="feature-card reveal-card">

            <div className="feature-icon">
              ⚙️
            </div>

            <h3>
              Personalized Path
            </h3>

            <p>
              AI builds a daily plan based on your target role and timeline.
            </p>

          </div>

          {/* CARD 6 */}

          <div className="feature-card reveal-card">

            <div className="feature-icon">
              ⚡
            </div>

            <h3>
              Placement Tracks
            </h3>

            <p>
              Targeted practice kits aligned with software engineering and campus placement assessment patterns.
            </p>

          </div>

        </div>

      </section>

      {/* TESTIMONIAL SECTION */}

      <section className="testimonial-section">

        <div className="testimonial-heading">

          <h2>
            Loved by future engineers
          </h2>

        </div>

        <div className="testimonial-grid">

          {/* CARD 1 */}

          <div className="testimonial-card">

            <div className="stars">
              ★★★★★
            </div>

            <p className="testimonial-text">
              “The mock interviews actually helped me speak more confidently
              in real interview rounds.”
            </p>

            <div className="testimonial-user">

              <img
                src={samImage}
                alt="Sam"
                className="user-avatar"
              />

              <div>

                <h4>
                  Sam
                </h4>

                <span>
                  BCA Student
                </span>

              </div>

            </div>

          </div>

          {/* CARD 2 */}

          <div className="testimonial-card">

            <div className="stars">
              ★★★★★
            </div>

            <p className="testimonial-text">
              “Resume feedback was surprisingly useful. I fixed issues I never
              noticed before.”
            </p>

            <div className="testimonial-user">

                    <img
                      src={adiImage}
                      alt="Adi"
                      className="user-avatar"
                    />

              <div>

                <h4>
                  Adi
                </h4>

                <span>
                  Frontend Learner
                </span>

              </div>

            </div>

          </div>

          {/* CARD 3 */}

          <div className="testimonial-card">

            <div className="stars">
              ★★★★★
            </div>

            <p className="testimonial-text">
              “The UI feels super clean and the coding section is easy to use
              daily.”
            </p>

            <div className="testimonial-user">

              <img
                src={vanshImage}
                alt="Vansh"
                className="user-avatar"
              />

              <div>

                <h4>
                  Vansh
                </h4>

                <span>
                  DSA Enthusiast
                </span>

              </div>

            </div>

          </div>

        </div>

      </section>

      {/* PUBLIC PRICING / SERVICES SECTION (INR) */}
      <section className="pricing-preview-section" style={{ width: "100%", background: "transparent", padding: "40px 20px 80px" }}>
        <div style={{ maxWidth: "1140px", margin: "0 auto", textAlign: "center" }}>
          <div className="hero-badge" style={{ marginBottom: "16px" }}>
            ✦ Transparent Pricing in INR
          </div>
          <h2 style={{ fontSize: "clamp(2rem, 3.5vw, 2.75rem)", fontWeight: "800", color: "var(--text-color, #0f172a)", marginBottom: "14px", letterSpacing: "-1px" }}>
            Simple, Transparent Plans in INR
          </h2>
          <p style={{ maxWidth: "680px", margin: "0 auto 40px auto", fontSize: "16px", color: "var(--text-muted, #64748b)", lineHeight: "1.7" }}>
            Get started for free or upgrade to Pro or Elite for unlimited AI mock interviews, ATS resume diagnostics, and compiler problem sets. All subscription plans are priced and charged in Indian Rupees (INR).
          </p>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))", gap: "24px", textAlign: "left" }}>
            {/* STARTER */}
            <div className="feature-card" style={{ display: "flex", flexDirection: "column", justifyContent: "space-between", minHeight: "auto", padding: "32px 28px" }}>
              <div>
                <span style={{ display: "inline-block", fontSize: "12px", fontWeight: "700", color: "#6366f1", background: "rgba(99, 102, 241, 0.1)", padding: "4px 12px", borderRadius: "999px", marginBottom: "12px" }}>
                  Starter Plan
                </span>
                <div style={{ display: "flex", alignItems: "baseline", gap: "6px", marginBottom: "12px" }}>
                  <span style={{ fontSize: "2.5rem", fontWeight: "800", color: "var(--text-color, #0f172a)" }}>₹0</span>
                  <span style={{ fontSize: "0.95rem", color: "var(--text-muted, #64748b)" }}>/ Forever</span>
                </div>
                <p style={{ fontSize: "14px", color: "var(--text-muted, #64748b)", marginBottom: "20px", lineHeight: "1.6" }}>
                  Essential placement preparation toolkit to practice algorithms and assess your interview readiness.
                </p>
                <ul style={{ listStyle: "none", padding: 0, margin: "0 0 24px 0", fontSize: "14px", color: "var(--text-color, #334155)", display: "flex", flexDirection: "column", gap: "10px" }}>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>✓ Standard AI mock interview sessions</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>✓ 5,050+ DSA & SQL coding sandboxes</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>✓ 5 ATS resume scans & structural score</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>✓ 22,060 aptitude practice questions</li>
                </ul>
              </div>
              <button
                onClick={() => navigate("/auth")}
                style={{ width: "100%", padding: "12px", borderRadius: "12px", border: "1px solid #cbd5e1", background: "var(--card-bg, #ffffff)", color: "var(--text-color, #0f172a)", fontWeight: "700", cursor: "pointer", fontSize: "14px", transition: "all 0.2s" }}
              >
                Start Preparing Free →
              </button>
            </div>

            {/* PRO */}
            <div className="feature-card" style={{ display: "flex", flexDirection: "column", justifyContent: "space-between", minHeight: "auto", padding: "32px 28px", border: "2px solid #6366f1", position: "relative" }}>
              <div style={{ position: "absolute", top: "12px", right: "20px", background: "linear-gradient(135deg, #6366f1, #06b6d4)", color: "#fff", fontSize: "11px", fontWeight: "800", padding: "3px 10px", borderRadius: "999px", letterSpacing: "0.5px" }}>
                RECOMMENDED
              </div>
              <div>
                <span style={{ display: "inline-block", fontSize: "12px", fontWeight: "700", color: "#6366f1", background: "rgba(99, 102, 241, 0.1)", padding: "4px 12px", borderRadius: "999px", marginBottom: "12px" }}>
                  Pro Plan
                </span>
                <div style={{ display: "flex", alignItems: "baseline", gap: "6px", marginBottom: "12px" }}>
                  <span style={{ fontSize: "2.5rem", fontWeight: "800", color: "var(--text-color, #0f172a)" }}>₹399</span>
                  <span style={{ fontSize: "0.95rem", color: "var(--text-muted, #64748b)" }}>/ Month</span>
                </div>
                <p style={{ fontSize: "14px", color: "var(--text-muted, #64748b)", marginBottom: "20px", lineHeight: "1.6" }}>
                  Advanced placement preparation for students actively targeting software engineering and tech roles.
                </p>
                <ul style={{ listStyle: "none", padding: 0, margin: "0 0 24px 0", fontSize: "14px", color: "var(--text-color, #334155)", display: "flex", flexDirection: "column", gap: "10px" }}>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>⚡ Unlimited AI technical & behavioral interviews</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>⚡ ATS resume analyzer with role keyword matching</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>⚡ Multi-level AI code hints & GitHub sync</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>⚡ In-depth performance & readiness analytics</li>
                </ul>
              </div>
              <button
                onClick={() => navigate("/pricing")}
                style={{ width: "100%", padding: "12px", borderRadius: "12px", border: "none", background: "linear-gradient(135deg, #6366f1, #06b6d4)", color: "#ffffff", fontWeight: "700", cursor: "pointer", fontSize: "14px", transition: "all 0.2s" }}
              >
                Choose Pro Plan (₹399) →
              </button>
            </div>

            {/* ELITE */}
            <div className="feature-card" style={{ display: "flex", flexDirection: "column", justifyContent: "space-between", minHeight: "auto", padding: "32px 28px" }}>
              <div>
                <span style={{ display: "inline-block", fontSize: "12px", fontWeight: "700", color: "#059669", background: "rgba(5, 150, 105, 0.1)", padding: "4px 12px", borderRadius: "999px", marginBottom: "12px" }}>
                  Elite Plan
                </span>
                <div style={{ display: "flex", alignItems: "baseline", gap: "6px", marginBottom: "12px" }}>
                  <span style={{ fontSize: "2.5rem", fontWeight: "800", color: "var(--text-color, #0f172a)" }}>₹799</span>
                  <span style={{ fontSize: "0.95rem", color: "var(--text-muted, #64748b)" }}>/ Month</span>
                </div>
                <p style={{ fontSize: "14px", color: "var(--text-muted, #64748b)", marginBottom: "20px", lineHeight: "1.6" }}>
                  Maximum placement acceleration with company-focused tracks, priority compute, and custom roadmaps.
                </p>
                <ul style={{ listStyle: "none", padding: 0, margin: "0 0 24px 0", fontSize: "14px", color: "var(--text-color, #334155)", display: "flex", flexDirection: "column", gap: "10px" }}>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>👑 Priority AI inference & audio analysis</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>👑 Tier-1 company specific placement tracks</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>👑 Recruiter-ready GitHub profile analyzer</li>
                  <li style={{ display: "flex", alignItems: "center", gap: "8px" }}>👑 Personalized AI Career Roadmap</li>
                </ul>
              </div>
              <button
                onClick={() => navigate("/pricing")}
                style={{ width: "100%", padding: "12px", borderRadius: "12px", border: "1px solid #cbd5e1", background: "var(--card-bg, #ffffff)", color: "var(--text-color, #0f172a)", fontWeight: "700", cursor: "pointer", fontSize: "14px", transition: "all 0.2s" }}
              >
                Choose Elite Plan (₹799) →
              </button>
            </div>
          </div>

          <div style={{ marginTop: "32px", fontSize: "13px", color: "var(--text-muted, #64748b)" }}>
            SamPrepIX is operated by <strong>SAMIR KUMAR</strong>. All commercial subscription fees are processed securely in <strong>Indian Rupees (INR)</strong> via Cashfree Payment Gateway.
            <div style={{ marginTop: "8px" }}>
              <Link to="/pricing" style={{ color: "#4f46e5", fontWeight: "700", textDecoration: "underline", marginRight: "16px" }}>Compare All Features & Plans</Link>
              <Link to="/refund-policy" style={{ color: "var(--text-muted, #64748b)", textDecoration: "underline", marginRight: "16px" }}>Refund & Cancellation Policy</Link>
              <Link to="/terms-and-conditions" style={{ color: "var(--text-muted, #64748b)", textDecoration: "underline" }}>Terms & Conditions</Link>
            </div>
          </div>
        </div>
      </section>

      <section className="cta-section">

        <div className="cta-box">

          <h2>
            Ready to land the offer?
          </h2>

          <p>
            Join Computer Science students preparing smarter — not harder.
          </p>

          <button onClick={() => navigate("/auth")}>
            Get started for free →
          </button>

        </div>

      </section>

      {/* FOOTER */}
      <Footer />



    </>
  );
}