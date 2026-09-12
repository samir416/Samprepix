import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { ChevronDown, AlertCircle } from "lucide-react";
import { updateProfile } from "../services/profileService";
import { getCurrentUser } from "../services/authService";
import "../styles/onboarding.css";

export default function Onboarding() {

    // Follow application global theme automatically without adding a theme toggle
    useEffect(() => {
        const syncTheme = () => {
            const savedTheme = localStorage.getItem("themePreference") || localStorage.getItem("theme") || "system";
            let isDark = false;
            if (savedTheme === "dark") {
                isDark = true;
            } else if (savedTheme === "light") {
                isDark = false;
            } else {
                isDark = typeof window !== "undefined" && window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
            }

            if (isDark) {
                document.body.classList.add("dark-theme");
                document.body.classList.remove("light-theme");
            } else {
                document.body.classList.remove("dark-theme");
                document.body.classList.add("light-theme");
            }
        };

        syncTheme();
        window.addEventListener("storage", syncTheme);
        return () => window.removeEventListener("storage", syncTheme);
    }, []);

    const navigate = useNavigate();

    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const [formData, setFormData] = useState({
        journeyType: "",
        targetRole: "",
        experienceLevel: "",
        currentRole: "",
        yearsOfExperience: "",
        careerGoal: ""
    });

    const handleChange = (field, value) => {
        if (error) setError("");
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const nextStep = () => {
        if (error) setError("");
        setStep(prev => prev + 1);
    };

    const previousStep = () => {
        if (error) setError("");
        setStep(prev => prev - 1);
    };

    const handleSubmit = async () => {
        if (loading) return;
        setError("");

        try {
            setLoading(true);
            await updateProfile(formData);

            let refreshedUser = null;
            try {
                refreshedUser = await getCurrentUser();
                if (refreshedUser) {
                    localStorage.setItem("user", JSON.stringify(refreshedUser));
                }
            } catch (fetchErr) {
                console.warn("Could not refresh user after onboarding:", fetchErr);
            }

            localStorage.setItem("onboardingCompleted", "true");

            navigate("/dashboard", { replace: true });

        } catch (err) {
            console.error("Profile update failed:", err);
            setError("Unable to save your profile. Please check your details and try again.");
        } finally {
            setLoading(false);
        }
    };

    return (

        <div className="onboarding-page">

            <section className="onboarding-wrapper">

                <div className="onboarding-background"></div>

                <div className="onboarding-overlay"></div>

                <motion.div

                    className="onboarding-container"

                    initial={{

                        opacity: 0,

                        scale: .96,

                        y: 40

                    }}

                    animate={{

                        opacity: 1,

                        scale: 1,

                        y: 0

                    }}

                    transition={{

                        duration: .45

                    }}

                >

                    <div className="onboarding-header">

                        <h1>

                            Complete Your Profile

                        </h1>

                        <p>

                            Let's personalize your placement journey.

                        </p>

                    </div>

                    {error && (
                        <div className="onboarding-error-alert" role="alert">
                            <div className="onboarding-error-icon">
                                <AlertCircle size={18} />
                            </div>
                            <div className="onboarding-error-content">
                                <h6>Unable to Save Profile</h6>
                                <p>{error}</p>
                            </div>
                            <button
                                type="button"
                                className="onboarding-error-retry-btn"
                                onClick={handleSubmit}
                                disabled={loading}
                            >
                                Try Again
                            </button>
                        </div>
                    )}

                    <div className="progress-wrapper">

                        <div className={step >= 1 ? "progress-node active" : "progress-node"}>

                            1

                        </div>

                        <div className={step >= 2 ? "progress-line active" : "progress-line"}></div>

                        <div className={step >= 2 ? "progress-node active" : "progress-node"}>

                            2

                        </div>

                        <div className={step >= 3 ? "progress-line active" : "progress-line"}></div>

                        <div className={step >= 3 ? "progress-node active" : "progress-node"}>

                            3

                        </div>

                    </div>

                    <AnimatePresence mode="wait">

                        <motion.div

                            key={step}

                            initial={{

                                opacity: 0,

                                y: 25

                            }}

                            animate={{

                                opacity: 1,

                                y: 0

                            }}

                            exit={{

                                opacity: 0,

                                y: -25

                            }}

                            transition={{

                                duration: .25

                            }}

                        >

                            {step === 1 && (

                                <div className="step-container">

                                    <span className="step-badge">

                                        Step 1 of 3

                                    </span>

                                    <h2>

                                        Choose Your Journey

                                    </h2>

                                    <p>

                                        Select the option that best describes your current career stage.

                                    </p>

                                    <div className="journey-grid">

                                        <motion.div

                                            whileHover={{
                                                y: -6,
                                                scale: 1.01
                                            }}

                                            whileTap={{
                                                scale: .98
                                            }}

                                            className={
                                                formData.journeyType === "STUDENT"
                                                    ? "journey-card active"
                                                    : "journey-card"
                                            }

                                            onClick={() =>
                                                handleChange(
                                                    "journeyType",
                                                    "STUDENT"
                                                )
                                            }

                                        >

                                            <div className="journey-icon">

                                                🎓

                                            </div>

                                            <h3>

                                                Student

                                            </h3>

                                            <p>

                                                Internship preparation, placement practice, resume building and interview readiness.

                                            </p>

                                        </motion.div>

                                        <motion.div

                                            whileHover={{
                                                y: -6,
                                                scale: 1.01
                                            }}

                                            whileTap={{
                                                scale: .98
                                            }}

                                            className={
                                                formData.journeyType === "WORKING_PROFESSIONAL"
                                                    ? "journey-card active"
                                                    : "journey-card"
                                            }

                                            onClick={() =>
                                                handleChange(
                                                    "journeyType",
                                                    "WORKING_PROFESSIONAL"
                                                )
                                            }

                                        >

                                            <div className="journey-icon">

                                                💼

                                            </div>

                                            <h3>

                                                Working Professional

                                            </h3>

                                            <p>

                                                Company switch, promotion, domain change and interview preparation.

                                            </p>

                                        </motion.div>

                                    </div>

                                    <div className="step-actions">

                                        <button

                                            className="continue-btn"

                                            disabled={!formData.journeyType}

                                            onClick={nextStep}

                                        >

                                            Continue →

                                        </button>

                                    </div>

                                </div>

                            )}

                            {step === 2 && (

                                <div className="step-container">

                                    <span className="step-badge">

                                        Step 2 of 3

                                    </span>

                                    <h2>

                                        Career Information

                                    </h2>

                                    <p>

                                        Help us personalize your interview preparation and placement journey.

                                    </p>

                                    <div className="onboarding-form">

                                        <div className="form-group">

                                            <label>

                                                Target Role

                                            </label>

                                            <input

                                                type="text"

                                                placeholder="Java Full Stack Developer"

                                                value={formData.targetRole}

                                                onChange={(e) =>
                                                    handleChange(
                                                        "targetRole",
                                                        e.target.value
                                                    )
                                                }

                                            />

                                        </div>

                                        <div className="form-group">

                                            <label htmlFor="onboarding-experience-level">

                                                Experience Level

                                            </label>

                                            <div className="custom-select-wrapper">

                                                <select
                                                    id="onboarding-experience-level"
                                                    value={formData.experienceLevel}

                                                    onChange={(e) =>
                                                        handleChange(
                                                            "experienceLevel",
                                                            e.target.value
                                                        )
                                                    }

                                                >

                                                    <option value="">Select your experience level</option>

                                                    <option value="BEGINNER">
                                                        Beginner
                                                    </option>

                                                    <option value="INTERMEDIATE">
                                                        Intermediate
                                                    </option>

                                                    <option value="ADVANCED">
                                                        Advanced
                                                    </option>
                                                </select>

                                                <ChevronDown className="select-chevron-icon" size={18} />

                                            </div>

                                        </div>

                                        {

                                            formData.journeyType === "WORKING_PROFESSIONAL" && (

                                                <>

                                                    <div className="form-group">

                                                        <label>

                                                            Current Role

                                                        </label>

                                                        <input

                                                            type="text"

                                                            placeholder="Software Engineer"

                                                            value={formData.currentRole}

                                                            onChange={(e) =>
                                                                handleChange(
                                                                    "currentRole",
                                                                    e.target.value
                                                                )
                                                            }

                                                        />

                                                    </div>

                                                    <div className="form-group">

                                                        <label>

                                                            Years of Experience

                                                        </label>

                                                        <input

                                                            type="number"

                                                            min="0"

                                                            placeholder="2"

                                                            value={formData.yearsOfExperience}

                                                            onChange={(e) =>
                                                                handleChange(
                                                                    "yearsOfExperience",
                                                                    e.target.value
                                                                )
                                                            }

                                                        />

                                                    </div>

                                                </>

                                            )

                                        }

                                    </div>

                                    <div className="step-actions">

                                        <button

                                            className="back-btn"

                                            onClick={previousStep}

                                        >

                                            ← Back

                                        </button>

                                        <button

                                            className="continue-btn"

                                            disabled={

                                                !formData.targetRole.trim() ||

                                                !formData.experienceLevel ||

                                                (

                                                    formData.journeyType === "WORKING_PROFESSIONAL" &&

                                                    (

                                                        !formData.currentRole.trim() ||

                                                        !formData.yearsOfExperience

                                                    )

                                                )

                                            }

                                            onClick={nextStep}

                                        >

                                            Continue →

                                        </button>

                                    </div>

                                </div>

                            )}

                            {step === 3 && (

                                <div className="step-container">

                                    <span className="step-badge">

                                        Step 3 of 3

                                    </span>

                                    <h2>

                                        Your Career Goal

                                    </h2>

                                    <p>

                                        Tell us your career goal so we can personalize your dashboard and interview preparation.

                                    </p>

                                    <div className="form-group onboarding-goal">

                                        <label htmlFor="onboarding-career-goal">

                                            Career Goal

                                        </label>

                                        <div className="custom-select-wrapper goal-select-wrapper">

                                            <select
                                                id="onboarding-career-goal"
                                                value={formData.careerGoal}

                                                onChange={(e) =>

                                                    handleChange(

                                                        "careerGoal",

                                                        e.target.value

                                                    )

                                                }

                                            >

                                                <option value="">

                                                    Select your career goal

                                                </option>

                                                <option value="JOB">

                                                    Get My First Job

                                                </option>

                                                <option value="COMPANY_SWITCH">

                                                    Switch Company

                                                </option>

                                                <option value="DOMAIN_SWITCH">

                                                    Switch Domain

                                                </option>

                                                <option value="PROMOTION">

                                                    Get Promotion

                                                </option>

                                                <option value="INTERVIEW_PRACTICE">

                                                    Practice Interviews

                                                </option>

                                            </select>

                                            <ChevronDown className="select-chevron-icon" size={18} />

                                        </div>

                                    </div>

                                    <div className="step-actions">
                                        <button

                                            className="back-btn"

                                            onClick={previousStep}

                                            disabled={loading}

                                        >

                                            ← Back

                                        </button>

                                        <button

                                            className="continue-btn"

                                            disabled={

                                                !formData.careerGoal.trim() ||

                                                loading

                                            }

                                            onClick={handleSubmit}

                                        >

                                            {

                                                loading

                                                    ? "Completing Profile..."

                                                    : "Complete Profile"

                                            }

                                        </button>

                                    </div>

                                </div>

                            )}

                        </motion.div>

                    </AnimatePresence>

                </motion.div>

            </section>

        </div>

    );

}
