import { useEffect, useState } from "react";
import DifficultyModal from "./DifficultyModal";
import InterviewCountdown from "./InterviewCountdown";

import {
    FiMic,
    FiMicOff,
    FiVolume2,
    FiVolumeX,
    FiPhoneOff,
    FiPlay
} from "react-icons/fi";

import { startInterview, submitAnswer } from "../services/interviewService";
import { submitFeedback } from "../services/feedbackService";
import "../styles/mockInterview.css";
import "../styles/AIOrb.css";
import AIOrb from "./AIOrb";
import { SpeechSynthesisService, SpeechRecognitionService } from "../services/speech";
import FirstInterviewFeedbackModal from "../feedback/FirstInterviewFeedbackModal";


export default function MockInterview() {

    const [started, setStarted] = useState(false);

    const [micMuted, setMicMuted] = useState(false);

    const [isListening, setIsListening] = useState(false);

    const [speakerMuted, setSpeakerMuted] = useState(false);

    const [isAiSpeaking, setIsAiSpeaking] = useState(false);

    const [seconds, setSeconds] = useState(0);

    const [sessionId, setSessionId] = useState(null);

    const [currentQuestion, setCurrentQuestion] = useState("");

    const [voiceEnabled, setVoiceEnabled] = useState(true);

    const [questionNumber, setQuestionNumber] = useState(1);

    const [loading, setLoading] = useState(false);

    const [answer, setAnswer] = useState("");

    const [submittedAnswer, setSubmittedAnswer] = useState("");

    const [showFeedbackModal, setShowFeedbackModal] = useState(false);

    const [firstInterviewCompleted, setFirstInterviewCompleted] = useState(false);

    const [answeredAtLeastOneQuestion, setAnsweredAtLeastOneQuestion] = useState(false);

    const [showDifficultyModal, setShowDifficultyModal] = useState(false);

    const [selectedDifficulty, setSelectedDifficulty] = useState("");

    const [showCountdown, setShowCountdown] = useState(false);

    const [countdown, setCountdown] = useState(4);

    const [pendingInterview, setPendingInterview] = useState(null);


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

            console.log("Speech Recognition is not supported in this browser.");
            

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

         SpeechSynthesisService.speak(

    currentQuestion,

    () => {

        if (

            SpeechRecognitionService.recognition &&

            !SpeechRecognitionService.isListening &&

            !micMuted

        ) {

            SpeechRecognitionService.start();

        }

    }

);


        }, 700);

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

        if (!isFinal) {

            return;

        }

        setAnswer((previousAnswer) => {

            const cleanText = text.trim();

            if (!cleanText) {

                return previousAnswer;

            }

            if (

                previousAnswer

                    .toLowerCase()

                    .includes(cleanText.toLowerCase())

            ) {

                return previousAnswer;

            }

            if (!previousAnswer.trim()) {

                return cleanText;

            }

            return previousAnswer + " " + cleanText;

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

     SpeechSynthesisService.setSpeakingStateListener(

    (speaking) => {

        console.log("AI Speaking:", speaking);

        setIsAiSpeaking(speaking);

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

    useEffect(() => {

    if (!showCountdown) {

        return;

    }

   if (countdown === 0) {

    setShowCountdown(false);

    setTimeout(() => {

        startInterviewSession();

    }, 350);

    return;

}


    const timer = setTimeout(() => {

        setCountdown((prev) => prev - 1);

    }, 1000);

    return () => clearTimeout(timer);

}, [

    showCountdown,

    countdown

]);


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

    const startInterviewSession = () => {

    if (!pendingInterview) {

        return;

    }

    setSessionId(pendingInterview.sessionId);

   setSessionId(pendingInterview.sessionId);

setQuestionNumber(1);

setSeconds(0);

setStarted(true);

setAnswer("");

setSubmittedAnswer("");

setCurrentQuestion(pendingInterview.firstQuestion);


    setPendingInterview(null);

};


    const handleStart = () => {

        setShowDifficultyModal(true);

    };

  const handleDifficultyStart = async () => {

    try {


        const response = await startInterview({

            interviewType: "HR",

            totalQuestions: 5

        });

        setPendingInterview(response.data);

        setShowDifficultyModal(false);

        setCountdown(3);

        setShowCountdown(true);

    }

    catch (error) {

        console.error(error);

        console.log("Failed to start interview", error);
        
    }



};




    const resetInterview = () => {

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

        setSubmittedAnswer("");

        setLoading(false);

        setAnsweredAtLeastOneQuestion(false);
    };

    /* END */

    const handleEnd = () => {

        SpeechRecognitionService.stop();
        SpeechSynthesisService.stop();


        if (answeredAtLeastOneQuestion) {

            setStarted(false);

            setShowFeedbackModal(true);

            return;

        }

        resetInterview();

    };

    const handleSubmit = async () => {

        SpeechRecognitionService.stop();

        if (!answer.trim()) {

            return;
        }

        try {

            setLoading(true);

            setSubmittedAnswer(answer);

            setAnsweredAtLeastOneQuestion(true);

            const response = await submitAnswer({

                sessionId: sessionId,
                answer: answer

            });

            if (response.data.completed) {

                SpeechSynthesisService.stop();

                SpeechRecognitionService.stop();

                setStarted(false);

                setFirstInterviewCompleted(true);

                setShowFeedbackModal(true);

                return;
            }

           SpeechRecognitionService.stop();

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

        <>

       <section
    className={
        showDifficultyModal
            ? "mock-page modal-open"
            : "mock-page"
    }
>

    


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

                     <AIOrb

    speaking={isAiSpeaking}

    listening={isListening}

/>

                    </div>


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
    disabled={showCountdown}
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

        <DifficultyModal

    open={showDifficultyModal}

    selectedDifficulty={selectedDifficulty}

    setSelectedDifficulty={setSelectedDifficulty}

    loading={false}

    onClose={() => {

        setShowDifficultyModal(false);

    }}

    onStart={handleDifficultyStart}

/>

{

    showCountdown && (

        <InterviewCountdown

            count={countdown}

        />

    )

}

<FirstInterviewFeedbackModal

    isOpen={showFeedbackModal}

    onClose={() => {

        setShowFeedbackModal(false);

        resetInterview();

    }}

    onSubmit={async (feedback) => {

        try {

            await submitFeedback({

                sessionId,

                rating: feedback.rating,

                suggestion: feedback.suggestion

            });

        }

        catch (error) {

            console.error(error);

        }

        finally {

            setShowFeedbackModal(false);

            resetInterview();

        }

    }}

/>

        </>

        
    );
}