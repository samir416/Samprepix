import "../../styles/profileDropdown.css";
import { useNavigate } from "react-router-dom";
import { FiSettings, FiBarChart, FiCreditCard, FiShield } from "react-icons/fi";

export default function ProfileDropdown({ user, onLogout, onClose, onOpenSettings }) {
    const navigate = useNavigate();
    const isAdmin = user?.role === "ADMIN";

    const menuItems = [
        { icon: "👤", label: "Profile", path: "/profile" },
        { icon: "⚙️", label: "Settings", isSettings: true },
        { icon: "📊", label: "Analytics", path: "/performance" },
        { icon: "💳", label: "Subscription", path: "/subscription" },
        { icon: "💳", label: "Billing", path: "/billing" }
    ];

    return (
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
                            if (item.isSettings) {
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
                    <span className="menu-emoji">🚪</span>
                    <span>Logout</span>
                </button>
            </div>
        </div>
    );
}