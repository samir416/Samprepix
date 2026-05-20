import "../../styles/profileDropdown.css";

export default function ProfileDropdown() {

    const menuItems = [

        {
            icon: "👤",
            label: "Profile"
        },

        {
            icon: "⚙️",
            label: "Settings"
        },

        {
            icon: "📊",
            label: "Analytics"
        },

        {
            icon: "💳",
            label: "Billing"
        }
    ];

    return (

        <div className="profile-dropdown">

            {/* TOP */}

            <div className="profile-dropdown-top">

                <div className="profile-avatar">

                    S

                </div>

                <div className="profile-user-info">

                    <h3>
                        Samir
                    </h3>

                    <p>
                        Frontend Developer
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

                <button className="logout-btn">

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