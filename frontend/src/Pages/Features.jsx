import {
    FiMic,
    FiFileText,
    FiCode,
    FiTrendingUp,
    FiZap,
    FiShield,
    FiUsers,
} from "react-icons/fi";

import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";

import styles from "../styles/features.module.css";

export default function Features() {
    useEffect(() => {
        updatePageSEO({
            title: "Features | Samprepix AI Placement Platform for CS Students",
            description: "Explore the comprehensive features of Samprepix for Computer Science students: 5,050+ coding problems, AI voice mock interviews, 22,060 aptitude questions, and ATS resume analytics.",
            canonicalPath: "/features"
        });
        trackPageView("/features", "Features | Samprepix");
    }, []);

    const features = [

        {
            icon: <FiMic />,
            title: "Voice Mock Interviews",
            desc: "Realistic AI interviewer with speech analysis & tone scoring.",
        },

        {
            icon: <FiFileText />,
            title: "Resume Analyzer",
            desc: "ATS scoring, keyword gaps and recruiter optimization reports.",
        },

        {
            icon: <FiCode />,
            title: "Coding Arena",
            desc: "Monaco editor with contests, testcases and instant judging.",
        },

        {
            icon: <FiZap />,
            title: "Adaptive Plans",
            desc: "AI roadmap that adapts according to weak preparation areas.",
        },

        {
            icon: <FiTrendingUp />,
            title: "Deep Analytics",
            desc: "Track confidence, accuracy & placement readiness in real-time.",
        },

        {
            icon: <FiShield />,
            title: "Privacy First",
            desc: "Your reports and analytics remain secured and encrypted.",
        },

        {
            icon: <FiCode />,
            title: "GitHub Sync",
            desc: "Automatic synchronization of passing solutions to your personal repository.",
        },

        {
            icon: <FiZap />,
            title: "Software Placement Tracks",
            desc: "Specialized preparation paths for software engineering roles & campus placements.",
        },

    ];

    return (

        <section className={styles["features-page"]}>

            {/* NAVBAR */}

            <Navbar />

            {/* HERO */}

            <div className={styles["features-hero"]}>

                <h1>

                    Built To Transform Computer Science Students Into Top Candidates

                </h1>

                <p>

                    All the tools CS students need to get hired —
                    in one beautifully designed AI-powered workspace.

                </p>

            </div>

            {/* FEATURES */}

            <div className={styles["features-grid"]}>

                {

                    features.map((item, index) => (

                        <div
                            className={styles["feature-card"]}
                            key={index}
                        >

                            <div className={styles["feature-icon"]}>

                                {item.icon}

                            </div>

                            <h3>

                                {item.title}

                            </h3>

                            <p>

                                {item.desc}

                            </p>

                        </div>

                    ))

                }

            </div>

            <Footer />
        </section>
    );
}