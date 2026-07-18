class SpeechSynthesisService {

    constructor() {

        this.synth = window.speechSynthesis;

        this.selectedVoice = null;

        this.currentUtterance = null;

        this.isReady = false;

        this.loadVoices();

    }

    loadVoices() {

        const load = () => {

            const voices = this.synth.getVoices();

            if (!voices || voices.length === 0) {

                return;

            }

            const filteredVoices = voices.filter((voice) => {

                const name = voice.name.toLowerCase();

                return (

                    !name.includes("natural") &&

                    !name.includes("online")

                );

            });

            this.selectedVoice =

                filteredVoices.find(

                    voice => voice.lang === "en-IN"

                )

                ||

                filteredVoices.find(

                    voice => voice.lang.startsWith("en")

                )

                ||

                filteredVoices[0]

                ||

                null;

            this.isReady = true;

            console.log(
                "Speech Voice Loaded :",
                this.selectedVoice
            );

        };

        load();

        window.speechSynthesis.onvoiceschanged = load;

    }

    isSupported() {

        return (

            "speechSynthesis" in window

        );

    }

    speak(text) {

        console.log("Speaking :", text);

        if (!this.isSupported()) {

            return;

        }

        if (!text || !text.trim()) {

            return;

        }

        const startSpeaking = () => {

            if (this.synth.speaking) {

                this.synth.cancel();

            }

            this.currentUtterance = new SpeechSynthesisUtterance(text);

            // Voice browser khud choose kare agar selected voice issue kare
            if (this.selectedVoice) {

                this.currentUtterance.voice = this.selectedVoice;

            }

            this.currentUtterance.lang = "en-US";

            this.currentUtterance.rate = 1;

            this.currentUtterance.pitch = 1;

            this.currentUtterance.volume = 1;

            this.currentUtterance.onstart = () => {

                console.log("Speech Started");

            };

            this.currentUtterance.onend = () => {

                console.log("Speech Finished");

            };

            this.currentUtterance.onerror = (event) => {

                console.error("Speech Error :", event.error);

            };

            this.synth.speak(this.currentUtterance);

        };

        if (!this.isReady) {

            setTimeout(() => {

                this.speak(text);

            }, 300);

            return;

        }

        setTimeout(() => {

            startSpeaking();

        }, 300);

    }

    stop() {

        this.synth.cancel();

    }

    pause() {

    if (

        this.synth.speaking &&

        !this.synth.paused

    ) {

        console.log("Speech Paused");

        this.synth.pause();

    }

}


    resume() {

    if (

        this.synth.paused

    ) {

        console.log("Speech Resumed");

        this.synth.resume();

    }

}


   isSpeaking() {

    return this.synth.speaking || this.synth.paused;

}

}

export default new SpeechSynthesisService();