import { useState, useEffect } from "react";
import AppRoutes from "./routes/AppRoutes";
import AppLoader from "./Components/Common/AppLoader";
import "./styles/mobile.css";

function App() {

    const [loading, setLoading] = useState(true);

    useEffect(() => {

        const savedTheme =
            localStorage.getItem("theme") || "light";

        if (savedTheme === "dark") {

            document.body.classList.add("dark-theme");

        } else {

            document.body.classList.remove("dark-theme");

        }

    }, []);

    useEffect(() => {

        const timer = setTimeout(() => {

            setLoading(false);

        }, 2500);

        return () => clearTimeout(timer);

    }, []);

    return (

        <>

            <AppLoader
                visible={loading}
            />

            <AppRoutes />

        </>

    );

}

export default App;