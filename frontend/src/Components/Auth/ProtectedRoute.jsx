import React, { useState, useEffect, useRef } from "react";
import { Navigate, useLocation, Outlet } from "react-router-dom";
import { getCleanToken, getCurrentUser } from "../../services/authService";
import AppLoader from "../Common/AppLoader";

export default function ProtectedRoute({ children }) {
    const location = useLocation();
    const token = getCleanToken();

    // Read cached user synchronously if available
    const getCachedUser = () => {
        try {
            const raw = localStorage.getItem("user");
            if (raw) {
                const parsed = JSON.parse(raw);
                if (parsed && typeof parsed.profileCompleted === "boolean") {
                    return parsed;
                }
            }
        } catch (_) {}
        return null;
    };

    const initialUser = getCachedUser();
    const [user, setUser] = useState(initialUser);
    const [loading, setLoading] = useState(!initialUser && !!token);
    const [sessionValid, setSessionValid] = useState(!!token);
    const mountedRef = useRef(true);

    useEffect(() => {
        mountedRef.current = true;
        const currentToken = getCleanToken();

        if (!currentToken) {
            setSessionValid(false);
            setLoading(false);
            return;
        }

        // Synchronize with any recent local storage updates
        const cached = getCachedUser();
        if (cached) {
            setUser(cached);
        }

        let isCurrent = true;
        getCurrentUser()
            .then(authoritativeUser => {
                if (!mountedRef.current || !isCurrent) return;
                setUser(authoritativeUser);
                setSessionValid(true);
                setLoading(false);
                try {
                    localStorage.setItem("user", JSON.stringify(authoritativeUser));
                    if (authoritativeUser?.profileCompleted) {
                        localStorage.setItem("onboardingCompleted", "true");
                    } else {
                        localStorage.removeItem("onboardingCompleted");
                    }
                } catch (_) {}
            })
            .catch(err => {
                if (!mountedRef.current || !isCurrent) return;
                console.warn("ProtectedRoute session verification failed:", err?.message);
                if (err?.response?.status === 401 || err?.message?.includes("No authentication token")) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("user");
                    localStorage.removeItem("onboardingCompleted");
                    setUser(null);
                    setSessionValid(false);
                }
                setLoading(false);
            });

        return () => {
            isCurrent = false;
        };
    }, [location.pathname]);

    // 1. NO TOKEN OR INVALID SESSION -> /login
    if (!token || !sessionValid) {
        return <Navigate to="/login" replace state={{ from: location }} />;
    }

    // 2. AUTHENTICATION IN-FLIGHT -> SHOW LOADER
    if (loading) {
        return (
            <AppLoader
                visible={true}
                title="Verifying session..."
                subtitle="Loading your personalized workspace"
            />
        );
    }

    // 3. AUTHORITATIVE ONBOARDING COMPLETION STATE
    // Evaluates memory state, synchronously cached storage, and completed marker
    const cached = getCachedUser();
    const isCompleted = (user?.profileCompleted === true)
        || (cached?.profileCompleted === true)
        || (localStorage.getItem("onboardingCompleted") === "true");

    // 4. INCOMPLETE PROFILE VISITING PROTECTED DASHBOARD/ARENA/ETC -> /onboarding
    if (!isCompleted && location.pathname !== "/onboarding") {
        return <Navigate to="/onboarding" replace />;
    }

    // 5. COMPLETED PROFILE VISITING /onboarding -> /dashboard
    if (isCompleted && location.pathname === "/onboarding") {
        return <Navigate to="/dashboard" replace />;
    }

    // 6. VALID SESSION & CORRECT ROUTE
    return children ? children : <Outlet />;
}
