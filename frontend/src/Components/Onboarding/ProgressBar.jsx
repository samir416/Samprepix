export default function ProgressBar({ step }) {

    return (

        <div className="progress-wrapper">

            <div className="progress-item">

                <div className={step >= 1 ? "progress-circle active" : "progress-circle"}>
                    1
                </div>

                <span>
                    Journey
                </span>

            </div>

            <div className={step >= 2 ? "progress-line active" : "progress-line"}></div>

            <div className="progress-item">

                <div className={step >= 2 ? "progress-circle active" : "progress-circle"}>
                    2
                </div>

                <span>
                    Details
                </span>

            </div>

            <div className={step >= 3 ? "progress-line active" : "progress-line"}></div>

            <div className="progress-item">

                <div className={step >= 3 ? "progress-circle active" : "progress-circle"}>
                    3
                </div>

                <span>
                    Review
                </span>

            </div>

        </div>

    );

}