import "../../styles/profileDropdown.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FiUser, FiSettings, FiBarChart2, FiCreditCard, FiAlertCircle, FiLogOut, FiTrash2 } from "react-icons/fi";
import { openReportProblemModal } from "../../services/supportService";
import { deleteAccount } from "../../services/profileService";
import { clearAuthSession } from "../../services/authService";

export default function ProfileDropdown({ user, onLogout, onClose, onOpenSettings }) {
    const navigate = useNavigate();
    const isAdmin = user?.role === "ADMIN" || user?.role === "ROLE_ADMIN";
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState("");

    const menuItems = [
        { icon: <FiUser />, label: "Profile", path: "/profile" },
        { icon: <FiSettings />, label: "Settings", isSettings: true },
        { icon: <FiBarChart2 />, label: "Analytics", path: "/performance" },
        { icon: <FiCreditCard />, label: "Subscription", path: "/pricing" },
        { icon: <FiAlertCircle />, label: "Report a Problem", isReportProblem: true }
    ];

    const handleDeleteAccount = async () => {
        try {
            setIsDeleting(true);
            setDeleteError("");
            await deleteAccount();
            clearAuthSession();
            if (onClose) onClose();
            setShowDeleteModal(false);
            navigate("/login", { replace: true, state: { accountDeleted: true } });
        } catch (err) {
            console.error("Account self-delete failed:", err);
            const msg = err?.response?.data?.message
                || err?.response?.data
                || err?.message
                || "Failed to delete account. Please try again.";
            setDeleteError(msg);
            setIsDeleting(false);
        }
    };

    return (
        <>
            <div className="profile-dropdown">
                <div className="profile-dropdown-top">
                    <div className="profile-avatar">
                        {user?.username?.charAt(0)?.toUpperCase() || user?.name?.charAt(0)?.toUpperCase() || "U"}
                    </div>
                    <div className="profile-user-info">
                        <h3>{user?.username || user?.name || "User"}</h3>
                        <p>{user?.email || "No email"}</p>
                    </div>
                </div>
                <div className="profile-menu">
                    {menuItems.map((item, index) => (
                        <button
                            key={index}
                            className="profile-menu-btn"
                            onClick={() => {
                                if (onClose) onClose();
                                if (item.isReportProblem) {
                                    openReportProblemModal({ feature: "General" });
                                } else if (item.isSettings) {
                                    if (onOpenSettings) onOpenSettings();
                                } else if (item.path) {
                                    navigate(item.path);
                                }
                            }}
                        >
                            <span className="menu-emoji">{item.icon}</span>
                            <span className="profile-menu-text">{item.label}</span>
                        </button>
                    ))}
                </div>
                <div className="profile-logout">
                    <button className="logout-btn" onClick={onLogout}>
                        <span className="menu-emoji"><FiLogOut /></span>
                        <span>Logout</span>
                    </button>
                    {!isAdmin && (
                        <button
                            className="delete-account-btn"
                            type="button"
                            onClick={() => setShowDeleteModal(true)}
                        >
                            <span className="menu-emoji"><FiTrash2 /></span>
                            <span>Delete Account</span>
                        </button>
                    )}
                </div>
            </div>

            {showDeleteModal && (
                <div className="delete-modal-overlay" onClick={() => !isDeleting && setShowDeleteModal(false)}>
                    <div className="delete-modal-card" onClick={(e) => e.stopPropagation()}>
                        <div className="delete-modal-icon">
                            <FiAlertCircle size={32} />
                        </div>
                        <h3>Delete Account</h3>
                        <p className="delete-modal-description">
                            Are you sure you want to permanently delete your account? All your profile information, interview sessions, progress, and history will be permanently erased.
                        </p>
                        <p className="delete-modal-warning">
                            This action cannot be undone.
                        </p>
                        {deleteError && (
                            <div className="delete-modal-error">
                                {deleteError}
                            </div>
                        )}
                        <div className="delete-modal-actions">
                            <button
                                type="button"
                                className="delete-cancel-btn"
                                onClick={() => setShowDeleteModal(false)}
                                disabled={isDeleting}
                            >
                                Cancel
                            </button>
                            <button
                                type="button"
                                className="delete-confirm-btn"
                                onClick={handleDeleteAccount}
                                disabled={isDeleting}
                            >
                                {isDeleting ? "Deleting..." : "Permanently Delete"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}