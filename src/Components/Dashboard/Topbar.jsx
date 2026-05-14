export default function Topbar() {

    return (

        <div className="dashboard-topbar">

            <input
                type="text"
                placeholder="Search problems, topics, companies..."
            />

            <div className="topbar-right">

                <button>
                    🌙
                </button>

                <button>
                    🔔
                </button>

                <div className="profile-circle">

                </div>

            </div>

        </div>
    );
}