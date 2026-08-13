import { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { toast } from "react-toastify";
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

import {
    startInterview,
    submitAnswer,
    endInterview
} from "../services/interviewService";

import { getProfile } from "../services/profileService";
import { submitFeedback } from "../services/feedbackService";
import "../styles/mockInterview.css";
import "../styles/AIOrb.css";
import AIOrb from "./AIOrb";

import {
    SpeechSynthesisService,
    SpeechRecognitionService
} from "../services/speech";

import FirstInterviewFeedbackModal from "../feedback/FirstInterviewFeedbackModal";

export default function MockInterview() {

    const navigate = useNavigate();
    const location = useLocation();

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

    const [technicalAccuracy, setTechnicalAccuracy] = useState(null);

    const [completeness, setCompleteness] = useState(null);

    const [communication, setCommunication] = useState(null);

    const [showDifficultyModal, setShowDifficultyModal] = useState(false);

    const [selectedDifficulty, setSelectedDifficulty] = useState("");

    const [showCountdown, setShowCountdown] = useState(false);

    const [countdown, setCountdown] = useState(4);

    const [pendingInterview, setPendingInterview] = useState(null);

    const [currentUser, setCurrentUser] = useState(() =>
        JSON.parse(localStorage.getItem("user")) || {}
    );

    const [profileLoading, setProfileLoading] = useState(true);

    const spokenQuestionRef = useRef(null);
    const aiSpeakingRef = useRef(false);
    const recognitionRestartTimerRef = useRef(null);

    const technicalSkills = Array.isArray(currentUser?.skills)
        ? currentUser.skills
        : typeof currentUser?.skills === "string"
            ? currentUser.skills
                .split(",")
                .map((skill) => skill.trim())
                .filter(Boolean)
            : [];

    const profileCompleted =
        Boolean(currentUser?.targetRole?.trim()) &&
        technicalSkills.length > 0;

    useEffect(() => {

        const openFeedback =
            location.state?.openFeedback === true &&
            location.state?.fromResult === true &&
            location.state?.sessionId;

        if (!openFeedback) {
            return;
        }

        const feedbackSessionId =
            location.state.sessionId;

        setSessionId(feedbackSessionId);

        setShowFeedbackModal(true);

        navigate(
            location.pathname,
            {
                replace: true,
                state: null
            }
        );

    }, [
        location.state,
        location.pathname,
        navigate
    ]);

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

            console.log(
                "Speech Recognition is not supported in this browser."
            );

            return;

        }

        if (aiSpeakingRef.current) {

            SpeechRecognitionService.stop();

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

        if (speakerMuted) {
            return;
        }

        const questionKey =
            `${questionNumber}:${currentQuestion.trim()}`;

        if (
            spokenQuestionRef.current === questionKey
        ) {
            return;
        }

        if (aiSpeakingRef.current) {
            return;
        }

        if (SpeechSynthesisService.isSpeaking()) {
            return;
        }

        if (
            SpeechRecognitionService.recognition &&
            SpeechRecognitionService.isListening
        ) {
            SpeechRecognitionService.stop();
        }

        spokenQuestionRef.current = questionKey;

        const timer = setTimeout(() => {

            if (
                speakerMuted ||
                !voiceEnabled ||
                !started
            ) {
                return;
            }

            if (
                SpeechRecognitionService.recognition &&
                SpeechRecognitionService.isListening
            ) {
                SpeechRecognitionService.stop();
            }

            SpeechSynthesisService.speak(
                currentQuestion,
                () => {

                    if (
                        !started ||
                        speakerMuted ||
                        !voiceEnabled ||
                        micMuted ||
                        aiSpeakingRef.current
                    ) {
                        return;
                    }

                    if (
                        SpeechRecognitionService.recognition &&
                        !SpeechRecognitionService.isListening
                    ) {

                        if (
                            recognitionRestartTimerRef.current
                        ) {
                            clearTimeout(
                                recognitionRestartTimerRef.current
                            );
                        }

                        recognitionRestartTimerRef.current =
                            setTimeout(() => {

                                if (
                                    !started ||
                                    speakerMuted ||
                                    !voiceEnabled ||
                                    micMuted ||
                                    aiSpeakingRef.current
                                ) {
                                    return;
                                }

                                try {

                                    SpeechRecognitionService.start();

                                } catch (error) {

                                    console.error(error);

                                }

                            }, 250);

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
        questionNumber
    ]);

    useEffect(() => {

        SpeechRecognitionService.setTranscriptListener(

            (text, isFinal) => {

                if (
                    !isFinal ||
                    aiSpeakingRef.current ||
                    SpeechSynthesisService.isSpeaking()
                ) {

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
                            .includes(
                                cleanText.toLowerCase()
                            )
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

                if (aiSpeakingRef.current) {

                    if (listening) {

                        SpeechRecognitionService.stop();

                    }

                    setIsListening(false);

                    setMicMuted(true);

                    return;

                }

                setIsListening(listening);

                setMicMuted(!listening);

            }

        );

        SpeechSynthesisService.setSpeakingStateListener(

            (speaking) => {

                aiSpeakingRef.current = speaking;

                setIsAiSpeaking(speaking);

                if (speaking) {

                    if (
                        recognitionRestartTimerRef.current
                    ) {

                        clearTimeout(
                            recognitionRestartTimerRef.current
                        );

                        recognitionRestartTimerRef.current = null;

                    }

                    SpeechRecognitionService.stop();

                    setIsListening(false);

                    setMicMuted(true);

                }

            }

        );

        return () => {

            if (
                recognitionRestartTimerRef.current
            ) {

                clearTimeout(
                    recognitionRestartTimerRef.current
                );

                recognitionRestartTimerRef.current = null;

            }

        };

    }, []);

    useEffect(() => {

        const loadLatestProfile = async () => {

            try {

                const profile = await getProfile();

                const latestUser =
                    JSON.parse(
                        localStorage.getItem("user")
                    ) || {};

                const updatedUser = {

                    ...latestUser,

                    ...profile

                };

                setCurrentUser(updatedUser);

                localStorage.setItem(
                    "user",
                    JSON.stringify(updatedUser)
                );

            } catch (error) {

                console.error(error);

            } finally {

                setProfileLoading(false);

            }

        };

        loadLatestProfile();

    }, []);

    useEffect(() => {

        let interval = null;

        if (started) {

            interval = setInterval(() => {

                setSeconds(
                    (prev) => prev + 1
                );

            }, 1000);

        } else {

            clearInterval(interval);

        }

        return () =>
            clearInterval(interval);

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

            setCountdown(
                (prev) => prev - 1
            );

        }, 1000);

        return () =>
            clearTimeout(timer);

    }, [
        showCountdown,
        countdown
    ]);

    useEffect(() => {

        return () => {

            if (
                recognitionRestartTimerRef.current
            ) {

                clearTimeout(
                    recognitionRestartTimerRef.current
                );

            }

            SpeechRecognitionService.stop();

            SpeechSynthesisService.stop();

        };

    }, []);

    const formatTime = () => {

        const mins = String(
            Math.floor(seconds / 60)
        ).padStart(2, "0");

        const secs = String(
            seconds % 60
        ).padStart(2, "0");

        return `${mins}:${secs}`;

    };

    const startInterviewSession = () => {

        if (!pendingInterview) {

            return;

        }

        SpeechRecognitionService.stop();

        SpeechSynthesisService.stop();

        spokenQuestionRef.current = null;

        aiSpeakingRef.current = false;

        setSessionId(
            pendingInterview.sessionId
        );

        localStorage.setItem(
            "lastInterviewSessionId",
            String(
                pendingInterview.sessionId
            )
        );

        setQuestionNumber(1);

        setSeconds(0);

        setStarted(true);

        setAnswer("");

        setSubmittedAnswer("");

        setTechnicalAccuracy(null);

        setCompleteness(null);

        setCommunication(null);

        setAnsweredAtLeastOneQuestion(false);

        setCurrentQuestion(
            pendingInterview.question
        );

        setPendingInterview(null);

    };

    const handleStart = () => {

        if (profileLoading) {

            return;

        }

        if (!profileCompleted) {

            toast.warning(
                "Add at least one technical skill to unlock AI interviews.",
                {
                    autoClose: 100
                }
            );

            return;

        }

        setShowDifficultyModal(true);

    };

    const handleDifficultyStart = async () => {

        if (
            loading ||
            !selectedDifficulty
        ) {

            return;

        }

        try {

            setLoading(true);

            const response =
                await startInterview({

                    interviewType:
                        "TECHNICAL",

                    targetRole:
                        currentUser.targetRole,

                    experienceLevel:
                        selectedDifficulty,

                    skills:
                        technicalSkills

                });

            setPendingInterview(
                response.data
            );

            setShowDifficultyModal(false);

            setCountdown(3);

            setShowCountdown(true);

        } catch (error) {

            console.error(error);

            toast.error(
                "Unable to start the interview. Please try again."
            );

        } finally {

            setLoading(false);

        }

    };

    const resetInterview = () => {

        if (
            recognitionRestartTimerRef.current
        ) {

            clearTimeout(
                recognitionRestartTimerRef.current
            );

            recognitionRestartTimerRef.current = null;

        }

        SpeechSynthesisService.stop();

        SpeechRecognitionService.stop();

        spokenQuestionRef.current = null;

        aiSpeakingRef.current = false;

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

        setTechnicalAccuracy(null);

        setCompleteness(null);

        setCommunication(null);

        setPendingInterview(null);

        setSelectedDifficulty("");

        setShowCountdown(false);

        setCountdown(4);

        setFirstInterviewCompleted(false);

    };

    const handleEnd = async () => {

        if (
            recognitionRestartTimerRef.current
        ) {

            clearTimeout(
                recognitionRestartTimerRef.current
            );

            recognitionRestartTimerRef.current = null;

        }

        SpeechRecognitionService.stop();

        SpeechSynthesisService.stop();

        spokenQuestionRef.current = null;

        aiSpeakingRef.current = false;

        if (!answeredAtLeastOneQuestion) {

            resetInterview();

            return;

        }

        if (!sessionId) {

            resetInterview();

            return;

        }

        try {

            setLoading(true);

            setShowFeedbackModal(false);

            await endInterview(
                sessionId
            );

            localStorage.setItem(
                "lastInterviewSessionId",
                String(sessionId)
            );

            setStarted(false);

            navigate(
                `/interview-result?sessionId=${sessionId}`,
                {
                    replace: true,
                    state: {
                        fromInterview: true,
                        sessionId: sessionId
                    }
                }
            );

        } catch (error) {

            console.error(error);

            toast.error(
                "Unable to end the interview. Please try again.",
                {
                    autoClose: 1500
                }
            );

        } finally {

            setLoading(false);

        }

    };

    const handleSubmit = async () => {

        SpeechRecognitionService.stop();

        SpeechSynthesisService.stop();

        spokenQuestionRef.current = null;

        aiSpeakingRef.current = false;

        if (!answer.trim()) {

            return;

        }

        try {

            setLoading(true);

            const response =
                await submitAnswer({

                    sessionId:
                        sessionId,

                    answer:
                        answer

                });

            setSubmittedAnswer(
                answer
            );

            setAnsweredAtLeastOneQuestion(
                true
            );

            setTechnicalAccuracy(
                response.data
                    .technicalAccuracy ??
                null
            );

            setCompleteness(
                response.data
                    .completeness ??
                null
            );

            setCommunication(
                response.data
                    .communication ??
                null
            );

            if (
                response.data.interviewCompleted
            ) {

                SpeechSynthesisService.stop();

                SpeechRecognitionService.stop();

                setStarted(false);

                setFirstInterviewCompleted(true);

                localStorage.setItem(
                    "lastInterviewSessionId",
                    String(sessionId)
                );

                navigate(
                    `/interview-result?sessionId=${sessionId}`,
                    {
                        replace: true,
                        state: {
                            fromInterview: true,
                            sessionId: sessionId
                        }
                    }
                );

                return;

            }

            SpeechRecognitionService.stop();

            setCurrentQuestion(
                response.data.nextQuestion
            );

            setQuestionNumber(
                response.data.questionNumber
            );

            setAnswer("");

        } catch (error) {

            console.error(error);

            toast.error(
                "Unable to submit your answer. Please try again."
            );

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

                <div className="mock-header">

                    <div>

                        <h1>
                            {
                                currentUser?.targetRole ||
                                "AI Mock Interview"
                            }
                        </h1>

                        <p>
                            {
                                Array.isArray(
                                    currentUser?.skills
                                )
                                    ? currentUser.skills.join(
                                        " • "
                                    )
                                    : technicalSkills.join(
                                        " • "
                                    )
                            }
                        </p>

                    </div>

                    {
                        started && (

                            <div className="timer-box">

                                {formatTime()}

                            </div>

                        )
                    }

                </div>

                <div className="mock-grid">

                    <div className="ai-panel">

                        <div className="question-badge">

                            Question No - {
                                questionNumber
                            }

                        </div>

                        <div className="ai-orb-wrapper">

                            <AIOrb

                                speaking={
                                    isAiSpeaking
                                }

                                listening={
                                    isListening
                                }

                            />

                        </div>

                        {
                            started && (

                                <div className="listening-box">

                                    <div className="green-dot"></div>

                                    Listening...

                                </div>
                            )
                        }

                        <div className="controls-wrapper">

                            {
                                !started
                                    ? (

                                        <button
                                            className="start-btn"
                                            onClick={
                                                handleStart
                                            }
                                            disabled={
                                                profileLoading ||
                                                showCountdown
                                            }
                                        >

                                            <FiPlay />

                                            Start Interview

                                        </button>

                                    )
                                    : (

                                        <div className="controls-row">

                                            <button
                                                className={
                                                    micMuted
                                                        ? "control-btn active-control"
                                                        : "control-btn"
                                                }
                                                onClick={
                                                    toggleMic
                                                }
                                            >

                                                {
                                                    micMuted
                                                        ? <FiMicOff />
                                                        : <FiMic />
                                                }

                                            </button>

                                            <button
                                                className={
                                                    speakerMuted
                                                        ? "control-btn active-control"
                                                        : "control-btn"
                                                }
                                                onClick={
                                                    toggleSpeaker
                                                }
                                            >

                                                {
                                                    speakerMuted
                                                        ? <FiVolumeX />
                                                        : <FiVolume2 />
                                                }

                                            </button>

                                            <button
                                                className="end-btn"
                                                onClick={
                                                    handleEnd
                                                }
                                                disabled={
                                                    loading
                                                }
                                            >

                                                <FiPhoneOff />

                                            </button>

                                        </div>

                                    )
                            }

                        </div>

                    </div>

                    <div className="feedback-column">

                        <div className="feedback-card">

                            <h3>
                                Real-time Feedback
                            </h3>

                            <div className="feedback-item">

                                <div className="feedback-top">

                                    <span>
                                        Technical Accuracy
                                    </span>

                                    <span>

                                        {
                                            technicalAccuracy === null
                                                ? "--"
                                                : `${technicalAccuracy}%`
                                        }

                                    </span>

                                </div>

                                <div className="feedback-bar">

                                    <div
                                        className="feedback-fill"
                                        style={{
                                            width:
                                                technicalAccuracy === null
                                                    ? "0%"
                                                    : `${technicalAccuracy}%`
                                        }}
                                    />

                                </div>

                            </div>

                            <div className="feedback-item">

                                <div className="feedback-top">

                                    <span>
                                        Completeness
                                    </span>

                                    <span>

                                        {
                                            completeness === null
                                                ? "--"
                                                : `${completeness}%`
                                        }

                                    </span>

                                </div>

                                <div className="feedback-bar">

                                    <div
                                        className="feedback-fill"
                                        style={{
                                            width:
                                                completeness === null
                                                    ? "0%"
                                                    : `${completeness}%`
                                        }}
                                    />

                                </div>

                            </div>

                            <div className="feedback-item">

                                <div className="feedback-top">

                                    <span>
                                        Communication
                                    </span>

                                    <span>

                                        {
                                            communication === null
                                                ? "--"
                                                : `${communication}%`
                                        }

                                    </span>

                                </div>

                                <div className="feedback-bar">

                                    <div
                                        className="feedback-fill"
                                        style={{
                                            width:
                                                communication === null
                                                    ? "0%"
                                                    : `${communication}%`
                                        }}
                                    />

                                </div>

                            </div>

                        </div>

                        <div className="transcript-card">

                            <h3>
                                Transcript
                            </h3>

                            <p>

                                <span>
                                    AI:
                                </span>

                                {
                                    currentQuestion ||
                                    "Click Start Interview to begin."
                                }

                            </p>

                            <p>

                                <span>
                                    You:
                                </span>

                                {
                                    submittedAnswer
                                        ? submittedAnswer.length > 120
                                            ? submittedAnswer.substring(
                                                0,
                                                120
                                            ) + "..."
                                            : submittedAnswer
                                        : "Your answer will appear here."
                                }

                            </p>

                            <textarea
                                className="typing-box"
                                placeholder="Type your answer..."
                                value={answer}
                                onChange={
                                    (e) =>
                                        setAnswer(
                                            e.target.value
                                        )
                                }
                            />

                            <button
                                className="submit-answer-btn"
                                onClick={
                                    handleSubmit
                                }
                                disabled={
                                    loading ||
                                    !answer.trim() ||
                                    !sessionId
                                }
                            >

                                Submit Answer

                            </button>

                        </div>

                    </div>

                </div>

                <div className="current-question-card">

                    <h4>
                        Current Question
                    </h4>

                    <p>
                        {
                            currentQuestion ||
                            "Click Start Interview to begin."
                        }
                    </p>

                </div>

            </section>

            <DifficultyModal

                open={
                    showDifficultyModal
                }

                selectedDifficulty={
                    selectedDifficulty
                }

                setSelectedDifficulty={
                    setSelectedDifficulty
                }

                loading={
                    loading
                }

                onClose={() => {

                    setShowDifficultyModal(
                        false
                    );

                }}

                onStart={
                    handleDifficultyStart
                }

            />

            {
                showCountdown && (

                    <InterviewCountdown

                        count={
                            countdown
                        }

                    />

                )
            }

            <FirstInterviewFeedbackModal

                isOpen={
                    showFeedbackModal
                }

                onClose={() => {

                    setShowFeedbackModal(
                        false
                    );

                    resetInterview();

                }}

                onSubmit={
                    async (feedback) => {

                        try {

                            await submitFeedback({

                                sessionId,

                                rating:
                                    feedback.rating,

                                suggestion:
                                    feedback.suggestion

                            });

                        } catch (error) {

                            console.error(
                                error
                            );

                        } finally {

                            setShowFeedbackModal(
                                false
                            );

                            resetInterview();

                        }

                    }
                }

            />

        </>

    );

}