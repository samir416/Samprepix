import { motion } from "framer-motion";
import "../styles/AIOrb.css";

function AIOrb({ speaking, listening }) {

    return (

        <div className="ai-orb-wrapper">

            <motion.div

                className={`ai-orb ${speaking ? "speaking" : ""} ${listening ? "listening" : ""}`}

                animate={

                    speaking

                        ? {

                            scale: [1, 1.05, 0.98, 1.08, 1],

                            transition: {

                                duration: 1.6,

                                repeat: Infinity,

                                ease: "easeInOut"

                            }

                        }

                        : {

                            scale: [1, 1.02, 1],

                            transition: {

                                duration: 4,

                                repeat: Infinity,

                                ease: "easeInOut"

                            }

                        }

                }

            >

                <div className="orb-aura"></div>

                <div className="orb-ring ring-1"></div>

                <div className="orb-ring ring-2"></div>

                <div className="orb-ring ring-3"></div>

                <div className="orb-core"></div>

                <div className="orb-particles"></div>

                <span>

                    AI

                </span>

            </motion.div>

        </div>

    );

}

export default AIOrb;