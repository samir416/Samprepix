import {
    FiMic,
    FiFileText,
    FiCode,
    FiTrendingUp,
    FiZap,
    FiShield,
    FiUsers,
} from "react-icons/fi";

import Navbar from "../Components/Common/Navbar";

import styles from "../styles/features.module.css";

export default function Features() {

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
            icon: <FiUsers />,
            title: "Peer Practice",
            desc: "Practice with peers, mentors and collaborative AI rooms.",
        },

        {
            icon: <FiZap />,
            title: "Company Tracks",
            desc: "Special preparation paths for product companies & startups.",
        },

    ];

    return (

        <section className={styles["features-page"]}>

            {/* NAVBAR */}

            <Navbar />

            {/* HERO */}

            <div className={styles["features-hero"]}>

                <h1>

                    Built To Transform Students Into Top Candidates

                </h1>

                <p>

                    All the tools you need to get hired —
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

        </section>
    );
}