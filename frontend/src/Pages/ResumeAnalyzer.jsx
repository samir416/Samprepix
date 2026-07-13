import { useEffect, useState } from "react";

import {
    FiUploadCloud,
    FiCheckCircle,
    FiAlertTriangle
} from "react-icons/fi";

import "../styles/resumeAnalyzer.css";
import { analyzeResumeFile } from "../services/resumeService";

export default function ResumeAnalyzer() {

    const [uploadStatus, setUploadStatus] = useState("idle");

    const [fileName, setFileName] = useState("");

    const [analysis, setAnalysis] = useState(null);

    const STORAGE_KEY = "resumeAnalysis";

    /* LOAD SAVED DATA */

    useEffect(() => {

        const savedStatus =
            localStorage.getItem("resumeUploadStatus");

        const savedFile =
            localStorage.getItem("resumeFileName");

        const savedAnalysis =
            localStorage.getItem(STORAGE_KEY);

        if (savedStatus) {
            setUploadStatus(savedStatus);
        }

        if (savedFile) {
            setFileName(savedFile);
        }

       if (savedAnalysis) {
    try {
        setAnalysis(JSON.parse(savedAnalysis));
    } catch (error) {
        console.error("Invalid saved analysis", error);
        localStorage.removeItem(STORAGE_KEY);
    }
}

    }, []);



    const handleFileChange = async (e) => {

        const file = e.target.files[0];

        if (!file) return;

        /* FILE TYPE */

        const allowedTypes = [
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ];

        if (!allowedTypes.includes(file.type)) {

            setAnalysis(null);

            localStorage.removeItem(STORAGE_KEY);

            setUploadStatus("error");

            setFileName("Unsupported file format");

            localStorage.setItem(
                "resumeUploadStatus",
                "error"
            );

            localStorage.setItem(
                "resumeFileName",
                "Unsupported file format"
            );

            e.target.value = "";

            return;
        }

        /* FILE SIZE */

        const maxSize = 10 * 1024 * 1024;

       if (file.size > maxSize) {

    setAnalysis(null);

    localStorage.removeItem(STORAGE_KEY);

    setUploadStatus("error");

    setFileName("File exceeds 10MB limit");

    localStorage.setItem(
        "resumeUploadStatus",
        "error"
    );

    localStorage.setItem(
        "resumeFileName",
        "File exceeds 10MB limit"
    );

    e.target.value = "";

    return;
}
        try {

            const data =
                await analyzeResumeFile(
                    file
                );


            setAnalysis(data);
            localStorage.setItem(
                STORAGE_KEY,
                JSON.stringify(data)
            );

        } catch (error) {

            console.error(error);

            setAnalysis(null);

            localStorage.removeItem(STORAGE_KEY);

            setUploadStatus("error");

            return;
        }

        /* SUCCESS */

        setUploadStatus("success");

        setFileName(file.name);

        localStorage.setItem(
            "resumeUploadStatus",
            "success"
        );

        localStorage.setItem(
            "resumeFileName",
            file.name
        );
    };

    return (

        <section className="resume-page">

            {/* HEADER */}

            <div className="resume-header">

                <h1>
                    Resume Analyzer
                </h1>

                <p>
                    Upload your resume to get ATS score and AI feedback.
                </p>

            </div>

            {/* GRID */}

            <div className="resume-grid">

                {/* Upload Card */}

                <div className="upload-card">

                    {
                        uploadStatus === "success"

                            ? (
                                <>
                                    <div className="success-icon">
                                        <FiCheckCircle />
                                    </div>

                                    <h2>Upload Complete</h2>

                                    <p className="uploaded-file">
                                        {fileName}
                                    </p>

                                    <span className="done-text">
                                        Done ✓
                                    </span>
                                </>
                            )

                            : uploadStatus === "error"

                                ? (
                                    <>
                                        <div className="error-icon">
                                            <FiAlertTriangle />
                                        </div>

                                        <h2>Upload Failed</h2>

                                        <p className="uploaded-file error-text">
                                            {fileName}
                                        </p>

                                        <span className="done-text">
                                            Only PDF & DOCX under 10MB
                                        </span>
                                    </>
                                )

                                : (
                                    <>
                                        <div className="upload-icon">
                                            <FiUploadCloud />
                                        </div>

                                        <h2>Upload your resume</h2>

                                        <p>PDF or DOCX up to 10MB</p>
                                    </>
                                )
                    }

                    <label className="browse-btn">

                        Browse file

                        <input
                            type="file"
                            accept=".pdf,.docx"
                            hidden
                            onChange={handleFileChange}
                        />

                    </label>

                </div>

                {/* ATS */}

                <div className="resume-right">

                    <div className="ats-card">

                        <div className="ats-bg-circle"></div>

                        <h3>ATS Score</h3>

                        <div
                            className="ats-circle"
                            style={{
                                "--score": `${analysis ? analysis.score : 0}%`
                            }}
                        >

                            <div className="ats-circle-inner">

                                <h2>

                                    {
                                        analysis
                                            ? analysis.score
                                            : "--"
                                    }

                                </h2>

                                <span>/100</span>

                            </div>

                        </div>

                        <div className="ats-status">

                            {
                                analysis
                                    ? analysis.score >= 90
                                        ? "Excellent"
                                        : analysis.score >= 75
                                            ? "Good"
                                            : analysis.score >= 60
                                                ? "Average"
                                                : "Needs Improvement"
                                    : "--"
                            }

                        </div>

                        <div className="ats-progress">

                            <div
                                className="ats-progress-fill"
                                style={{
                                    width: `${analysis ? analysis.score : 0}%`
                                }}
                            />

                        </div>

                        <div className="ats-role">

                            {
                                analysis?.role || "Role Not Detected"
                            }

                        </div>

                    </div>

                </div>

                {/* Suggestions */}

                <div className="skills-card suggestions-card">

                    <h3>
                        AI Suggestions
                    </h3>

                    {
                        analysis?.suggestions?.map((suggestion, index) => (

                            <div
                                className="skill-item"
                                key={index}
                            >

                                <div className="skill-top">

                                    <span>{suggestion}</span>

                                </div>

                            </div>

                        ))
                    }

                </div>

            </div>
        </section>
    );
}