import { useEffect, useState } from "react";

import {
    FiMic,
    FiMicOff,
    FiVolume2,
    FiVolumeX,
    FiPhoneOff,
    FiPlay
} from "react-icons/fi";

import { startInterview, submitAnswer } from "../services/interviewService";
import "../styles/mockInterview.css";
import { SpeechSynthesisService, SpeechRecognitionService } from "../services/speech";

export default function MockInterview() {

    const [started, setStarted] = useState(false);

    const [micMuted, setMicMuted] = useState(false);

    const [isListening, setIsListening] = useState(false);

    const [speakerMuted, setSpeakerMuted] = useState(false);

    const [seconds, setSeconds] = useState(0);

    const [sessionId, setSessionId] = useState(null);

    const [currentQuestion, setCurrentQuestion] = useState("");

    const [voiceEnabled, setVoiceEnabled] = useState(true);

    const [questionNumber, setQuestionNumber] = useState(1);

    const [loading, setLoading] = useState(false);

    const [answer, setAnswer] = useState("");

    const [submittedAnswer, setSubmittedAnswer] = useState("");


    const toggleSpeaker = () => {

    if (speakerMuted) {

        setSpeakerMuted(false);

        if (SpeechSynthesisService.isSpeaking()) {

            SpeechSynthesisService.resume();

        }

    } else {

        setSpeakerMuted(true);

        SpeechSynthesisService.pause();

    }

};

   const toggleMic = () => {

    if (!SpeechRecognitionService.recognition) {

        alert("Speech Recognition is not supported in this browser.");

        return;

    }

    try {

        if (SpeechRecognitionService.isListening) {

            SpeechRecognitionService.stop();

        } else {

            SpeechRecognitionService.start();

        }

    } catch (error) {

        console.error(error);

    }

};

  useEffect(() => {

    if (!started) {

        return;

    }

    if (!voiceEnabled) {

        return;

    }

    if (!currentQuestion?.trim()) {

        return;

    }

    // Speaker OFF hai to new question read mat karo
    if (speakerMuted) {

        return;

    }

    // Agar pause hui speech chal rahi hai to resume karo
    if (SpeechSynthesisService.isSpeaking()) {

        return;

    }

    const timer = setTimeout(() => {

        SpeechSynthesisService.speak(currentQuestion);

    }, 250);

    return () => {

        clearTimeout(timer);

    };

}, [

    started,

    currentQuestion,

    voiceEnabled

]);

    useEffect(() => {

        SpeechRecognitionService.setTranscriptListener(

            (text, isFinal) => {

                if (!isFinal) return;

                setAnswer((previousAnswer) => {

                    if (!previousAnswer.trim()) {

                        return text.trim();

                    }

                    return previousAnswer + " " + text.trim();

                });

            }

        );
        SpeechRecognitionService.setErrorListener(

            (error) => {

                console.error(error);

            }

        );

        SpeechRecognitionService.setStateListener(

    (listening) => {

        setIsListening(listening);

        setMicMuted(!listening);

    }

);

    }, []);


    /* TIMER */

    useEffect(() => {

        let interval = null;

        if (started) {

            interval = setInterval(() => {

                setSeconds((prev) => prev + 1);

            }, 1000);
        }

        else {

            clearInterval(interval);
        }

        return () => clearInterval(interval);

    }, [started]);

    /* FORMAT TIMER */

    const formatTime = () => {

        const mins = String(
            Math.floor(seconds / 60)
        ).padStart(2, "0");

        const secs = String(
            seconds % 60
        ).padStart(2, "0");

        return `${mins}:${secs}`;
    };

    /* START */

    const handleStart = async () => {

        try {

            setLoading(true);

            const response = await startInterview({

                interviewType: "HR",
                totalQuestions: 5

            });

            setSessionId(response.data.sessionId);

            setCurrentQuestion(response.data.firstQuestion);

            setQuestionNumber(1);

            setStarted(true);

            setSeconds(0);

        } catch (error) {

            console.error(error);

            alert("Failed to start interview.");

        } finally {

            setLoading(false);
        }
    };


    /* END */

    const handleEnd = () => {

        SpeechSynthesisService.stop();

        SpeechRecognitionService.stop();

        setStarted(false);

        setMicMuted(false);

        setSpeakerMuted(false);

        setSeconds(0);

        setSessionId(null);

        setCurrentQuestion("");

        setQuestionNumber(1);

        setAnswer("");
    };

    const handleSubmit = async () => {

        if (!answer.trim()) {

            return;
        }

        try {

            setLoading(true);

            setSubmittedAnswer(answer);

            const response = await submitAnswer({

                sessionId: sessionId,
                answer: answer

            });

            if (response.data.completed) {

                SpeechSynthesisService.stop();

                SpeechRecognitionService.stop();

                alert("Interview Completed!");

                return;
            }

            setCurrentQuestion(response.data.nextQuestion);

            setQuestionNumber((prev) => prev + 1);

            setAnswer("");

        } catch (error) {

            console.error(error);

            alert("Failed to submit answer.");

        } finally {

            setLoading(false);
        }

    };


    return (

        <section className="mock-page">

            {/* HEADER */}

            <div className="mock-header">

                <div>

                    <h1>
                        AI Mock Interview
                    </h1>

                    <p>
                        Behavioral · Frontend Engineer · Google
                    </p>

                </div>

                {/* TIMER */}

                {

                    started && (

                        <div className="timer-box">

                            {formatTime()}

                        </div>

                    )

                }

            </div>

            {/* MAIN */}

            <div className="mock-grid">

                {/* LEFT */}

                <div className="ai-panel">

                    {/* QUESTION */}

                    <div className="question-badge">

                        Question No - {questionNumber}
                    </div>

                    {/* AI ORB */}

                    <div className="ai-orb-wrapper">

                        <div className="ai-orb">

                            <span>
                                AI
                            </span>

                        </div>

                    </div>

                    {/* STATUS */}

                    {/* STATUS */}

                    {

                        started && (

                            <div className="listening-box">

                                <div className="green-dot"></div>

                                Listening...

                            </div>

                        )

                    }

                    {/* CONTROLS */}

                    <div className="controls-wrapper">

                        {

                            !started

                                ? (

                                    <button
                                        className="start-btn"
                                        onClick={handleStart}
                                        disabled={loading}
                                    >
                                        <FiPlay />
                                        {loading ? "Starting..." : "Start Interview"}
                                    </button>

                                )

                                : (

                                    <div className="controls-row">

                                        {/* MIC */}

                                        <button
                                            className={
                                                micMuted
                                                    ? "control-btn active-control"
                                                    : "control-btn"
                                            }
                                            onClick={toggleMic}
                                        >

                                            {
                                                micMuted
                                                    ? <FiMicOff />
                                                    : <FiMic />
                                            }

                                        </button>

                                        {/* SPEAKER */}

                                        <button
                                            className={
                                                speakerMuted
                                                    ? "control-btn active-control"
                                                    : "control-btn"
                                            }
                                            onClick={toggleSpeaker}

                                        >

                                            {
                                                speakerMuted
                                                    ? <FiVolumeX />
                                                    : <FiVolume2 />
                                            }

                                        </button>

                                        {/* END */}

                                        <button
                                            className="end-btn"
                                            onClick={handleEnd}
                                        >

                                            <FiPhoneOff />

                                        </button>

                                    </div>

                                )

                        }

                    </div>

                </div>

                {/* RIGHT */}

                <div className="feedback-column">

                    {/* FEEDBACK */}

                    <div className="feedback-card">

                        <h3>
                            Real-time feedback
                        </h3>

                        {/* ITEM */}

                        <div className="feedback-item">

                            <div className="feedback-top">

                                <span>
                                    Clarity
                                </span>

                                <span>
                                    84
                                </span>

                            </div>

                            <div className="feedback-bar">

                                <div
                                    className="feedback-fill"
                                    style={{
                                        width: "84%"
                                    }}
                                ></div>

                            </div>

                        </div>

                        {/* ITEM */}

                        <div className="feedback-item">

                            <div className="feedback-top">

                                <span>
                                    Confidence
                                </span>

                                <span>
                                    72
                                </span>

                            </div>

                            <div className="feedback-bar">

                                <div
                                    className="feedback-fill"
                                    style={{
                                        width: "72%"
                                    }}
                                ></div>

                            </div>

                        </div>

                        {/* ITEM */}

                        <div className="feedback-item">

                            <div className="feedback-top">

                                <span>
                                    Structure
                                </span>

                                <span>
                                    90
                                </span>

                            </div>

                            <div className="feedback-bar">

                                <div
                                    className="feedback-fill"
                                    style={{
                                        width: "90%"
                                    }}
                                ></div>

                            </div>

                        </div>

                    </div>

                    {/* TRANSCRIPT */}

                    <div className="transcript-card">

                        <h3>
                            Transcript
                        </h3>

                        <p>

                            <span>
                                AI:
                            </span>

                            {currentQuestion || "Click Start Interview to begin."}

                        </p>

                        <p>

                            <span>
                                You:
                            </span>

                            {submittedAnswer
                                ? submittedAnswer.length > 120
                                    ? submittedAnswer.substring(0, 120) + "..."
                                    : submittedAnswer
                                : "Your answer will appear here."
                            }

                        </p>

                        <textarea
                            className="typing-box"
                            placeholder="Type your answer..."
                            value={answer}
                            onChange={(e) => setAnswer(e.target.value)}
                        />

                        <button
                            className="submit-answer-btn"
                            onClick={handleSubmit}
                            disabled={loading || !answer.trim()}
                        >
                            Submit Answer
                        </button>

                    </div>

                </div>

            </div>

            {/* CURRENT QUESTION */}

            <div className="current-question-card">

                <h4>
                    Current Question
                </h4>

                <p>
                    {currentQuestion || "Click Start Interview to begin."}
                </p>

            </div>

        </section>
    );
}