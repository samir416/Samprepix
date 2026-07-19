import React, { useEffect, useState } from "react";
import {
  Star,
  MessageSquareText,
  Sparkles,
  CheckCircle2,
} from "lucide-react";
import "../styles/FirstInterviewFeedbackModal.css";


const FirstInterviewFeedbackModal = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [suggestion, setSuggestion] = useState("");
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "auto";
    }

    return () => {
      document.body.style.overflow = "auto";
    };
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) {
      setRating(0);
      setHoverRating(0);
      setSuggestion("");
      setSubmitted(false);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = () => {
    if (rating === 0) return;

    if (onSubmit) {
      onSubmit({
        rating,
        suggestion,
      });
    }

    setSubmitted(true);

    setTimeout(() => {
      if (onClose) onClose();
    }, 1800);
  };

  return (
    <div className="feedback-overlay">
      <div
        className={`feedback-modal ${
          submitted ? "feedback-success-mode" : ""
        }`}
      >
        {!submitted ? (
          <>
            <div className="feedback-header">
              <div className="feedback-icon">
                <Sparkles size={24} />
              </div>

              <h3>Share Your Feedback</h3>

              <p>
                Thank you for completing your first AI Interview.
                Your feedback helps us improve the experience for
                every learner.
              </p>
            </div>

            <div className="feedback-divider"></div>

            <div className="feedback-rating-section">
              <span className="feedback-label">
                Overall Experience
              </span>

              <div className="feedback-stars">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    className="star-button"
                    onMouseEnter={() => setHoverRating(star)}
                    onMouseLeave={() => setHoverRating(0)}
                    onClick={() => setRating(star)}
                    aria-label={`Rate ${star}`}
                  >
                    <Star
                      size={34}
                      className={
                        star <= (hoverRating || rating)
                          ? "star-filled"
                          : "star-empty"
                      }
                      fill={
                        star <= (hoverRating || rating)
                          ? "currentColor"
                          : "none"
                      }
                    />
                  </button>
                ))}
              </div>

              {rating > 0 && (
                <span className="rating-text">
                  {
                    [
                      "",
                      "Poor",
                      "Fair",
                      "Good",
                      "Very Good",
                      "Excellent",
                    ][rating]
                  }
                </span>
              )}
            </div>

            <div className="feedback-divider"></div>

            <div className="feedback-input-section">
              <label>
                <MessageSquareText size={18} />
                Suggestions (Optional)
              </label>

              <textarea
                maxLength={500}
                value={suggestion}
                onChange={(e) =>
                  setSuggestion(e.target.value)
                }
                placeholder="Tell us what we can improve..."
              />

              <div className="character-counter">
                {suggestion.length}/500
              </div>
            </div>

            <div className="feedback-actions">
              <button
                className="feedback-later-btn"
                onClick={onClose}
              >
                Later
              </button>

              <button
                className="feedback-submit-btn"
                disabled={rating === 0}
                onClick={handleSubmit}
              >
                Submit Feedback
              </button>
            </div>
          </>
        ) : (
          <div className="feedback-success">
            <div className="success-icon">
              <CheckCircle2 size={60} />
            </div>

            <h3>Thank You!</h3>

            <p>
              Your feedback has been received successfully.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default FirstInterviewFeedbackModal;