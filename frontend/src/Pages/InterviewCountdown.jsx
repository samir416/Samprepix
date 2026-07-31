import "../styles/interviewCountdown.css";

export default function InterviewCountdown({ count }) {

    return (

        <div className="countdown-overlay">

            <div className="countdown-circle">

                {count}

            </div>

        </div>

    );

}