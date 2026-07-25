export default function JourneyStep({
    formData,
    setFormData,
    setStep
}) {

    return (

        <div className="onboarding-card">

            <span className="onboarding-badge">
                Step 1 of 3
            </span>

            <h1>
                Welcome 👋
            </h1>

            <p>
                Tell us about your journey so we can personalize your experience.
            </p>

            <div className="journey-grid">

                <button
                    type="button"
                    className={
                        formData.journeyType === "STUDENT"
                            ? "journey-card active"
                            : "journey-card"
                    }
                    onClick={() =>
                        setFormData({
                            ...formData,
                            journeyType: "STUDENT"
                        })
                    }
                >

                    <h3>
                        Student
                    </h3>

                    <p>
                        Preparing for internships and placements.
                    </p>

                </button>

                <button
                    type="button"
                    className={
                        formData.journeyType === "WORKING_PROFESSIONAL"
                            ? "journey-card active"
                            : "journey-card"
                    }
                    onClick={() =>
                        setFormData({
                            ...formData,
                            journeyType: "WORKING_PROFESSIONAL"
                        })
                    }
                >

                    <h3>
                        Working Professional
                    </h3>

                    <p>
                        Upskill, switch companies or prepare for interviews.
                    </p>

                </button>

            </div>

            <div className="onboarding-actions">

                <button
                    type="button"
                    className="primary-btn"
                    disabled={!formData.journeyType}
                    onClick={() => setStep(2)}
                >
                    Continue
                </button>

            </div>

        </div>

    );

}