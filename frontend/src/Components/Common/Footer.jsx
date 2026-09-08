import { Link } from "react-router-dom";
import Logo from "../../assets/Logo.png";
import "../../styles/home.css";

export default function Footer() {
    return (
        <footer className="footer-section" role="contentinfo" aria-label="Site Footer">
            <div className="footer-container">
                {/* BRAND COLUMN */}
                <div className="footer-brand">
                    <div className="footer-logo">
                        <img
                            src={Logo}
                            alt="Samprepix Logo"
                            className="footer-logo-img"
                            width="42"
                            height="42"
                            loading="lazy"
                        />
                    </div>
                    <h2>Samprepix</h2>
                    <p>
                        Comprehensive AI-powered interview practice, coding arena, aptitude assessments, and ATS resume analytics designed for ambitious candidates and competitive engineering teams.
                    </p>
                </div>

                {/* NAVIGATION SLOTS */}
                <div className="footer-links-wrapper">
                    {/* PRODUCT */}
                    <div className="footer-column">
                        <h4>Product</h4>
                        <Link to="/features">Features</Link>
                        <Link to="/pricing">Pricing</Link>
                        <Link to="/changelog">Changelog</Link>
                        <Link to="/roadmap">Roadmap</Link>
                    </div>

                    {/* RESOURCES */}
                    <div className="footer-column">
                        <h4>Resources</h4>
                        <Link to="/docs">Docs</Link>
                        <Link to="/blog">Blog</Link>
                        <Link to="/guides">Guides</Link>
                        <Link to="/community">Community</Link>
                    </div>

                    {/* COMPANY */}
                    <div className="footer-column">
                        <h4>Company</h4>
                        <Link to="/about">About</Link>
                        <Link to="/careers">Careers</Link>
                        <Link to="/contact">Contact</Link>
                        <Link to="/legal">Legal</Link>
                    </div>
                </div>
            </div>

            {/* BOTTOM BAR */}
            <div className="footer-bottom">
                <span>© {new Date().getFullYear()} Samprepix Inc. All rights reserved. Platform built for engineering excellence.</span>
            </div>
        </footer>
    );
}
