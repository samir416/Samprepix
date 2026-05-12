import Navbar from "../Components/Common/Navbar";
import brainImage from "../assets/brain.png";
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

                    <div className="feature-card">

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

                    <div className="feature-card">

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

                    <div className="feature-card">

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

                    <div className="feature-card">

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

                    <div className="feature-card">

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

                    <div className="feature-card">

                        <div className="feature-icon">
                            ⚡
                        </div>

                        <h3>
                            Company Packs
                        </h3>

                        <p>
                            Targeted prep kits for Google, Meta, Amazon and 100+ companies.
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

        <div className="user-avatar"></div>

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

        <div className="user-avatar"></div>

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

        <div className="user-avatar"></div>

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

        </>
    );
}