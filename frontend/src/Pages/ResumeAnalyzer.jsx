import { useEffect, useState } from "react";

import {
    FiUploadCloud,
    FiCheckCircle,
    FiAlertTriangle
} from "react-icons/fi";

import "../styles/resumeAnalyzer.css";

export default function ResumeAnalyzer() {

    const [uploadStatus, setUploadStatus] = useState("idle");

    const [fileName, setFileName] = useState("");

    /* LOAD SAVED DATA */

    useEffect(() => {

        const savedStatus =
            localStorage.getItem("resumeUploadStatus");

        const savedFile =
            localStorage.getItem("resumeFileName");

        if (savedStatus) {

            setUploadStatus(savedStatus);
        }

        if (savedFile) {

            setFileName(savedFile);
        }

    }, []);

    const handleFileChange = (e) => {

        const file = e.target.files[0];

        if (!file) return;

        /* FILE TYPE */

        const allowedTypes = [
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ];

        if (!allowedTypes.includes(file.type)) {

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

                {/* LEFT */}

                <div className="upload-card">

                    {/* SUCCESS */}

                    {
                        uploadStatus === "success"

                            ? (

                                <>

                                    <div className="success-icon">

                                        <FiCheckCircle />

                                    </div>

                                    <h2>
                                        Upload Complete
                                    </h2>

                                    <p className="uploaded-file">

                                        {fileName}

                                    </p>

                                    <span className="done-text">

                                        Done ✓
                                    </span>

                                </>

                            )

                            /* ERROR */

                            : uploadStatus === "error"

                                ? (

                                    <>

                                        <div className="error-icon">

                                            <FiAlertTriangle />

                                        </div>

                                        <h2>
                                            Upload Failed
                                        </h2>

                                        <p className="uploaded-file error-text">

                                            {fileName}

                                        </p>

                                        <span className="done-text">

                                            Only PDF & DOCX under 10MB
                                        </span>

                                    </>

                                )

                                /* DEFAULT */

                                : (

                                    <>

                                        <div className="upload-icon">

                                            <FiUploadCloud />

                                        </div>

                                        <h2>
                                            Upload your resume
                                        </h2>

                                        <p>
                                            PDF or DOCX up to 10MB
                                        </p>

                                    </>

                                )
                    }

                    {/* BUTTON ALWAYS VISIBLE */}

                    <label className="browse-btn">

                        Browse file

                        <input
                            type="file"
                            accept=".pdf,.docx"
                            multiple={false}
                            hidden
                            onChange={handleFileChange}
                        />

                    </label>

                </div>

                {/* RIGHT */}

                <div className="resume-right">

                    {/* ATS */}

                    <div className="ats-card">

                        <h3>
                            ATS Score
                        </h3>

                        <div className="ats-score">

                            --

                        </div>

                        <p>
                            out of 100
                        </p>

                        <div className="ats-line"></div>

                    </div>

                    {/* SKILLS */}

                    <div className="skills-card">

                        <h3>
                            Skill gap analysis
                        </h3>

                        {/* ITEM */}

                        <div className="skill-item">

                            <div className="skill-top">

                                <span>
                                    React
                                </span>

                                <span>
                                    92%
                                </span>

                            </div>

                            <div className="skill-bar">

                                <div
                                    className="skill-fill"
                                    style={{
                                        width: "92%"
                                    }}
                                ></div>

                            </div>

                        </div>

                        {/* ITEM */}

                        <div className="skill-item">

                            <div className="skill-top">

                                <span>
                                    TypeScript
                                </span>

                                <span>
                                    64%
                                </span>

                            </div>

                            <div className="skill-bar">

                                <div
                                    className="skill-fill"
                                    style={{
                                        width: "64%"
                                    }}
                                ></div>

                            </div>

                        </div>

                        {/* ITEM */}

                        <div className="skill-item">

                            <div className="skill-top">

                                <span>
                                    System Design
                                </span>

                                <span>
                                    38%
                                </span>

                            </div>

                            <div className="skill-bar">

                                <div
                                    className="skill-fill"
                                    style={{
                                        width: "38%"
                                    }}
                                ></div>

                            </div>

                        </div>

                        {/* ITEM */}

                        <div className="skill-item">

                            <div className="skill-top">

                                <span>
                                    DSA
                                </span>

                                <span>
                                    78%
                                </span>

                            </div>

                            <div className="skill-bar">

                                <div
                                    className="skill-fill"
                                    style={{
                                        width: "78%"
                                    }}
                                ></div>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </section>
    );
}