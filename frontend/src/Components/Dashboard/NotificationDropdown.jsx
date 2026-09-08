import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
    FiCode,
    FiMic,
    FiFileText,
    FiBell,
    FiCheck,
    FiRefreshCw,
    FiAlertCircle,
    FiTrash2,
    FiX,
    FiPercent
} from "react-icons/fi";
import {
    getUserNotifications,
    markAllNotificationsAsRead,
    deleteNotification,
    clearAllNotifications
} from "../../services/notificationService";
import "../../styles/notification.css";

export default function NotificationDropdown({ onCountChange, onClose }) {
    const navigate = useNavigate();
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [markingRead, setMarkingRead] = useState(false);
    const [clearingAll, setClearingAll] = useState(false);
    const [deletingId, setDeletingId] = useState(null);

    const loadNotifications = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getUserNotifications();
            const list = Array.isArray(data) ? data : [];
            setNotifications(list);
            if (onCountChange) {
                const unread = list.filter((n) => n.unread).length;
                onCountChange(unread);
            }
        } catch (err) {
            console.error("Failed to load notifications:", err);
            setError("Unable to load notifications");
        } finally {
            setLoading(false);
        }
    }, [onCountChange]);

    useEffect(() => {
        loadNotifications();
    }, [loadNotifications]);

    const handleMarkAllRead = async () => {
        if (markingRead) return;
        setMarkingRead(true);
        try {
            await markAllNotificationsAsRead();
            setNotifications((prev) => prev.map((n) => ({ ...n, unread: false })));
            if (onCountChange) {
                onCountChange(0);
            }
        } catch (err) {
            console.error("Failed to mark notifications as read:", err);
        } finally {
            setMarkingRead(false);
        }
    };

    const handleDeleteOne = async (e, id) => {
        e.stopPropagation();
        if (deletingId === id) return;
        setDeletingId(id);
        try {
            await deleteNotification(id);
            setNotifications((prev) => {
                const next = prev.filter((n) => n.id !== id);
                if (onCountChange) {
                    onCountChange(next.filter((n) => n.unread).length);
                }
                return next;
            });
        } catch (err) {
            console.error("Failed to delete notification:", err);
        } finally {
            setDeletingId(null);
        }
    };

    const handleClearAll = async () => {
        if (clearingAll || notifications.length === 0) return;
        setClearingAll(true);
        try {
            await clearAllNotifications();
            setNotifications([]);
            if (onCountChange) {
                onCountChange(0);
            }
        } catch (err) {
            console.error("Failed to clear notifications:", err);
        } finally {
            setClearingAll(false);
        }
    };

    const handleItemClick = (item) => {
        if (onClose) onClose();
        if (item.targetUrl) {
            navigate(item.targetUrl);
        }
    };

    const unreadCount = notifications.filter((n) => n.unread).length;

    const getIcon = (type) => {
        switch (type) {
            case "CODING":
                return <FiCode className="notif-icon coding" />;
            case "INTERVIEW":
                return <FiMic className="notif-icon interview" />;
            case "RESUME":
                return <FiFileText className="notif-icon resume" />;
            case "APTITUDE":
                return <FiPercent className="notif-icon aptitude" />;
            default:
                return <FiBell className="notif-icon system" />;
        }
    };

    return (
        <div className="notification-dropdown">
            <div className="notification-top">
                <div className="notification-top-left">
                    <h3>Notifications</h3>
                    {unreadCount > 0 ? (
                        <span className="notif-badge-pill">{unreadCount} New</span>
                    ) : (
                        <span className="notif-badge-pill-read">All Caught Up</span>
                    )}
                </div>

                {unreadCount > 0 && (
                    <button
                        type="button"
                        className="mark-all-read-btn"
                        onClick={handleMarkAllRead}
                        disabled={markingRead}
                        title="Mark all as read"
                    >
                        <FiCheck size={14} />
                        <span>{markingRead ? "Updating..." : "Mark Read"}</span>
                    </button>
                )}
            </div>

            <div className="notification-list">
                {loading ? (
                    <div className="notification-state loading-state">
                        <div className="notif-spinner" />
                        <p>Fetching notifications...</p>
                    </div>
                ) : error ? (
                    <div className="notification-state error-state">
                        <FiAlertCircle size={24} />
                        <p>{error}</p>
                        <button className="notif-retry-btn" onClick={loadNotifications}>
                            <FiRefreshCw size={12} /> Retry
                        </button>
                    </div>
                ) : notifications.length === 0 ? (
                    <div className="notification-state empty-state">
                        <FiBell size={32} />
                        <h4>No notifications yet</h4>
                        <p>
                            Real updates will appear here as you solve coding problems,
                            complete mock interviews, and analyze your resumes.
                        </p>
                    </div>
                ) : (
                    notifications.map((item) => (
                        <div
                            key={item.id}
                            className={`notification-item ${item.unread ? "unread" : ""}`}
                            onClick={() => handleItemClick(item)}
                        >
                            <div className="notif-item-left">
                                <div className="notif-icon-circle">
                                    {getIcon(item.type)}
                                    {item.unread && <div className="notification-dot" />}
                                </div>
                            </div>

                            <div className="notification-content">
                                <div className="notif-content-header">
                                    <h4>{item.title}</h4>
                                    <button
                                        type="button"
                                        className="notif-delete-btn"
                                        title="Dismiss notification"
                                        aria-label="Dismiss notification"
                                        onClick={(e) => handleDeleteOne(e, item.id)}
                                        disabled={deletingId === item.id}
                                    >
                                        <FiX size={14} />
                                    </button>
                                </div>
                                <p>{item.message}</p>
                                <small>{item.timestamp}</small>
                            </div>
                        </div>
                    ))
                )}
            </div>

            {notifications.length > 0 && (
                <div className="notification-footer">
                    <button
                        type="button"
                        className="notif-footer-btn"
                        onClick={handleMarkAllRead}
                        disabled={markingRead || unreadCount === 0}
                    >
                        <FiCheck size={13} />
                        <span>Mark all read</span>
                    </button>

                    <button
                        type="button"
                        className="notif-footer-btn clear-history-btn"
                        onClick={handleClearAll}
                        disabled={clearingAll}
                    >
                        <FiTrash2 size={13} />
                        <span>{clearingAll ? "Clearing..." : "Clear history"}</span>
                    </button>
                </div>
            )}
        </div>
    );
}
