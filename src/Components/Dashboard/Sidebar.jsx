import Logo from "../../assets/Logo.png";

import {
    FiGrid,
    FiFileText,
    FiMic,
    FiCode,
    FiShield
} from "react-icons/fi";

export default function Sidebar() {

    return (

        <aside className="dashboard-sidebar">

            <div className="sidebar-top">

                {/* LOGO */}

                <div className="dashboard-logo">

                    <img
                        src={Logo}
                        alt="logo"
                    />

                    <h2>
                        Samprepix
                    </h2>

                </div>

                {/* MENU */}

                <nav className="dashboard-menu">

                    <button className="active">

                        <FiGrid />

                        Dashboard

                    </button>

                    <button>

                        <FiFileText />

                        Resume Analyzer

                    </button>

                    <button>

                        <FiMic />

                        AI Interview

                    </button>

                    <button>

                        <FiCode />

                        Coding Arena

                    </button>

                    <button>

                        <FiShield />

                        Admin

                    </button>

                </nav>

            </div>

            {/* UPGRADE */}

            <div className="upgrade-card">

                <h3>
                    Upgrade to Pro
                </h3>

                <p>
                    Unlimited mocks, all tracks.
                </p>

                <button>
                    Upgrade
                </button>

            </div>

        </aside>
    );
}