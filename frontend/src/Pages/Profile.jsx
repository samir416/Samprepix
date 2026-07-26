import { useEffect, useState } from "react";
import { updateProfile } from "../services/profileService";
import { getCurrentUser } from "../services/authService";
import "../styles/profile.css";

export default function Profile() {

    const [user, setUser] = useState(null);

    const [isEditing, setIsEditing] = useState(false);

    const [selectedImage, setSelectedImage] = useState(null);

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

    const avatarLetter =
        user?.name?.charAt(0)?.toUpperCase()
        ||
        user?.username?.charAt(0)?.toUpperCase()
        ||
        "U";

    const handleChange = (e) => {

        const { name, value } = e.target;

        setFormData(prev => ({

            ...prev,

            [name]: value

        }));

    };

    const handleImageChange = (e) => {

        const file = e.target.files[0];

        if (!file) return;

        const imageUrl = URL.createObjectURL(file);

        setSelectedImage(imageUrl);

    };

    const handleSave = async () => {

        try {

            const updatedUser = await updateProfile({

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

            localStorage.setItem(
                "user",
                JSON.stringify(updatedUser)
            );

            setUser(updatedUser);

            setFormData({

                name: updatedUser.name || "",

                username: updatedUser.username || "",

                email: updatedUser.email || "",

                journeyType: updatedUser.journeyType || "",

                targetRole: updatedUser.targetRole || "",

                experienceLevel: updatedUser.experienceLevel || "",

                currentRole: updatedUser.currentRole || "",

                yearsOfExperience: updatedUser.yearsOfExperience || "",

                careerGoal: updatedUser.careerGoal || "",

                collegeName: updatedUser.college || "",

                degree: updatedUser.course || "",

                graduationYear: updatedUser.graduationYear || "",

                currentCompany: updatedUser.currentCompany || "",

                phone: updatedUser.phone || "",

                gender: updatedUser.gender || "",

                githubUrl: updatedUser.githubUrl || "",

                linkedinUrl: updatedUser.linkedinUrl || "",

                portfolioUrl: updatedUser.portfolioUrl || "",

            });

            setIsEditing(false);

        }

        catch (error) {

            console.error("Profile update failed:", error);

        }

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
                        >

                            Save Changes

                        </button>

                    }

                    <button
                        className="profile-edit-btn"
                        onClick={() => setIsEditing(!isEditing)}
                    >

                        {isEditing ? "Cancel" : "Edit Profile"}

                    </button>

                </div>

            </div>

            <div className="profile-page-card">

                <div className="profile-page-cover"></div>

                <div className="profile-page-hero">

                    <div className="profile-page-avatar-wrapper">

                        {

                            selectedImage ?

                                <img
                                    src={selectedImage}
                                    alt="Profile"
                                    className="profile-page-avatar-large"
                                />

                                :

                                user?.profilePicture ?

                                    <img
                                        src={user.profilePicture}
                                        alt="Profile"
                                        className="profile-page-avatar-large"
                                    />

                                    :

                                    <div className="profile-page-avatar-large">

                                        {avatarLetter}

                                    </div>

                        }

                        {

                            isEditing &&

                            <label className="profile-page-camera-btn">

                                📷

                                <input
                                    type="file"
                                    accept="image/*"
                                    hidden
                                    onChange={handleImageChange}
                                />

                            </label>

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

                            65%

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

                        Personal Information

                    </h2>

                </div>

                <div className="profile-page-card-body">

                    <div className="profile-page-info-grid">

                        <div className="profile-page-info-item">

                            <label>

                                Full Name

                            </label>

                            {

                                isEditing ?

                                    <input
                                        type="text"
                                        name="name"
                                        value={formData.name}
                                        onChange={handleChange}
                                        className="profile-page-input"
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
                                        name="username"
                                        value={formData.username}
                                        onChange={handleChange}
                                        className="profile-page-input"
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
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        className="profile-page-input"
                                    />

                                    :

                                    <span>

                                        {formData.email || "-"}

                                    </span>

                            }

                        </div>

                        <div className="profile-page-info-item">

                            <label>

                                Journey

                            </label>

                            {

                                isEditing ?

                                    <select
                                        name="journeyType"
                                        value={formData.journeyType}
                                        onChange={handleChange}
                                        className="profile-page-select"
                                    >

                                        <option value="">

                                            Select Journey

                                        </option>

                                        <option value="STUDENT">

                                            Student

                                        </option>

                                        <option value="WORKING_PROFESSIONAL">

                                            Working Professional

                                        </option>

                                        <option value="OTHER">

                                            Other

                                        </option>

                                    </select>

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

                                    <input
                                        type="text"
                                        name="targetRole"
                                        value={formData.targetRole}
                                        onChange={handleChange}
                                        className="profile-page-input"
                                        placeholder="Enter Target Role"
                                    />

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

                                    :

                                    <span>

                                        {formData.experienceLevel || "-"}

                                    </span>

                            }

                        </div>

                        <div className="profile-page-info-item">

                            <label>

                                Phone

                            </label>

                            {

                                isEditing ?

                                    <input
                                        type="text"
                                        name="phone"
                                        value={formData.phone}
                                        onChange={handleChange}
                                        className="profile-page-input"
                                    />

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

                        <div className="profile-page-info-item">

                            <label>

                                Experience

                            </label>

                            <span>

                                {
                                    formData.yearsOfExperience !== "" &&
                                        formData.yearsOfExperience !== null
                                        ? `${formData.yearsOfExperience} Year${Number(formData.yearsOfExperience) === 1 ? "" : "s"}`
                                        : "-"
                                }

                            </span>

                        </div>

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
                                                    formData.yearsOfExperience !== "" &&
                                                        formData.yearsOfExperience !== null
                                                        ? `${formData.yearsOfExperience} Year${Number(formData.yearsOfExperience) === 1 ? "" : "s"}`
                                                        : "-"
                                                }

                                            </span>

                                    }

                                </div>

                            </>

                        }

                        {

                            formData.journeyType === "OTHER" &&

                            <>

                                <div className="profile-page-info-item">

                                    <label>

                                        Institute / Organization

                                    </label>

                                    {

                                        isEditing ?

                                            <input
                                                type="text"
                                                name="collegeName"
                                                value={formData.collegeName}
                                                onChange={handleChange}
                                                className="profile-page-input"
                                                placeholder="Enter Institute / Organization"
                                            />

                                            :

                                            <span>

                                                {formData.collegeName || "-"}

                                            </span>

                                    }

                                </div>

                                <div className="profile-page-info-item">

                                    <label>

                                        Course / Program

                                    </label>

                                    {

                                        isEditing ?

                                            <input
                                                type="text"
                                                name="degree"
                                                value={formData.degree}
                                                onChange={handleChange}
                                                className="profile-page-input"
                                                placeholder="Enter Course / Program"
                                            />

                                            :

                                            <span>

                                                {formData.degree || "-"}

                                            </span>

                                    }

                                </div>

                            </>

                        }
                        <div className="profile-page-info-item">

                            <label>

                                GitHub

                            </label>

                            {

                                isEditing ?

                                    <input
                                        type="url"
                                        name="githubUrl"
                                        value={formData.githubUrl}
                                        onChange={handleChange}
                                        className="profile-page-input"
                                        placeholder="https://github.com/username"
                                    />

                                    :

                                    <span>

                                        {

                                            formData.githubUrl ?

                                                <a
                                                    href={formData.githubUrl}
                                                    target="_blank"
                                                    rel="noreferrer"
                                                >

                                                    {formData.githubUrl}

                                                </a>

                                                :

                                                "-"

                                        }

                                    </span>

                            }

                        </div>

                        <div className="profile-page-info-item">

                            <label>

                                LinkedIn

                            </label>

                            {

                                isEditing ?

                                    <input
                                        type="url"
                                        name="linkedinUrl"
                                        value={formData.linkedinUrl}
                                        onChange={handleChange}
                                        className="profile-page-input"
                                        placeholder="https://linkedin.com/in/username"
                                    />

                                    :

                                    <span>

                                        {

                                            formData.linkedinUrl ?

                                                <a
                                                    href={formData.linkedinUrl}
                                                    target="_blank"
                                                    rel="noreferrer"
                                                >

                                                    {formData.linkedinUrl}

                                                </a>

                                                :

                                                "-"

                                        }

                                    </span>

                            }

                        </div>

                        <div className="profile-page-info-item">

                            <label>

                                Portfolio

                            </label>

                            {

                                isEditing ?

                                    <input
                                        type="url"
                                        name="portfolioUrl"
                                        value={formData.portfolioUrl}
                                        onChange={handleChange}
                                        className="profile-page-input"
                                        placeholder="https://yourportfolio.com"
                                    />

                                    :

                                    <span>

                                        {

                                            formData.portfolioUrl ?

                                                <a
                                                    href={formData.portfolioUrl}
                                                    target="_blank"
                                                    rel="noreferrer"
                                                >

                                                    {formData.portfolioUrl}

                                                </a>

                                                :

                                                "-"

                                        }

                                    </span>

                            }

                        </div>

                    </div>

                </div>

            </div>

        </div>

    );
}
