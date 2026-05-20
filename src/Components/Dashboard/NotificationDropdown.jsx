import "../../styles/notification.css";

import dummyNotifications from "../../data/dummyNotifications";

export default function NotificationDropdown() {

    return (

        <div className="notification-dropdown">

            <div className="notification-top">

                <h3>
                    Notifications
                </h3>

                <span>
                    4 New
                </span>

            </div>

            <div className="notification-list">

                {

                    dummyNotifications.map((item) => (

                        <div
                            key={item.id}
                            className={
                                item.unread
                                    ? "notification-item unread"
                                    : "notification-item"
                            }
                        >

                            <div className="notification-dot"></div>

                            <div className="notification-content">

                                <h4>
                                    {item.title}
                                </h4>

                                <p>
                                    {item.message}
                                </p>

                                <small>
                                    {item.time}
                                </small>

                            </div>

                        </div>
                    ))
                }

            </div>

        </div>
    );
}