import { AnimatePresence, motion } from "framer-motion";
import "../styles/confirmationModal.css";

export default function ConfirmationModal({
    open,
    title,
    message,
    confirmText,
    cancelText,
    onConfirm,
    onCancel
}) {

    return (

        <AnimatePresence>

            {

                open &&

                <motion.div
                    className="confirmation-overlay"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                >

                    <motion.div
                        className="confirmation-modal"
                        initial={{
                            opacity: 0,
                            scale: .92,
                            y: 30
                        }}
                        animate={{
                            opacity: 1,
                            scale: 1,
                            y: 0
                        }}
                        exit={{
                            opacity: 0,
                            scale: .92,
                            y: 30
                        }}
                        transition={{
                            duration: .25
                        }}
                    >

                        <h2>{title}</h2>

                        <p>{message}</p>

                        <div className="confirmation-actions">

                            <button
                                className="confirmation-cancel"
                                onClick={onCancel}
                            >
                                {cancelText}
                            </button>

                            <button
                                className="confirmation-confirm"
                                onClick={onConfirm}
                            >
                                {confirmText}
                            </button>

                        </div>

                    </motion.div>

                </motion.div>

            }

        </AnimatePresence>

    );

}