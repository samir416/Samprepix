import { Navigate, useLocation, Outlet } from "react-router-dom";

export default function ProtectedRoute({ children }) {

    const token = localStorage.getItem("token");

    let user = null;
    try {
        const storedUser = localStorage.getItem("user");
        if (storedUser) user = JSON.parse(storedUser);
    } catch (_) {}

    const onboardingCompleted =
        user?.profileCompleted === true ||
        user?.role === "ADMIN" ||
        (localStorage.getItem("onboardingCompleted") === "true" && user?.id);

    const location = useLocation();

    if (!token) {

        return (
            <Navigate
                to="/login"
                replace
            />
        );

    }

    if (

        !onboardingCompleted &&

        location.pathname !== "/onboarding"

    ) {

        return (
            <Navigate
                to="/onboarding"
                replace
            />
        );

    }

    if (

        onboardingCompleted &&

        location.pathname === "/onboarding"

    ) {

        return (
            <Navigate
                to="/dashboard"
                replace
            />
        );

    }

    return children ? children : <Outlet />;
}
