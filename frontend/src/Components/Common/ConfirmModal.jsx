import React from "react";
import { motion, AnimatePresence } from "framer-motion";
import { AlertTriangle } from "lucide-react";
import "../../styles/admin.css";

export default function ConfirmModal({ isOpen, message, onConfirm, onCancel }) {
    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    className="admin-confirm-overlay"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.2 }}
                >
                    <motion.div
                        className="admin-confirm-content"
                        initial={{ scale: 0.95, opacity: 0, y: 10 }}
                        animate={{ scale: 1, opacity: 1, y: 0 }}
                        exit={{ scale: 0.95, opacity: 0, y: 10 }}
                        transition={{ type: "spring", stiffness: 300, damping: 25 }}
                    >
                        <div className="admin-confirm-icon-wrapper">
                            <AlertTriangle size={32} strokeWidth={2.5} />
                        </div>
                        <h3>Confirm Action</h3>
                        <p>{message}</p>
                        
                        <div className="admin-confirm-actions">
                            <button className="admin-confirm-cancel" onClick={onCancel}>
                                Cancel
                            </button>
                            <button className="admin-confirm-submit" onClick={onConfirm}>
                                Confirm
                            </button>
                        </div>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
}
