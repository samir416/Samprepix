import { useEffect, useState } from "react";
import {
    updateProfile,
    uploadProfilePicture
} from "../services/profileService";
import {
    User,
    Briefcase,
    GraduationCap,
    Link2
} from "lucide-react";

import { getCurrentUser } from "../services/authService";
import "../styles/profile.css";

export default function Profile() {

    const [user, setUser] = useState(null);

    const [isEditing, setIsEditing] = useState(false);

    const [selectedImage, setSelectedImage] = useState(null);

    const [selectedFile, setSelectedFile] = useState(null);

    const [isSaving, setIsSaving] = useState(false);

    const [hasChanges, setHasChanges] = useState(false);

    const [errors, setErrors] = useState({});

    const [imageError, setImageError] = useState(false);

    const [isUploadingImage, setIsUploadingImage] = useState(false);

    const [message, setMessage] = useState({
        type: "",
        text: ""
    });

    const [formData, setFormData] = useState({

        name: "",

        username: "",

        email: "",

        journeyType: "",

        targetRole: "",

        currentRole: "",

        experienceLevel: "",

        collegeName: "",

        degree: "",

        graduationYear: "",

        currentCompany: "",

        phone: "",

        gender: "",

        githubUrl: "",

        linkedinUrl: "",

        portfolioUrl: "",

        yearsOfExperience: "",

        careerGoal: "",

    });

    useEffect(() => {

        const loadUser = async () => {

            try {

                const currentUser = await getCurrentUser();

                localStorage.setItem(
                    "user",
                    JSON.stringify(currentUser)
                );

                setUser(currentUser);
                setImageError(false);

                setFormData({

                    name: currentUser.name || "",

                    username: currentUser.username || "",

                    email: currentUser.email || "",

                    journeyType: currentUser.journeyType || "",

                    targetRole: currentUser.targetRole || "",

                    currentRole: currentUser.currentRole || "",

                    experienceLevel: currentUser.experienceLevel || "",

                    collegeName: currentUser.college || "",

                    degree: currentUser.course || "",

                    graduationYear: currentUser.graduationYear || "",

                    currentCompany: currentUser.currentCompany || "",

                    phone: currentUser.phone || "",

                    gender: currentUser.gender || "",

                    githubUrl: currentUser.githubUrl || "",

                    linkedinUrl: currentUser.linkedinUrl || "",

                    portfolioUrl: currentUser.portfolioUrl || "",

                    yearsOfExperience: currentUser.yearsOfExperience || "",

                    careerGoal: currentUser.careerGoal || "",

                });

            }

            catch (error) {

                console.error(error);

                const storedUser = localStorage.getItem("user");

                if (storedUser) {

                    const parsedUser = JSON.parse(storedUser);

                    setUser(parsedUser);

                    setFormData({

                        name: parsedUser.name || "",

                        username: parsedUser.username || "",

                        email: parsedUser.email || "",

                        journeyType: parsedUser.journeyType || "",

                        targetRole: parsedUser.targetRole || "",

                        currentRole: parsedUser.currentRole || "",

                        experienceLevel: parsedUser.experienceLevel || "",

                        collegeName: parsedUser.college || "",

                        degree: parsedUser.course || "",

                        graduationYear: parsedUser.graduationYear || "",

                        currentCompany: parsedUser.currentCompany || "",

                        phone: parsedUser.phone || "",

                        gender: parsedUser.gender || "",

                        githubUrl: parsedUser.githubUrl || "",

                        linkedinUrl: parsedUser.linkedinUrl || "",

                        portfolioUrl: parsedUser.portfolioUrl || "",

                        yearsOfExperience: parsedUser.yearsOfExperience || "",

                        careerGoal: parsedUser.careerGoal || "",

                    });

                }

            }

        };

        loadUser();

    }, []);

    useEffect(() => {

        const handleBeforeUnload = (event) => {

            if (!hasChanges) {

                return;

            }

            event.preventDefault();

            event.returnValue = "";

        };

        window.addEventListener(
            "beforeunload",
            handleBeforeUnload
        );

        return () => {

            window.removeEventListener(
                "beforeunload",
                handleBeforeUnload
            );

        };

    }, [hasChanges]);


    const avatarLetter =
        user?.name?.charAt(0)?.toUpperCase()
        ||
        user?.username?.charAt(0)?.toUpperCase()
        ||
        "U";

    const profileFields = [

        formData.name,

        formData.username,

        formData.email,

        formData.journeyType,

        formData.targetRole,

        formData.currentRole,

        formData.experienceLevel,

        formData.phone,

        formData.gender,

        formData.careerGoal,

        formData.githubUrl,

        formData.linkedinUrl,

        formData.portfolioUrl,

        user?.profilePicture,

        formData.collegeName,

        formData.degree,

        formData.graduationYear,

        formData.currentCompany,

        formData.yearsOfExperience

    ];

    const completedFields = profileFields.filter(value => {

        if (value === null || value === undefined) {

            return false;

        }

        return String(value).trim() !== "";

    }).length;

    const profileCompletion = Math.round(

        (completedFields / profileFields.length) * 100

    );


    const handleChange = (e) => {

        const { name, value } = e.target;

        const updatedForm = {

            ...formData,

            [name]: value

        };

        setFormData(updatedForm);

        setHasChanges(true);

        const newErrors = {

            ...errors

        };

        if (name === "targetRole") {

            if (!value.trim()) {

                newErrors.targetRole = "Target Role is required.";

            } else {

                delete newErrors.targetRole;

            }

        }

        if (name === "journeyType") {

            if (!value) {

                newErrors.journeyType = "Journey is required.";

            } else {

                delete newErrors.journeyType;

            }

        }

        if (name === "experienceLevel") {

            if (!value) {

                newErrors.experienceLevel = "Experience Level is required.";

            } else {

                delete newErrors.experienceLevel;

            }

        }

        if (name === "phone") {

            if (value) {

                const phoneRegex = /^[6-9]\d{9}$/;

                if (!phoneRegex.test(value)) {

                    newErrors.phone = "Enter a valid phone number.";

                } else {

                    delete newErrors.phone;

                }

            } else {

                delete newErrors.phone;

            }

        }

        if (name === "githubUrl") {

            if (value) {

                try {

                    new URL(value);

                    delete newErrors.githubUrl;

                } catch {

                    newErrors.githubUrl = "Invalid GitHub URL.";

                }

            } else {

                delete newErrors.githubUrl;

            }

        }

        if (name === "linkedinUrl") {

            if (value) {

                try {

                    new URL(value);

                    delete newErrors.linkedinUrl;

                } catch {

                    newErrors.linkedinUrl = "Invalid LinkedIn URL.";

                }

            } else {

                delete newErrors.linkedinUrl;

            }

        }

        if (name === "portfolioUrl") {

            if (value) {

                try {

                    new URL(value);

                    delete newErrors.portfolioUrl;

                } catch {

                    newErrors.portfolioUrl = "Invalid Portfolio URL.";

                }

            } else {

                delete newErrors.portfolioUrl;

            }

        }

        setErrors(newErrors);

    };


    const handleImageChange = (e) => {

        setErrors({});

        setMessage({
            type: "",
            text: ""
        });

        const file = e.target.files[0];

        if (!file) return;

        if (!file.type.startsWith("image/")) {

            alert("Please select a valid image.");

            return;
        }

        if (file.size > 5 * 1024 * 1024) {

            alert("Image size must be less than 5 MB.");

            return;
        }

        if (selectedImage) {

            URL.revokeObjectURL(selectedImage);

        }

        const preview = URL.createObjectURL(file);

        setSelectedFile(file);

        setSelectedImage(preview);

        setHasChanges(true);

        setImageError(false);

    };

    const handleSave = async () => {

        setMessage({
            type: "",
            text: ""
        });

        if (!validateForm()) {

            return;

        }

        try {
            setIsSaving(true);

            let profilePicture = user?.profilePicture;

            if (selectedFile) {

                setIsUploadingImage(true);

                try {

                    const imageUrl = await uploadProfilePicture(selectedFile);

                    profilePicture = imageUrl.startsWith("http")
                        ? imageUrl
                        : `http://localhost:8080${imageUrl}`;

                } finally {

                    setIsUploadingImage(false);

                }

            }

            const updatedProfile = await updateProfile({

                journeyType: formData.journeyType,

                targetRole: formData.targetRole,

                experienceLevel: formData.experienceLevel,

                yearsOfExperience:
                    formData.yearsOfExperience === ""
                        ? null
                        : Number(formData.yearsOfExperience),

                careerGoal: formData.careerGoal,

                college: formData.collegeName,

                course: formData.degree,

                graduationYear: formData.graduationYear,

                currentRole: formData.currentRole,

                currentCompany: formData.currentCompany,

                phone: formData.phone,

                gender: formData.gender,

                githubUrl: formData.githubUrl,

                linkedinUrl: formData.linkedinUrl,

                portfolioUrl: formData.portfolioUrl

            });

            const updatedUser = {

                ...updatedProfile,

                name: user?.name,

                username: user?.username,

                email: user?.email,

                profilePicture

            };

            setUser(updatedUser);

            localStorage.setItem(
                "user",
                JSON.stringify(updatedUser)
            );

            if (selectedImage) {

                URL.revokeObjectURL(selectedImage);

            }

            setSelectedImage(null);

            setSelectedFile(null);

            setHasChanges(false);

            setImageError(false);

            setErrors({});

            setIsEditing(false);

            setMessage({
                type: "success",
                text: "Profile updated successfully."
            });

            setTimeout(() => {

                setMessage({
                    type: "",
                    text: ""
                });

            }, 3000);

        } catch (error) {

            console.error(error);

            setMessage({
                type: "error",
                text: "Failed to save profile."
            });

            setTimeout(() => {

                setMessage({
                    type: "",
                    text: ""
                });

            }, 3000);

        } finally {

            setIsSaving(false);

        }

    };

    const validateForm = () => {

        const newErrors = {};

        if (!formData.targetRole.trim()) {

            newErrors.targetRole = "Target Role is required.";

        }

        if (!formData.journeyType) {

            newErrors.journeyType = "Journey is required.";

        }

        if (!formData.experienceLevel) {

            newErrors.experienceLevel = "Experience Level is required.";

        }

        if (formData.phone) {

            const phoneRegex = /^[6-9]\d{9}$/;

            if (!phoneRegex.test(formData.phone)) {

                newErrors.phone = "Enter a valid phone number.";

            }

        }

        if (formData.githubUrl) {

            try {

                new URL(formData.githubUrl);

            } catch {

                newErrors.githubUrl = "Invalid GitHub URL.";

            }

        }

        if (formData.linkedinUrl) {

            try {

                new URL(formData.linkedinUrl);

            } catch {

                newErrors.linkedinUrl = "Invalid LinkedIn URL.";

            }

        }

        if (formData.portfolioUrl) {

            try {

                new URL(formData.portfolioUrl);

            } catch {

                newErrors.portfolioUrl = "Invalid Portfolio URL.";

            }

        }

        setErrors(newErrors);

        return Object.keys(newErrors).length === 0;

    };


    return (

        <div className="profile-page">

            <div className="profile-header">

                <div>

                    <h1>

                        My Profile

                    </h1>

                    <p>

                        Manage your account, career preferences and personal information.

                    </p>

                </div>

                <div className="profile-actions">

                    {

                        isEditing &&
                        <button
                            className="profile-save-btn"
                            onClick={handleSave}
                            disabled={isSaving || !hasChanges || Object.keys(errors).length > 0}
                        >
                            {isSaving ? "Saving..." : "Save Changes"}

                        </button>

                    }

                    <button
                        className="profile-edit-btn"
                        onClick={async () => {

                            if (isEditing) {

                                if (hasChanges) {

                                    const discard = window.confirm(

                                        "You have unsaved changes. Do you want to discard them?"

                                    );

                                    if (!discard) {

                                        return;

                                    }

                                }

                                if (selectedImage) {

                                    URL.revokeObjectURL(selectedImage);

                                }

                                setSelectedImage(null);

                                setSelectedFile(null);

                                setHasChanges(false);

                                setImageError(false);

                                setErrors({});

                                setMessage({
                                    type: "",
                                    text: ""
                                });

                                try {

                                    const currentUser = await getCurrentUser();

                                    setUser(currentUser);

                                    setFormData({

                                        name: currentUser.name || "",

                                        username: currentUser.username || "",

                                        email: currentUser.email || "",

                                        journeyType: currentUser.journeyType || "",

                                        targetRole: currentUser.targetRole || "",

                                        currentRole: currentUser.currentRole || "",

                                        experienceLevel: currentUser.experienceLevel || "",

                                        collegeName: currentUser.college || "",

                                        degree: currentUser.course || "",

                                        graduationYear: currentUser.graduationYear || "",

                                        currentCompany: currentUser.currentCompany || "",

                                        phone: currentUser.phone || "",

                                        gender: currentUser.gender || "",

                                        githubUrl: currentUser.githubUrl || "",

                                        linkedinUrl: currentUser.linkedinUrl || "",

                                        portfolioUrl: currentUser.portfolioUrl || "",

                                        yearsOfExperience: currentUser.yearsOfExperience || "",

                                        careerGoal: currentUser.careerGoal || ""

                                    });

                                } catch (error) {

                                    console.error(error);

                                }

                            }

                            setIsEditing(!isEditing);

                        }}
                    >

                        {isEditing ? "Cancel" : "Edit Profile"}

                    </button>

                </div>

            </div>

            {

                message.text &&

                <div
                    className={
                        message.type === "success"
                            ? "profile-page-success-message"
                            : "profile-page-error-message"
                    }
                >

                    {message.text}

                </div>

            }

            <div className="profile-page-card">

                <div className="profile-page-cover"></div>

                <div className="profile-page-hero">

                    <div className="profile-page-avatar-wrapper">

                        {

                            isUploadingImage ?

                                <div className="profile-page-avatar-large profile-page-uploading">

                                    Uploading...

                                </div>

                                :

                                selectedImage ?

                                    <img
                                        src={selectedImage}
                                        alt="Profile"
                                        className="profile-page-avatar-large"
                                    />

                                    :

                                    user?.profilePicture && !imageError ?

                                        <img
                                            src={
                                                user.profilePicture.startsWith("http")
                                                    ? user.profilePicture
                                                    : `http://localhost:8080${user.profilePicture}`
                                            }
                                            alt="Profile"
                                            className="profile-page-avatar-large"
                                            onError={() => setImageError(true)}
                                        />

                                        :

                                        <div className="profile-page-avatar-large">

                                            {avatarLetter}

                                        </div>

                        }

                        {

                            isEditing &&

                            <div className="profile-page-image-actions">

                                <label className="profile-page-camera-btn">

                                    📷

                                    <input
                                        type="file"
                                        accept="image/*"
                                        hidden
                                        onChange={handleImageChange}
                                    />

                                </label>

                                {

                                    (selectedImage || user?.profilePicture) &&

                                    <button
                                        type="button"
                                        className="profile-page-remove-btn"
                                        onClick={() => {

                                            if (selectedImage) {

                                                URL.revokeObjectURL(selectedImage);

                                            }

                                            setSelectedImage(null);

                                            setSelectedFile(null);

                                            setUser(prev => ({

                                                ...prev,

                                                profilePicture: ""

                                            }));

                                            setHasChanges(true);

                                            setImageError(false);

                                        }}

                                    >

                                        Remove

                                    </button>

                                }

                            </div>

                        }

                    </div>


                    <div className="profile-page-hero-content">

                        <h2>

                            {formData.name || "Your Name"}

                        </h2>


                    </div>

                </div>

                <div className="profile-page-stats">

                    <div className="profile-page-stat-card">

                        <h3>

                            {profileCompletion}%

                        </h3>

                        <span>

                            Profile Completion

                        </span>

                    </div>

                    <div className="profile-page-stat-card">

                        <h3>

                            0

                        </h3>

                        <span>

                            Mock Interviews

                        </span>

                    </div>

                    <div className="profile-page-stat-card">

                        <h3>

                            0

                        </h3>

                        <span>

                            Coding Tests

                        </span>

                    </div>

                    <div className="profile-page-stat-card">

                        <h3>

                            0

                        </h3>

                        <span>

                            Achievements

                        </span>

                    </div>

                </div>

                <div className="profile-page-card-header">

                    <h2>

                        Profile Details

                    </h2>

                </div>

                <form
                    className="profile-page-card-body"
                    onSubmit={(e) => {

                        e.preventDefault();

                        if (
                            !isSaving &&
                            hasChanges &&
                            Object.keys(errors).length === 0
                        ) {
                            handleSave();
                        }

                    }}
                >

                    <>

                        <div className="profile-page-section">

                            <h3 className="profile-page-section-title">

                                 <User size={20} />

                                Personal details

                            </h3>

                            <div className="profile-page-info-grid">

                                <div className="profile-page-info-item">

                                    <label>

                                        Full Name

                                    </label>

                                    {
                                        isEditing ?

                                            <input
                                                type="text"
                                                value={formData.name}
                                                className="profile-page-input"
                                                readOnly
                                            />

                                            :

                                            <span>

                                                {formData.name || "-"}

                                            </span>

                                    }

                                </div>

                                <div className="profile-page-info-item">

                                    <label>

                                        Username

                                    </label>

                                    {
                                        isEditing ?

                                            <input
                                                type="text"
                                                value={formData.username}
                                                className="profile-page-input"
                                                readOnly
                                            />

                                            :

                                            <span>

                                                {formData.username || "-"}

                                            </span>

                                    }

                                </div>

                                <div className="profile-page-info-item">

                                    <label>

                                        Email

                                    </label>

                                    {
                                        isEditing ?

                                            <input
                                                type="email"
                                                value={formData.email}
                                                className="profile-page-input"
                                                readOnly
                                            />

                                            :

                                            <span>

                                                {formData.email || "-"}

                                            </span>

                                    }

                                </div>

                                <div className="profile-page-info-item">

                                    <label>

                                        Phone

                                    </label>

                                    {

                                        isEditing ?

                                            <>

                                                <input
                                                    type="text"
                                                    name="phone"
                                                    value={formData.phone}
                                                    onChange={handleChange}
                                                    className="profile-page-input"
                                                />

                                                {

                                                    errors.phone &&

                                                    <small className="profile-page-error">

                                                        {errors.phone}

                                                    </small>

                                                }

                                            </>

                                            :

                                            <span>

                                                {formData.phone || "-"}

                                            </span>

                                    }

                                </div>

                                <div className="profile-page-info-item">

                                    <label>

                                        Gender

                                    </label>

                                    {

                                        isEditing ?

                                            <select
                                                name="gender"
                                                value={formData.gender}
                                                onChange={handleChange}
                                                className="profile-page-select"
                                            >

                                                <option value="">Select Gender</option>

                                                <option value="MALE">Male</option>

                                                <option value="FEMALE">Female</option>

                                                <option value="OTHER">Other</option>

                                            </select>

                                            :

                                            <span>

                                                {formData.gender || "-"}

                                            </span>

                                    }

                                </div>

                            </div>

                        </div>

                        <div className="profile-page-section">

    <h3 className="profile-page-section-title">

        <Briefcase size={20} />

        Career Information

    </h3>

    <div className="profile-page-info-grid">

        <div className="profile-page-info-item">

            <label>

                Journey

            </label>

            {

                isEditing ?

                    <>

                        <select
                            name="journeyType"
                            value={formData.journeyType}
                            onChange={handleChange}
                            className="profile-page-select"
                        >

                            <option value="">Select Journey</option>

                            <option value="STUDENT">Student</option>

                            <option value="WORKING_PROFESSIONAL">Working Professional</option>

                            <option value="OTHER">Other</option>

                        </select>

                        {

                            errors.journeyType &&

                            <small className="profile-page-error">

                                {errors.journeyType}

                            </small>

                        }

                    </>

                    :

                    <span>

                        {formData.journeyType || "-"}

                    </span>

            }

        </div>

        <div className="profile-page-info-item">

            <label>

                Target Role

            </label>

            {

                isEditing ?

                    <>

                        <input
                            type="text"
                            name="targetRole"
                            value={formData.targetRole}
                            onChange={handleChange}
                            className="profile-page-input"
                            placeholder="Enter Target Role"
                        />

                        {

                            errors.targetRole &&

                            <small className="profile-page-error">

                                {errors.targetRole}

                            </small>

                        }

                    </>

                    :

                    <span>

                        {formData.targetRole || "-"}

                    </span>

            }

        </div>

        <div className="profile-page-info-item">

            <label>

                Current Role

            </label>

            {

                isEditing ?

                    <input
                        type="text"
                        name="currentRole"
                        value={formData.currentRole}
                        onChange={handleChange}
                        className="profile-page-input"
                        placeholder="Enter Current Role"
                    />

                    :

                    <span>

                        {formData.currentRole || "-"}

                    </span>

            }

        </div>

        <div className="profile-page-info-item">

            <label>

                Career Goal

            </label>

            {

                isEditing ?

                    <select
                        name="careerGoal"
                        value={formData.careerGoal}
                        onChange={handleChange}
                        className="profile-page-select"
                    >

                        <option value="">Select Career Goal</option>

                        <option value="JOB">Get a Job</option>

                        <option value="COMPANY_SWITCH">Switch Company</option>

                        <option value="DOMAIN_SWITCH">Switch Domain</option>

                        <option value="PROMOTION">Promotion</option>

                        <option value="INTERVIEW_PRACTICE">Interview Practice</option>

                    </select>

                    :

                    <span>

                        {formData.careerGoal || "-"}

                    </span>

            }

        </div>

        <div className="profile-page-info-item">

            <label>

                Experience Level

            </label>

            {

                isEditing ?

                    <>

                        <select
                            name="experienceLevel"
                            value={formData.experienceLevel}
                            onChange={handleChange}
                            className="profile-page-select"
                        >

                            <option value="">Select Experience Level</option>

                            <option value="FRESHER">Fresher</option>

                            <option value="BEGINNER">Beginner</option>

                            <option value="INTERMEDIATE">Intermediate</option>

                            <option value="ADVANCED">Advanced</option>

                        </select>

                        {

                            errors.experienceLevel &&

                            <small className="profile-page-error">

                                {errors.experienceLevel}

                            </small>

                        }

                    </>

                    :

                    <span>

                        {formData.experienceLevel || "-"}

                    </span>

            }

        </div>

    </div>

</div>

<div className="profile-page-section">

    <h3 className="profile-page-section-title">

            <GraduationCap size={20} />


        Education / Experience

    </h3>

    <div className="profile-page-info-grid">

        {

            formData.journeyType === "STUDENT" &&

            <>

                <div className="profile-page-info-item">

                    <label>

                        College Name

                    </label>

                    {

                        isEditing ?

                            <input
                                type="text"
                                name="collegeName"
                                value={formData.collegeName}
                                onChange={handleChange}
                                className="profile-page-input"
                                placeholder="Enter College Name"
                            />

                            :

                            <span>

                                {formData.collegeName || "-"}

                            </span>

                    }

                </div>

                <div className="profile-page-info-item">

                    <label>

                        Degree

                    </label>

                    {

                        isEditing ?

                            <input
                                type="text"
                                name="degree"
                                value={formData.degree}
                                onChange={handleChange}
                                className="profile-page-input"
                                placeholder="Enter Degree"
                            />

                            :

                            <span>

                                {formData.degree || "-"}

                            </span>

                    }

                </div>

                <div className="profile-page-info-item">

                    <label>

                        Graduation Year

                    </label>

                    {

                        isEditing ?

                            <select
                                name="graduationYear"
                                value={formData.graduationYear}
                                onChange={handleChange}
                                className="profile-page-select"
                            >

                                <option value="">Select Graduation Year</option>

                                <option value="2025">2025</option>

                                <option value="2026">2026</option>

                                <option value="2027">2027</option>

                                <option value="2028">2028</option>

                                <option value="2029">2029</option>

                                <option value="2030">2030</option>

                            </select>

                            :

                            <span>

                                {formData.graduationYear || "-"}

                            </span>

                    }

                </div>

            </>

        }

        {

            formData.journeyType === "WORKING_PROFESSIONAL" &&

            <>

                <div className="profile-page-info-item">

                    <label>

                        Company Name

                    </label>

                    {

                        isEditing ?

                            <input
                                type="text"
                                name="currentCompany"
                                value={formData.currentCompany}
                                onChange={handleChange}
                                className="profile-page-input"
                                placeholder="Enter Company Name"
                            />

                            :

                            <span>

                                {formData.currentCompany || "-"}

                            </span>

                    }

                </div>

                <div className="profile-page-info-item">

                    <label>

                        Years of Experience

                    </label>

                    {

                        isEditing ?

                            <select
                                name="yearsOfExperience"
                                value={formData.yearsOfExperience}
                                onChange={handleChange}
                                className="profile-page-select"
                            >

                                <option value="">Select Experience</option>

                                <option value="0.0">Fresher</option>

                                <option value="1.0">1 Year</option>

                                <option value="2.0">2 Years</option>

                                <option value="3.0">3 Years</option>

                                <option value="4.0">4 Years</option>

                                <option value="5.0">5+ Years</option>

                            </select>

                            :

                            <span>

                                {
                                    formData.yearsOfExperience
                                        ? `${formData.yearsOfExperience} Year${Number(formData.yearsOfExperience) === 1 ? "" : "s"}`
                                        : "-"
                                }

                            </span>

                    }

                </div>

            </>

        }

    </div>

</div>

<div className="profile-page-section">

    <h3 className="profile-page-section-title">

            <Link2 size={20} />


        Social Links

    </h3>

    <div className="profile-page-info-grid">

        <div className="profile-page-info-item">

            <label>

                GitHub

            </label>

            {

                isEditing ?

                    <>

                        <input
                            type="url"
                            name="githubUrl"
                            value={formData.githubUrl}
                            onChange={handleChange}
                            className="profile-page-input"
                            placeholder="https://github.com/username"
                        />

                        {

                            errors.githubUrl &&

                            <small className="profile-page-error">

                                {errors.githubUrl}

                            </small>

                        }

                    </>

                    :

                    <span>

                        {formData.githubUrl || "-"}

                    </span>

            }

        </div>

        <div className="profile-page-info-item">

            <label>

                LinkedIn

            </label>

            {

                isEditing ?

                    <>

                        <input
                            type="url"
                            name="linkedinUrl"
                            value={formData.linkedinUrl}
                            onChange={handleChange}
                            className="profile-page-input"
                            placeholder="https://linkedin.com/in/username"
                        />

                        {

                            errors.linkedinUrl &&

                            <small className="profile-page-error">

                                {errors.linkedinUrl}

                            </small>

                        }

                    </>

                    :

                    <span>

                        {formData.linkedinUrl || "-"}

                    </span>

            }

        </div>

        <div className="profile-page-info-item">

            <label>

                Portfolio

            </label>

            {

                isEditing ?

                    <>

                        <input
                            type="url"
                            name="portfolioUrl"
                            value={formData.portfolioUrl}
                            onChange={handleChange}
                            className="profile-page-input"
                            placeholder="https://yourportfolio.com"
                        />

                        {

                            errors.portfolioUrl &&

                            <small className="profile-page-error">

                                {errors.portfolioUrl}

                            </small>

                        }

                    </>

                    :

                    <span>

                        {formData.portfolioUrl || "-"}

                    </span>

            }

        </div>

    </div>

</div>

                    </>

                </form>

            </div>

        </div>

    );


}
