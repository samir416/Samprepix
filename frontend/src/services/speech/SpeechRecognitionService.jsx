class SpeechRecognitionService {

    constructor() {

        const Recognition =
            window.SpeechRecognition ||
            window.webkitSpeechRecognition;

        this.recognition = Recognition ? new Recognition() : null;

        this.isListening = false;

        this.onTranscript = null;

        this.onError = null;

        this.onStateChange = null;

        if (!this.recognition) return;

        this.recognition.lang = "en-IN";

        this.recognition.continuous = true;

        this.recognition.interimResults = true;

        this.recognition.maxAlternatives = 1;

        this.initializeEvents();
    }

    initializeEvents() {

        this.recognition.onstart = () => {

    this.isListening = true;

    if (this.onStateChange) {

        this.onStateChange(true);

    }

};

        this.recognition.onend = () => {

    this.isListening = false;

    if (this.onStateChange) {

        this.onStateChange(false);

    }

};

        this.recognition.onerror = (event) => {

            if (this.onError) {

                this.onError(event.error);
            }
        };

        this.recognition.onresult = (event) => {

    for (

        let i = event.resultIndex;

        i < event.results.length;

        i++

    ) {

        const transcript = event.results[i][0].transcript;

        const isFinal = event.results[i].isFinal;

        if (this.onTranscript) {

            this.onTranscript(transcript, isFinal);

        }

    }

};
    }

    start() {

        if (!this.recognition) return;

        if (this.isListening) return;

        this.recognition.start();
    }

    stop() {

        if (!this.recognition) return;

        this.recognition.stop();
    }

    abort() {

        if (!this.recognition) return;

        this.recognition.abort();
    }

    setTranscriptListener(callback) {

        this.onTranscript = callback;
    }

    setErrorListener(callback) {

        this.onError = callback;
    }

    setStateListener(callback) {

    this.onStateChange = callback;

}

}

export default new SpeechRecognitionService();