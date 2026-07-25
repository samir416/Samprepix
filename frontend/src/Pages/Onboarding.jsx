import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { updateProfile } from "../services/profileService";
import "../styles/onboarding.css";

export default function Onboarding() {

    useEffect(() => {

        const savedTheme = localStorage.getItem("theme");

        if (savedTheme === "light") {

            document.body.classList.add("light-theme");

        } else {

            document.body.classList.remove("light-theme");

        }

    }, []);


    const navigate = useNavigate();

    const [step, setStep] = useState(1);

    const [loading, setLoading] = useState(false);

    const [formData, setFormData] = useState({

        journeyType: "",

        targetRole: "",

        experienceLevel: "",

        currentRole: "",

        yearsOfExperience: "",

        careerGoal: ""

    });

    const handleChange = (field, value) => {

        setFormData(prev => ({

            ...prev,

            [field]: value

        }));

    };

    const nextStep = () => {

        setStep(prev => prev + 1);

    };

    const previousStep = () => {

        setStep(prev => prev - 1);

    };

    const handleSubmit = async () => {

        try {

            setLoading(true);

            const updatedUser =
                await updateProfile(formData);

            localStorage.setItem(
                "user",
                JSON.stringify(updatedUser)
            );

            localStorage.setItem(
                "onboardingCompleted",
                "true"
            );

            navigate(
                "/dashboard",
                {
                    replace: true
                }
            );

        } catch (error) {

            console.error(
                "Profile update failed:",
                error
            );

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

                                            <label>

                                                Experience Level

                                            </label>

                                            <select

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

                                        <label>

                                            Career Goal

                                        </label>

                                        <div className="goal-select-wrapper">

                                            <select
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
