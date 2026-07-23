import { AnimatePresence, motion } from "framer-motion";
import Logo from "../../assets/Logo.png";
import "../../styles/apploader.css";

export default function AppLoader({

    visible,

    title = "Preparing your workspace",

    subtitle = "Loading your personalized dashboard"

}) {

    return (

        <AnimatePresence>

            {

                visible && (

                    <motion.div

                        className="ai-loader-overlay"

                        initial={{
                            opacity: 0
                        }}

                        animate={{
                            opacity: 1
                        }}

                        exit={{
                            opacity: 0
                        }}

                        transition={{
                            duration: .35
                        }}

                    >

                        <div className="ai-loader-background">

                            <div className="ai-loader-orb ai-loader-orb-one"></div>

                            <div className="ai-loader-orb ai-loader-orb-two"></div>

                            <div className="ai-loader-orb ai-loader-orb-three"></div>

                        </div>

                        <motion.div

                            className="ai-loader-content"

                            initial={{

                                opacity: 0,

                                scale: .92,

                                y: 35

                            }}

                            animate={{

                                opacity: 1,

                                scale: 1,

                                y: 0

                            }}

                            exit={{

                                opacity: 0,

                                scale: .96,

                                y: 20

                            }}

                            transition={{

                                duration: .45

                            }}

                        >

                            <motion.div

                                className="ai-loader-logo-wrapper"

                                animate={{

                                    y: [0, -8, 0],

                                    scale: [1, 1.03, 1]

                                }}

                                transition={{

                                    duration: 3,

                                    repeat: Infinity,

                                    ease: "easeInOut"

                                }}

                            >

                                <div className="ai-loader-logo-glow"></div>

                                <img

                                    src={Logo}

                                    alt="Logo"

                                    className="ai-loader-logo"

                                />

                            </motion.div>

                            <motion.h2

                                initial={{

                                    opacity: 0,

                                    y: 10

                                }}

                                animate={{

                                    opacity: 1,

                                    y: 0

                                }}

                                transition={{

                                    delay: .15

                                }}

                            >

                                {title}

                            </motion.h2>

                            <motion.p

                                initial={{

                                    opacity: 0

                                }}

                                animate={{

                                    opacity: 1

                                }}

                                transition={{

                                    delay: .30

                                }}

                            >

                                {subtitle}

                            </motion.p>

                            <div className="ai-loader-progress">

                                <motion.div

                                    className="ai-loader-progress-fill"

                                    animate={{

                                        x: ["-120%", "260%"]

                                    }}

                                    transition={{

                                        repeat: Infinity,

                                        duration: 1.35,

                                        ease: "easeInOut"

                                    }}

                                />

                            </div>

                        </motion.div>

                    </motion.div>

                )

            }

        </AnimatePresence>

    );

}