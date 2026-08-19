import "../styles/codingarena.css";

import { useState, useRef, useEffect, useMemo } from "react";

import Editor from "@monaco-editor/react";

import {
    FiPlay,
    FiCpu,
    FiChevronDown,
    FiX,
    FiSearch,
    FiArrowLeft,
    FiArrowRight
} from "react-icons/fi";

import {
    getCodingProblems,
    getCodingProgress,
    selectCodingProblem,
    saveCodingState,
    executeCodingCode,
    submitCodingCode,
    getCodingHint
} from "../services/codingService";

export default function CodingArena() {

    const [language, setLanguage] =
        useState("");

    const [searchLanguage, setSearchLanguage] =
        useState("");

    const [showLanguages, setShowLanguages] =
        useState(false);

    const [selectedIndex, setSelectedIndex] =
        useState(0);

    const [problems, setProblems] =
        useState([]);

    const [progress, setProgress] =
        useState(null);

    const [selectedProblemIndex, setSelectedProblemIndex] =
        useState(null);

    const [codeMap, setCodeMap] =
        useState({});

    const [executionResult, setExecutionResult] =
        useState(null);

    const [isExecuting, setIsExecuting] =
        useState(false);

    const [isSubmitting, setIsSubmitting] =
        useState(false);

    const [isGeneratingHint, setIsGeneratingHint] =
        useState(false);

    const [aiHint, setAiHint] =
        useState("");

    const [aiHintError, setAiHintError] =
        useState("");

    const [showAiHint, setShowAiHint] =
        useState(false);

    const [showProblemMenu, setShowProblemMenu] =
        useState(false);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    const dropdownRef =
        useRef(null);

    const optionRefs =
        useRef([]);

    const problemMenuRef =
        useRef(null);

    const saveTimerRef =
        useRef(null);

    const selectedProblem =
        selectedProblemIndex !== null
            ? problems[selectedProblemIndex] || null
            : null;

    const hasSelectedProblem =
        selectedProblem !== null;

    const isFirstProblem =
        selectedProblemIndex === 0;

    const isLastProblem =
        selectedProblemIndex !== null &&
        selectedProblemIndex === problems.length - 1;

    const normalizeLanguageValue = (
        value
    ) => {

        if (!value) {
            return "";
        }

        return String(value)
            .trim()
            .toLowerCase();

    };

    const getLanguageDefaults = (
        value
    ) => {

        const normalized =
            normalizeLanguageValue(value);

        const defaults = {
            java: {
                label: "Java",
                monacoLanguage: "java",
                runtimeLanguage: "java"
            },
            python: {
                label: "Python",
                monacoLanguage: "python",
                runtimeLanguage: "python"
            },
            javascript: {
                label: "JavaScript",
                monacoLanguage: "javascript",
                runtimeLanguage: "javascript"
            },
            typescript: {
                label: "TypeScript",
                monacoLanguage: "typescript",
                runtimeLanguage: "typescript"
            },
            c: {
                label: "C",
                monacoLanguage: "c",
                runtimeLanguage: "c"
            },
            cpp: {
                label: "C++",
                monacoLanguage: "cpp",
                runtimeLanguage: "c++"
            },
            "c++": {
                label: "C++",
                monacoLanguage: "cpp",
                runtimeLanguage: "c++"
            },
            csharp: {
                label: "C#",
                monacoLanguage: "csharp",
                runtimeLanguage: "csharp"
            },
            "c#": {
                label: "C#",
                monacoLanguage: "csharp",
                runtimeLanguage: "csharp"
            },
            go: {
                label: "Go",
                monacoLanguage: "go",
                runtimeLanguage: "go"
            },
            rust: {
                label: "Rust",
                monacoLanguage: "rust",
                runtimeLanguage: "rust"
            },
            kotlin: {
                label: "Kotlin",
                monacoLanguage: "kotlin",
                runtimeLanguage: "kotlin"
            },
            swift: {
                label: "Swift",
                monacoLanguage: "swift",
                runtimeLanguage: "swift"
            },
            php: {
                label: "PHP",
                monacoLanguage: "php",
                runtimeLanguage: "php"
            },
            ruby: {
                label: "Ruby",
                monacoLanguage: "ruby",
                runtimeLanguage: "ruby"
            },
            dart: {
                label: "Dart",
                monacoLanguage: "dart",
                runtimeLanguage: "dart"
            },
            scala: {
                label: "Scala",
                monacoLanguage: "scala",
                runtimeLanguage: "scala"
            },
            bash: {
                label: "Bash",
                monacoLanguage: "shell",
                runtimeLanguage: "bash"
            },
            shell: {
                label: "Shell",
                monacoLanguage: "shell",
                runtimeLanguage: "bash"
            },
            sql: {
                label: "SQL",
                monacoLanguage: "sql",
                runtimeLanguage: "sqlite3"
            }
        };

        return defaults[normalized] || {
            label:
                String(value)
                    .charAt(0)
                    .toUpperCase() +
                String(value).slice(1),
            monacoLanguage:
                normalized || "plaintext",
            runtimeLanguage:
                normalized
        };
    };

    const parseLanguageConfigurations = (
        problem
    ) => {

        if (!problem) {
            return {};
        }

        const raw =
            problem.languageConfigurations;

        if (
            raw &&
            typeof raw === "object" &&
            !Array.isArray(raw)
        ) {
            return raw;
        }

        if (typeof raw !== "string") {
            return {};
        }

        try {

            const parsed =
                JSON.parse(raw);

            if (
                parsed &&
                typeof parsed === "object" &&
                !Array.isArray(parsed)
            ) {
                return parsed;
            }

        } catch (parseError) {

            console.error(
                "Invalid language configuration",
                parseError
            );

        }

        return {};
    };

    const getProblemLanguages = (
        problem
    ) => {

        if (!problem) {
            return [];
        }

        const configurations =
            parseLanguageConfigurations(
                problem
            );

        const configurationLanguages =
            Object.entries(
                configurations
            )
                .filter(
                    ([, configuration]) =>
                        configuration &&
                        typeof configuration === "object" &&
                        !Array.isArray(configuration)
                )
                .map(
                    ([value, configuration]) => {

                        const defaults =
                            getLanguageDefaults(
                                value
                            );

                        return {
                            value,
                            label:
                                configuration.displayName ||
                                configuration.name ||
                                defaults.label,
                            monacoLanguage:
                                configuration.monacoLanguage ||
                                defaults.monacoLanguage,
                            runtimeLanguage:
                                configuration.runtimeLanguage ||
                                defaults.runtimeLanguage,
                            runtimeVersion:
                                configuration.runtimeVersion ||
                                "",
                            fileName:
                                configuration.fileName ||
                                "",
                            starterCode:
                                configuration.starterCode ||
                                "",
                            executionTemplate:
                                configuration.executionTemplate ||
                                ""
                        };
                    }
                );

        if (
            configurationLanguages.length > 0
        ) {
            return configurationLanguages;
        }

        const starterCodes =
            problem.starterCodes;

        if (
            starterCodes &&
            typeof starterCodes === "object" &&
            !Array.isArray(starterCodes)
        ) {

            return Object.keys(
                starterCodes
            ).map(
                (value) => {

                    const defaults =
                        getLanguageDefaults(
                            value
                        );

                    return {
                        value,
                        label: defaults.label,
                        monacoLanguage:
                            defaults.monacoLanguage,
                        runtimeLanguage:
                            defaults.runtimeLanguage,
                        runtimeVersion: "",
                        fileName: "",
                        starterCode:
                            typeof starterCodes[value] === "string"
                                ? starterCodes[value]
                                : "",
                        executionTemplate: ""
                    };
                }
            );
        }

        if (
            typeof problem.starterCode ===
            "string" &&
            problem.starterCode.trim()
        ) {

            try {

                const parsed =
                    JSON.parse(
                        problem.starterCode
                    );

                if (
                    parsed &&
                    typeof parsed === "object" &&
                    !Array.isArray(parsed)
                ) {

                    return Object.keys(
                        parsed
                    ).map(
                        (value) => {

                            const defaults =
                                getLanguageDefaults(
                                    value
                                );

                            return {
                                value,
                                label:
                                    defaults.label,
                                monacoLanguage:
                                    defaults.monacoLanguage,
                                runtimeLanguage:
                                    defaults.runtimeLanguage,
                                runtimeVersion: "",
                                fileName: "",
                                starterCode:
                                    typeof parsed[value] ===
                                    "string"
                                        ? parsed[value]
                                        : "",
                                executionTemplate: ""
                            };
                        }
                    );
                }

            } catch {

                return [];
            }
        }

        return [];
    };

    const availableLanguages =
        useMemo(
            () =>
                getProblemLanguages(
                    selectedProblem
                ),
            [selectedProblem]
        );

    const filteredLanguages =
        availableLanguages.filter(
            (item) =>
                item.label
                    .toLowerCase()
                    .includes(
                        searchLanguage
                            .toLowerCase()
                    ) ||
                item.value
                    .toLowerCase()
                    .includes(
                        searchLanguage
                            .toLowerCase()
                    )
        );

    useEffect(() => {

        loadCodingArena();

        return () => {

            if (saveTimerRef.current) {

                clearTimeout(
                    saveTimerRef.current
                );

            }

        };

    }, []);

    useEffect(() => {

        if (!selectedProblem) {
            return;
        }

        const languages =
            getProblemLanguages(
                selectedProblem
            );

        if (languages.length === 0) {

            setLanguage("");
            setSearchLanguage("");

            return;
        }

        const currentExists =
            languages.some(
                (item) =>
                    item.value === language
            );

        if (!currentExists) {

            const progressLanguage =
                progress?.lastLanguage;

            const progressExists =
                progressLanguage &&
                languages.some(
                    (item) =>
                        item.value ===
                        progressLanguage
                );

            const nextLanguage =
                progressExists
                    ? progressLanguage
                    : languages[0].value;

            const nextLanguageData =
                languages.find(
                    (item) =>
                        item.value ===
                        nextLanguage
                );

            setLanguage(
                nextLanguage
            );

            setSearchLanguage(
                nextLanguageData?.label ||
                nextLanguage
            );
        }

    }, [
        selectedProblem,
        progress,
        language
    ]);

    useEffect(() => {

        const handleOutsideClick = (
            event
        ) => {

            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(
                    event.target
                )
            ) {

                setShowLanguages(false);
            }

            if (
                problemMenuRef.current &&
                !problemMenuRef.current.contains(
                    event.target
                )
            ) {

                setShowProblemMenu(false);
            }
        };

        document.addEventListener(
            "mousedown",
            handleOutsideClick
        );

        return () => {

            document.removeEventListener(
                "mousedown",
                handleOutsideClick
            );

        };

    }, []);

    const getStarterCode = (
        problem,
        currentLanguage
    ) => {

        if (
            !problem ||
            !currentLanguage
        ) {
            return "";
        }

        const configurations =
            parseLanguageConfigurations(
                problem
            );

        const configuration =
            configurations[
                currentLanguage
            ];

        if (
            configuration &&
            typeof configuration === "object" &&
            typeof configuration.starterCode ===
            "string"
        ) {

            return configuration.starterCode;
        }

        const starterCodes =
            problem.starterCodes;

        if (
            starterCodes &&
            typeof starterCodes === "object" &&
            !Array.isArray(starterCodes)
        ) {

            const exact =
                starterCodes[
                    currentLanguage
                ];

            if (typeof exact === "string") {
                return exact;
            }

            const matchingKey =
                Object.keys(
                    starterCodes
                ).find(
                    (key) =>
                        normalizeLanguageValue(key) ===
                        normalizeLanguageValue(
                            currentLanguage
                        )
                );

            if (
                matchingKey &&
                typeof starterCodes[
                    matchingKey
                ] === "string"
            ) {

                return starterCodes[
                    matchingKey
                ];
            }
        }

        if (
            typeof problem.starterCode ===
            "string"
        ) {

            try {

                const parsed =
                    JSON.parse(
                        problem.starterCode
                    );

                if (
                    parsed &&
                    typeof parsed === "object" &&
                    typeof parsed[
                        currentLanguage
                    ] === "string"
                ) {

                    return parsed[
                        currentLanguage
                    ];
                }

                if (
                    parsed &&
                    typeof parsed === "object"
                ) {

                    const matchingKey =
                        Object.keys(
                            parsed
                        ).find(
                            (key) =>
                                normalizeLanguageValue(
                                    key
                                ) ===
                                normalizeLanguageValue(
                                    currentLanguage
                                )
                        );

                    if (
                        matchingKey &&
                        typeof parsed[
                            matchingKey
                        ] === "string"
                    ) {

                        return parsed[
                            matchingKey
                        ];
                    }
                }

            } catch {

                return problem.starterCode;
            }
        }

        const selectedLanguage =
            getProblemLanguages(
                problem
            ).find(
                (item) =>
                    normalizeLanguageValue(
                        item.value
                    ) ===
                    normalizeLanguageValue(
                        currentLanguage
                    )
            );

        return selectedLanguage?.starterCode ||
            "";
    };

    const getCodeKey = (
        problemId,
        currentLanguage
    ) => {

        return `${problemId}_${currentLanguage}`;
    };

    const getCurrentCode = () => {

        if (
            !selectedProblem ||
            !language
        ) {
            return "";
        }

        const key =
            getCodeKey(
                selectedProblem.id,
                language
            );

        if (
            Object.prototype.hasOwnProperty.call(
                codeMap,
                key
            )
        ) {

            return codeMap[key];
        }

        return getStarterCode(
            selectedProblem,
            language
        );
    };

    const getMonacoLanguage = () => {

        const selected =
            availableLanguages.find(
                (item) =>
                    item.value === language
            );

        return (
            selected?.monacoLanguage ||
            getLanguageDefaults(
                language
            ).monacoLanguage ||
            "plaintext"
        );
    };

    const loadCodingArena = async () => {

        try {

            setLoading(true);
            setError("");

            const [
                problemsResponse,
                progressResponse
            ] = await Promise.all([
                getCodingProblems(),
                getCodingProgress()
            ]);

            const backendProblems =
                Array.isArray(
                    problemsResponse.data
                )
                    ? problemsResponse.data
                    : [];

            const backendProgress =
                progressResponse.data ||
                null;

            setProblems(
                backendProblems
            );

            setProgress(
                backendProgress
            );

            if (
                backendProblems.length === 0
            ) {

                setSelectedProblemIndex(
                    null
                );

                return;
            }

            const currentProblemId =
                backendProgress?.currentProblem?.id;

            const lastSelectedProblemId =
                backendProgress
                    ?.lastSelectedProblem?.id;

            let restoredProblemId =
                currentProblemId ||
                lastSelectedProblemId;

            if (
                backendProgress?.completed &&
                !currentProblemId
            ) {

                restoredProblemId = null;
            }

            let restoredIndex = -1;

            if (restoredProblemId) {

                restoredIndex =
                    backendProblems.findIndex(
                        (problem) =>
                            problem.id ===
                            restoredProblemId
                    );
            }

            if (restoredIndex === -1) {
                restoredIndex = 0;
            }

            setSelectedProblemIndex(
                restoredIndex
            );

            const restoredProblem =
                backendProblems[
                    restoredIndex
                ];

            const restoredLanguages =
                getProblemLanguages(
                    restoredProblem
                );

            const progressLanguage =
                backendProgress?.lastLanguage;

            const restoredLanguageExists =
                progressLanguage &&
                restoredLanguages.some(
                    (item) =>
                        normalizeLanguageValue(
                            item.value
                        ) ===
                        normalizeLanguageValue(
                            progressLanguage
                        )
                );

            const initialLanguage =
                restoredLanguageExists
                    ? progressLanguage
                    : restoredLanguages[0]?.value ||
                      "";

            const initialLanguageData =
                restoredLanguages.find(
                    (item) =>
                        item.value ===
                        initialLanguage
                );

            setLanguage(
                initialLanguage
            );

            setSearchLanguage(
                initialLanguageData?.label ||
                initialLanguage
            );

            if (
                backendProgress?.lastCode &&
                backendProgress?.lastLanguage &&
                backendProgress
                    ?.lastSelectedProblem?.id
            ) {

                const key =
                    getCodeKey(
                        backendProgress
                            .lastSelectedProblem.id,
                        backendProgress.lastLanguage
                    );

                setCodeMap(
                    (previous) => ({
                        ...previous,
                        [key]:
                            backendProgress.lastCode
                    })
                );
            }

        } catch (requestError) {

            console.error(
                "Failed to load Coding Arena",
                requestError
            );

            setError(
                requestError?.response?.data?.message ||
                requestError?.response?.data?.error ||
                "Unable to load Coding Arena."
            );

            setProblems([]);

            setSelectedProblemIndex(
                null
            );

        } finally {

            setLoading(false);
        }
    };

    const handleProblemSelect = async (
        index
    ) => {

        const problem =
            problems[index];

        if (!problem) {
            return;
        }

        setSelectedProblemIndex(
            index
        );

        setShowProblemMenu(
            false
        );

        setExecutionResult(
            null
        );

        setAiHint("");
        setAiHintError("");
        setShowAiHint(false);

        const problemLanguages =
            getProblemLanguages(
                problem
            );

        const currentLanguageExists =
            problemLanguages.some(
                (item) =>
                    normalizeLanguageValue(
                        item.value
                    ) ===
                    normalizeLanguageValue(
                        language
                    )
            );

        const nextLanguage =
            currentLanguageExists
                ? language
                : problemLanguages[0]?.value ||
                  "";

        const nextLanguageData =
            problemLanguages.find(
                (item) =>
                    item.value ===
                    nextLanguage
            );

        setLanguage(
            nextLanguage
        );

        setSearchLanguage(
            nextLanguageData?.label ||
            nextLanguage
        );

        const key =
            getCodeKey(
                problem.id,
                nextLanguage
            );

        if (
            nextLanguage &&
            !Object.prototype.hasOwnProperty.call(
                codeMap,
                key
            )
        ) {

            setCodeMap(
                (previous) => ({
                    ...previous,
                    [key]:
                        getStarterCode(
                            problem,
                            nextLanguage
                        )
                })
            );
        }

        try {

            const response =
                await selectCodingProblem(
                    problem.id
                );

            setProgress(
                response.data
            );

        } catch (requestError) {

            console.error(
                "Failed to save selected problem",
                requestError
            );
        }
    };

    const handlePreviousProblem = async () => {

        if (
            !hasSelectedProblem ||
            isFirstProblem
        ) {
            return;
        }

        await handleProblemSelect(
            selectedProblemIndex - 1
        );
    };

    const handleNextProblem = async () => {

        if (
            !hasSelectedProblem ||
            isLastProblem
        ) {
            return;
        }

        await handleProblemSelect(
            selectedProblemIndex + 1
        );
    };

    const handleCodeChange = (
        value
    ) => {

        if (
            !hasSelectedProblem ||
            !language
        ) {
            return;
        }

        const nextCode =
            value || "";

        const key =
            getCodeKey(
                selectedProblem.id,
                language
            );

        setCodeMap(
            (previous) => ({
                ...previous,
                [key]:
                    nextCode
            })
        );

        if (saveTimerRef.current) {

            clearTimeout(
                saveTimerRef.current
            );
        }

        saveTimerRef.current =
            setTimeout(
                async () => {

                    try {

                        const response =
                            await saveCodingState(
                                selectedProblem.id,
                                language,
                                nextCode
                            );

                        setProgress(
                            response.data
                        );

                    } catch (requestError) {

                        console.error(
                            "Failed to save code state",
                            requestError
                        );
                    }

                },
                800
            );
    };

    const handleLanguageChange = (
        selectedLanguage
    ) => {

        if (!selectedLanguage) {
            return;
        }

        if (selectedProblem) {

            const key =
                getCodeKey(
                    selectedProblem.id,
                    selectedLanguage
                );

            if (
                !Object.prototype.hasOwnProperty.call(
                    codeMap,
                    key
                )
            ) {

                setCodeMap(
                    (previous) => ({
                        ...previous,
                        [key]:
                            getStarterCode(
                                selectedProblem,
                                selectedLanguage
                            )
                    })
                );
            }
        }

        setLanguage(
            selectedLanguage
        );

        const selected =
            availableLanguages.find(
                (item) =>
                    item.value ===
                    selectedLanguage
            );

        setSearchLanguage(
            selected?.label ||
            selectedLanguage
        );

        setShowLanguages(
            false
        );

        setExecutionResult(
            null
        );

        setAiHint("");
        setAiHintError("");
    };

    const handleAiHint = async () => {

        if (
            !selectedProblem ||
            !language ||
            isGeneratingHint
        ) {
            return;
        }

        const code =
            getCurrentCode();

        setIsGeneratingHint(
            true
        );

        setAiHintError(
            ""
        );

        setShowAiHint(
            true
        );

        try {

            const response =
                await getCodingHint(
                    selectedProblem.title,
                    selectedProblem.description,
                    language,
                    code
                );

            const hint =
                typeof response.data ===
                "string"
                    ? response.data
                    : response.data?.hint ||
                      response.data?.message ||
                      "";

            if (!hint.trim()) {

                throw new Error(
                    "AI returned an empty hint."
                );
            }

            setAiHint(
                hint.trim()
            );

        } catch (requestError) {

            console.error(
                "AI hint generation failed",
                requestError
            );

            setAiHint(
                ""
            );

            setAiHintError(
                requestError?.response?.data?.message ||
                requestError?.response?.data?.error ||
                requestError?.message ||
                "Unable to generate AI hint."
            );

        } finally {

            setIsGeneratingHint(
                false
            );
        }
    };

    const buildExecutionError = (
        requestError,
        fallbackMessage
    ) => {

        return {
            status: "ERROR",
            passed: false,
            totalTests: 0,
            passedTests: 0,
            failedTests: 0,
            runtime: 0,
            memory: 0,
            output: "",
            expectedOutput: "",
            error:
                requestError?.response?.data?.message ||
                requestError?.response?.data?.error ||
                fallbackMessage,
            message:
                fallbackMessage,
            testCases: []
        };
    };

    const handleRun = async () => {

        if (
            !hasSelectedProblem ||
            !language ||
            isExecuting ||
            isSubmitting
        ) {
            return;
        }

        const code =
            getCurrentCode();

        if (!code.trim()) {

            setExecutionResult({
                status: "ERROR",
                passed: false,
                totalTests: 0,
                passedTests: 0,
                failedTests: 0,
                runtime: 0,
                memory: 0,
                output: "",
                expectedOutput: "",
                error:
                    "Code cannot be empty.",
                message:
                    "Please write some code before running.",
                testCases: []
            });

            return;
        }

        try {

            setIsExecuting(
                true
            );

            setExecutionResult(
                null
            );

            const response =
                await executeCodingCode(
                    selectedProblem.id,
                    language,
                    code
                );

            setExecutionResult(
                response.data
            );

        } catch (requestError) {

            console.error(
                "Code execution failed",
                requestError
            );

            setExecutionResult(
                buildExecutionError(
                    requestError,
                    "Code execution failed."
                )
            );

        } finally {

            setIsExecuting(
                false
            );
        }
    };

    const handleSubmit = async () => {

        if (
            !hasSelectedProblem ||
            !language ||
            isExecuting ||
            isSubmitting
        ) {
            return;
        }

        const code =
            getCurrentCode();

        if (!code.trim()) {

            setExecutionResult({
                status: "ERROR",
                passed: false,
                totalTests: 0,
                passedTests: 0,
                failedTests: 0,
                runtime: 0,
                memory: 0,
                output: "",
                expectedOutput: "",
                error:
                    "Code cannot be empty.",
                message:
                    "Please write some code before submitting.",
                testCases: []
            });

            return;
        }

        try {

            setIsSubmitting(
                true
            );

            setExecutionResult(
                null
            );

            const response =
                await submitCodingCode(
                    selectedProblem.id,
                    language,
                    code
                );

            const result =
                response.data;

            setExecutionResult(
                result
            );

            if (
                result?.passed === true
            ) {

                try {

                    const progressResponse =
                        await getCodingProgress();

                    setProgress(
                        progressResponse.data
                    );

                } catch (progressError) {

                    console.error(
                        "Failed to refresh coding progress",
                        progressError
                    );
                }
            }

        } catch (requestError) {

            console.error(
                "Failed to submit coding problem",
                requestError
            );

            setExecutionResult(
                buildExecutionError(
                    requestError,
                    "Unable to submit code."
                )
            );

        } finally {

            setIsSubmitting(
                false
            );
        }
    };

    const handleOpenProblemMenu = () => {

        setShowProblemMenu(
            (previous) => !previous
        );
    };

    const renderExecutionResult = () => {

        if (!executionResult) {
            return null;
        }

        const testCases =
            Array.isArray(
                executionResult.testCases
            )
                ? executionResult.testCases
                : [];

        const passed =
            executionResult.passed === true;

        const status =
            executionResult.status ||
            "RESULT";

        const error =
            executionResult.error;

        const totalTests =
            executionResult.totalTests || 0;

        const passedTests =
            executionResult.passedTests || 0;

        const progressWidth =
            totalTests > 0
                ? `${Math.min(
                    100,
                    (
                        passedTests /
                        totalTests
                    ) *
                    100
                )}%`
                : "0%";

        return (

            <div
                className={
                    passed
                        ? "run-result-card accepted-result"
                        : "run-result-card failed-result"
                }
            >

                <div className="run-top">

                    <div>

                        <strong>
                            {status}
                        </strong>

                        <span>
                            {
                                executionResult.message ||
                                ""
                            }
                        </span>

                    </div>

                    <div>

                        {
                            executionResult.runtime !==
                            undefined
                                ? `${executionResult.runtime} ms`
                                : ""
                        }

                    </div>

                </div>

                {
                    totalTests > 0 && (

                        <div className="run-progress">

                            <div
                                className="run-fill"
                                style={{
                                    width:
                                        progressWidth
                                }}
                            />

                        </div>
                    )
                }

                <div className="execution-summary">

                    <span>
                        Passed: {passedTests}
                    </span>

                    <span>
                        Failed: {
                            executionResult.failedTests ||
                            0
                        }
                    </span>

                    <span>
                        Total: {totalTests}
                    </span>

                </div>

                {
                    error && (

                        <div className="execution-error">

                            {error}

                        </div>
                    )
                }

                {
                    testCases.length > 0 && (

                        <div className="execution-testcases">

                            {
                                testCases.map(
                                    (
                                        testCase
                                    ) => (

                                        <div
                                            className={
                                                testCase.passed
                                                    ? "execution-testcase passed"
                                                    : "execution-testcase failed"
                                            }
                                            key={
                                                testCase.testCaseNumber
                                            }
                                        >

                                            <div className="execution-testcase-header">

                                                <strong>
                                                    Test Case {
                                                        testCase.testCaseNumber
                                                    }
                                                </strong>

                                                <span>
                                                    {
                                                        testCase.passed
                                                            ? "Passed"
                                                            : "Failed"
                                                    }
                                                </span>

                                            </div>

                                            <div className="execution-testcase-content">

                                                {
                                                    testCase.input !==
                                                    null &&
                                                    testCase.input !==
                                                    undefined && (

                                                        <p>

                                                            <strong>
                                                                Input:
                                                            </strong>

                                                            {" "}

                                                            {
                                                                testCase.input
                                                            }

                                                        </p>
                                                    )
                                                }

                                                {
                                                    testCase.expectedOutput !==
                                                    null &&
                                                    testCase.expectedOutput !==
                                                    undefined && (

                                                        <p>

                                                            <strong>
                                                                Expected:
                                                            </strong>

                                                            {" "}

                                                            {
                                                                testCase.expectedOutput
                                                            }

                                                        </p>
                                                    )
                                                }

                                                {
                                                    testCase.actualOutput !==
                                                    null &&
                                                    testCase.actualOutput !==
                                                    undefined && (

                                                        <p>

                                                            <strong>
                                                                Output:
                                                            </strong>

                                                            {" "}

                                                            {
                                                                testCase.actualOutput
                                                            }

                                                        </p>
                                                    )
                                                }

                                                {
                                                    testCase.error && (

                                                        <p>

                                                            <strong>
                                                                Error:
                                                            </strong>

                                                            {" "}

                                                            {
                                                                testCase.error
                                                            }

                                                        </p>
                                                    )
                                                }

                                            </div>

                                        </div>
                                    )
                                )
                            }

                        </div>
                    )
                }

            </div>
        );
    };

    return (

        <section className="coding-page">

            <div className="coding-topbar">

                <div
                    className="problem-head"
                    ref={problemMenuRef}
                >

                    <span
                        className="coding-problem-trigger"
                        onClick={
                            handleOpenProblemMenu
                        }
                    >

                        PROBLEM

                        {
                            selectedProblem
                                ? ` · ${selectedProblem.difficulty}`
                                : ""
                        }

                    </span>

                    <h1
                        className="coding-problem-trigger"
                        onClick={
                            handleOpenProblemMenu
                        }
                    >

                        {
                            selectedProblem
                                ? `${selectedProblem.id}. ${selectedProblem.title}`
                                : "Your Next Challenge Awaits"
                        }

                    </h1>

                    {
                        showProblemMenu && (

                            <div className="coding-problem-menu">

                                <div className="coding-problem-menu-header">

                                    <div>

                                        <span>
                                            CODING ARENA
                                        </span>

                                        <h3>
                                            Choose a Problem
                                        </h3>

                                    </div>

                                    <button
                                        type="button"
                                        onClick={() =>
                                            setShowProblemMenu(
                                                false
                                            )
                                        }
                                    >

                                        <FiX />

                                    </button>

                                </div>

                                <div className="coding-problem-list">

                                    {
                                        problems.length > 0
                                            ? problems.map(
                                                (
                                                    problem,
                                                    index
                                                ) => (

                                                    <button
                                                        type="button"
                                                        key={
                                                            problem.id
                                                        }
                                                        className={
                                                            index ===
                                                            selectedProblemIndex
                                                                ? "coding-problem-option active"
                                                                : "coding-problem-option"
                                                        }
                                                        onClick={() =>
                                                            handleProblemSelect(
                                                                index
                                                            )
                                                        }
                                                    >

                                                        <span className="coding-problem-option-number">

                                                            {
                                                                index + 1
                                                            }

                                                        </span>

                                                        <span className="coding-problem-option-content">

                                                            <strong>
                                                                {
                                                                    problem.title
                                                                }
                                                            </strong>

                                                            <small>
                                                                {
                                                                    Array.isArray(
                                                                        problem.tags
                                                                    )
                                                                        ? problem.tags.join(
                                                                            " · "
                                                                        )
                                                                        : ""
                                                                }
                                                            </small>

                                                        </span>

                                                        <span
                                                            className={
                                                                `coding-problem-option-difficulty ${
                                                                    String(
                                                                        problem.difficulty ||
                                                                        ""
                                                                    ).toLowerCase()
                                                                }`
                                                            }
                                                        >

                                                            {
                                                                problem.difficulty
                                                            }

                                                        </span>

                                                    </button>
                                                )
                                            )
                                            : (

                                                <div className="coding-problem-empty">

                                                    {
                                                        loading
                                                            ? "Loading problems..."
                                                            : error ||
                                                              "No problems available"
                                                    }

                                                </div>
                                            )
                                    }

                                </div>

                            </div>
                        )
                    }

                </div>

                <div className="coding-actions">

                    <button
                        className="coding-mobile-problem-btn"
                        type="button"
                        onClick={
                            handlePreviousProblem
                        }
                        disabled={
                            !hasSelectedProblem ||
                            isFirstProblem
                        }
                    >

                        <FiArrowLeft />

                        Previous

                    </button>

                    <button
                        className="coding-mobile-problem-btn"
                        type="button"
                        onClick={
                            handleNextProblem
                        }
                        disabled={
                            !hasSelectedProblem ||
                            isLastProblem
                        }
                    >

                        Next

                        <FiArrowRight />

                    </button>

                    <button
                        className="run-btn"
                        type="button"
                        disabled={
                            !hasSelectedProblem ||
                            !language ||
                            isExecuting ||
                            isSubmitting
                        }
                        onClick={
                            handleRun
                        }
                    >

                        <FiPlay />

                        {
                            isExecuting
                                ? "Running..."
                                : "Run"
                        }

                    </button>

                    <button
                        className="submit-btn"
                        type="button"
                        disabled={
                            !hasSelectedProblem ||
                            !language ||
                            isExecuting ||
                            isSubmitting
                        }
                        onClick={
                            handleSubmit
                        }
                    >

                        {
                            isSubmitting
                                ? "Submitting..."
                                : "Submit"
                        }

                    </button>

                </div>

            </div>

            <div className="coding-grid">

                <div className="problem-card">

                    {
                        loading ? (

                            <div className="coding-empty-problem">

                                Loading your coding arena...

                            </div>

                        ) : selectedProblem ? (

                            <>

                                <div className="problem-tags">

                                    <span className="tag-green">

                                        {
                                            selectedProblem.difficulty
                                        }

                                    </span>

                                    {
                                        Array.isArray(
                                            selectedProblem.tags
                                        ) &&
                                        selectedProblem.tags.map(
                                            (
                                                tag,
                                                index
                                            ) => (

                                                <span
                                                    key={
                                                        `${tag}-${index}`
                                                    }
                                                    className="tag-blue"
                                                >

                                                    {
                                                        tag
                                                    }

                                                </span>
                                            )
                                        )
                                    }

                                    <div className="problem-time">

                                        ⏱ 15:24

                                    </div>

                                </div>

                                <div className="problem-description">

                                    <p>

                                        {
                                            selectedProblem.description
                                        }

                                    </p>

                                </div>

                                {
                                    (
                                        selectedProblem.inputExample ||
                                        selectedProblem.outputExample
                                    ) && (

                                        <div className="example-box">

                                            {
                                                selectedProblem.inputExample && (

                                                    <p>

                                                        <strong>
                                                            Input:
                                                        </strong>

                                                        {
                                                            ` ${selectedProblem.inputExample}`
                                                        }

                                                    </p>
                                                )
                                            }

                                            {
                                                selectedProblem.outputExample && (

                                                    <p>

                                                        <strong>
                                                            Output:
                                                        </strong>

                                                        {
                                                            ` ${selectedProblem.outputExample}`
                                                        }

                                                    </p>
                                                )
                                            }

                                        </div>
                                    )
                                }

                                {
                                    Array.isArray(
                                        selectedProblem.constraints
                                    ) &&
                                    selectedProblem.constraints.length >
                                    0 && (

                                        <div className="constraints-box">

                                            <h3>
                                                Constraints
                                            </h3>

                                            <ul>

                                                {
                                                    selectedProblem.constraints.map(
                                                        (
                                                            constraint,
                                                            index
                                                        ) => (

                                                            <li
                                                                key={
                                                                    index
                                                                }
                                                            >

                                                                {
                                                                    constraint
                                                                }

                                                            </li>
                                                        )
                                                    )
                                                }

                                            </ul>

                                        </div>
                                    )
                                }

                            </>

                        ) : (

                            <div className="coding-empty-problem">

                                {
                                    error ||
                                    "Your Next Challenge Awaits"
                                }

                            </div>
                        )
                    }

                </div>

                <div className="editor-column">

                    <div className="editor-card">

                        <div className="editor-header">

                            <div className="editor-dots">

                                <span></span>
                                <span></span>
                                <span></span>

                            </div>

                            <div
                                className="language-search-wrapper"
                                ref={dropdownRef}
                            >

                                <FiSearch className="search-icon" />

                                <input
                                    type="text"
                                    value={
                                        searchLanguage
                                    }
                                    placeholder={
                                        availableLanguages.length > 0
                                            ? "Search language..."
                                            : "No language available"
                                    }
                                    className="language-search"
                                    disabled={
                                        !hasSelectedProblem ||
                                        availableLanguages.length === 0
                                    }
                                    onFocus={() => {

                                        if (
                                            availableLanguages.length ===
                                            0
                                        ) {
                                            return;
                                        }

                                        setShowLanguages(
                                            true
                                        );

                                        setSelectedIndex(
                                            0
                                        );
                                    }}
                                    onClick={() => {

                                        if (
                                            availableLanguages.length ===
                                            0
                                        ) {
                                            return;
                                        }

                                        setShowLanguages(
                                            true
                                        );
                                    }}
                                    onChange={(event) => {

                                        setSearchLanguage(
                                            event.target.value
                                        );

                                        setShowLanguages(
                                            true
                                        );

                                        setSelectedIndex(
                                            0
                                        );
                                    }}
                                    onKeyDown={(event) => {

                                        if (
                                            event.key ===
                                            "ArrowDown"
                                        ) {

                                            event.preventDefault();

                                            setSelectedIndex(
                                                (previous) => {

                                                    const nextIndex =
                                                        previous <
                                                        filteredLanguages.length -
                                                        1
                                                            ? previous + 1
                                                            : previous;

                                                    optionRefs
                                                        .current[
                                                            nextIndex
                                                        ]
                                                        ?.scrollIntoView(
                                                            {
                                                                block:
                                                                    "nearest",
                                                                behavior:
                                                                    "smooth"
                                                            }
                                                        );

                                                    return nextIndex;
                                                }
                                            );
                                        }

                                        if (
                                            event.key ===
                                            "ArrowUp"
                                        ) {

                                            event.preventDefault();

                                            setSelectedIndex(
                                                (previous) => {

                                                    const nextIndex =
                                                        previous >
                                                        0
                                                            ? previous - 1
                                                            : 0;

                                                    optionRefs
                                                        .current[
                                                            nextIndex
                                                        ]
                                                        ?.scrollIntoView(
                                                            {
                                                                block:
                                                                    "nearest",
                                                                behavior:
                                                                    "smooth"
                                                            }
                                                        );

                                                    return nextIndex;
                                                }
                                            );
                                        }

                                        if (
                                            event.key ===
                                            "Enter"
                                        ) {

                                            event.preventDefault();

                                            const selected =
                                                filteredLanguages[
                                                    selectedIndex
                                                ];

                                            if (selected) {

                                                handleLanguageChange(
                                                    selected.value
                                                );
                                            }
                                        }

                                        if (
                                            event.key ===
                                            "Escape"
                                        ) {

                                            setShowLanguages(
                                                false
                                            );
                                        }
                                    }}
                                />

                                <FiChevronDown
                                    className="dropdown-arrow"
                                    onClick={() => {

                                        if (
                                            availableLanguages.length ===
                                            0
                                        ) {
                                            return;
                                        }

                                        setShowLanguages(
                                            (previous) =>
                                                !previous
                                        );
                                    }}
                                />

                                {
                                    showLanguages && (

                                        <div className="language-dropdown">

                                            {
                                                filteredLanguages.length >
                                                0
                                                    ? filteredLanguages.map(
                                                        (
                                                            item,
                                                            index
                                                        ) => (

                                                            <div
                                                                key={
                                                                    item.value
                                                                }
                                                                ref={(
                                                                    element
                                                                ) =>
                                                                    optionRefs.current[
                                                                        index
                                                                    ] =
                                                                        element
                                                                }
                                                                className={
                                                                    index ===
                                                                    selectedIndex
                                                                        ? "language-item active-language"
                                                                        : "language-item"
                                                                }
                                                                onMouseEnter={() =>
                                                                    setSelectedIndex(
                                                                        index
                                                                    )
                                                                }
                                                                onClick={() =>
                                                                    handleLanguageChange(
                                                                        item.value
                                                                    )
                                                                }
                                                            >

                                                                <div>

                                                                    <h4>
                                                                        {
                                                                            item.label
                                                                        }
                                                                    </h4>

                                                                    <p>
                                                                        Problem-specific snippet
                                                                    </p>

                                                                </div>

                                                                <span>
                                                                    {
                                                                        item.value
                                                                    }
                                                                </span>

                                                            </div>
                                                        )
                                                    )
                                                    : (

                                                        <div className="language-item">

                                                            <div>

                                                                <h4>
                                                                    No language found
                                                                </h4>

                                                            </div>

                                                        </div>
                                                    )
                                            }

                                        </div>
                                    )
                                }

                            </div>

                            <button
                                type="button"
                                className="ai-hint"
                                disabled={
                                    !hasSelectedProblem ||
                                    !language ||
                                    isGeneratingHint
                                }
                                onClick={
                                    handleAiHint
                                }
                            >

                                <FiCpu />

                                {
                                    isGeneratingHint
                                        ? "Thinking..."
                                        : "AI Hint"
                                }

                            </button>

                        </div>

                        <div className="monaco-wrapper">

                            <Editor
                                height="100%"
                                theme="vs-dark"
                                language={
                                    getMonacoLanguage()
                                }
                                value={
                                    hasSelectedProblem &&
                                    language
                                        ? getCurrentCode()
                                        : ""
                                }
                                onChange={
                                    handleCodeChange
                                }
                                options={{
                                    fontSize: 13,
                                    minimap: {
                                        enabled: false
                                    },
                                    scrollBeyondLastLine:
                                        false,
                                    automaticLayout:
                                        true,
                                    readOnly:
                                        !hasSelectedProblem ||
                                        !language
                                }}
                            />

                        </div>

                    </div>

                    {
                        showAiHint && (

                            <div className="ai-hint-panel">

                                <div className="ai-hint-panel-header">

                                    <div>

                                        <FiCpu />

                                        <strong>
                                            AI Hint
                                        </strong>

                                    </div>

                                    <button
                                        type="button"
                                        onClick={() =>
                                            setShowAiHint(
                                                false
                                            )
                                        }
                                    >

                                        <FiX />

                                    </button>

                                </div>

                                <div className="ai-hint-panel-content">

                                    {
                                        isGeneratingHint ? (

                                            <div className="ai-hint-loading">

                                                <span></span>

                                                <span></span>

                                                <span></span>

                                                <p>
                                                    Analyzing your approach...
                                                </p>

                                            </div>

                                        ) : aiHintError ? (

                                            <div className="ai-hint-error">

                                                {aiHintError}

                                            </div>

                                        ) : aiHint ? (

                                            <p>
                                                {aiHint}
                                            </p>

                                        ) : (

                                            <p>
                                                Click AI Hint to get guidance for this problem.
                                            </p>

                                        )
                                    }

                                </div>

                            </div>
                        )
                    }

                    <div className="testcase-card">

                        <div className="testcase-top">

                            <h3>
                                Test cases
                            </h3>

                            <span>

                                {
                                    executionResult
                                        ? `${executionResult.passedTests || 0}/${executionResult.totalTests || 0} passed`
                                        : hasSelectedProblem
                                            ? `${Array.isArray(
                                                selectedProblem.testCases
                                            )
                                                ? selectedProblem.testCases.length
                                                : 0} public cases`
                                            : "Select a problem"
                                }

                            </span>

                        </div>

                        {
                            executionResult &&
                            Array.isArray(
                                executionResult.testCases
                            ) &&
                            executionResult.testCases.length > 0 ? (

                                executionResult.testCases.map(
                                    (
                                        testCase
                                    ) => (

                                        <div
                                            className="case-item"
                                            key={
                                                testCase.testCaseNumber
                                            }
                                        >

                                            <p>

                                                <strong>
                                                    Test Case {
                                                        testCase.testCaseNumber
                                                    }
                                                </strong>

                                                {" · "}

                                                {
                                                    testCase.passed
                                                        ? "Passed"
                                                        : "Failed"
                                                }

                                            </p>

                                            <span>
                                                {
                                                    testCase.passed
                                                        ? "✓"
                                                        : "✕"
                                                }
                                            </span>

                                        </div>
                                    )
                                )

                            ) : selectedProblem &&
                              Array.isArray(
                                  selectedProblem.testCases
                              ) &&
                              selectedProblem.testCases.length > 0 ? (

                                selectedProblem.testCases.map(
                                    (
                                        testCase
                                    ) => (

                                        <div
                                            className="case-item"
                                            key={
                                                testCase.testCaseNumber
                                            }
                                        >

                                            <p>

                                                Test Case {
                                                    testCase.testCaseNumber
                                                }

                                            </p>

                                            <span>
                                                ○
                                            </span>

                                        </div>
                                    )
                                )

                            ) : selectedProblem ? (

                                <>

                                    <div className="case-item">

                                        <p>

                                            Input: {
                                                selectedProblem.inputExample ||
                                                "Sample input"
                                            }

                                        </p>

                                    </div>

                                    <div className="case-item">

                                        <p>

                                            Expected: {
                                                selectedProblem.outputExample ||
                                                "Sample output"
                                            }

                                        </p>

                                    </div>

                                </>

                            ) : (

                                <div className="coding-empty-problem">

                                    Select a problem to view test cases

                                </div>
                            )
                        }

                    </div>

                    {
                        renderExecutionResult()
                    }

                </div>

            </div>

        </section>
    );
}