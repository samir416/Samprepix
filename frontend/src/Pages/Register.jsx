import { useEffect } from "react";
import AuthModal from "../Components/Auth/AuthModal";
import { updatePageSEO } from "../utils/seo";

export default function Register() {
    useEffect(() => {
        updatePageSEO({
            title: "Sign Up",
            description: "Create your free Samprepix account to master coding interviews, aptitude tests, and ATS resume analytics.",
            canonicalPath: "/register"
        });
    }, []);

    return <AuthModal />;
}
