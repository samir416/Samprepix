import { Navigate, useLocation } from "react-router-dom";

export default function ProtectedRoute({ children }) {

    const token = localStorage.getItem("token");

    const onboardingCompleted =
        localStorage.getItem("onboardingCompleted") === "true";

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

    return children;

}