import { FiCheck } from "react-icons/fi";
import "../styles/difficultyModal.css";

const difficulties = [
    {
        id: "easy",
        title: "Easy"
    },
    {
        id: "medium",
        title: "Medium"
    },
    {
        id: "hard",
        title: "Hard"
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


                <div className="difficulty-header">

                    <h2>
                        Choose Difficulty
                    </h2>


                </div>

               <div className="difficulty-segment">

    {

        difficulties.map((item) => (

            <button

                key={item.id}

              className={
selectedDifficulty === item.id
? "difficulty-option active"
: "difficulty-option"
}

                onClick={() =>
                    setSelectedDifficulty(item.id)
                }

            >

               <span>

{item.title}

</span>

            </button>

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
