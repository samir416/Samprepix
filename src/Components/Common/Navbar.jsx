import { Link } from "react-router-dom";
import { Moon } from "lucide-react";
import logo from "../../assets/Logo.png";
import "../../styles/home.css";

function Navbar() {

  return (

    <div className="navbar-wrapper">

      <nav className="custom-navbar">

        {/* LEFT */}

        <div className="logo-section">

          <div className="logo-box">
            <img
        src={logo}
        alt="Samprepix Logo"
        className="navbar-logo"
    />
          </div>

          <h2 className="logo-text">
            Samprepix
          </h2>

        </div>

        {/* CENTER */}

        <ul className="nav-links">

          <li>
            <Link to="/">Home</Link>
          </li>

          <li>
            <Link to="/">Features</Link>
          </li>

          <li>
            <Link to="/">Pricing</Link>
          </li>

        </ul>

        {/* RIGHT */}

        <div className="nav-right">

          <div className="theme-icon">
            <Moon size={18} />
          </div>

          <Link className="signin-btn" to="/login">
            Sign in
          </Link>

          <Link className="getstarted-btn" to="/register">
            Get started
          </Link>

        </div>

      </nav>

    </div>

  )
}

export default Navbar;