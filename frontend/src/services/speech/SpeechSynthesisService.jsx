import audioService from "../audioService";

class SpeechSynthesisService {
    constructor() {
        this.synth = typeof window !== "undefined" ? window.speechSynthesis : null;
        this.onSpeakingStateChange = null;
    }

    isSupported() {
        return audioService.isSpeechSupported();
    }

    speak(text, onComplete = null) {
        if (!text || !text.trim()) {
            if (onComplete) onComplete();
            return;
        }

        audioService.speakText(
            text,
            () => {
                if (typeof onComplete === "function") {
                    onComplete();
                }
            },
            (isSpeaking) => {
                if (this.onSpeakingStateChange) {
                    this.onSpeakingStateChange(isSpeaking);
                }
            }
        );
    }

    stop() {
        audioService.stopSpeaking();
        if (this.onSpeakingStateChange) {
            this.onSpeakingStateChange(false);
        }
    }

    pause() {
        if (this.synth && this.synth.speaking && !this.synth.paused) {
            this.synth.pause();
        }
    }

    resume() {
        if (this.synth && this.synth.paused) {
            this.synth.resume();
        }
    }

    isSpeaking() {
        return audioService.isSpeaking();
    }

    setSpeakingStateListener(callback) {
        this.onSpeakingStateChange = callback;
    }
}

export default new SpeechSynthesisService();