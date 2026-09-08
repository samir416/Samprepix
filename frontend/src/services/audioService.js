/**
 * Reusable Production Audio Service for AI Placement Platform
 * Features:
 *  - Native Web Audio API synthesized audio cues (success, error, timer, completion, notification)
 *  - Native Web Speech API text-to-speech for interview and question read-aloud
 *  - Full integration with user's Settings sound toggle ("setting_sound_enabled")
 *  - Lazy AudioContext initialization conforming to browser autoplay security policies
 *  - Zero external MP3/WAV dependencies or paid APIs
 */

class AudioService {
    constructor() {
        this.audioCtx = null;
        this.synth = typeof window !== "undefined" ? window.speechSynthesis : null;
        this.selectedVoice = null;
        this.voicesLoaded = false;
        this.currentUtterance = null;

        if (typeof window !== "undefined" && this.synth) {
            this.initVoices();
        }
    }

    isSoundEnabled() {
        if (typeof window === "undefined") return false;
        return localStorage.getItem("setting_sound_enabled") !== "false";
    }

    setSoundEnabled(enabled) {
        if (typeof window === "undefined") return;
        localStorage.setItem("setting_sound_enabled", String(enabled));
        if (!enabled) {
            this.stopSpeaking();
        }
    }

    getAudioContext() {
        if (typeof window === "undefined") return null;
        try {
            if (!this.audioCtx) {
                const AudioContextClass = window.AudioContext || window.webkitAudioContext;
                if (AudioContextClass) {
                    this.audioCtx = new AudioContextClass();
                }
            }
            if (this.audioCtx && this.audioCtx.state === "suspended") {
                this.audioCtx.resume().catch(() => {});
            }
        } catch (err) {
            console.debug("AudioContext initialization/resume notice:", err);
        }
        return this.audioCtx;
    }

    /**
     * Synthesize a smooth tone burst
     */
    playTone(freq, type = "sine", duration = 0.15, startTime = 0, gainLevel = 0.15) {
        if (!this.isSoundEnabled()) return;
        const ctx = this.getAudioContext();
        if (!ctx) return;

        try {
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();

            osc.type = type;
            osc.frequency.setValueAtTime(freq, ctx.currentTime + startTime);

            gain.gain.setValueAtTime(gainLevel, ctx.currentTime + startTime);
            gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + startTime + duration);

            osc.connect(gain);
            gain.connect(ctx.destination);

            osc.start(ctx.currentTime + startTime);
            osc.stop(ctx.currentTime + startTime + duration);
        } catch (err) {
            // Non-critical audio failure fallback
            console.debug("Web Audio tone playback error:", err);
        }
    }

    /**
     * Synthesize a tone burst without checking isSoundEnabled (for Settings test audio preview)
     */
    playToneDirect(freq, type = "sine", duration = 0.15, startTime = 0, gainLevel = 0.15) {
        const ctx = this.getAudioContext();
        if (!ctx) return;

        try {
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();

            osc.type = type;
            osc.frequency.setValueAtTime(freq, ctx.currentTime + startTime);

            gain.gain.setValueAtTime(gainLevel, ctx.currentTime + startTime);
            gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + startTime + duration);

            osc.connect(gain);
            gain.connect(ctx.destination);

            osc.start(ctx.currentTime + startTime);
            osc.stop(ctx.currentTime + startTime + duration);
        } catch (err) {
            console.debug("Web Audio direct tone playback error:", err);
        }
    }

    /**
     * Pleasant 2-tone ascending chime for passed tests / correct submissions
     */
    playSuccessChime() {
        if (!this.isSoundEnabled()) return;
        this.playTone(523.25, "sine", 0.12, 0.0, 0.12); // C5
        this.playTone(659.25, "sine", 0.22, 0.1, 0.15); // E5
    }

    /**
     * Subtle low tone for test failure or compilation errors
     */
    playErrorChime() {
        if (!this.isSoundEnabled()) return;
        this.playTone(329.63, "triangle", 0.15, 0.0, 0.12); // E4
        this.playTone(261.63, "sine", 0.22, 0.1, 0.12);     // C4
    }

    /**
     * Urgent but gentle tick for timer countdown (e.g. final 60 seconds)
     */
    playTimerWarning() {
        if (!this.isSoundEnabled()) return;
        this.playTone(880, "sine", 0.05, 0.0, 0.08); // A5
    }

    /**
     * Celebratory 3-tone chord for aptitude assessment completion or streak milestone
     */
    playCompletionChime() {
        if (!this.isSoundEnabled()) return;
        this.playTone(523.25, "sine", 0.12, 0.0, 0.12); // C5
        this.playTone(659.25, "sine", 0.15, 0.1, 0.14); // E5
        this.playTone(783.99, "sine", 0.35, 0.22, 0.18); // G5
    }

    /**
     * Soft UI pop chime for notifications
     */
    playNotificationChime() {
        if (!this.isSoundEnabled()) return;
        this.playTone(587.33, "sine", 0.08, 0.0, 0.1);  // D5
        this.playTone(880.00, "sine", 0.18, 0.07, 0.12); // A5
    }

    /**
     * Clear, rich synthesized melodic chime for audio testing in Settings
     */
    previewSound(force = false) {
        if (!force && !this.isSoundEnabled()) return;
        this.playToneDirect(523.25, "sine", 0.12, 0.0, 0.16);  // C5
        this.playToneDirect(659.25, "sine", 0.14, 0.08, 0.18); // E5
        this.playToneDirect(783.99, "sine", 0.28, 0.16, 0.22); // G5
    }

    /* =========================================================
       WEB SPEECH API (READ-ALOUD & MOCK INTERVIEW QUESTIONS)
    ========================================================= */

    initVoices() {
        if (!this.synth) return;
        const load = () => {
            const voices = this.synth.getVoices();
            if (voices && voices.length > 0) {
                this.selectedVoice =
                    voices.find((v) => v.lang === "en-IN") ||
                    voices.find((v) => v.lang.startsWith("en-US")) ||
                    voices.find((v) => v.lang.startsWith("en")) ||
                    voices[0];
                this.voicesLoaded = true;
            }
        };
        load();
        if (this.synth.onvoiceschanged !== undefined) {
            this.synth.onvoiceschanged = load;
        }
    }

    isSpeechSupported() {
        return typeof window !== "undefined" && "speechSynthesis" in window;
    }

    speakText(text, onComplete = null, onSpeakingChange = null) {
        if (!this.isSoundEnabled() || !this.isSpeechSupported()) {
            if (onComplete) onComplete();
            return;
        }

        if (!text || !text.trim()) {
            if (onComplete) onComplete();
            return;
        }

        try {
            if (this.synth.speaking) {
                this.synth.cancel();
            }

            const cleanText = text.replace(/[*_#`~[\]()]/g, " ").replace(/\s+/g, " ").trim();
            const utterance = new SpeechSynthesisUtterance(cleanText);

            if (!this.voicesLoaded) {
                this.initVoices();
            }

            if (this.selectedVoice) {
                utterance.voice = this.selectedVoice;
            }

            utterance.lang = "en-US";
            utterance.rate = 1.0;
            utterance.pitch = 1.0;
            utterance.volume = 1.0;

            utterance.onstart = () => {
                if (onSpeakingChange) onSpeakingChange(true);
            };

            utterance.onend = () => {
                this.currentUtterance = null;
                if (onSpeakingChange) onSpeakingChange(false);
                if (onComplete) onComplete();
            };

            utterance.onerror = (e) => {
                this.currentUtterance = null;
                console.debug("Speech synthesis notice:", e);
                if (onSpeakingChange) onSpeakingChange(false);
                if (onComplete) onComplete();
            };

            // Retain reference on instance to protect against Chrome GC drop mid-speech
            this.currentUtterance = utterance;
            this.synth.speak(utterance);
        } catch (err) {
            this.currentUtterance = null;
            console.debug("Failed to invoke speech synthesis:", err);
            if (onSpeakingChange) onSpeakingChange(false);
            if (onComplete) onComplete();
        }
    }

    stopSpeaking() {
        this.currentUtterance = null;
        if (this.isSpeechSupported() && this.synth) {
            try {
                this.synth.cancel();
            } catch (err) {
                // ignore
            }
        }
    }

    isSpeaking() {
        if (!this.isSpeechSupported() || !this.synth) return false;
        return this.synth.speaking;
    }
}

const audioService = new AudioService();
export default audioService;
