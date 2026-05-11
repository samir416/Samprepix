import { Routes, Route } from "react-router-dom";
import Home from "../Pages/Home";
import Login from "../Pages/Login";
import Register from "../Pages/Register";
import Dashboard from "../Pages/Dashboard";
import CodingArena from "../Pages/CodingArena";
import MockInterview from "../Pages/MockInterview";

export default function AppRoutes() {
  return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/coding-arena" element={<CodingArena />} />
            <Route path="/mock-interview" element={<MockInterview />} />
        </Routes>
  )
};