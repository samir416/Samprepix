export default function ReviewStep({
    formData,
    setStep,
    onSubmit
}) {

    return (

        <div className="onboarding-card">

            <span className="onboarding-badge">
                Step 3 of 3
            </span>

            <h1>
                Review
            </h1>

            <p>
                Please review your information before continuing.
            </p>

            <div className="review-list">

                <div className="review-item">

                    <span>Journey</span>

                    <strong>
                        {formData.journeyType}
                    </strong>

                </div>

                {
                    formData.journeyType === "STUDENT" && (

                        <>

                            <div className="review-item">

                                <span>Target Role</span>

                                <strong>
                                    {formData.targetRole}
                                </strong>

                            </div>

                            <div className="review-item">

                                <span>Preferred Language</span>

                                <strong>
                                    {formData.preferredLanguage}
                                </strong>

                            </div>

                        </>

                    )
                }

                {
                    formData.journeyType === "WORKING_PROFESSIONAL" && (

                        <>

                            <div className="review-item">

                                <span>Current Role</span>

                                <strong>
                                    {formData.currentRole}
                                </strong>

                            </div>

                            <div className="review-item">

                                <span>Experience</span>

                                <strong>
                                    {formData.yearsOfExperience} Years
                                </strong>

                            </div>

                            <div className="review-item">

                                <span>Career Goal</span>

                                <strong>
                                    {formData.careerGoal}
                                </strong>

                            </div>

                            <div className="review-item">

                                <span>Preferred Language</span>

                                <strong>
                                    {formData.preferredLanguage}
                                </strong>

                            </div>

                        </>

                    )
                }

            </div>

            <div className="onboarding-actions">

                <button
                    type="button"
                    className="secondary-btn"
                    onClick={() => setStep(2)}
                >
                    Back
                </button>

                <button
                    type="button"
                    className="primary-btn"
                    onClick={onSubmit}
                >
                    Done
                </button>

            </div>

        </div>

    );

}