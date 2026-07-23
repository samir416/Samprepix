import "../../styles/profileDropdown.css";

export default function ProfileDropdown({
    user,
    onLogout
}) {
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