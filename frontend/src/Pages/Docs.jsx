import { useState, useEffect, useMemo } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import {
    FaSearch,
    FaBook,
    FaCode,
    FaGraduationCap,
    FaFileAlt,
    FaMicrophone,
    FaChartBar,
    FaSlidersH,
    FaUserShield,
    FaWrench,
    FaInfoCircle,
    FaLightbulb,
    FaExclamationTriangle,
    FaBars,
    FaQuestionCircle
} from "react-icons/fa";
import "../styles/docs.css";

// Comprehensive Documentation Topic Database
const DOC_SECTIONS = [
    {
        id: "faq",
        title: "Frequently Asked Questions",
        icon: <FaQuestionCircle />,
        topics: [
            {
                id: "platform-faq",
                title: "Platform & Placement FAQ",
                lead: "Everything you need to know about Samprepix features, coding evaluations, AI interviews, and accounts.",
                content: (
                    <div className="docs-faq-container">
                        <div className="faq-item">
                            <h3>1. What is Samprepix and how does it help in campus and tech placements?</h3>
                            <p>Samprepix is a unified interview and placement acceleration platform tailored for computer science students and software engineers. It replaces fragmented study materials with an authentic multi-language coding arena, aptitude modules, real-time AI voice mock interviews, ATS resume diagnostics, and GitHub portfolio intelligence.</p>
                        </div>
                        <div className="faq-item">
                            <h3>2. Which programming languages are supported in the Coding Arena?</h3>
                            <p>The Coding Arena supports 8 core languages: Java, Python, C++, C, JavaScript, TypeScript, Go, and Rust. Every submission executes inside an isolated compiler sandbox against verified standard input and hidden testcase suites with real-time compilation feedback.</p>
                        </div>
                        <div className="faq-item">
                            <h3>3. How does code execution and evaluation work?</h3>
                            <p>When you click &quot;Run Code&quot;, your solution is compiled and executed against public sample testcases. When you click &quot;Submit Solution&quot;, your code is tested against hidden boundary cases and memory constraints. Passing solutions can automatically synchronize to your personal GitHub repository.</p>
                        </div>
                        <div className="faq-item">
                            <h3>4. How do Multi-Level AI Hints work in the Coding Arena?</h3>
                            <p>When you encounter a roadblock, the platform provides three tiered hint levels: <strong>Level 1 (Concept)</strong> explains the underlying data structure or technique without revealing code; <strong>Level 2 (Approach)</strong> outlines the optimal time/space complexity strategy; and <strong>Level 3 (Solution Walkthrough)</strong> provides step-by-step logic breakdown.</p>
                        </div>
                        <div className="faq-item">
                            <h3>5. What is the GitHub Profile Analyzer and how is the score computed?</h3>
                            <p>The GitHub Profile Analyzer inspects your public GitHub profile and repository metrics to compute an objective 0–100 engineering readiness score based on 5 pillars: Repository Quality (0–25), Documentation &amp; READMEs (0–20), Consistency &amp; Activity (0–20), Tech Stack Diversity (0–20), and Project Impact (0–15).</p>
                        </div>
                        <div className="faq-item">
                            <h3>6. What is the Tailored Profile README generator?</h3>
                            <p>Inside the GitHub Analyzer, the README generator synthesizes your verified repositories, highlighted technologies, and contact links into a modern, recruiter-ready markdown template that you can copy directly to your special GitHub profile repository (e.g. <code>username/username</code>).</p>
                        </div>
                        <div className="faq-item">
                            <h3>7. How does the AI Personalized Roadmap work?</h3>
                            <p>The AI Roadmap generates an 8-to-9 phase progressive engineering curriculum customized to your target role (Full-Stack, Backend, Frontend, AI/ML, DevOps, Mobile, Data Engineering, or Cybersecurity). You can track phase milestones, check off completed concepts, gain XP points, and export your roadmap as a printable PDF report.</p>
                        </div>
                        <div className="faq-item">
                            <h3>8. How do AI Mock Interviews work?</h3>
                            <p>AI Mock Interviews simulate real technical and HR interviews with speech-to-text recognition, dynamic AI follow-up questions tailored to your responses, and real-time audio analysis. You can answer via microphone or text, and the system assesses both technical accuracy and behavioral clarity.</p>
                        </div>
                        <div className="faq-item">
                            <h3>9. What feedback is provided after a mock interview?</h3>
                            <p>After concluding an interview, the platform generates a comprehensive scorecard featuring: Overall Readiness Score, Technical Accuracy percentage, Communication &amp; Articulation score, granular strengths, prioritized improvement areas, and a question-by-question transcript analysis.</p>
                        </div>
                        <div className="faq-item">
                            <h3>10. How does the ATS Resume Analyzer score resumes?</h3>
                            <p>The ATS Resume Analyzer parses your uploaded PDF resume and inspects text structure, section completeness (Contact, Education, Experience, Projects, Skills), quantified achievement bullet points, and keyword alignment with target software engineering job descriptions.</p>
                        </div>
                        <div className="faq-item">
                            <h3>11. What is the difference between Starter, PRO, and ELITE plans?</h3>
                            <p><strong>Starter (Free):</strong> Access to core problem sets, foundation roadmap phases, and basic tests.<br /><strong>PRO:</strong> Unrestricted access to all 5,050+ coding problems, complete AI roadmap phases, resume analyzer, and GitHub sync.<br /><strong>ELITE:</strong> Everything in PRO plus unlimited AI Mock Interviews, video/audio analytics, priority hints, and dedicated performance reports.</p>
                        </div>
                        <div className="faq-item">
                            <h3>12. What are the current subscription prices on the platform?</h3>
                            <p>The platform currently uses sandbox test pricing: <strong>PRO is ₹1</strong> and <strong>ELITE is ₹2</strong>. This allows candidates to test and experience the full suite of premium features seamlessly.</p>
                        </div>
                        <div className="faq-item">
                            <h3>13. What payment methods are supported for subscriptions?</h3>
                            <p>Subscriptions are processed through secure 256-bit encrypted payment gateways supporting UPI (Google Pay, PhonePe, Paytm), Credit/Debit Cards, Net Banking, and digital wallets.</p>
                        </div>
                        <div className="faq-item">
                            <h3>14. How does the Aptitude assessment module help in campus drives?</h3>
                            <p>Most initial campus screening rounds include an aptitude elimination test. Samprepix provides 22,060+ practice questions across Quantitative Aptitude, Logical Reasoning, Verbal Ability, and Technical MCQs with timed mock tests and category-level accuracy telemetry.</p>
                        </div>
                        <div className="faq-item">
                            <h3>15. How do I report a problem or submit feedback?</h3>
                            <p>You can report an issue anytime by opening your Profile dropdown and clicking &quot;Report Problem&quot;, or by clicking &quot;Report an Issue or Bug&quot; inside the AI Help Bot in your workspace. You can categorize the issue, attach details, and our engineering team receives the ticket immediately.</p>
                        </div>
                    </div>
                )
            }
        ]
    },
    {
        id: "career-intelligence",
        title: "Career Intelligence & AI",
        icon: <FaChartBar />,
        topics: [
            {
                id: "github-analyzer",
                title: "GitHub Profile Analyzer",
                lead: "Automated intelligence and audits for your public GitHub developer presence.",
                content: (
                    <>
                        <h2>Overview</h2>
                        <p>
                            The GitHub Profile Analyzer scans your public GitHub profile and repositories to provide an objective 0–100 engineering score, granular deduction breakdowns, repository health checks, and a recruiter-ready profile README generator.
                        </p>
                        <h2>5 Core Scoring Pillars</h2>
                        <ul>
                            <li><strong>Repository Quality (0–25):</strong> Evaluates commit depth, descriptive titles, license presence, and clean project structure.</li>
                            <li><strong>Documentation & README (0–20):</strong> Audits project READMEs for clear installation steps, architecture diagrams, and usage examples.</li>
                            <li><strong>Consistency & Activity (0–20):</strong> Analyzes commit regularity and active contribution cadences over time.</li>
                            <li><strong>Technology Diversity (0–20):</strong> Rewards multi-stack versatility across languages, frameworks, and tools.</li>
                            <li><strong>Project Impact (0–15):</strong> Measures community stars, forks, and deployment links.</li>
                        </ul>
                        <h2>Tailored README Generator</h2>
                        <p>
                            Generates an optimized, recruiter-friendly markdown profile README synthesizing your best repositories, verified skills, and contact details with one-click clipboard copy.
                        </p>
                    </>
                )
            },
            {
                id: "ai-roadmap",
                title: "AI Personalized Roadmap",
                lead: "Adaptive, milestone-driven curriculum tailored to your domain and skills.",
                content: (
                    <>
                        <h2>Curated Engineering Tracks</h2>
                        <p>
                            Choose from 8 specialized engineering tracks: Full-Stack Web, Backend Systems, Frontend & UI/UX, AI/ML Engineering, DevOps & Cloud, Mobile App, Data Engineering, and Cybersecurity.
                        </p>
                        <h2>Phases & Progressive XP</h2>
                        <p>
                            Each track contains 8 to 9 structured progression phases from Core Foundations to Production & System Design. Toggle completed milestones to earn XP, level up your developer rank, and export your roadmap to PDF.
                        </p>
                    </>
                )
            },
            {
                id: "premium-entitlement",
                title: "Plans & Premium Access",
                lead: "Understanding Starter, PRO, and ELITE subscription tiers.",
                content: (
                    <>
                        <h2>Tier Comparison</h2>
                        <p>
                            <strong>Starter:</strong> Access to core problem sets, foundational roadmap phases, and basic assessments.<br />
                            <strong>PRO & ELITE:</strong> Unrestricted access to all 5,050+ problems, complete AI roadmap phases, deep GitHub audit & recruiter view, priority AI interview compute, and detailed analytics.
                        </p>
                    </>
                )
            }
        ]
    },

    {
        id: "getting-started",
        title: "Getting Started",
        icon: <FaBook />,
        topics: [
            {
                id: "introduction",
                title: "Introduction to Samprepix",
                lead: "Welcome to Samprepix, an integrated platform engineered to prepare software developers for technical campus placements and product-company interviews.",
                content: (
                    <>
                        <h2>What is Samprepix?</h2>
                        <p>
                            Samprepix is a unified career acceleration ecosystem. It combines an authentic multi-language coding arena, full-length aptitude assessments, AI-driven speech interviews, and automated ATS resume analysis.
                        </p>
                        <div className="docs-callout tip">
                            <FaLightbulb className="docs-callout-icon" />
                            <div className="docs-callout-content">
                                <strong>Core Mission:</strong> To replace fragmented, passive study with authentic simulation. Every problem you solve runs in a genuine compiler sandbox, and every interview you complete is scored against standard technical hiring rubrics.
                            </div>
                        </div>
                        <h2>Key Platform Capabilities</h2>
                        <ul>
                            <li><strong>Coding Arena:</strong> 5,050+ algorithmic problems and 1,200+ SQL challenges across 50+ programming runtimes and MySQL sandboxes.</li>
                            <li><strong>Aptitude Training:</strong> 22,060 curated questions covering Quantitative, Logical, Verbal, and Technical domains.</li>
                            <li><strong>AI Mock Interviews:</strong> Real-time voice simulations evaluating technical clarity, behavioral reasoning, and algorithmic articulation.</li>
                            <li><strong>ATS Resume Analyzer:</strong> Instant parsing that scores structural formatting, role keyword density, and technical competence.</li>
                            <li><strong>GitHub Sync:</strong> Automatic synchronization of passing code solutions directly to your personal GitHub repository.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "quick-start",
                title: "Quick Start Guide",
                lead: "Get up and running with Samprepix in under five minutes.",
                content: (
                    <>
                        <h2>1. Create Your Account</h2>
                        <p>
                            Sign up with your academic or professional email address. After signing in, complete the brief onboarding survey to specify your current graduation timeline, target role, and preferred programming languages.
                        </p>
                        <h2>2. Configure Your Preferences</h2>
                        <p>
                            Open <strong>Settings</strong> from your topbar or profile dropdown. Set your <em>Preferred Coding Language</em> (e.g., Python, Java, C++, JavaScript). This language will automatically be pre-selected whenever you open any problem in the Coding Arena.
                        </p>
                        <h2>3. Solve Your First Problem</h2>
                        <p>
                            Navigate to the <strong>Coding Arena</strong>. Choose problem #1 (Two Sum). Select your preferred language, review the starter template, write your solution, and click <strong>Run Code</strong> to test sample inputs. When confident, click <strong>Submit Solution</strong>.
                        </p>
                        <h2>4. Benchmark Your Aptitude</h2>
                        <p>
                            Head to <strong>Aptitude</strong>, pick a category (e.g. Quantitative or Logical), and start a 10-question practice set to calibrate your speed and accuracy.
                        </p>
                    </>
                )
            },
            {
                id: "platform-overview",
                title: "Platform Overview",
                lead: "An architectural tour of the primary modules available across the platform.",
                content: (
                    <>
                        <h2>Module Structure</h2>
                        <p>
                            Samprepix is organized into five dedicated functional zones accessible from the global navigation sidebar:
                        </p>
                        <ul>
                            <li><strong>Dashboard:</strong> Centralized hub displaying readiness indicators, daily activity streaks, recent submissions, and quick navigation shortcuts.</li>
                            <li><strong>Coding Arena:</strong> Full-featured IDE with split-pane problem statements, test runner, input/output console, and multi-tiered hints.</li>
                            <li><strong>Aptitude Hub:</strong> Standardized test-taking interface featuring syllabus tracking, category filters, and timed question palettes.</li>
                            <li><strong>AI Mock Interview:</strong> Interactive conversational voice room with real-time speech synthesis, pacing timers, and comprehensive scorecards.</li>
                            <li><strong>Resume Analyzer:</strong> Document parser accepting PDF and Word formats for instant ATS compatibility auditing.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "account-profile",
                title: "Account & Profile Setup",
                lead: "Personalizing your engineering candidate profile and visibility.",
                content: (
                    <>
                        <h2>Profile Completeness</h2>
                        <p>
                            Your candidate profile summarizes your academic background, degree, branch, graduation year, technical skills, and connected accounts (such as GitHub and LinkedIn).
                        </p>
                        <h2>Updating Skills</h2>
                        <p>
                            Navigate to the <strong>Profile</strong> tab in the dashboard. You can add or update your primary programming proficiencies, frameworks, database proficiencies, and developer tooling. These skills inform the role-matching heuristics in the Resume Analyzer.
                        </p>
                    </>
                )
            },
            {
                id: "dashboard",
                title: "Navigating the Dashboard",
                lead: "Understanding your central command center and performance summaries.",
                content: (
                    <>
                        <h2>Dashboard Metrics</h2>
                        <p>
                            The dashboard synthesizes your holistic readiness across three core verticals:
                        </p>
                        <ul>
                            <li><strong>Placement Readiness Score:</strong> A composite 0–100% metric combining coding accuracy, aptitude benchmark scores, and mock interview ratings.</li>
                            <li><strong>Problems Solved:</strong> Breakdown across Easy, Medium, and Hard algorithmic challenges plus Database queries.</li>
                            <li><strong>Aptitude Accuracy:</strong> Historical ratio of correct answers across all completed assessment sessions.</li>
                            <li><strong>Interview Average:</strong> Cumulative average score from completed AI technical and behavioral interviews.</li>
                        </ul>
                    </>
                )
            }
        ]
    },
    {
        id: "core-platform",
        title: "Core Platform",
        icon: <FaCode />,
        topics: [
            {
                id: "coding-arena",
                title: "Coding Arena Guide",
                lead: "Mastering the live programming environment and problem workbench.",
                content: (
                    <>
                        <h2>The Arena Workspace</h2>
                        <p>
                            The Coding Arena provides an institutional-grade algorithmic workbench. The left pane contains the full problem description, constraints, public examples, and topic tags. The right pane hosts the Monaco code editor, language selector, execution action bar, and test results console.
                        </p>
                        <div className="docs-callout important">
                            <FaInfoCircle className="docs-callout-icon" />
                            <div className="docs-callout-content">
                                <strong>Real Runtimes:</strong> Your code is executed inside secure, isolated Linux sandboxes. Standard library imports (e.g. `java.util.*`, `math`, `collections`, `algorithm`) are fully available.
                            </div>
                        </div>
                    </>
                )
            },
            {
                id: "finding-problems",
                title: "Finding & Filtering Problems",
                lead: "Browsing and selecting challenges from our 5,050+ problem catalog.",
                content: (
                    <>
                        <h2>Problem Menu</h2>
                        <p>
                            Click the problem header in the Coding Arena topbar to open the problem drawer. You can browse problems with instant real-time search:
                        </p>
                        <ul>
                            <li><strong>Category Filter:</strong> Switch between <em>All Problems</em>, <em>DSA / Algorithms</em> (5,050+ problems), and <em>Database / SQL</em> (1,200+ queries).</li>
                            <li><strong>Difficulty Filter:</strong> Filter by Easy, Medium, or Hard difficulty ratings.</li>
                            <li><strong>Topic Tags:</strong> Select algorithmic topics like Dynamic Programming, Graphs, Trees, Strings, Arrays, Backtracking, or Binary Search.</li>
                            <li><strong>Full-Text Search:</strong> Search by problem title, number, or concept keywords.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "programming-languages",
                title: "Programming Languages",
                lead: "Supported languages, compilers, and runtime environments.",
                content: (
                    <>
                        <h2>Supported Language Runtimes</h2>
                        <p>
                            Samprepix supports 50 compiled and interpreted programming languages plus MySQL for database challenges. Every language includes syntax highlighting, bracket matching, and starter templates:
                        </p>
                        <div className="docs-pill-list">
                            {["Python 3.10+", "Java 21", "C++ 20 (GCC)", "C 17", "JavaScript (Node.js)", "TypeScript", "Go", "Rust", "C# (.NET)", "Swift", "Kotlin", "Scala", "Ruby", "PHP", "Bash", "MySQL 8.0"].map((l) => (
                                <span key={l} className="docs-lang-pill">{l}</span>
                            ))}
                        </div>
                        <h2>Default Language Precedence</h2>
                        <p>
                            The platform automatically resolves your editor language using a deterministic 4-step hierarchy:
                        </p>
                        <ol>
                            <li><strong>Database Problem Isolation:</strong> When opening a Database/SQL challenge, the language is strictly locked to MySQL.</li>
                            <li><strong>Active Session Override:</strong> If you explicitly pick a language for the current problem, that choice is respected for that problem only.</li>
                            <li><strong>Saved User Preference:</strong> Your configured preference in Settings (e.g., Python) takes absolute priority when opening fresh problems or logging in.</li>
                            <li><strong>Platform Fallback:</strong> If no preference is set, Python or the problem&apos;s first supported runtime is used.</li>
                        </ol>
                    </>
                )
            },
            {
                id: "code-editor",
                title: "Code Editor & Customization",
                lead: "Customizing Monaco font size, autocomplete, and editor layout.",
                content: (
                    <>
                        <h2>Editor Preferences</h2>
                        <p>
                            You can customize your editor ergonomics directly inside the <strong>Settings</strong> modal:
                        </p>
                        <ul>
                            <li><strong>Font Size:</strong> Choose between 13px, 14px, 15px, or 16px.</li>
                            <li><strong>IntelliSense & Autocomplete:</strong> Enable or disable keyword suggestions, parameter hints, and word-based completions.</li>
                            <li><strong>Theme Integration:</strong> The editor automatically synchronizes with the system Dark and Light appearance theme.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "run-and-test",
                title: "Run vs. Submit Execution Model",
                lead: "Understanding the difference between sample runs and official test submissions.",
                content: (
                    <>
                        <h2>Run Code (Public Sample Validation)</h2>
                        <p>
                            Clicking <strong>Run Code</strong> compiles and executes your code exclusively against the public sample test cases shown in the problem description.
                        </p>
                        <ul>
                            <li>Safe for rapid debugging, printing debug statements, and testing edge cases.</li>
                            <li>Does NOT alter your problem completion status or acceptance metrics.</li>
                            <li>Provides stdout/stderr output and execution duration.</li>
                        </ul>
                        <h2>Submit Solution (Full Evaluation)</h2>
                        <p>
                            Clicking <strong>Submit Solution</strong> runs your code against the entire test suite, including extensive private, hidden edge cases designed to test time and memory bounds.
                        </p>
                        <div className="docs-callout warning">
                            <FaExclamationTriangle className="docs-callout-icon" />
                            <div className="docs-callout-content">
                                <strong>Passing Run Prerequisite:</strong> To prevent accidental blank submissions, <em>Submit</em> requires that your code has successfully passed public sample cases in a recent run.
                            </div>
                        </div>
                    </>
                )
            },
            {
                id: "submit-and-verdicts",
                title: "Submit & Verdict Definitions",
                lead: "Decoding compiler verdicts and execution outcomes.",
                content: (
                    <>
                        <h2>Standard Verdicts</h2>
                        <ul>
                            <li><strong>Accepted (Pass):</strong> Your solution produced correct outputs for 100% of public and hidden test cases within all resource limits.</li>
                            <li><strong>Wrong Answer (WA):</strong> The output produced by your program did not match the expected canonical answer for one or more test cases.</li>
                            <li><strong>Time Limit Exceeded (TLE):</strong> Your code exceeded the maximum allowed wall-clock execution time (typically 2 to 5 seconds depending on language). Check for infinite loops or suboptimal Big-O complexity.</li>
                            <li><strong>Memory Limit Exceeded (MLE):</strong> Your program allocated more memory than permitted (typically 256MB to 512MB).</li>
                            <li><strong>Runtime Error (RE):</strong> An unhandled exception was thrown during execution (e.g., null pointer, division by zero, index out of bounds).</li>
                            <li><strong>Compilation Error (CE):</strong> Syntax or type check error prevented the program from compiling. Detailed compiler error messages are displayed in the console.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "hidden-tests",
                title: "Hidden Test Cases & Integrity",
                lead: "Why test cases are hidden and how to test for boundary conditions.",
                content: (
                    <>
                        <h2>Why Are Some Tests Hidden?</h2>
                        <p>
                            In real campus coding tests and technical screening rounds, comprehensive test suites are hidden to ensure genuine algorithmic problem-solving rather than hardcoded edge-case handling.
                        </p>
                        <h2>Common Hidden Pitfalls</h2>
                        <ul>
                            <li>Empty arrays, single-element collections, or null pointers.</li>
                            <li>Integer overflow when multiplying large values (use 64-bit integers where applicable).</li>
                            <li>Extremes of input bounds (e.g. $N = 10^5$ with quadratic $O(N^2)$ algorithms causing TLE).</li>
                            <li>Negative numbers, duplicated elements, or un-ordered sequences.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "coding-progress",
                title: "Coding Progress & Streaks",
                lead: "How completed problems, streaks, and submissions are tracked.",
                content: (
                    <>
                        <h2>Persistence & Progress Tracking</h2>
                        <p>
                            Whenever a problem receives an <strong>Accepted</strong> verdict, the completion is permanently recorded to your user account. Your solved count increments, and your last active problem and language are updated.
                        </p>
                        <h2>Code Persistence</h2>
                        <p>
                            Your editor code is automatically cached per problem and per language in local storage. Returning to a problem will restore your draft code for each respective language.
                        </p>
                    </>
                )
            },
            {
                id: "sql-database",
                title: "SQL & Database Practice",
                lead: "Querying live MySQL 8.0 relational sandboxes.",
                content: (
                    <>
                        <h2>Database Sandboxes</h2>
                        <p>
                            The Database category features 1,200+ relational challenges covering SELECT queries, multi-table JOINs, subqueries, GROUP BY aggregations, and window functions.
                        </p>
                        <h2>Execution Behavior</h2>
                        <p>
                            Each SQL problem automatically provisions schema tables and sample data. Your query is executed against a genuine MySQL 8.0 instance, and the returned tabular result set is compared against expected rows and columns.
                        </p>
                    </>
                )
            },
            {
                id: "ai-hint",
                title: "Multi-Level AI Hints",
                lead: "Getting unblocked with progressive algorithmic guidance.",
                content: (
                    <>
                        <h2>Progressive Assistance Tiers</h2>
                        <p>
                            Stuck on a tricky problem? Click <strong>AI Hint</strong> in the editor toolbar. Hints are organized into three structured levels:
                        </p>
                        <ol>
                            <li><strong>Level 1 (Concept):</strong> Identifies the core algorithmic paradigm (e.g., Two Pointers, Monotonic Stack, Dijkstra) without spoiling the implementation.</li>
                            <li><strong>Level 2 (Approach):</strong> Outlines the step-by-step logic, state transitions, and edge cases to consider.</li>
                            <li><strong>Level 3 (Solution Walkthrough):</strong> Provides pseudo-code and optimal time/space complexity targets.</li>
                        </ol>
                        <div className="docs-callout tip">
                            <FaLightbulb className="docs-callout-icon" />
                            <div className="docs-callout-content">
                                <strong>Anti-Cheat Cooldown:</strong> To promote independent problem-solving, a brief 30-second cooldown is observed between consecutive hint generations.
                            </div>
                        </div>
                    </>
                )
            },
            {
                id: "github-sync",
                title: "GitHub Solution Synchronization",
                lead: "Automatically syncing your verified solutions to your personal GitHub repository.",
                content: (
                    <>
                        <h2>Automated Portfolio Building</h2>
                        <p>
                            Samprepix can automatically commit your passing solutions directly to a dedicated GitHub repository of your choice.
                        </p>
                        <h2>How It Works</h2>
                        <ol>
                            <li>Connect your GitHub account in <strong>Settings</strong> or the Arena topbar.</li>
                            <li>Upon receiving an <strong>Accepted</strong> verdict on a problem submission, the platform structures your solution file (e.g. `coding-solutions/two-sum/Solution.py`).</li>
                            <li>The solution is committed with a clear commit message detailing the problem title and timestamp. Duplicate solutions are automatically recognized to prevent noisy commits.</li>
                            <li>If a network error occurs during synchronization, a <strong>Retry Sync</strong> button is available in the Arena topbar.</li>
                        </ol>
                    </>
                )
            }
        ]
    },
    {
        id: "aptitude",
        title: "Aptitude Training",
        icon: <FaGraduationCap />,
        topics: [
            {
                id: "aptitude-overview",
                title: "Aptitude Training Overview",
                lead: "Mastering the high-elimination screening tests for campus placements.",
                content: (
                    <>
                        <h2>Why Aptitude Matters</h2>
                        <p>
                            A substantial portion of candidates are eliminated in preliminary online aptitude screening assessments during campus recruitment. Rigorous preparation across quantitative, logical, and verbal tracks is essential to clear this initial benchmark.
                        </p>
                        <p>
                            Samprepix provides 22,060 questions calibrated across four critical assessment tracks to ensure speed, formula fluency, and logical deduction.
                        </p>
                    </>
                )
            },
            {
                id: "aptitude-tracks",
                title: "Syllabus & Practice Tracks",
                lead: "Exploring our four standardized aptitude syllabus domains.",
                content: (
                    <>
                        <h2>1. Quantitative Aptitude</h2>
                        <p>Percentages, Profit and Loss, Simple & Compound Interest, Time & Work, Speed Distance Time, Probability, Permutations & Combinations, Number Systems, Ratio & Proportion.</p>
                        <h2>2. Logical Reasoning</h2>
                        <p>Syllogisms, Blood Relations, Seating Arrangements, Coding-Decoding, Series Completion, Direction Sense, Data Sufficiency.</p>
                        <h2>3. Verbal Ability</h2>
                        <p>Sentence Correction, Error Spotting, Reading Comprehension, Synonyms & Antonyms, Idioms, Para Jumbles.</p>
                        <h2>4. Technical Aptitude</h2>
                        <p>Object-Oriented Programming principles, Data Structures fundamentals, Operating Systems concepts (deadlocks, paging), Computer Networks (OSI layers, TCP/UDP), and Database normalization.</p>
                    </>
                )
            },
            {
                id: "aptitude-assessments",
                title: "Taking Timed Assessments",
                lead: "Navigating the assessment environment, timers, and question palettes.",
                content: (
                    <>
                        <h2>Assessment Interface</h2>
                        <p>
                            When launching an assessment:
                        </p>
                        <ul>
                            <li><strong>Live Countdown Timer:</strong> Tracks remaining session time to enforce pacing discipline.</li>
                            <li><strong>Question Palette:</strong> Visual grid showing answered, unvisited, and flagged-for-review questions.</li>
                            <li><strong>Instant Feedback:</strong> After completing the test, detailed step-by-step mathematical explanations are provided for every question.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "aptitude-results",
                title: "Results & Accuracy Breakdown",
                lead: "Analyzing topic accuracy, time per question, and improvement areas.",
                content: (
                    <>
                        <h2>Post-Test Scorecard</h2>
                        <p>
                            Every completed test produces an in-depth scorecard showing your raw score, overall accuracy percentage, average response latency per question, and sub-topic mastery badges.
                        </p>
                    </>
                )
            },
            {
                id: "aptitude-past-attempts",
                title: "Reviewing Past Attempts",
                lead: "Tracking longitudinal performance across all assessment sessions.",
                content: (
                    <>
                        <h2>Historical Archives</h2>
                        <p>
                            Past attempts are archived in your Aptitude dashboard. You can re-open any past session to review incorrect choices, re-read solution derivations, and measure whether your accuracy has improved over successive weeks.
                        </p>
                    </>
                )
            }
        ]
    },
    {
        id: "resume",
        title: "Resume Analyzer",
        icon: <FaFileAlt />,
        topics: [
            {
                id: "resume-analyzer",
                title: "Resume Analyzer Overview",
                lead: "Understanding Applicant Tracking System (ATS) optimization.",
                content: (
                    <>
                        <h2>Why ATS Optimization Is Critical</h2>
                        <p>
                            Large recruiters use automated Applicant Tracking Systems to filter incoming resumes before a recruiter ever reviews them. Resumes with multi-column tables, unsupported fonts, or missing technical keywords are routinely discarded.
                        </p>
                    </>
                )
            },
            {
                id: "resume-analysis",
                title: "Uploading & Parsing Guidelines",
                lead: "Best practices for document formats and parsing fidelity.",
                content: (
                    <>
                        <h2>Supported Document Formats</h2>
                        <p>
                            Samprepix accepts standard `.pdf` and `.docx` (Microsoft Word) documents up to 10MB.
                        </p>
                        <h2>Formatting Best Practices</h2>
                        <ul>
                            <li>Use a clean, single-column linear layout.</li>
                            <li>Use standard section headers: Experience, Education, Projects, Technical Skills.</li>
                            <li>Avoid graphic charts, embedded text boxes, or icon-based skill bars.</li>
                            <li>Ensure text is highlightable and selectable.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "resume-results",
                title: "Scoring & Scored Feedback",
                lead: "Interpreting your structural score and role keyword matches.",
                content: (
                    <>
                        <h2>Score Breakdown</h2>
                        <ul>
                            <li><strong>Overall ATS Score:</strong> Composite rating from 0 to 100 based on structural readability and density.</li>
                            <li><strong>Skills Identified:</strong> Catalog of recognized languages, frameworks, databases, and developer tools.</li>
                            <li><strong>Missing Critical Keywords:</strong> High-value keywords commonly required for your target engineering role that are absent from your resume.</li>
                            <li><strong>Actionable Recommendations:</strong> Concrete bullet points on rewording passive sentences into quantifiable achievements.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "ats-insights",
                title: "ATS Insights & Formatting Tips",
                lead: "Writing high-impact bullet points with quantifiable results.",
                content: (
                    <>
                        <h2>The Google X-Y-Z Formula</h2>
                        <p>
                            Transform passive bullet points into impactful engineering evidence using the formula:
                            <br />
                            <em>&quot;Accomplished [X], as measured by [Y], by doing [Z].&quot;</em>
                        </p>
                        <p>
                            Example: <em>&quot;Reduced API response latency by 35% (as measured by Datadog) by implementing a Redis caching layer and indexing PostgreSQL foreign keys.&quot;</em>
                        </p>
                    </>
                )
            }
        ]
    },
    {
        id: "mock-interview",
        title: "Mock Interview",
        icon: <FaMicrophone />,
        topics: [
            {
                id: "mock-interview-overview",
                title: "AI Mock Interview System Overview",
                lead: "Realistic conversational interview simulations with real-time feedback.",
                content: (
                    <>
                        <h2>Speech-Driven Practice</h2>
                        <p>
                            The AI Mock Interview module replicates a live technical or HR round. The AI interviewer speaks questions aloud using natural speech synthesis, listens to your verbal explanations via speech recognition, and asks adaptive follow-up inquiries based on your answers.
                        </p>
                    </>
                )
            },
            {
                id: "interview-flow",
                title: "Interview Flow & Controls",
                lead: "How a mock interview session is conducted from start to finish.",
                content: (
                    <>
                        <h2>Session Stages</h2>
                        <ol>
                            <li><strong>Setup:</strong> Select role focus (e.g. Frontend, Backend, Full Stack, SDE-1) and interview round type (Technical or HR/Behavioral).</li>
                            <li><strong>Interactive Dialogue:</strong> The AI poses a question. Speak your response clearly. An audio orb visualizer reflects voice activity.</li>
                            <li><strong>Adaptive Follow-ups:</strong> The AI asks clarifying questions, probes edge cases, or asks you to justify algorithmic choices.</li>
                            <li><strong>Conclusion:</strong> After completing the scheduled questions, your transcript is analyzed and scored.</li>
                        </ol>
                    </>
                )
            },
            {
                id: "ai-evaluation",
                title: "Scoring Rubric & Evaluation",
                lead: "How responses are evaluated for technical depth and clarity.",
                content: (
                    <>
                        <h2>Grading Dimensions</h2>
                        <ul>
                            <li><strong>Technical Accuracy (40%):</strong> Correctness of conceptual definitions, algorithmic logic, and system trade-offs.</li>
                            <li><strong>Communication Clarity (30%):</strong> Structured delivery, conciseness, and effective use of engineering vocabulary.</li>
                            <li><strong>Problem-Solving Methodology (20%):</strong> Breaking problems down systematically before jumping to conclusions.</li>
                            <li><strong>Pacing & Confidence (10%):</strong> Speaking cadence, pause frequency, and composed delivery.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "interview-results",
                title: "Reviewing Interview Scorecards",
                lead: "Accessing detailed question transcripts and performance reports.",
                content: (
                    <>
                        <h2>Comprehensive Transcript Review</h2>
                        <p>
                            Every completed interview generates a permanent report card displaying your overall score, positive highlights, specific technical gaps, and suggested model answers for every question asked during the session.
                        </p>
                    </>
                )
            }
        ]
    },
    {
        id: "performance",
        title: "Performance & Analytics",
        icon: <FaChartBar />,
        topics: [
            {
                id: "dashboard-analytics",
                title: "Placement Readiness Score",
                lead: "Understanding the mathematical model behind your readiness index.",
                content: (
                    <>
                        <h2>Holistic Readiness Index</h2>
                        <p>
                            Your Placement Readiness Score is a dynamic indicator combining your problem-solving breadth, code acceptance rates, aptitude test consistency, and interview rubric scores into an actionable percentage.
                        </p>
                    </>
                )
            },
            {
                id: "coding-analytics",
                title: "Algorithmic Mastery Tracking",
                lead: "Visualizing problem distributions across difficulty and topics.",
                content: (
                    <>
                        <h2>Topic Mastery Breakdown</h2>
                        <p>
                            The Analytics page charts your solved problems across Easy, Medium, and Hard tiers, displaying radar charts of topic strengths (e.g., Arrays vs. Graphs) so you can target under-practiced areas.
                        </p>
                    </>
                )
            },
            {
                id: "aptitude-analytics",
                title: "Speed & Accuracy Benchmarks",
                lead: "Tracking response time and accuracy trends over time.",
                content: (
                    <>
                        <h2>Time vs. Accuracy Quadrant</h2>
                        <p>
                            Identifies whether errors stem from conceptual misunderstanding or time-pressure rushing. Helps calibrate when to skip vs. solve complex multi-step quantitative problems.
                        </p>
                    </>
                )
            },
            {
                id: "interview-analytics",
                title: "Communication & Evaluation Trends",
                lead: "Reviewing your progress across successive interview rounds.",
                content: (
                    <>
                        <h2>Score Trajectory</h2>
                        <p>
                            Examines your rubric performance over multiple sessions to verify that your technical clarity, confidence, and pacing improve consistently as campus drive season approaches.
                        </p>
                    </>
                )
            },
            {
                id: "resume-insights",
                title: "Resume Keyword Coverage History",
                lead: "Tracking keyword optimization across resume revisions.",
                content: (
                    <>
                        <h2>Version Comparisons</h2>
                        <p>
                            Review historical resume scans to ensure newly acquired skills and verified projects are reflected in your ATS compatibility scores.
                        </p>
                    </>
                )
            }
        ]
    },
    {
        id: "personalization",
        title: "Personalization & Settings",
        icon: <FaSlidersH />,
        topics: [
            {
                id: "profile-settings",
                title: "Editing Profile & Academic Details",
                lead: "Keeping your graduation year, university, and bio updated.",
                content: (
                    <>
                        <h2>Updating Profile Fields</h2>
                        <p>
                            From the <strong>Profile</strong> view, you can edit your display name, username, bio, college/university name, degree, and graduation year. These details customize your dashboard benchmarks against peers graduating in the same year.
                        </p>
                    </>
                )
            },
            {
                id: "settings-preferences",
                title: "Settings & Editor Preferences",
                lead: "Configuring default language, font size, and autocomplete.",
                content: (
                    <>
                        <h2>Preferred Coding Language</h2>
                        <p>
                            In the <strong>Settings</strong> modal (accessible via topbar gear icon), configure your Preferred Coding Language. This choice is saved locally and determines the default language across all Coding Arena problems.
                        </p>
                        <div className="docs-callout important">
                            <FaInfoCircle className="docs-callout-icon" />
                            <div className="docs-callout-content">
                                <strong>Transactional Settings:</strong> Changes made in the settings dialog are draft state. Clicking <strong>Done</strong> commits the changes. Clicking Cancel, closing, or pressing Escape rolls back drafts cleanly.
                            </div>
                        </div>
                    </>
                )
            },
            {
                id: "notifications-panel",
                title: "In-App Notifications & Alerts",
                lead: "Receiving system updates, achievement badges, and placement notices.",
                content: (
                    <>
                        <h2>Notification Center</h2>
                        <p>
                            The bell icon in the topbar reveals your notification drawer. Notifications include problem milestone alerts, streak achievements, assessment reminders, and platform updates.
                        </p>
                    </>
                )
            },
            {
                id: "appearance-theme",
                title: "Appearance & Theme Switching",
                lead: "Switching between Dark and Light mode across all views.",
                content: (
                    <>
                        <h2>Theme Toggle</h2>
                        <p>
                            Click the Sun/Moon icon in the topbar or navbar to toggle between high-contrast Light mode and eye-friendly Dark mode. Your selection is remembered across browser sessions.
                        </p>
                    </>
                )
            },
            {
                id: "accessibility",
                title: "Accessibility & Reduced Motion",
                lead: "Keyboard navigation, contrast standards, and animation controls.",
                content: (
                    <>
                        <h2>Accessible Navigation</h2>
                        <p>
                            Samprepix adheres to WCAG 2.1 AA guidelines with semantic heading hierarchies, visible focus rings, ARIA roles on modal dialogs, and support for the `prefers-reduced-motion` media query.
                        </p>
                    </>
                )
            },
            {
                id: "audio-preferences",
                title: "Audio & Sound Effects",
                lead: "Managing chime feedback and synthesized voice playback.",
                content: (
                    <>
                        <h2>Sound Feedback</h2>
                        <p>
                            Subtle chimes accompany test successes and interview turn completions. You can adjust volume or mute audio feedback directly through your browser media permissions.
                        </p>
                    </>
                )
            }
        ]
    },
    {
        id: "account",
        title: "Account & Security",
        icon: <FaUserShield />,
        topics: [
            {
                id: "account-management",
                title: "Account Management & Passwords",
                lead: "Managing authentication, password resets, and sessions.",
                content: (
                    <>
                        <h2>Password Resets</h2>
                        <p>
                            If you forget your password, click &quot;Forgot Password&quot; on the Login page. A secure reset token will be delivered to your registered email address allowing you to establish a new password.
                        </p>
                    </>
                )
            },
            {
                id: "privacy-policy",
                title: "Privacy & Data Protection",
                lead: "How your personal information and code are safeguarded.",
                content: (
                    <>
                        <h2>Strict Data Isolation</h2>
                        <p>
                            We do not sell, rent, or share candidate source code, resume files, or interview audio recordings with unauthorized third parties. All user content remains strictly your property.
                        </p>
                    </>
                )
            },
            {
                id: "security-standards",
                title: "Security Standards",
                lead: "Authentication architecture and communication security.",
                content: (
                    <>
                        <h2>Transport & Session Security</h2>
                        <p>
                            All network communication is conducted over encrypted HTTPS/TLS channels. User sessions utilize industry-standard cryptographic bearer tokens with automatic expiration and protected storage.
                        </p>
                    </>
                )
            },
            {
                id: "data-ownership",
                title: "Data Ownership & Deletion",
                lead: "Exporting your progress and requesting account deletion.",
                content: (
                    <>
                        <h2>Right to Erasure</h2>
                        <p>
                            Candidates have full control over their account data. You may request complete deletion of your account, history, submissions, and stored resumes at any time by contacting samirprajapat5@gmail.com.
                        </p>
                    </>
                )
            }
        ]
    },
    {
        id: "troubleshooting",
        title: "Troubleshooting",
        icon: <FaWrench />,
        topics: [
            {
                id: "common-issues",
                title: "Common Issues & Quick Fixes",
                lead: "Fast solutions to frequent candidate questions.",
                content: (
                    <>
                        <h2>Quick Checklist</h2>
                        <ul>
                            <li><strong>Page not loading fresh state?</strong> Perform a hard reload (Ctrl+F5 on Windows, Cmd+Shift+R on Mac) to refresh cached frontend assets.</li>
                            <li><strong>Logged out unexpectedly?</strong> Authentication sessions expire after extended inactivity for security; simply sign in again.</li>
                            <li><strong>Audio not recording during mock interview?</strong> Check that your browser has granted microphone permission to the site.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "coding-issues",
                title: "Coding Execution & Sandbox Issues",
                lead: "Resolving compiler timeouts, memory errors, and template resets.",
                content: (
                    <>
                        <h2>Time Limit Exceeded (TLE)</h2>
                        <p>
                            If your solution triggers TLE, your algorithm&apos;s computational complexity is too high for the input bounds (e.g. $O(N^2)$ on $N = 10^5$). Refactor to use hashmaps ($O(1)$ lookup), two pointers, or binary search ($O(\log N)$).
                        </p>
                        <h2>Resetting Starter Code</h2>
                        <p>
                            To restore the original problem template, select another language in the dropdown and switch back, or click the code reset icon in the editor action bar.
                        </p>
                    </>
                )
            },
            {
                id: "ai-issues",
                title: "AI Hint & Interview Latency",
                lead: "What to do if AI hints or voice evaluations take longer than expected.",
                content: (
                    <>
                        <h2>Response Latency</h2>
                        <p>
                            AI hint generation and interview transcript scoring typically complete within 2 to 4 seconds. Under high platform traffic, processing may take up to 8 seconds. Please avoid repeatedly clicking the hint trigger during an active request.
                        </p>
                    </>
                )
            },
            {
                id: "github-issues",
                title: "GitHub Synchronization Troubleshooting",
                lead: "Resolving repository permission errors and missed commits.",
                content: (
                    <>
                        <h2>Common GitHub Sync Checks</h2>
                        <ul>
                            <li>Verify that your designated solution repository exists on GitHub and is accessible by your account.</li>
                            <li>Ensure your OAuth authorization has not been revoked in your GitHub user settings.</li>
                            <li>Click <strong>Retry Sync</strong> in the Coding Arena topbar if a network hiccup prevented the commit from completing.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "resume-issues",
                title: "Resume Upload & Parsing Issues",
                lead: "Handling unreadable fonts, image-only PDFs, and file size limits.",
                content: (
                    <>
                        <h2>Resolving Parse Errors</h2>
                        <ul>
                            <li><strong>Scanned Image PDFs:</strong> The ATS analyzer requires selectable digital text. Scanned images or flattened graphics will fail keyword extraction.</li>
                            <li><strong>Password-Protected Files:</strong> Remove any encryption or permissions passwords before uploading.</li>
                            <li><strong>File Size Exceeded:</strong> Ensure the document is under 10MB by removing uncompressed header images.</li>
                        </ul>
                    </>
                )
            },
            {
                id: "account-issues",
                title: "Account & Password Recovery Issues",
                lead: "Troubleshooting email verification, login failures, and token expiration.",
                content: (
                    <>
                        <h2>Password Reset Emails</h2>
                        <p>
                            Password reset tokens are valid for 15 minutes. If you do not see the reset email, check your Spam or Promotions folder. If issues persist, contact samirprajapat5@gmail.com.
                        </p>
                    </>
                )
            }
        ]
    }
];

export default function Docs() {
    const [searchQuery, setSearchQuery] = useState("");
    const [activeTopicId, setActiveTopicId] = useState("introduction");
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    // Deep link support via hash or search param
    useEffect(() => {
        const hash = window.location.hash?.replace("#", "");
        const params = new URLSearchParams(window.location.search);
        const topicParam = params.get("topic") || hash;

        if (topicParam) {
            // Find topic in sections
            for (const sec of DOC_SECTIONS) {
                const found = sec.topics.find((t) => t.id === topicParam);
                if (found) {
                    setActiveTopicId(found.id);
                    break;
                }
            }
        }
    }, []);

    // Filtered topics based on search
    const filteredSections = useMemo(() => {
        const q = searchQuery.trim().toLowerCase();
        if (!q) return DOC_SECTIONS;

        return DOC_SECTIONS.map((sec) => ({
            ...sec,
            topics: sec.topics.filter(
                (t) =>
                    t.title.toLowerCase().includes(q) ||
                    t.lead.toLowerCase().includes(q) ||
                    sec.title.toLowerCase().includes(q)
            )
        })).filter((sec) => sec.topics.length > 0);
    }, [searchQuery]);

    // Exact topic count calculations
    const totalTopicsCount = useMemo(() => {
        return DOC_SECTIONS.reduce((acc, sec) => acc + sec.topics.length, 0);
    }, []);

    const filteredTopicsCount = useMemo(() => {
        return filteredSections.reduce((acc, sec) => acc + sec.topics.length, 0);
    }, [filteredSections]);

    // Active topic data
    const activeTopic = useMemo(() => {
        for (const sec of DOC_SECTIONS) {
            const found = sec.topics.find((t) => t.id === activeTopicId);
            if (found) {
                return { ...found, sectionTitle: sec.title };
            }
        }
        return DOC_SECTIONS[0].topics[0];
    }, [activeTopicId]);

    // Update SEO dynamically when active topic changes
    useEffect(() => {
        if (activeTopic) {
            updatePageSEO({
                title: `${activeTopic.title} | Samprepix Docs`,
                description: activeTopic.lead,
                canonicalPath: `/docs?topic=${activeTopic.id}`
            });
            trackPageView(`/docs?topic=${activeTopic.id}`, `${activeTopic.title} | Docs`);
        }
    }, [activeTopic]);

    const handleSelectTopic = (topicId) => {
        setActiveTopicId(topicId);
        setMobileMenuOpen(false);
        window.history.replaceState(null, "", `/docs?topic=${topicId}`);
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    return (
        <div className="docs-page">
            <Navbar />

            <div className="docs-layout">
                {/* MOBILE MENU TOGGLE */}
                <button
                    type="button"
                    className="docs-mobile-toggle"
                    onClick={() => setMobileMenuOpen((p) => !p)}
                    aria-label="Toggle Documentation Navigation Menu"
                >
                    <span>
                        <FaBars style={{ marginRight: "8px" }} />
                        {activeTopic?.sectionTitle} &gt; {activeTopic?.title}
                    </span>
                    <span>{mobileMenuOpen ? "Close ▲" : "Menu ▼"}</span>
                </button>

                {/* SIDEBAR NAVIGATION */}
                <aside className={`docs-sidebar ${mobileMenuOpen ? "open" : ""}`} role="navigation" aria-label="Documentation Navigation">
                    <div className="docs-search-box">
                        <FaSearch className="docs-search-icon" />
                        <input
                            type="text"
                            placeholder={`Search ${totalTopicsCount} documentation topics...`}
                            className="docs-search-input"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            aria-label="Search documentation topics"
                        />
                    </div>
                    {searchQuery.trim() && (
                        <div style={{ fontSize: "0.78rem", color: "#64748b", margin: "-12px 0 16px 4px", fontWeight: "600" }}>
                            Showing {filteredTopicsCount} of {totalTopicsCount} topics
                        </div>
                    )}

                    {filteredSections.map((sec) => (
                        <div key={sec.id} className="docs-nav-group">
                            <div className="docs-group-title">
                                {sec.title}
                            </div>
                            {sec.topics.map((t) => (
                                <button
                                    key={t.id}
                                    type="button"
                                    className={`docs-nav-item ${activeTopicId === t.id ? "active" : ""}`}
                                    onClick={() => handleSelectTopic(t.id)}
                                    aria-current={activeTopicId === t.id ? "page" : undefined}
                                >
                                    {t.title}
                                </button>
                            ))}
                        </div>
                    ))}
                </aside>

                {/* MAIN READING CONTENT PANE */}
                <main className="docs-content" role="main">
                    {activeTopic && (
                        <article>
                            <div className="docs-header">
                                <span className="docs-cat-badge">
                                    {activeTopic.sectionTitle}
                                </span>
                                <h1 className="docs-title">
                                    {activeTopic.title}
                                </h1>
                                <p className="docs-lead">
                                    {activeTopic.lead}
                                </p>
                            </div>

                            <div className="docs-body-prose">
                                {activeTopic.content}
                            </div>
                        </article>
                    )}
                </main>
            </div>

            <Footer />
        </div>
    );
}
