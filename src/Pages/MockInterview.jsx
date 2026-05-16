import { useEffect, useState } from "react";

import {
    FiMic,
    FiMicOff,
    FiVolume2,
    FiVolumeX,
    FiPhoneOff,
    FiPlay
} from "react-icons/fi";

import "../styles/mockInterview.css";

export default function MockInterview() {

    const [started, setStarted] = useState(false);

    const [micMuted, setMicMuted] = useState(false);

    const [speakerMuted, setSpeakerMuted] = useState(false);

    const [seconds, setSeconds] = useState(0);

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

    const handleStart = () => {

        setStarted(true);

        setSeconds(0);
    };

    /* END */

    const handleEnd = () => {

        setStarted(false);

        setMicMuted(false);

        setSpeakerMuted(false);

        setSeconds(0);
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

                        Question No-1

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
                                    >

                                        <FiPlay />

                                        Start Interview

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
                                            onClick={() =>
                                                setMicMuted(!micMuted)
                                            }
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
                                            onClick={() =>
                                                setSpeakerMuted(!speakerMuted)
                                            }
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

                            Tell me about a time you led a tough project.

                        </p>

                        <p>

                            <span>
                                You:
                            </span>

                            Last semester I led a team of four to ship our college fest app...

                        </p>

                        <div className="typing-box">

                            Type instead

                        </div>

                    </div>

                </div>

            </div>

            {/* CURRENT QUESTION */}

            <div className="current-question-card">

                <h4>
                    Current Question
                </h4>

                <p>

                    “Tell me about a time you had to push back on a stakeholder.
                    How did you handle it?”

                </p>

            </div>

        </section>
    );
}