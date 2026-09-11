import "../styles/admin.css";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getCurrentUser } from "../services/authService";

import {
    getAdminStats,
    getAllUsers,
    deleteUser,
    updateUserRole,
    updateUserStatus,
    grantTemporaryEntitlement,
    grantLifetimeEntitlement,
    revokeEntitlement,
    revokeAllEntitlements,
    getAllSubscriptions,
} from "../services/adminService";

import {
    getAllPlans,
    deletePlan,
    createPlan,
    updatePlan,
} from "../services/planService";

import {
    FiUsers,
    FiCreditCard,
    FiServer,
    FiTrendingUp,
    FiTrash2,
    FiShield,
    FiCheckCircle,
    FiXCircle,
    FiAlertCircle,
    FiSearch,
    FiPlus,
    FiRefreshCw,
    FiX,
} from "react-icons/fi";

import { FaCrown } from "react-icons/fa";

export default function Admin() {
    const navigate = useNavigate();

    const [activeTab, setActiveTab] = useState("dashboard");

    const [stats, setStats] = useState(null);
    const [users, setUsers] = useState([]);
    const [plans, setPlans] = useState([]);
    const [subscriptions, setSubscriptions] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [adminVerified, setAdminVerified] = useState(false);

    const [userRoleFilter, setUserRoleFilter] = useState("");
    const [userStatusFilter, setUserStatusFilter] = useState("");
    const [userSearch, setUserSearch] = useState("");

    const [entitlementModalUser, setEntitlementModalUser] = useState(null);
    const [entitlementPlan, setEntitlementPlan] = useState("PRO");
    const [entitlementType, setEntitlementType] = useState("temporary");
    const [entitlementDuration, setEntitlementDuration] = useState(30);
    const [entitlementReason, setEntitlementReason] = useState("");

    const [showPlanForm, setShowPlanForm] = useState(false);
    const [editingPlan, setEditingPlan] = useState(null);

    const [planForm, setPlanForm] = useState({
        name: "",
        description: "",
        priceInr: 0,
        priceUsd: 0,
        interval: "Month",
        maxMockInterviews: 3,
        maxResumeScans: 5,
        maxCodingProblems: 100,
        maxAptitudeQuestions: 100,
        includesAIHints: false,
        includesAnalytics: false,
        includesTier1Companies: false,
        includesPriorityCompute: false,
        active: true,
        featured: false,
    });

    // =========================================================
    // ADMIN ACCESS
    // =========================================================

    useEffect(() => {
        const verifyAdmin = async () => {
            const token = localStorage.getItem("token");

            if (!token) {
                navigate("/login");
                return;
            }

            try {
                const currentUser = await getCurrentUser();

                if (!currentUser || currentUser.role !== "ADMIN") {
                    navigate("/dashboard");
                    return;
                }

                // Update stored user with authoritative server data
                localStorage.setItem("user", JSON.stringify(currentUser));
                setAdminVerified(true);
            } catch (err) {
                console.error("Admin verification failed:", err);
                if (err?.response?.status === 401) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("user");
                    navigate("/login");
                } else {
                    navigate("/dashboard");
                }
            }
        };

        verifyAdmin();
    }, [navigate]);

    // =========================================================
    // LOAD DATA
    // =========================================================

    useEffect(() => {
        if (!adminVerified) {
            return;
        }

        loadData();
    }, [
        adminVerified,
        activeTab,
        userRoleFilter,
        userStatusFilter,
    ]);

    const loadData = async () => {
        if (!adminVerified) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            // -------------------------
            // Dashboard
            // -------------------------

            if (activeTab === "dashboard") {
                const statsData = await getAdminStats();
                setStats(statsData);
            }

            // -------------------------
            // Users / Subscriptions /
            // Entitlements need users
            // -------------------------

            if (
                activeTab === "users" ||
                activeTab === "subscriptions" ||
                activeTab === "entitlements"
            ) {
                const usersData = await getAllUsers(
                    0,
                    20,
                    userRoleFilter || undefined,
                    userStatusFilter || undefined
                );

                setUsers(
                    usersData?.content ||
                    usersData ||
                    []
                );
            }

            // -------------------------
            // Plans
            // -------------------------

            if (activeTab === "plans") {
                const plansData = await getAllPlans();
                setPlans(plansData || []);
            }

            // -------------------------
            // Subscriptions
            // -------------------------

            if (activeTab === "subscriptions") {
                const subsData =
                    await getAllSubscriptions();

                setSubscriptions(
                    subsData || []
                );
            }

        } catch (err) {
            console.error(
                "Admin data loading error:",
                err
            );

            setError(
                err?.response?.data?.message ||
                err?.response?.data?.error ||
                "Failed to load admin data"
            );
        } finally {
            setLoading(false);
        }
    };

    // =========================================================
    // USER ACTIONS
    // =========================================================

    const handleDeleteUser = async (id) => {
        if (
            !window.confirm(
                "Are you sure you want to delete this user?"
            )
        ) {
            return;
        }

        try {
            await deleteUser(id);
            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to delete user"
            );
        }
    };

    const handleUpdateUserRole = async (
        id,
        role
    ) => {
        try {
            await updateUserRole(id, role);
            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to update user role"
            );
        }
    };

    const handleUpdateUserStatus = async (
        id,
        status
    ) => {
        try {
            await updateUserStatus(id, status);
            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to update user status"
            );
        }
    };

    // =========================================================
    // PLAN ACTIONS
    // =========================================================

    const resetPlanForm = () => {
        setPlanForm({
            name: "",
            description: "",
            priceInr: 0,
            priceUsd: 0,
            interval: "Month",
            maxMockInterviews: 3,
            maxResumeScans: 5,
            maxCodingProblems: 100,
            maxAptitudeQuestions: 100,
            includesAIHints: false,
            includesAnalytics: false,
            includesTier1Companies: false,
            includesPriorityCompute: false,
            active: true,
            featured: false,
        });
    };

    const handleSavePlan = async (e) => {
        e.preventDefault();

        try {
            if (editingPlan) {
                await updatePlan(
                    editingPlan.id,
                    planForm
                );
            } else {
                await createPlan(planForm);
            }

            setShowPlanForm(false);
            setEditingPlan(null);
            resetPlanForm();

            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to save plan"
            );
        }
    };

    const handleDeletePlan = async (id) => {
        try {
            await deletePlan(id);
            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to delete plan"
            );
        }
    };

    const handleEditPlan = (plan) => {
        setEditingPlan(plan);

        setPlanForm({
            name: plan.name || "",
            description: plan.description || "",
            priceInr: plan.priceInr ?? 0,
            priceUsd: plan.priceUsd ?? 0,
            interval: plan.interval || "Month",
            maxMockInterviews:
                plan.maxMockInterviews ?? 3,
            maxResumeScans:
                plan.maxResumeScans ?? 5,
            maxCodingProblems:
                plan.maxCodingProblems ?? 100,
            maxAptitudeQuestions:
                plan.maxAptitudeQuestions ?? 100,
            includesAIHints:
                !!plan.includesAIHints,
            includesAnalytics:
                !!plan.includesAnalytics,
            includesTier1Companies:
                !!plan.includesTier1Companies,
            includesPriorityCompute:
                !!plan.includesPriorityCompute,
            active: !!plan.active,
            featured: !!plan.featured,
        });

        setShowPlanForm(true);
    };

    // =========================================================
    // ENTITLEMENT ACTIONS
    // =========================================================

    const openEntitlementModal = (
        user,
        plan
    ) => {
        setEntitlementModalUser(user);
        setEntitlementPlan(plan);
        setEntitlementType("temporary");
        setEntitlementDuration(30);
        setEntitlementReason("");
    };

    const handleGrantEntitlement = async (
        userId
    ) => {
        try {
            const data = {
                planName: entitlementPlan,
                durationDays:
                    entitlementType === "temporary"
                        ? entitlementDuration
                        : undefined,
                reason:
                    entitlementReason.trim() ||
                    "Admin granted entitlement",
            };

            if (
                entitlementType ===
                "temporary"
            ) {
                await grantTemporaryEntitlement(
                    userId,
                    data
                );
            } else {
                await grantLifetimeEntitlement(
                    userId,
                    data
                );
            }

            setEntitlementModalUser(null);

            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to grant entitlement"
            );
        }
    };

    const handleRevokeEntitlement = async (
        userId,
        entitlementId
    ) => {
        try {
            await revokeEntitlement(
                userId,
                entitlementId
            );

            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to revoke entitlement"
            );
        }
    };

    const handleRevokeAllEntitlements = async (
        userId
    ) => {
        if (
            !window.confirm(
                "Revoke all entitlements for this user?"
            )
        ) {
            return;
        }

        try {
            await revokeAllEntitlements(userId);

            await loadData();
        } catch (err) {
            console.error(err);

            setError(
                err?.response?.data?.message ||
                "Failed to revoke entitlements"
            );
        }
    };

    // =========================================================
    // FILTER USERS
    // =========================================================

    const visibleUsers = users.filter(
        (user) => {
            const search =
                userSearch.trim().toLowerCase();

            if (!search) {
                return true;
            }

            return (
                user.username
                    ?.toLowerCase()
                    .includes(search) ||
                user.email
                    ?.toLowerCase()
                    .includes(search) ||
                String(user.id)
                    .includes(search)
            );
        }
    );

    // =========================================================
    // TAB CONFIG
    // =========================================================

    const tabs = [
        {
            key: "dashboard",
            label: "Overview",
            icon: FiServer,
        },
        {
            key: "users",
            label: "Users",
            icon: FiUsers,
        },
        {
            key: "plans",
            label: "Plans",
            icon: FiServer,
        },
        {
            key: "subscriptions",
            label: "Subscriptions",
            icon: FiCreditCard,
        },
        {
            key: "entitlements",
            label: "Entitlements",
            icon: FaCrown,
        },
    ];

    // =========================================================
    // RENDER
    // =========================================================

    return (
        <div className="admin-page">
            <main className="admin-content">

                {/* =================================================
                    HEADER
                ================================================= */}

                <div className="admin-page-header">
                    <div>
                        <div className="admin-title-row">

                            <div className="admin-title-icon">
                                <FiShield />
                            </div>

                            <div>
                                <h1>
                                    Admin Panel
                                </h1>

                                <p>
                                    Manage users, plans,
                                    subscriptions and
                                    premium access.
                                </p>
                            </div>

                        </div>
                    </div>

                    <button
                        className="admin-refresh-btn"
                        onClick={loadData}
                        disabled={loading}
                    >
                        <FiRefreshCw
                            className={
                                loading
                                    ? "spin"
                                    : ""
                            }
                        />

                        Refresh
                    </button>
                </div>

                {/* =================================================
                    ADMIN NAVIGATION
                ================================================= */}

                <div className="admin-tabs">
                    {tabs.map((tab) => {
                        const Icon = tab.icon;

                        return (
                            <button
                                key={tab.key}
                                className={
                                    activeTab ===
                                    tab.key
                                        ? "admin-tab active"
                                        : "admin-tab"
                                }
                                onClick={() => {
                                    setError(null);
                                    setActiveTab(
                                        tab.key
                                    );
                                }}
                            >
                                <Icon />
                                <span>
                                    {tab.label}
                                </span>
                            </button>
                        );
                    })}
                </div>

                {/* =================================================
                    ERROR
                ================================================= */}

                {error && (
                    <div className="admin-error">
                        <FiAlertCircle />

                        <span>
                            {error}
                        </span>

                        <button
                            onClick={() =>
                                setError(null)
                            }
                            aria-label="Close error"
                        >
                            <FiX />
                        </button>
                    </div>
                )}

                {/* =================================================
                    LOADING
                ================================================= */}

                {loading && (
                    <div className="admin-loading">
                        <FiRefreshCw className="spin" />

                        <span>
                            Loading admin data...
                        </span>
                    </div>
                )}

                {/* =================================================
                    DASHBOARD
                ================================================= */}

                {activeTab === "dashboard" &&
                    !loading &&
                    stats && (
                        <div className="admin-dashboard">

                            <div className="admin-section-heading">
                                <div>
                                    <h2>
                                        Overview
                                    </h2>

                                    <p>
                                        Current platform
                                        and subscription
                                        statistics.
                                    </p>
                                </div>
                            </div>

                            <div className="admin-stats-grid">

                                <div className="admin-stat-card">
                                    <div className="stat-icon">
                                        <FiUsers />
                                    </div>

                                    <div className="stat-value">
                                        {stats.totalUsers ??
                                            0}
                                    </div>

                                    <div className="stat-label">
                                        Total Users
                                    </div>
                                </div>

                                <div className="admin-stat-card">
                                    <div className="stat-icon">
                                        <FiCheckCircle />
                                    </div>

                                    <div className="stat-value">
                                        {stats.activeUsers ??
                                            0}
                                    </div>

                                    <div className="stat-label">
                                        Active Users
                                    </div>
                                </div>

                                <div className="admin-stat-card">
                                    <div className="stat-icon">
                                        <FiAlertCircle />
                                    </div>

                                    <div className="stat-value">
                                        {stats.pendingUsers ??
                                            0}
                                    </div>

                                    <div className="stat-label">
                                        Pending Users
                                    </div>
                                </div>

                                <div className="admin-stat-card">
                                    <div className="stat-icon">
                                        <FiCreditCard />
                                    </div>

                                    <div className="stat-value">
                                        {stats.totalSubscriptions ??
                                            0}
                                    </div>

                                    <div className="stat-label">
                                        Total Subscriptions
                                    </div>
                                </div>

                                <div className="admin-stat-card">
                                    <div className="stat-icon">
                                        <FiTrendingUp />
                                    </div>

                                    <div className="stat-value">
                                        {stats.activeSubscriptions ??
                                            0}
                                    </div>

                                    <div className="stat-label">
                                        Active Subscriptions
                                    </div>
                                </div>

                                <div className="admin-stat-card">
                                    <div className="stat-icon">
                                        <FiTrendingUp />
                                    </div>

                                    <div className="stat-value">
                                        ₹
                                        {Number(
                                            stats.totalRevenue ||
                                            0
                                        ).toFixed(0)}
                                    </div>

                                    <div className="stat-label">
                                        Revenue
                                    </div>
                                </div>

                            </div>
                        </div>
                    )}

                {/* =================================================
                    USERS
                ================================================= */}

                {activeTab === "users" &&
                    !loading && (
                        <div className="admin-section">

                            <div className="admin-section-header">
                                <div>
                                    <h2>
                                        User Management
                                    </h2>

                                    <p>
                                        Manage user roles,
                                        account status and
                                        access.
                                    </p>
                                </div>

                                <div className="admin-filters">

                                    <div className="admin-search">
                                        <FiSearch />

                                        <input
                                            type="text"
                                            placeholder="Search users..."
                                            value={userSearch}
                                            onChange={(e) =>
                                                setUserSearch(
                                                    e.target.value
                                                )
                                            }
                                        />
                                    </div>

                                    <select
                                        value={
                                            userRoleFilter
                                        }
                                        onChange={(e) =>
                                            setUserRoleFilter(
                                                e.target.value
                                            )
                                        }
                                    >
                                        <option value="">
                                            All Roles
                                        </option>

                                        <option value="USER">
                                            User
                                        </option>

                                        <option value="ADMIN">
                                            Admin
                                        </option>
                                    </select>

                                    <select
                                        value={
                                            userStatusFilter
                                        }
                                        onChange={(e) =>
                                            setUserStatusFilter(
                                                e.target.value
                                            )
                                        }
                                    >
                                        <option value="">
                                            All Status
                                        </option>

                                        <option value="ACTIVE">
                                            Active
                                        </option>

                                        <option value="PENDING">
                                            Pending
                                        </option>
                                    </select>

                                </div>
                            </div>

                            <div className="admin-table-wrapper">
                                <table className="admin-table">

                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>User</th>
                                            <th>Email</th>
                                            <th>Role</th>
                                            <th>Status</th>
                                            <th>Plan</th>
                                            <th>Created</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {visibleUsers.length ===
                                        0 ? (
                                            <tr>
                                                <td
                                                    colSpan="8"
                                                    className="admin-empty"
                                                >
                                                    No users found.
                                                </td>
                                            </tr>
                                        ) : (
                                            visibleUsers.map(
                                                (user) => (
                                                    <tr
                                                        key={
                                                            user.id
                                                        }
                                                    >

                                                        <td>
                                                            {
                                                                user.id
                                                            }
                                                        </td>

                                                        <td>
                                                            <strong>
                                                                {user.username ||
                                                                    "-"}
                                                            </strong>
                                                        </td>

                                                        <td>
                                                            {user.email ||
                                                                "-"}
                                                        </td>

                                                        <td>
                                                            {user.email === "samirprajapat5@gmail.com" ? (
                                                                <span className="badge bg-primary text-white">Owner (ADMIN)</span>
                                                            ) : (
                                                                <select
                                                                    value={user.role}
                                                                    onChange={(e) =>
                                                                        handleUpdateUserRole(
                                                                            user.id,
                                                                            e.target.value
                                                                        )
                                                                    }
                                                                    className="admin-inline-select"
                                                                >
                                                                    <option value="USER">User</option>
                                                                </select>
                                                            )}
                                                        </td>

                                                        <td>
                                                            <span
                                                                className={`status-badge ${
                                                                    user.accountStatus ===
                                                                    "ACTIVE"
                                                                        ? "active"
                                                                        : "pending"
                                                                }`}
                                                            >
                                                                {user.accountStatus ||
                                                                    "-"}
                                                            </span>
                                                        </td>

                                                        <td>
                                                            {user.currentPlan ||
                                                                "STARTER"}
                                                        </td>

                                                        <td>
                                                            {user.createdAt
                                                                ? new Date(
                                                                      user.createdAt
                                                                  ).toLocaleDateString()
                                                                : "-"}
                                                        </td>

                                                        <td>
                                                            {user.email === "samirprajapat5@gmail.com" ? (
                                                                <span className="text-muted" style={{ fontSize: "0.8rem" }}>Protected</span>
                                                            ) : (
                                                                <div className="admin-action-group">
                                                                    <button
                                                                        className="admin-btn-danger"
                                                                        onClick={() =>
                                                                            handleDeleteUser(
                                                                                user.id
                                                                            )
                                                                        }
                                                                        title="Delete User"
                                                                    >
                                                                        <FiTrash2 />
                                                                    </button>

                                                                    <button
                                                                        className="admin-btn-secondary"
                                                                        onClick={() =>
                                                                            handleUpdateUserStatus(
                                                                                user.id,
                                                                                user.accountStatus ===
                                                                                    "ACTIVE"
                                                                                    ? "PENDING"
                                                                                    : "ACTIVE"
                                                                            )
                                                                        }
                                                                        title="Toggle Status"
                                                                    >
                                                                        {user.accountStatus ===
                                                                        "ACTIVE" ? (
                                                                            <FiXCircle />
                                                                        ) : (
                                                                            <FiCheckCircle />
                                                                        )}
                                                                    </button>
                                                                </div>
                                                            )}
                                                        </td>

                                                    </tr>
                                                )
                                            )
                                        )}
                                    </tbody>

                                </table>
                            </div>
                        </div>
                    )}

                {/* =================================================
                    PLANS
                ================================================= */}

                {activeTab === "plans" &&
                    !loading && (
                        <div className="admin-section">

                            <div className="admin-section-header">
                                <div>
                                    <h2>
                                        Plan Management
                                    </h2>

                                    <p>
                                        Manage Pro, Elite and
                                        other subscription
                                        plans.
                                    </p>
                                </div>

                                <button
                                    className="admin-btn-primary"
                                    onClick={() => {
                                        setEditingPlan(null);
                                        resetPlanForm();
                                        setShowPlanForm(true);
                                    }}
                                >
                                    <FiPlus />
                                    Add Plan
                                </button>
                            </div>

                            {/* PLAN FORM */}

                            {showPlanForm && (
                                <form
                                    className="admin-plan-form"
                                    onSubmit={
                                        handleSavePlan
                                    }
                                >
                                    <div className="admin-form-header">
                                        <div>
                                            <h3>
                                                {editingPlan
                                                    ? "Edit Plan"
                                                    : "Create Plan"}
                                            </h3>

                                            <p>
                                                Configure plan
                                                pricing and
                                                limits.
                                            </p>
                                        </div>
                                    </div>

                                    <div className="plan-form-grid">

                                        <div>
                                            <label>
                                                Plan Name
                                            </label>

                                            <input
                                                type="text"
                                                value={
                                                    planForm.name
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        name: e.target
                                                            .value,
                                                    })
                                                }
                                                required
                                            />
                                        </div>

                                        <div>
                                            <label>
                                                Description
                                            </label>

                                            <textarea
                                                value={
                                                    planForm.description
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        description:
                                                            e.target
                                                                .value,
                                                    })
                                                }
                                                required
                                            />
                                        </div>

                                        <div>
                                            <label>
                                                Price (INR)
                                            </label>

                                            <input
                                                type="number"
                                                min="0"
                                                value={
                                                    planForm.priceInr
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        priceInr:
                                                            Number(
                                                                e.target
                                                                    .value
                                                            ),
                                                    })
                                                }
                                            />
                                        </div>

                                        <div>
                                            <label>
                                                Price (USD)
                                            </label>

                                            <input
                                                type="number"
                                                min="0"
                                                value={
                                                    planForm.priceUsd
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        priceUsd:
                                                            Number(
                                                                e.target
                                                                    .value
                                                            ),
                                                    })
                                                }
                                            />
                                        </div>

                                        <div>
                                            <label>
                                                Interval
                                            </label>

                                            <select
                                                value={
                                                    planForm.interval
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        interval:
                                                            e.target
                                                                .value,
                                                    })
                                                }
                                            >
                                                <option value="Month">
                                                    Month
                                                </option>

                                                <option value="Year">
                                                    Year
                                                </option>
                                            </select>
                                        </div>

                                        <div>
                                            <label>
                                                Max Mock Interviews
                                            </label>

                                            <input
                                                type="number"
                                                min="0"
                                                value={
                                                    planForm.maxMockInterviews
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        maxMockInterviews:
                                                            Number(
                                                                e.target
                                                                    .value
                                                            ),
                                                    })
                                                }
                                            />
                                        </div>

                                        <div>
                                            <label>
                                                Max Resume Scans
                                            </label>

                                            <input
                                                type="number"
                                                min="0"
                                                value={
                                                    planForm.maxResumeScans
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        maxResumeScans:
                                                            Number(
                                                                e.target
                                                                    .value
                                                            ),
                                                    })
                                                }
                                            />
                                        </div>

                                        <div>
                                            <label>
                                                Max Coding Problems
                                            </label>

                                            <input
                                                type="number"
                                                min="0"
                                                value={
                                                    planForm.maxCodingProblems
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        maxCodingProblems:
                                                            Number(
                                                                e.target
                                                                    .value
                                                            ),
                                                    })
                                                }
                                            />
                                        </div>

                                        <div>
                                            <label>
                                                Max Aptitude Questions
                                            </label>

                                            <input
                                                type="number"
                                                min="0"
                                                value={
                                                    planForm.maxAptitudeQuestions
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        maxAptitudeQuestions:
                                                            Number(
                                                                e.target
                                                                    .value
                                                            ),
                                                    })
                                                }
                                            />
                                        </div>

                                        <label className="plan-checkbox">
                                            <input
                                                type="checkbox"
                                                checked={
                                                    planForm.includesAIHints
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        includesAIHints:
                                                            e.target
                                                                .checked,
                                                    })
                                                }
                                            />
                                            AI Hints
                                        </label>

                                        <label className="plan-checkbox">
                                            <input
                                                type="checkbox"
                                                checked={
                                                    planForm.includesAnalytics
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        includesAnalytics:
                                                            e.target
                                                                .checked,
                                                    })
                                                }
                                            />
                                            Analytics
                                        </label>

                                        <label className="plan-checkbox">
                                            <input
                                                type="checkbox"
                                                checked={
                                                    planForm.includesTier1Companies
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        includesTier1Companies:
                                                            e.target
                                                                .checked,
                                                    })
                                                }
                                            />
                                            Tier-1 Companies
                                        </label>

                                        <label className="plan-checkbox">
                                            <input
                                                type="checkbox"
                                                checked={
                                                    planForm.includesPriorityCompute
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        includesPriorityCompute:
                                                            e.target
                                                                .checked,
                                                    })
                                                }
                                            />
                                            Priority Compute
                                        </label>

                                        <label className="plan-checkbox">
                                            <input
                                                type="checkbox"
                                                checked={
                                                    planForm.active
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        active:
                                                            e.target
                                                                .checked,
                                                    })
                                                }
                                            />
                                            Active
                                        </label>

                                        <label className="plan-checkbox">
                                            <input
                                                type="checkbox"
                                                checked={
                                                    planForm.featured
                                                }
                                                onChange={(e) =>
                                                    setPlanForm({
                                                        ...planForm,
                                                        featured:
                                                            e.target
                                                                .checked,
                                                    })
                                                }
                                            />
                                            Featured
                                        </label>

                                    </div>

                                    <div className="plan-form-actions">

                                        <button
                                            type="submit"
                                            className="admin-btn-primary"
                                        >
                                            Save Plan
                                        </button>

                                        <button
                                            type="button"
                                            className="admin-btn-secondary"
                                            onClick={() => {
                                                setShowPlanForm(false);
                                                setEditingPlan(null);
                                                resetPlanForm();
                                            }}
                                        >
                                            Cancel
                                        </button>

                                    </div>
                                </form>
                            )}

                            {/* PLANS TABLE */}

                            <div className="admin-table-wrapper">
                                <table className="admin-table">

                                    <thead>
                                        <tr>
                                            <th>Name</th>
                                            <th>Price</th>
                                            <th>Mocks</th>
                                            <th>Resumes</th>
                                            <th>Problems</th>
                                            <th>Active</th>
                                            <th>Featured</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {plans.length === 0 ? (
                                            <tr>
                                                <td
                                                    colSpan="8"
                                                    className="admin-empty"
                                                >
                                                    No plans found.
                                                </td>
                                            </tr>
                                        ) : (
                                            plans.map(
                                                (plan) => (
                                                    <tr
                                                        key={
                                                            plan.id
                                                        }
                                                    >

                                                        <td>
                                                            <strong>
                                                                {
                                                                    plan.name
                                                                }
                                                            </strong>
                                                        </td>

                                                        <td>
                                                            ₹
                                                            {
                                                                plan.priceInr
                                                            }
                                                            /
                                                            {String(
                                                                plan.interval ||
                                                                    "Month"
                                                            ).toLowerCase()}
                                                        </td>

                                                        <td>
                                                            {
                                                                plan.maxMockInterviews
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                plan.maxResumeScans
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                plan.maxCodingProblems
                                                            }
                                                        </td>

                                                        <td>
                                                            <span
                                                                className={`status-badge ${
                                                                    plan.active
                                                                        ? "active"
                                                                        : "pending"
                                                                }`}
                                                            >
                                                                {plan.active
                                                                    ? "Yes"
                                                                    : "No"}
                                                            </span>
                                                        </td>

                                                        <td>
                                                            {plan.featured
                                                                ? "⭐"
                                                                : "-"}
                                                        </td>

                                                        <td>
                                                            <div className="admin-action-group">

                                                                <button
                                                                    className="admin-btn-secondary"
                                                                    onClick={() =>
                                                                        handleEditPlan(
                                                                            plan
                                                                        )
                                                                    }
                                                                >
                                                                    Edit
                                                                </button>

                                                                <button
                                                                    className="admin-btn-danger"
                                                                    onClick={() => {
                                                                        if (
                                                                            window.confirm(
                                                                                "Delete this plan?"
                                                                            )
                                                                        ) {
                                                                            handleDeletePlan(
                                                                                plan.id
                                                                            );
                                                                        }
                                                                    }}
                                                                >
                                                                    Delete
                                                                </button>

                                                            </div>
                                                        </td>

                                                    </tr>
                                                )
                                            )
                                        )}
                                    </tbody>

                                </table>
                            </div>
                        </div>
                    )}

                {/* =================================================
                    SUBSCRIPTIONS
                ================================================= */}

                {activeTab ===
                    "subscriptions" &&
                    !loading && (
                        <div className="admin-section">

                            <div className="admin-section-header">
                                <div>
                                    <h2>
                                        Subscriptions
                                    </h2>

                                    <p>
                                        View all active and
                                        historical
                                        subscriptions.
                                    </p>
                                </div>
                            </div>

                            <div className="admin-table-wrapper">
                                <table className="admin-table">

                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Plan</th>
                                            <th>Status</th>
                                            <th>Amount</th>
                                            <th>Currency</th>
                                            <th>Subscribed</th>
                                            <th>Expires</th>
                                            <th>Auto Renew</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {subscriptions.length ===
                                        0 ? (
                                            <tr>
                                                <td
                                                    colSpan="8"
                                                    className="admin-empty"
                                                >
                                                    No subscriptions found.
                                                </td>
                                            </tr>
                                        ) : (
                                            subscriptions.map(
                                                (sub) => (
                                                    <tr
                                                        key={
                                                            sub.id
                                                        }
                                                    >

                                                        <td>
                                                            {
                                                                sub.id
                                                            }
                                                        </td>

                                                        <td>
                                                            {sub.planName ||
                                                                sub.plan?.name ||
                                                                "-"}
                                                        </td>

                                                        <td>
                                                            <span
                                                                className={`status-badge ${
                                                                    sub.subscriptionStatus ===
                                                                    "ACTIVE"
                                                                        ? "active"
                                                                        : "pending"
                                                                }`}
                                                            >
                                                                {sub.subscriptionStatus ||
                                                                    sub.status ||
                                                                    "-"}
                                                            </span>
                                                        </td>

                                                        <td>
                                                            ₹
                                                            {sub.amountPaid ??
                                                                sub.amount ??
                                                                0}
                                                        </td>

                                                        <td>
                                                            {sub.currency ||
                                                                "INR"}
                                                        </td>

                                                        <td>
                                                            {sub.subscribedAt
                                                                ? new Date(
                                                                      sub.subscribedAt
                                                                  ).toLocaleDateString()
                                                                : "-"}
                                                        </td>

                                                        <td>
                                                            {sub.expiresAt
                                                                ? new Date(
                                                                      sub.expiresAt
                                                                  ).toLocaleDateString()
                                                                : "-"}
                                                        </td>

                                                        <td>
                                                            {sub.autoRenew
                                                                ? "Yes"
                                                                : "No"}
                                                        </td>

                                                    </tr>
                                                )
                                            )
                                        )}
                                    </tbody>

                                </table>
                            </div>
                        </div>
                    )}

                {/* =================================================
                    ENTITLEMENTS
                ================================================= */}

                {activeTab ===
                    "entitlements" &&
                    !loading && (
                        <div className="admin-section">

                            <div className="admin-section-header">
                                <div>
                                    <h2>
                                        Manual Entitlements
                                    </h2>

                                    <p>
                                        Grant or revoke Pro
                                        and Elite access
                                        manually.
                                    </p>
                                </div>
                            </div>

                            <div className="admin-table-wrapper">
                                <table className="admin-table">

                                    <thead>
                                        <tr>
                                            <th>User</th>
                                            <th>Email</th>
                                            <th>Role</th>
                                            <th>
                                                Effective Plan
                                            </th>
                                            <th>
                                                Actions
                                            </th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {users.length ===
                                        0 ? (
                                            <tr>
                                                <td
                                                    colSpan="5"
                                                    className="admin-empty"
                                                >
                                                    No users found.
                                                </td>
                                            </tr>
                                        ) : (
                                            users.map(
                                                (user) => (
                                                    <tr
                                                        key={
                                                            user.id
                                                        }
                                                    >

                                                        <td>
                                                            <strong>
                                                                {user.username ||
                                                                    "-"}
                                                            </strong>
                                                        </td>

                                                        <td>
                                                            {user.email ||
                                                                "-"}
                                                        </td>

                                                        <td>
                                                            {user.role ||
                                                                "USER"}
                                                        </td>

                                                        <td>
                                                            <span className="plan-badge">
                                                                {user.currentPlan ||
                                                                    "STARTER"}
                                                            </span>
                                                        </td>

                                                        <td>
                                                            <div className="admin-action-group entitlement-actions">

                                                                <button
                                                                    className="admin-btn-primary"
                                                                    onClick={() =>
                                                                        openEntitlementModal(
                                                                            user,
                                                                            "PRO"
                                                                        )
                                                                    }
                                                                >
                                                                    <FaCrown />
                                                                    Pro
                                                                </button>

                                                                <button
                                                                    className="admin-btn-primary"
                                                                    onClick={() =>
                                                                        openEntitlementModal(
                                                                            user,
                                                                            "ELITE"
                                                                        )
                                                                    }
                                                                >
                                                                    <FaCrown />
                                                                    Elite
                                                                </button>

                                                                <button
                                                                    className="admin-btn-secondary"
                                                                    onClick={() =>
                                                                        handleRevokeAllEntitlements(
                                                                            user.id
                                                                        )
                                                                    }
                                                                >
                                                                    <FiX />
                                                                    Revoke
                                                                </button>

                                                            </div>
                                                        </td>

                                                    </tr>
                                                )
                                            )
                                        )}
                                    </tbody>

                                </table>
                            </div>

                            {/* =================================================
                                ENTITLEMENT MODAL
                            ================================================= */}

                            {entitlementModalUser && (
                                <div className="entitlement-modal-overlay">

                                    <div className="entitlement-modal">

                                        <div className="entitlement-modal-header">

                                            <div>
                                                <h3>
                                                    Grant{" "}
                                                    {
                                                        entitlementPlan
                                                    }{" "}
                                                    Access
                                                </h3>

                                                <p>
                                                    {
                                                        entitlementModalUser.username ||
                                                        entitlementModalUser.email
                                                    }
                                                </p>
                                            </div>

                                            <button
                                                className="modal-close-btn"
                                                onClick={() =>
                                                    setEntitlementModalUser(
                                                        null
                                                    )
                                                }
                                            >
                                                <FiX />
                                            </button>

                                        </div>

                                        <div className="entitlement-form">

                                            <label>
                                                Plan
                                            </label>

                                            <select
                                                value={
                                                    entitlementPlan
                                                }
                                                onChange={(e) =>
                                                    setEntitlementPlan(
                                                        e.target
                                                            .value
                                                    )
                                                }
                                            >
                                                <option value="PRO">
                                                    PRO
                                                </option>

                                                <option value="ELITE">
                                                    ELITE
                                                </option>
                                            </select>

                                            <label>
                                                Access Type
                                            </label>

                                            <select
                                                value={
                                                    entitlementType
                                                }
                                                onChange={(e) =>
                                                    setEntitlementType(
                                                        e.target
                                                            .value
                                                    )
                                                }
                                            >
                                                <option value="temporary">
                                                    Temporary
                                                </option>

                                                <option value="lifetime">
                                                    Lifetime
                                                </option>
                                            </select>

                                            {entitlementType ===
                                                "temporary" && (
                                                <>
                                                    <label>
                                                        Duration
                                                        (days)
                                                    </label>

                                                    <input
                                                        type="number"
                                                        min="1"
                                                        value={
                                                            entitlementDuration
                                                        }
                                                        onChange={(
                                                            e
                                                        ) =>
                                                            setEntitlementDuration(
                                                                Number(
                                                                    e
                                                                        .target
                                                                        .value
                                                                )
                                                            )
                                                        }
                                                    />
                                                </>
                                            )}

                                            <label>
                                                Reason
                                            </label>

                                            <textarea
                                                value={
                                                    entitlementReason
                                                }
                                                onChange={(e) =>
                                                    setEntitlementReason(
                                                        e.target
                                                            .value
                                                    )
                                                }
                                                placeholder="Reason for granting access..."
                                            />

                                            <div className="entitlement-form-actions">

                                                <button
                                                    className="admin-btn-primary"
                                                    onClick={() =>
                                                        handleGrantEntitlement(
                                                            entitlementModalUser.id
                                                        )
                                                    }
                                                >
                                                    Grant Access
                                                </button>

                                                <button
                                                    className="admin-btn-secondary"
                                                    onClick={() =>
                                                        setEntitlementModalUser(
                                                            null
                                                        )
                                                    }
                                                >
                                                    Cancel
                                                </button>

                                            </div>

                                        </div>
                                    </div>
                                </div>
                            )}

                        </div>
                    )}

            </main>
        </div>
    );
}