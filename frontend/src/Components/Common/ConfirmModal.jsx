import React from "react";
import { motion, AnimatePresence } from "framer-motion";
import { AlertTriangle } from "lucide-react";
import "../../styles/admin.css";

export default function ConfirmModal({ isOpen, message, onConfirm, onCancel }) {
    if (!isOpen) return null;

    return (
        <AnimatePresence>
            <motion.div
                className="admin-modal-overlay"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
            >
                <motion.div
                    className="admin-modal-content"
                    initial={{ scale: 0.95, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    exit={{ scale: 0.95, opacity: 0 }}
                    style={{ maxWidth: "400px", textAlign: "center", padding: "30px 20px" }}
                >
                    <div style={{ display: "flex", justifyContent: "center", marginBottom: "15px", color: "#ef4444" }}>
                        <AlertTriangle size={48} />
                    </div>
                    <h3 style={{ margin: "0 0 10px 0", fontSize: "20px", color: "var(--text-color, #0f172a)" }}>
                        Confirm Action
                    </h3>
                    <p style={{ margin: "0 0 24px 0", color: "var(--text-secondary, #475569)", fontSize: "15px" }}>
                        {message}
                    </p>
                    <div style={{ display: "flex", gap: "12px", justifyContent: "center" }}>
                        <button className="admin-btn-secondary" onClick={onCancel}>
                            Cancel
                        </button>
                        <button className="admin-btn-danger" onClick={onConfirm}>
                            Confirm
                        </button>
                    </div>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
}
