import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
    FiCompass,
    FiCheck,
    FiLock,
    FiAward,
    FiDownload,
    FiRefreshCw,
    FiChevronRight,
    FiClock,
    FiLayers,
    FiZap,
    FiX,
    FiArrowRight,
    FiLayout,
    FiServer,
    FiShield,
    FiFolder,
    FiCheckCircle,
    FiSearch,
    FiRotateCcw,
    FiCpu,
    FiTerminal,
    FiExternalLink
} from "react-icons/fi";
import {
    FaJava,
    FaPython,
    FaReact,
    FaBrain,
    FaDocker,
    FaMobileAlt,
    FaShieldAlt,
    FaGamepad
} from "react-icons/fa";
import jsPDF from "jspdf";
import {
    getAiRoadmap,
    switchRoadmapTrack,
    toggleRoadmapMilestone,
    getTrackSuggestions
} from "../services/aiRoadmapService";
import "../styles/aiRoadmap.css";

const TRACK_META = {
    "java-fullstack": { icon: <FaJava size={22} color="#f89820" />, badge: "Spring Boot & React", category: "Full Stack" },
    "python-fullstack": { icon: <FaPython size={22} color="#38bdf8" />, badge: "FastAPI & Django", category: "Full Stack" },
    "mern": { icon: <FaReact size={22} color="#61dafb" />, badge: "Node & MongoDB", category: "JavaScript" },
    "frontend": { icon: <FiLayout size={22} color="#38bdf8" />, badge: "React & Next.js", category: "Frontend" },
    "backend": { icon: <FiServer size={22} color="#a855f7" />, badge: "Distributed Systems", category: "Backend" },
    "ai-data": { icon: <FaBrain size={22} color="#ec4899" />, badge: "PyTorch & Data", category: "AI / ML" },
    "devops-cloud": { icon: <FaDocker size={22} color="#2496ed" />, badge: "K8s & Terraform", category: "Cloud & SRE" },
    "mobile": { icon: <FaMobileAlt size={22} color="#10b981" />, badge: "Flutter & Dart", category: "Mobile" },
    "custom": { icon: <FiCompass size={22} color="#8b5cf6" />, badge: "Custom Engineering", category: "Specialized" }
};

const DEFAULT_SUGGESTIONS = [
    "Cybersecurity Specialist",
    "Flutter Developer",
    "Android Kotlin Developer",
    "Data Analyst & Business Intelligence",
    "Machine Learning Engineer",
    "Game Developer (Unity & C#)",
    "Cloud & DevOps Architect",
    "Golang Microservices Engineer"
];

const TRACK_CATALOG = [
    "Java Full Stack Developer",
    "Python Full Stack Developer",
    "MERN Stack Engineer",
    "Modern Frontend Engineer",
    "Scalable Backend Engineer",
    "AI & Data Science Engineer",
    "DevOps & Cloud Architect",
    "Cross-Platform Mobile Developer",
    "Flutter Developer",
    "Cybersecurity Specialist",
    "Android Kotlin Developer",
    "Data Analyst & Business Intelligence",
    "Machine Learning Engineer",
    "Game Developer (Unity & C#)",
    "Cloud & DevOps Architect",
    "Golang Microservices Engineer",
    "System Design & Distributed Systems"
];

const getMilestoneFeatureAction = (m) => {
    const text = `${m.title || ""} ${m.description || ""} ${(m.skills || []).join(" ")} ${m.recommendedProject || ""}`.toLowerCase();
    if (text.includes("dsa") || text.includes("algorithm") || text.includes("coding") || text.includes("trees") || text.includes("graphs") || text.includes("array") || text.includes("arena")) {
        return { label: "Practice in Coding Arena", route: "/coding-arena", icon: <FiTerminal size={12} /> };
    }
    if (text.includes("interview") || text.includes("behavioral") || text.includes("mock") || text.includes("system design") || text.includes("architecture")) {
        return { label: "Launch AI Interview", route: "/mock-interview", icon: <FiExternalLink size={12} /> };
    }
    if (text.includes("aptitude") || text.includes("quant") || text.includes("logic") || text.includes("verbal") || text.includes("reasoning")) {
        return { label: "Practice Aptitude", route: "/aptitude", icon: <FiAward size={12} /> };
    }
    if (text.includes("resume") || text.includes("ats")) {
        return { label: "Scan in ATS Analyzer", route: "/resume-analyzer", icon: <FiExternalLink size={12} /> };
    }
    if (text.includes("github") || text.includes("git ") || text.includes("repository") || text.includes("open source")) {
        return { label: "Analyze in GitHub Analyzer", route: "/github-analyzer", icon: <FiFolder size={12} /> };
    }
    return { label: "Practice in Arena", route: "/coding-arena", icon: <FiTerminal size={12} /> };
};

const FALLBACK_ROADMAP = {
    trackId: "java-fullstack",
    trackTitle: "Java Full Stack Developer",
    detectedTrack: "Java Full Stack Developer",
    description: "An industry-aligned AI progression architecture taking you from foundational engineering paradigms to tier-1 placement mastery.",
    totalMilestones: 12,
    completedMilestonesCount: 0,
    progressPercentage: 0,
    totalXp: 1045,
    earnedXp: 0,
    currentLevel: "Level 1: Foundation Explorer",
    premium: false,
    effectivePlan: "FREE",
    availableTracks: [
        { id: "java-fullstack", title: "Java Full Stack Developer", description: "Spring Boot, React, MySQL, Microservices & JPA" },
        { id: "python-fullstack", title: "Python Full Stack Developer", description: "Django, FastAPI, React, PostgreSQL & Cloud" },
        { id: "mern", title: "MERN Stack Engineer", description: "MongoDB, Express, React, Node.js & REST APIs" },
        { id: "frontend", title: "Modern Frontend Engineer", description: "React, TypeScript, Next.js, Tailwind & Performance" },
        { id: "backend", title: "Scalable Backend Engineer", description: "Distributed Systems, Caching, SQL/NoSQL & System Design" },
        { id: "ai-data", title: "AI & Data Science Engineer", description: "Python, Machine Learning, Deep Learning, SQL & Pandas" },
        { id: "devops-cloud", title: "DevOps & Cloud Architect", description: "Docker, Kubernetes, AWS, CI/CD Pipelines & Terraform" },
        { id: "mobile", title: "Cross-Platform Mobile Developer", description: "Flutter, Dart, Firebase, State Management & Mobile UX" }
    ],
    phases: [
        {
            phaseId: "jf-p1",
            phaseNumber: 1,
            title: "Foundation & Modern Java",
            subtitle: "Core Java, OOP Principles & Streams API",
            locked: false,
            milestones: [
                { id: "jf-m1", title: "Java 17/21 Syntax & OOP", description: "Encapsulation, Inheritance, Polymorphism, Records, and Pattern Matching.", estimatedTime: "6 hours", xp: 50, skills: ["Java 21", "OOP", "Clean Code"], recommendedProject: "Object-oriented banking CLI with validation rules", completed: false, locked: false },
                { id: "jf-m2", title: "Collections & Streams API", description: "List, Map, Set, Stream filtering, mapping, collectors, and concurrency.", estimatedTime: "8 hours", xp: 75, skills: ["Java Collections", "Streams API", "Lambdas"], recommendedProject: "High-throughput data filtering pipeline", completed: false, locked: false }
            ]
        },
        {
            phaseId: "jf-p2",
            phaseNumber: 2,
            title: "Data Structures & Algorithms",
            subtitle: "Algorithm Optimization & Placement Problem Solving",
            locked: false,
            milestones: [
                { id: "jf-m3", title: "Linear & Non-Linear Structures", description: "Arrays, Strings, LinkedLists, Trees, Heaps, and Graphs in Java.", estimatedTime: "12 hours", xp: 80, skills: ["DSA", "Trees", "Graphs"], recommendedProject: "Binary Search Tree visualizer and balance tester", completed: false, locked: false },
                { id: "jf-m4", title: "Coding Arena Placement Sprint", description: "Solve 50+ Easy, Medium, and Hard problems in the Samprepix Arena.", estimatedTime: "15 hours", xp: 100, skills: ["Algorithms", "DP", "Time Complexity"], recommendedProject: "Achieve 5-day streak in Coding Arena", completed: false, locked: false }
            ]
        },
        {
            phaseId: "jf-p3",
            phaseNumber: 3,
            title: "Database Engineering & JPA",
            subtitle: "Relational Modeling, Hibernate & Spring Data",
            locked: true,
            milestones: [
                { id: "jf-m5", title: "MySQL & Relational Design", description: "Normalization, Complex Joins, Composite Indexes, and Query Plans.", estimatedTime: "8 hours", xp: 75, skills: ["MySQL", "SQL", "Indexing"], recommendedProject: "Database schema design for a multi-tenant university portal", completed: false, locked: true },
                { id: "jf-m6", title: "Hibernate & Spring Data JPA", description: "Entity relationships, Lazy Loading, N+1 query problem, and transactions.", estimatedTime: "10 hours", xp: 85, skills: ["Spring Data JPA", "Hibernate", "Transactions"], recommendedProject: "Repository layer with optimized JPQL queries", completed: false, locked: true }
            ]
        },
        {
            phaseId: "jf-p4",
            phaseNumber: 4,
            title: "Spring Boot & RESTful Microservices",
            subtitle: "Enterprise API Architecture & Validation",
            locked: true,
            milestones: [
                { id: "jf-m7", title: "Spring Boot 3 Core & Architecture", description: "Dependency Injection, Controllers, Services, DTOs, and global exception handling.", estimatedTime: "10 hours", xp: 90, skills: ["Spring Boot", "REST APIs", "Validation"], recommendedProject: "Production-grade RESTful API with OpenAPI/Swagger docs", completed: false, locked: true }
            ]
        }
    ]
};

export default function AIRoadmap() {
    const navigate = useNavigate();
    const phaseRefs = useRef({});

    const [roadmap, setRoadmap] = useState(null);
    const [loading, setLoading] = useState(true);
    const [switchingTrack, setSwitchingTrack] = useState(false);
    const [showTrackModal, setShowTrackModal] = useState(false);
    const [showCustomInput, setShowCustomInput] = useState(false);
    const [customTrackInput, setCustomTrackInput] = useState("");
    const [suggestions, setSuggestions] = useState(DEFAULT_SUGGESTIONS);
    const [highlightedIndex, setHighlightedIndex] = useState(-1);
    const [loadingSuggestions, setLoadingSuggestions] = useState(false);
    const [togglingMilestoneId, setTogglingMilestoneId] = useState(null);
    const [exportingPdf, setExportingPdf] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [notice, setNotice] = useState("");
    const [activePhaseIndex, setActivePhaseIndex] = useState(0);

    const handleRefreshRoadmap = async () => {
        if (refreshing || loading) return;
        try {
            setRefreshing(true);
            const data = await getAiRoadmap();
            if (data && data.phases && data.phases.length > 0) {
                setRoadmap(data);
                setNotice("Curriculum successfully refreshed and synchronized with latest platform activities.");
            } else {
                setRoadmap(FALLBACK_ROADMAP);
                setNotice("Curriculum synchronized with latest baseline.");
            }
        } catch (err) {
            console.warn("Failed to refresh roadmap:", err);
            setNotice("Curriculum synchronized with latest baseline.");
        } finally {
            setRefreshing(false);
        }
    };

    const fetchRoadmap = async () => {
        try {
            setLoading(true);
            const data = await getAiRoadmap();
            if (data && data.phases && data.phases.length > 0) {
                setRoadmap(data);
                setNotice("Roadmap aligned with your verified profile, resume ATS records, and coding history.");
            } else {
                setRoadmap(FALLBACK_ROADMAP);
                setNotice("Baseline curriculum loaded. Complete placement milestones to synchronize live progress.");
            }
        } catch (err) {
            console.warn("Backend roadmap sync notice:", err);
            setRoadmap(FALLBACK_ROADMAP);
            setNotice("Baseline curriculum loaded. Complete placement milestones to synchronize live progress.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRoadmap();
    }, []);

    // Dynamic suggestions query with authoritative track catalog prefix/substring matching
    useEffect(() => {
        let isMounted = true;
        const query = customTrackInput.trim().toLowerCase();

        if (query) {
            const localMatches = TRACK_CATALOG.filter(t => t.toLowerCase().includes(query));
            if (localMatches.length > 0) {
                setSuggestions(localMatches);
            }
        } else {
            setSuggestions(DEFAULT_SUGGESTIONS);
        }
        setHighlightedIndex(-1);

        const fetchSuggestions = async () => {
            try {
                setLoadingSuggestions(true);
                const data = await getTrackSuggestions(customTrackInput);
                if (isMounted && Array.isArray(data) && data.length > 0) {
                    const combined = Array.from(new Set([
                        ...(query ? TRACK_CATALOG.filter(t => t.toLowerCase().includes(query)) : []),
                        ...data
                    ]));
                    setSuggestions(combined.slice(0, 10));
                }
            } catch (err) {
                if (isMounted && !query) setSuggestions(DEFAULT_SUGGESTIONS);
            } finally {
                if (isMounted) setLoadingSuggestions(false);
            }
        };

        const timer = setTimeout(fetchSuggestions, 180);
        return () => {
            isMounted = false;
            clearTimeout(timer);
        };
    }, [customTrackInput]);

    const handleSwitchTrack = async (trackId, customTitle = null) => {
        try {
            setSwitchingTrack(true);
            const updated = await switchRoadmapTrack(trackId, customTitle);
            if (updated && updated.phases) {
                setRoadmap(updated);
            }
            setShowTrackModal(false);
            setShowCustomInput(false);
            setCustomTrackInput("");
            setHighlightedIndex(-1);
            setActivePhaseIndex(0);
        } catch (err) {
            console.warn("Track switch fallback:", err);
            const displayTitle = customTitle || (trackId === "custom" ? "Custom Career Track" : trackId.replace("-", " ").toUpperCase() + " Track");
            setRoadmap((prev) => ({
                ...(prev || FALLBACK_ROADMAP),
                trackId,
                trackTitle: displayTitle
            }));
            setShowTrackModal(false);
            setShowCustomInput(false);
            setCustomTrackInput("");
            setHighlightedIndex(-1);
        } finally {
            setSwitchingTrack(false);
        }
    };

    const handleToggleMilestone = async (phaseLocked, milestoneId) => {
        if (phaseLocked || !roadmap || togglingMilestoneId) return;
        try {
            setTogglingMilestoneId(milestoneId);
            const updated = await toggleRoadmapMilestone(roadmap.trackId, milestoneId);
            if (updated && updated.phases) {
                setRoadmap(updated);
            }
        } catch (err) {
            console.error("Failed to toggle milestone:", err);
        } finally {
            setTogglingMilestoneId(null);
        }
    };

    const handleCustomInputKeyDown = (e) => {
        if (e.key === "ArrowDown") {
            e.preventDefault();
            setHighlightedIndex(prev => (prev < suggestions.length - 1 ? prev + 1 : 0));
        } else if (e.key === "ArrowUp") {
            e.preventDefault();
            setHighlightedIndex(prev => (prev > 0 ? prev - 1 : suggestions.length - 1));
        } else if (e.key === "Enter") {
            e.preventDefault();
            if (highlightedIndex >= 0 && suggestions[highlightedIndex]) {
                const selected = suggestions[highlightedIndex];
                setCustomTrackInput(selected);
                handleSwitchTrack("custom", selected);
            } else if (customTrackInput.trim()) {
                handleSwitchTrack("custom", customTrackInput.trim());
            }
        } else if (e.key === "Escape") {
            e.preventDefault();
            setShowCustomInput(false);
        }
    };

    const scrollToPhase = (index) => {
        setActivePhaseIndex(index);
        const ref = phaseRefs.current[index];
        if (ref) {
            ref.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    };

    const handleExportPdf = () => {
        if (!roadmap) return;
        setExportingPdf(true);

        try {
            const doc = new jsPDF({
                orientation: "portrait",
                unit: "pt",
                format: "a4"
            });

            const primaryColor = [99, 102, 241];
            const darkColor = [15, 23, 42];
            const mutedColor = [100, 116, 139];

            // Header Banner
            doc.setFillColor(primaryColor[0], primaryColor[1], primaryColor[2]);
            doc.rect(0, 0, 595, 80, "F");

            doc.setTextColor(255, 255, 255);
            doc.setFont("helvetica", "bold");
            doc.setFontSize(22);
            doc.text("SAMPREPIX AI CAREER ROADMAP", 40, 42);

            doc.setFont("helvetica", "normal");
            doc.setFontSize(10);
            doc.text(`Target Track: ${roadmap.trackTitle || "Software Engineering"} | Generated: ${new Date().toLocaleDateString()}`, 40, 62);

            // Overview Section
            let y = 110;
            doc.setTextColor(darkColor[0], darkColor[1], darkColor[2]);
            doc.setFont("helvetica", "bold");
            doc.setFontSize(14);
            doc.text("ROADMAP PROGRESSION OVERVIEW", 40, y);

            y += 20;
            doc.setFont("helvetica", "normal");
            doc.setFontSize(10);
            doc.setTextColor(mutedColor[0], mutedColor[1], mutedColor[2]);

            const progressPct = roadmap.progressPercentage || 0;
            const completedCount = roadmap.completedMilestonesCount || 0;
            const totalCount = roadmap.totalMilestones || 0;
            const earnedXp = roadmap.earnedXp || 0;
            const totalXp = roadmap.totalXp || 0;

            doc.text(`Current Level: ${roadmap.currentLevel || "Level 1: Foundation Explorer"}`, 40, y);
            y += 14;
            doc.text(`Progression: ${progressPct}% (${completedCount}/${totalCount} Milestones Completed)`, 40, y);
            y += 14;
            doc.text(`Experience Points: ${earnedXp} / ${totalXp} XP`, 40, y);

            y += 25;
            doc.setDrawColor(226, 232, 240);
            doc.line(40, y, 555, y);
            y += 20;

            // Phases & Milestones
            if (roadmap.phases && roadmap.phases.length > 0) {
                roadmap.phases.forEach((phase) => {
                    if (y > 720) {
                        doc.addPage();
                        y = 50;
                    }

                    doc.setFont("helvetica", "bold");
                    doc.setFontSize(12);
                    doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
                    doc.text(`PHASE ${phase.phaseNumber}: ${phase.title.toUpperCase()} ${phase.locked ? "[PRO ONLY]" : ""}`, 40, y);

                    y += 15;
                    doc.setFont("helvetica", "italic");
                    doc.setFontSize(9);
                    doc.setTextColor(mutedColor[0], mutedColor[1], mutedColor[2]);
                    doc.text(phase.subtitle, 40, y);

                    y += 16;

                    if (phase.milestones && phase.milestones.length > 0) {
                        phase.milestones.forEach((m) => {
                            if (y > 740) {
                                doc.addPage();
                                y = 50;
                            }

                            doc.setFont("helvetica", "bold");
                            doc.setFontSize(10);
                            doc.setTextColor(darkColor[0], darkColor[1], darkColor[2]);

                            const statusMark = m.completed ? "[x] " : "[ ] ";
                            doc.text(`${statusMark}${m.title} (+${m.xp} XP)`, 50, y);

                            y += 12;
                            doc.setFont("helvetica", "normal");
                            doc.setFontSize(9);
                            doc.setTextColor(mutedColor[0], mutedColor[1], mutedColor[2]);

                            const splitDesc = doc.splitTextToSize(m.description, 480);
                            doc.text(splitDesc, 50, y);
                            y += splitDesc.length * 11 + 4;

                            if (m.recommendedProject) {
                                doc.setFont("helvetica", "bold");
                                doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
                                doc.text(`Project: ${m.recommendedProject}`, 50, y);
                                y += 14;
                            }

                            y += 6;
                        });
                    }

                    y += 12;
                });
            }

            // Footer note
            doc.setFont("helvetica", "normal");
            doc.setFontSize(8);
            doc.setTextColor(mutedColor[0], mutedColor[1], mutedColor[2]);
            doc.text("Official placement preparation document - generated by Samprepix AI Engine", 40, 800);

            doc.save(`Samprepix_AI_Roadmap_${(roadmap.trackTitle || "Track").replace(/\s+/g, "_")}.pdf`);
        } catch (error) {
            console.error("PDF generation failed:", error);
            window.print();
        } finally {
            setExportingPdf(false);
        }
    };

    if (loading && !roadmap) {
        return (
            <div className="ai-roadmap-page">
                <div className="air-loading-container">
                    <div className="air-spinner" />
                    <h3>Synthesizing AI Placement Roadmap...</h3>
                    <p>Compiling curriculum phases, project requirements, and industry benchmarks.</p>
                </div>
            </div>
        );
    }

    const progressValue = roadmap?.progressPercentage ?? 0;
    const isNotStarted = progressValue === 0 && (!roadmap?.completedMilestonesCount || roadmap.completedMilestonesCount === 0);

    // Compute active step coordinates
    const currentPhase = roadmap?.phases?.find(p => !p.locked && p.milestones?.some(m => !m.completed)) || roadmap?.phases?.[0];
    const nextMilestone = currentPhase?.milestones?.find(m => !m.completed) || currentPhase?.milestones?.[0];
    const nextSkill = nextMilestone?.skills?.[0] || "Core Placement Patterns";
    const recommendedProject = nextMilestone?.recommendedProject || currentPhase?.milestones?.[0]?.recommendedProject || "Enterprise Project";
    const currentMeta = TRACK_META[roadmap?.trackId] || TRACK_META["custom"];

    return (
        <div className="ai-roadmap-page">
            {/* FUTURISTIC COMMAND DECK HERO */}
            <div className="air-deck-hero">
                <div className="air-deck-left">
                    <div className="air-deck-eyebrow">
                        <span className="air-pulse-radar" />
                        <span className="air-deck-badge-text">AI Placement Intelligence Engine</span>
                        <span className="air-status-pill">Active Mode</span>
                    </div>

                    {/* PROMINENT CURRENT TRACK DISPLAY */}
                    <div className="air-hero-track-showcase">
                        <span className="air-track-super">CURRENT CAREER TRACK</span>
                        <div className="air-track-title-row">
                            <div className="air-track-hero-icon">
                                {currentMeta.icon}
                            </div>
                            <h1 className="air-track-hero-heading">{roadmap?.trackTitle}</h1>
                        </div>
                        <p className="air-track-hero-desc">{roadmap?.description}</p>
                    </div>

                    <div className="air-hero-action-toolbar">
                        <button
                            type="button"
                            className="air-deck-btn primary-btn"
                            onClick={() => setShowTrackModal(true)}
                        >
                            <FiRefreshCw /> Switch Specialization
                        </button>

                        <button
                            type="button"
                            className="air-deck-btn secondary-btn"
                            onClick={handleExportPdf}
                            disabled={exportingPdf}
                        >
                            <FiDownload /> {exportingPdf ? "Building PDF..." : "Export Official PDF"}
                        </button>

                        <button
                            type="button"
                            className="air-deck-btn refresh-btn"
                            onClick={handleRefreshRoadmap}
                            disabled={refreshing || loading}
                            title="Synchronize latest progress"
                        >
                            <FiRefreshCw className={refreshing ? "air-spin" : ""} /> {refreshing ? "Refreshing..." : "Refresh"}
                        </button>
                    </div>
                </div>

                {/* 3D HOLOGRAPHIC TELEMETRY CARD */}
                <div className="air-telemetry-deck">
                    <div className="air-telemetry-item">
                        <div className="air-tele-top">
                            <span className="air-tele-label">Placement Rank</span>
                            <FiAward className="air-tele-icon" />
                        </div>
                        <div className="air-tele-val">{roadmap?.currentLevel ? roadmap.currentLevel.split(":")[0] : "Level 1"}</div>
                        <span className="air-tele-sub">{roadmap?.currentLevel ? roadmap.currentLevel.split(":")[1]?.trim() || "Foundation" : "Foundation"}</span>
                    </div>

                    <div className="air-telemetry-item">
                        <div className="air-tele-top">
                            <span className="air-tele-label">Milestone Matrix</span>
                            <FiCheckCircle className="air-tele-icon" />
                        </div>
                        <div className="air-tele-val">
                            {roadmap?.completedMilestonesCount || 0} / {roadmap?.totalMilestones || 0}
                        </div>
                        <div className="air-meter-container">
                            <div className="air-meter-fill" style={{ width: `${progressValue}%` }} />
                        </div>
                        <span className="air-tele-sub">{isNotStarted ? "Not started" : `${progressValue}% complete`}</span>
                    </div>

                    <div className="air-telemetry-item">
                        <div className="air-tele-top">
                            <span className="air-tele-label">Experience Points</span>
                            <FiZap className="air-tele-icon" style={{ color: "#f59e0b" }} />
                        </div>
                        <div className="air-tele-val xp-val">
                            {roadmap?.earnedXp || 0} <span className="air-tele-max">/ {roadmap?.totalXp || 0} XP</span>
                        </div>
                        <span className="air-tele-sub">Verified through Arena & Mocks</span>
                    </div>
                </div>
            </div>

            {notice && (
                <div className="air-sync-banner">
                    <FiTerminal className="air-sync-icon" />
                    <span>{notice}</span>
                </div>
            )}

            {/* FUTURISTIC 3D STRUCTURED PIPELINE */}
            <div className="air-progression-pipeline-box">
                <div className="air-pipeline-header">
                    <div className="air-pipe-title">
                        <FiCompass /> Target Advancement Pipeline
                    </div>
                    <div className="air-pipe-desc">Live coordinate tracking towards placement qualification</div>
                </div>

                <div className="air-pipeline-ribbon">
                    <div className="air-pipe-node">
                        <span className="air-pipe-tag">Current Level</span>
                        <div className="air-pipe-content">
                            <FiAward className="air-node-icon" />
                            <strong>{roadmap?.currentLevel ? roadmap.currentLevel.split(":")[0] : "Level 1"}</strong>
                        </div>
                    </div>

                    <div className="air-pipe-beam"><FiChevronRight /></div>

                    <div className="air-pipe-node">
                        <span className="air-pipe-tag">Active Phase</span>
                        <div className="air-pipe-content">
                            <FiLayers className="air-node-icon" />
                            <strong>Phase {currentPhase?.phaseNumber || 1}</strong>
                        </div>
                    </div>

                    <div className="air-pipe-beam"><FiChevronRight /></div>

                    <div className="air-pipe-node">
                        <span className="air-pipe-tag">Focus Skill</span>
                        <div className="air-pipe-content">
                            <FiZap className="air-node-icon" />
                            <strong>{nextSkill}</strong>
                        </div>
                    </div>

                    <div className="air-pipe-beam"><FiChevronRight /></div>

                    <div className="air-pipe-node">
                        <span className="air-pipe-tag">Next Milestone</span>
                        <div className="air-pipe-content" title={nextMilestone?.title}>
                            <FiCheckCircle className="air-node-icon" />
                            <strong>{nextMilestone?.title ? (nextMilestone.title.length > 20 ? nextMilestone.title.slice(0, 20) + "..." : nextMilestone.title) : "Foundation"}</strong>
                        </div>
                    </div>

                    <div className="air-pipe-beam"><FiChevronRight /></div>

                    <div className="air-pipe-node pipe-project-node">
                        <span className="air-pipe-tag">Capstone Project</span>
                        <div className="air-pipe-content" title={recommendedProject}>
                            <FiFolder className="air-node-icon" />
                            <strong>{recommendedProject.length > 20 ? recommendedProject.slice(0, 20) + "..." : recommendedProject}</strong>
                        </div>
                    </div>
                </div>
            </div>

            {/* INTERACTIVE PHASE HORIZON STEPPER */}
            <div className="air-horizon-stepper">
                <span className="air-stepper-title">Curriculum Phases:</span>
                <div className="air-stepper-scroll">
                    {roadmap?.phases?.map((p, idx) => {
                        const isLocked = p.locked;
                        const isDone = !isLocked && p.milestones?.every(m => m.completed);
                        const isCurrent = activePhaseIndex === idx;

                        return (
                            <button
                                key={p.phaseId}
                                type="button"
                                className={`air-step-tab ${isCurrent ? "active" : ""} ${isLocked ? "locked" : ""} ${isDone ? "done" : ""}`}
                                onClick={() => scrollToPhase(idx)}
                            >
                                <span className="air-step-num">0{p.phaseNumber}</span>
                                <span className="air-step-label">{p.title}</span>
                                {isLocked ? (
                                    <FiLock className="air-step-indicator lock" />
                                ) : isDone ? (
                                    <FiCheck className="air-step-indicator done" />
                                ) : (
                                    <span className="air-step-indicator live" />
                                )}
                            </button>
                        );
                    })}
                </div>
            </div>

            {/* ROADMAP PHASES TIMELINE STAGE */}
            <div className="air-phases-stage">
                {roadmap?.phases?.map((phase, pIdx) => (
                    <div
                        key={phase.phaseId}
                        ref={(el) => (phaseRefs.current[pIdx] = el)}
                        className={`air-phase-deck ${phase.locked ? "deck-locked" : ""}`}
                    >
                        <div className="air-deck-header">
                            <div className="air-deck-title-cluster">
                                <div className="air-phase-indexer">
                                    <span>{String(phase.phaseNumber).padStart(2, "0")}</span>
                                </div>
                                <div>
                                    <h2 className="air-phase-main-title">{phase.title}</h2>
                                    <p className="air-phase-sub-title">{phase.subtitle}</p>
                                </div>
                            </div>

                            {phase.locked ? (
                                <div className="air-badge-lock">
                                    <FiLock /> Pro Exclusive
                                </div>
                            ) : (
                                <div className="air-badge-available">
                                    <FiCpu /> Phase Active
                                </div>
                            )}
                        </div>

                        {phase.locked ? (
                            <div className="air-locked-curtain">
                                <div className="air-locked-content">
                                    <div className="air-lock-icon-wrap">
                                        <FiLock size={28} />
                                    </div>
                                    <h3>Phase {phase.phaseNumber} is Locked in Starter Preview</h3>
                                    <p>
                                        Unlock deep architecture modules, comprehensive system design blueprints, and verified
                                        placement credentials with Pro or Elite.
                                    </p>
                                    <button
                                        type="button"
                                        className="air-deck-btn primary-btn"
                                        onClick={() => navigate("/pricing")}
                                    >
                                        <FiAward /> Upgrade to Unlock Full Curriculum
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="air-milestone-deck-grid">
                                {phase.milestones?.map((m) => {
                                    const isDone = m.completed;
                                    const statusClass = isDone ? "completed" : "available";

                                    return (
                                        <div
                                            key={m.id}
                                            className={`air-milestone-card ${statusClass}`}
                                        >
                                            <div className="air-card-top-bar">
                                                <div className="air-card-status-chip">
                                                    {isDone ? (
                                                        <span className="air-chip done">
                                                            <FiCheck size={11} /> Completed
                                                        </span>
                                                    ) : (
                                                        <span className="air-chip in-progress">
                                                            <FiZap size={10} /> Available
                                                        </span>
                                                    )}
                                                    <span className="air-time-pill">
                                                        <FiClock size={11} /> {m.estimatedTime || "6 hours"}
                                                    </span>
                                                </div>

                                                <span className="air-xp-reward">
                                                    +{m.xp} XP
                                                </span>
                                            </div>

                                            <h3 className="air-m-card-title">{m.title}</h3>
                                            <p className="air-m-card-desc">{m.description}</p>

                                            <div className="air-skills-tags-tray">
                                                {m.skills?.map((skill, sIdx) => (
                                                    <span key={sIdx} className="air-skill-tag">
                                                        {skill}
                                                    </span>
                                                ))}
                                            </div>

                                            {m.recommendedProject && (
                                                <div className="air-project-brief-box">
                                                    <div className="air-project-label">
                                                        <FiFolder size={12} /> Flagship Project Benchmark
                                                    </div>
                                                    <div className="air-project-name">
                                                        {m.recommendedProject}
                                                    </div>
                                                </div>
                                            )}

                                            <div className="air-card-footer-action">
                                                <div className="air-card-action-row">
                                                    {!phase.locked && !isDone && (() => {
                                                        const actionMeta = getMilestoneFeatureAction(m);
                                                        return (
                                                            <button
                                                                type="button"
                                                                className="air-action-link-btn"
                                                                onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    navigate(actionMeta.route, {
                                                                        state: {
                                                                            fromRoadmap: true,
                                                                            roadmapTopic: m.title,
                                                                            milestoneId: m.id,
                                                                            skills: m.skills || [],
                                                                            trackTitle: roadmap?.trackTitle || ""
                                                                        }
                                                                    });
                                                                }}
                                                                title={actionMeta.label}
                                                            >
                                                                {actionMeta.icon}
                                                                <span>{actionMeta.label}</span>
                                                            </button>
                                                        );
                                                    })()}

                                                    <button
                                                        type="button"
                                                        className={`air-interactive-toggle-btn ${isDone ? "btn-undo" : "btn-complete"}`}
                                                        onClick={() => handleToggleMilestone(phase.locked, m.id)}
                                                        disabled={togglingMilestoneId === m.id}
                                                    >
                                                        {togglingMilestoneId === m.id ? (
                                                            <span>Updating XP...</span>
                                                        ) : isDone ? (
                                                            <>
                                                                <FiRotateCcw size={12} /> Mark Milestone Incomplete
                                                            </>
                                                        ) : (
                                                            <>
                                                                <FiCheckCircle size={13} /> Mark Completed (+{m.xp} XP)
                                                            </>
                                                        )}
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {/* TRACK SWITCHER MODAL WITH DYNAMIC SUGGESTIONS */}
            {showTrackModal && (
                <div className="air-modal-overlay" onClick={() => setShowTrackModal(false)}>
                    <div className="air-modal-deck" onClick={(e) => e.stopPropagation()}>
                        <div className="air-modal-header">
                            <div>
                                <h2 className="air-modal-heading">Select Career Specialization</h2>
                                <p className="air-modal-subheading">
                                    Choose a core technology stack or build a custom curriculum for any domain.
                                </p>
                            </div>
                            <button
                                type="button"
                                className="air-modal-close-btn"
                                onClick={() => setShowTrackModal(false)}
                                aria-label="Close modal"
                            >
                                <FiX />
                            </button>
                        </div>

                        {/* ACTIVE TRACK CHIP IN MODAL */}
                        <div className="air-modal-current-indicator">
                            <span className="air-radar-pulse" />
                            <span>Currently Active:</span>
                            <strong>{roadmap?.trackTitle}</strong>
                        </div>

                        {/* PREDEFINED INDUSTRY TRACKS */}
                        <div className="air-modal-tracks-grid">
                            {roadmap?.availableTracks?.filter(t => t.id !== "custom")?.map((t) => {
                                const isCurrent = roadmap.trackId === t.id;
                                const meta = TRACK_META[t.id] || { icon: <FiCompass size={22} color="#6366f1" />, badge: "General" };

                                return (
                                    <button
                                        key={t.id}
                                        type="button"
                                        className={`air-track-selector-card ${isCurrent ? "active-track" : ""}`}
                                        onClick={() => handleSwitchTrack(t.id)}
                                        disabled={switchingTrack}
                                    >
                                        <div className="air-selector-card-top">
                                            <div className="air-selector-icon">
                                                {meta.icon}
                                            </div>
                                            {isCurrent ? (
                                                <span className="air-active-badge">
                                                    <FiCheck size={11} /> Selected
                                                </span>
                                            ) : (
                                                <span className="air-category-badge">{meta.badge}</span>
                                            )}
                                        </div>
                                        <h4 className="air-selector-title">{t.title}</h4>
                                        <p className="air-selector-desc">{t.description}</p>
                                    </button>
                                );
                            })}

                            {/* CUSTOM DOMAIN CARD */}
                            <button
                                type="button"
                                className={`air-track-selector-card custom-card ${showCustomInput || roadmap?.trackId === "custom" ? "active-track" : ""}`}
                                onClick={() => setShowCustomInput(true)}
                            >
                                <div className="air-selector-card-top">
                                    <div className="air-selector-icon custom-glow">
                                        <FiCompass size={22} color="#a855f7" />
                                    </div>
                                    <span className="air-category-badge custom-pill">Any Domain</span>
                                </div>
                                <h4 className="air-selector-title">Other / Custom Track</h4>
                                <p className="air-selector-desc">
                                    Personalize an AI curriculum for Flutter, Cybersecurity, Game Dev, Data Analyst, etc.
                                </p>
                            </button>
                        </div>

                        {/* DYNAMIC SEARCH & SUGGESTION PANEL */}
                        {showCustomInput && (
                            <div className="air-custom-search-container">
                                <div className="air-search-header">
                                    <h4>Enter or Search Specialization</h4>
                                    <p>Select a trending industry stack or type your exact engineering domain:</p>
                                </div>

                                <div className="air-input-search-row">
                                    <div className="air-input-wrapper">
                                        <FiSearch className="air-search-input-icon" />
                                        <input
                                            type="text"
                                            className="air-domain-input"
                                            placeholder="Type domain (e.g. Flutter, Cybersecurity, Game Dev, SRE)..."
                                            value={customTrackInput}
                                            onChange={(e) => setCustomTrackInput(e.target.value)}
                                            onKeyDown={handleCustomInputKeyDown}
                                            autoFocus
                                        />
                                    </div>

                                    <button
                                        type="button"
                                        className="air-deck-btn primary-btn air-gen-btn"
                                        disabled={!customTrackInput.trim() || switchingTrack}
                                        onClick={() => handleSwitchTrack("custom", customTrackInput.trim())}
                                    >
                                        <FiCheck /> {switchingTrack ? "Generating..." : "Generate Roadmap"}
                                    </button>
                                </div>

                                {/* REAL-TIME DYNAMIC SUGGESTIONS */}
                                <div className="air-suggestions-wrapper">
                                    <span className="air-sugg-title">
                                        {loadingSuggestions ? "Searching stacks..." : "Dynamic Industry Suggestions:"}
                                    </span>
                                    <div className="air-chips-cloud">
                                        {suggestions.map((item, idx) => (
                                            <button
                                                key={item}
                                                type="button"
                                                className={`air-dynamic-chip ${highlightedIndex === idx || customTrackInput.toLowerCase() === item.toLowerCase() ? "chip-active" : ""}`}
                                                onClick={() => {
                                                    setCustomTrackInput(item);
                                                    handleSwitchTrack("custom", item);
                                                }}
                                            >
                                                {item}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
