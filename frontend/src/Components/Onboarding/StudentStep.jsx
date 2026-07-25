export default function StudentStep({
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
                Student Details
            </h1>

            <p>
                Help us personalize your preparation journey.
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
                            setFormData({
                                ...formData,
                                targetRole: e.target.value
                            })
                        }
                    />

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
                        !formData.targetRole ||
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