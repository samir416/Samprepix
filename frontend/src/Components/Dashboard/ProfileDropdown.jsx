import "../../styles/profileDropdown.css";
import { useNavigate } from "react-router-dom";

export default function ProfileDropdown({
    user,
    onLogout,
    onClose

}) {

    const navigate = useNavigate();

    const menuItems = [

        {
            icon: "👤",
            label: "Profile",
            path: "/profile"
        },

        {
            icon: "⚙️",
            label: "Settings",
            path: "/settings"
        },

        {
            icon: "📊",
            label: "Analytics",
            path: "/performance"
        },

        {
            icon: "💳",
            label: "Billing",
            path: "/billing"
        }

    ];



    return (

        <div className="profile-dropdown">

            {/* TOP */}

            <div className="profile-dropdown-top">

                <div className="profile-avatar">

                    {
                        user?.username?.charAt(0)?.toUpperCase()
                        ||
                        user?.name?.charAt(0)?.toUpperCase()
                        ||
                        "U"
                    }

                </div>

                <div className="profile-user-info">

                    <h3>
                        {
                            user?.username
                            ||
                            user?.name
                            ||
                            "User"
                        }
                    </h3>

                    <p>
                        {
                            user?.email
                            ||
                            "No email"
                        }
                    </p>

                </div>

            </div>

            {/* MENU */}

            <div className="profile-menu">

                {
                    menuItems.map((item, index) => (

                        <button
                            key={index}
                            className="profile-menu-btn"
                            onClick={() => {

                                navigate(item.path);

                                if (onClose) {

                                    onClose();

                                }

                            }}
                        >

                            <span className="menu-emoji">

                                {item.icon}

                            </span>

                            <span className="profile-menu-text">

                                {item.label}

                            </span>

                        </button>
                    ))
                }

            </div>

            {/* LOGOUT */}

            <div className="profile-logout">

                <button
                    className="logout-btn"
                    onClick={onLogout}
                >

                    <span className="menu-emoji">

                        🚪

                    </span>

                    <span>

                        Logout

                    </span>

                </button>

            </div>

        </div>
    );
}