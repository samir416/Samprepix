import Navbar from "../Components/Common/Navbar";
import "../styles/home.css";

export default function Home() {
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
            designed workspace.
          </p>

          <div className="hero-buttons">

            <button className="start-btn">
              Start preparing free →
            </button>

            <button className="demo-btn">
              Live demo
            </button>

          </div>

          <div className="hero-stats">
            <span>⭐⭐⭐⭐⭐</span>
            <span>Built for students</span>
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
                  className="brain-image" src={brainImage}
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

    </>
  );
}