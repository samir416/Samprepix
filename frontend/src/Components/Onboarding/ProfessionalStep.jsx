export default function ProfessionalStep({
    formData,
    setFormData,
    setStep
}) {

    return (

        <div className="onboarding-card">

            <span className="onboarding-badge">
                Step 2 of 3
            </span>

            <h1>
                Professional Details
            </h1>

            <p>
                Tell us about your experience and career goals.
            </p>

            <div className="onboarding-form">

                <div className="form-group">

                    <label>
                        Current Role
                    </label>

                    <input
                        type="text"
                        placeholder="Software Engineer"
                        value={formData.currentRole || ""}
                        onChange={(e) =>
                            setFormData({
                                ...formData,
                                currentRole: e.target.value
                            })
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
                        step="0.5"
                        placeholder="2.5"
                        value={formData.yearsOfExperience || ""}
                        onChange={(e) =>
                            setFormData({
                                ...formData,
                                yearsOfExperience: e.target.value
                            })
                        }
                    />

                </div>

                <div className="form-group">

                    <label>
                        Career Goal
                    </label>

                    <select
                        value={formData.careerGoal || ""}
                        onChange={(e) =>
                            setFormData({
                                ...formData,
                                careerGoal: e.target.value
                            })
                        }
                    >

                        <option value="">
                            Select Career Goal
                        </option>

                        <option value="JOB">
                            Get a Job
                        </option>

                        <option value="COMPANY_SWITCH">
                            Switch Company
                        </option>

                        <option value="DOMAIN_SWITCH">
                            Switch Domain
                        </option>

                        <option value="PROMOTION">
                            Get a Promotion
                        </option>

                        <option value="INTERVIEW_PRACTICE">
                            Interview Practice
                        </option>

                    </select>

                </div>

                <div className="form-group">

                    <label>
                        Preferred Language
                    </label>

                    <select
                        value={formData.preferredLanguage}
                        onChange={(e) =>
                            setFormData({
                                ...formData,
                                preferredLanguage: e.target.value
                            })
                        }
                    >

                        <option value="">
                            Select Language
                        </option>

                        <option value="JAVA">
                            Java
                        </option>

                        <option value="CPP">
                            C++
                        </option>

                        <option value="PYTHON">
                            Python
                        </option>

                        <option value="JAVASCRIPT">
                            JavaScript
                        </option>

                    </select>

                </div>

            </div>

            <div className="onboarding-actions">

                <button
                    type="button"
                    className="secondary-btn"
                    onClick={() => setStep(1)}
                >
                    Back
                </button>

                <button
                    type="button"
                    className="primary-btn"
                    disabled={
                        !formData.currentRole ||
                        !formData.yearsOfExperience ||
                        !formData.careerGoal ||
                        !formData.preferredLanguage
                    }
                    onClick={() => setStep(3)}
                >
                    Continue
                </button>

            </div>

        </div>

    );

}