import { FiCheck, FiX } from "react-icons/fi";
import "../styles/difficultyModal.css";

const difficulties = [
    {
        id: "easy",
        title: "Easy",
        subtitle: "Beginner Friendly Interview",
        duration: "Approx. 8 - 10 Minutes"
    },
    {
        id: "medium",
        title: "Medium",
        subtitle: "Balanced Interview Experience",
        duration: "Approx. 10 - 15 Minutes"
    },
    {
        id: "hard",
        title: "Hard",
        subtitle: "Advanced Level Challenge",
        duration: "Approx. 15 - 20 Minutes"
    }
];

export default function DifficultyModal({
    open,
    selectedDifficulty,
    setSelectedDifficulty,
    onClose,
    onStart,
    loading = false
}) {

    if (!open) return null;

    return (

        <div
            className="difficulty-overlay"
            onClick={onClose}
        >

            <div
                className="difficulty-modal"
                onClick={(e) => e.stopPropagation()}
            >

                <button
                    className="difficulty-close"
                    onClick={onClose}
                >
                    <FiX />
                </button>

                <div className="difficulty-header">

                    <h2>
                        Choose Difficulty
                    </h2>

                    <p>
                        Select your preferred interview difficulty.
                        This only affects question complexity.
                    </p>

                </div>

                <div className="difficulty-list">

                    {

                        difficulties.map((item) => (

                            <div

                                key={item.id}

                                className={
                                    selectedDifficulty === item.id
                                        ? "difficulty-card active"
                                        : "difficulty-card"
                                }

                                onClick={() =>
                                    setSelectedDifficulty(item.id)
                                }

                            >

                                <div className="difficulty-info">

                                    <div className="difficulty-title">

                                        {item.title}

                                    </div>

                                    <div className="difficulty-subtitle">

                                        {item.subtitle}

                                    </div>

                                    <div className="difficulty-duration">

                                        {item.duration}

                                    </div>

                                </div>

                                {

                                    selectedDifficulty === item.id && (

                                        <div className="difficulty-check">

                                            <FiCheck />

                                        </div>

                                    )

                                }

                            </div>

                        ))

                    }

                </div>

                <div className="difficulty-footer">

                    <button
                        className="difficulty-cancel-btn"
                        onClick={onClose}
                    >
                        Cancel
                    </button>

                    <button
                        className="difficulty-start-btn"
                        disabled={
                            !selectedDifficulty || loading
                        }
                        onClick={onStart}
                    >
                        {

                            loading
                                ? "Starting..."
                                : "Start Interview"

                        }
                    </button>

                </div>

            </div>

        </div>

    );

}
