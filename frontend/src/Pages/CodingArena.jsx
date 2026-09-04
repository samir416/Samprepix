import "../styles/codingarena.css";

import { useState, useRef, useEffect, useMemo } from "react";

import Editor from "@monaco-editor/react";

import {
    FiPlay,
    FiCpu,
    FiChevronDown,
    FiChevronUp,
    FiX,
    FiSearch,
    FiArrowLeft,
    FiArrowRight,
    FiList,
    FiCheck,
    FiCheckCircle,
    FiTag,
    FiRefreshCw,
    FiCode,
    FiFilter,
    FiGithub,
    FiExternalLink,
    FiAlertCircle
} from "react-icons/fi";

import {
    getCodingProblems,
    getCodingProblem,
    getCodingProgress,
    selectCodingProblem,
    saveCodingState,
    executeCodingCode,
    submitCodingCode,
    getCodingHint,
    getCodingRuntimes,
    getCodingProblemTags,
    getCodingLanguages,
    retryGitHubSync
} from "../services/codingService";

import { getGitHubRepository } from "../services/profileService";

export default function CodingArena() {
    const [language, setLanguage] = useState("");
    const [searchLanguage, setSearchLanguage] = useState("");
    const [showLanguages, setShowLanguages] = useState(false);
    const [registeredLanguages, setRegisteredLanguages] = useState({ all: [], popular: [] });
    const [pendingLanguage, setPendingLanguage] = useState(null);
    const [showLanguageModal, setShowLanguageModal] = useState(false);
    const [activeLangIndex, setActiveLangIndex] = useState(0);

    const [runtimes, setRuntimes] = useState([]);
    const [runtimesLoading, setRuntimesLoading] = useState(false);
    const [runtimeError, setRuntimeError] = useState("");

    const [problems, setProblems] = useState([]);
    const [selectedProblemDetails, setSelectedProblemDetails] = useState(null);
    const [problemPage, setProblemPage] = useState(0);
    const [problemTotal, setProblemTotal] = useState(0);
    const [problemListLoading, setProblemListLoading] = useState(false);
    const [progress, setProgress] = useState(null);
    const [selectedProblemIndex, setSelectedProblemIndex] = useState(null);

    const [codeMap, setCodeMap] = useState({});

    const [executionResult, setExecutionResult] = useState(null);
    const [lastSuccessfulRun, setLastSuccessfulRun] = useState(null);
    const [isExecuting, setIsExecuting] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [gitHubInfo, setGitHubInfo] = useState(null);
    const [isRetryingSync, setIsRetryingSync] = useState(false);

    const [isGeneratingHint, setIsGeneratingHint] = useState(false);
    const [aiHint, setAiHint] = useState("");
    const [aiHintError, setAiHintError] = useState("");
    const [showAiHint, setShowAiHint] = useState(false);
    const [hintLevel, setHintLevel] = useState(1);
    const [hintLevelName, setHintLevelName] = useState("Concept");
    const [hintCooldown, setHintCooldown] = useState(0);

    const [showProblemMenu, setShowProblemMenu] = useState(false);
    const [problemSearch, setProblemSearch] = useState("");
    const [problemFilter, setProblemFilter] = useState("all");
    const [topicFilter, setTopicFilter] = useState("all");
    const [categoryFilter, setCategoryFilter] = useState("all");
    const [availableTopics, setAvailableTopics] = useState([]);
    const [showTopicMenu, setShowTopicMenu] = useState(false);
    const [topicSearchQuery, setTopicSearchQuery] = useState("");

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const dropdownRef = useRef(null);
    const langSearchInputRef = useRef(null);
    const topicDropdownRef = useRef(null);
    const optionRefs = useRef([]);
    const problemMenuRef = useRef(null);
    const saveTimerRef = useRef(null);
    const problemSearchTimerRef = useRef(null);
    const problemListRef = useRef(null);
    const hintCooldownTimerRef = useRef(null);

    const selectedProblem =
        selectedProblemDetails ||
        (selectedProblemIndex !== null
            ? problems[selectedProblemIndex] || null
            : null);

    const hasSelectedProblem = selectedProblem !== null;

    const normalizeLanguageValue = (value) => {
        if (!value) {
            return "";
        }

        return String(value).trim().toLowerCase();
    };

    const getLanguageDefaults = (value) => {
        const normalized = normalizeLanguageValue(value);

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
                label: "Bash",
                monacoLanguage: "shell",
                runtimeLanguage: "bash"
            },
            lua: {
                label: "Lua",
                monacoLanguage: "lua",
                runtimeLanguage: "lua"
            },
            elixir: {
                label: "Elixir",
                monacoLanguage: "elixir",
                runtimeLanguage: "elixir"
            },
            erlang: {
                label: "Erlang",
                monacoLanguage: "erlang",
                runtimeLanguage: "erlang"
            },
            perl: {
                label: "Perl",
                monacoLanguage: "perl",
                runtimeLanguage: "perl"
            },
            haskell: {
                label: "Haskell",
                monacoLanguage: "haskell",
                runtimeLanguage: "haskell"
            },
            dart: {
                label: "Dart",
                monacoLanguage: "dart",
                runtimeLanguage: "dart"
            },
            racket: {
                label: "Racket",
                monacoLanguage: "scheme",
                runtimeLanguage: "racket"
            },
            r: {
                label: "R",
                monacoLanguage: "r",
                runtimeLanguage: "rscript"
            },
            groovy: {
                label: "Groovy",
                monacoLanguage: "java",
                runtimeLanguage: "groovy"
            },
            julia: {
                label: "Julia",
                monacoLanguage: "julia",
                runtimeLanguage: "julia"
            },
            d: {
                label: "D",
                monacoLanguage: "c",
                runtimeLanguage: "d"
            },
            cobol: {
                label: "COBOL",
                monacoLanguage: "cobol",
                runtimeLanguage: "cobol"
            },
            ocaml: {
                label: "OCaml",
                monacoLanguage: "ocaml",
                runtimeLanguage: "ocaml"
            },
            nim: {
                label: "Nim",
                monacoLanguage: "python",
                runtimeLanguage: "nim"
            },
            pascal: {
                label: "Pascal",
                monacoLanguage: "pascal",
                runtimeLanguage: "pascal"
            },
            raku: {
                label: "Raku",
                monacoLanguage: "perl",
                runtimeLanguage: "raku"
            },
            vlang: {
                label: "V",
                monacoLanguage: "go",
                runtimeLanguage: "vlang"
            }
        };

        return (
            defaults[normalized] || {
                label:
                    String(value).charAt(0).toUpperCase() +
                    String(value).slice(1),
                monacoLanguage: normalized || "plaintext",
                runtimeLanguage: normalized
            }
        );
    };

    const parseLanguageConfigurations = (problem) => {
        if (!problem) {
            return {};
        }

        const raw = problem.languageConfigurations;

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
            const parsed = JSON.parse(raw);

            if (
                parsed &&
                typeof parsed === "object" &&
                !Array.isArray(parsed)
            ) {
                return parsed;
            }
        } catch {
            return {};
        }

        return {};
    };

    const getConfiguredLanguages = (problem) => {
        if (!problem) {
            return [];
        }

        const configurations =
            parseLanguageConfigurations(problem);

        const configurationLanguages =
            Object.entries(configurations)
                .filter(
                    ([, configuration]) =>
                        configuration &&
                        typeof configuration === "object" &&
                        !Array.isArray(configuration)
                )
                .map(([value, configuration]) => {
                    const defaults =
                        getLanguageDefaults(value);

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
                            configuration.runtimeVersion || "",
                        fileName:
                            configuration.fileName || "",
                        starterCode:
                            configuration.starterCode || "",
                        executionTemplate:
                            configuration.executionTemplate || ""
                    };
                });

        if (configurationLanguages.length > 0) {
            return configurationLanguages;
        }

        const starterCodes = problem.starterCodes;

        if (
            starterCodes &&
            typeof starterCodes === "object" &&
            !Array.isArray(starterCodes)
        ) {
            return Object.keys(starterCodes).map((value) => {
                const defaults =
                    getLanguageDefaults(value);

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
            });
        }

        if (
            typeof problem.starterCode === "string" &&
            problem.starterCode.trim()
        ) {
            try {
                const parsed =
                    JSON.parse(problem.starterCode);

                if (
                    parsed &&
                    typeof parsed === "object" &&
                    !Array.isArray(parsed)
                ) {
                    return Object.keys(parsed).map(
                        (value) => {
                            const defaults =
                                getLanguageDefaults(value);

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

    const isDatabaseProblem = useMemo(() => {
        return Boolean(
            selectedProblem?.category === "DATABASE" ||
            selectedProblem?.category === "SQL" ||
            selectedProblem?.sourceId?.startsWith("sql-")
        );
    }, [selectedProblem]);

    const getProblemLanguages = (problem) => {
        if (!problem) return [];
        const isDb = Boolean(
            problem.category === "DATABASE" ||
            problem.category === "SQL" ||
            problem.sourceId?.startsWith("sql-")
        );

        if (isDb) {
            const dbList = registeredLanguages.database ||
                registeredLanguages.all?.filter((spec) => spec.executionMode === "database" || spec.key === "mysql") || [];
            if (dbList.length > 0) {
                return dbList.map((spec) => ({
                    value: spec.key,
                    label: spec.displayName,
                    runtimeLanguage: spec.runtimeLanguage,
                    runtimeVersion: spec.runtimeVersion,
                    fileName: spec.fileName,
                    monacoLanguage: spec.monacoLanguage || "sql",
                    starterCode: spec.starterCode,
                    popular: true,
                    category: "DATABASE"
                }));
            }
            return [{
                value: "mysql",
                label: "MySQL",
                runtimeLanguage: "mysql",
                runtimeVersion: "8.0",
                fileName: "solution.sql",
                monacoLanguage: "sql",
                starterCode: "-- Write your MySQL query statement below\nSELECT\n    *\nFROM\n    ;\n",
                popular: true,
                category: "DATABASE"
            }];
        }

        const isLegacyProblem = Boolean(
            problem.functionName &&
            !problem.sourceId?.startsWith("dsa-") &&
            !problem.slug?.includes("-variant-")
        );
        const progList = registeredLanguages.programming ||
            registeredLanguages.all?.filter((spec) => spec.executionMode !== "database" && spec.key !== "mysql") || [];
        if (!isLegacyProblem && progList.length > 0) {
            return progList.map((spec) => ({
                value: spec.key,
                label: spec.displayName,
                runtimeLanguage: spec.runtimeLanguage,
                runtimeVersion: spec.runtimeVersion,
                fileName: spec.fileName,
                monacoLanguage: spec.monacoLanguage,
                starterCode: spec.starterCode,
                popular: Boolean(spec.popular),
                category: "PROGRAMMING"
            }));
        }
        return getConfiguredLanguages(problem).map((c) => ({
            ...c,
            category: "PROGRAMMING",
            popular: ["java", "python", "cpp", "javascript", "typescript"].includes(normalizeLanguageValue(c.value))
        }));
    };

    const availableLanguages = useMemo(() => {
        return getProblemLanguages(selectedProblem);
    }, [selectedProblem, registeredLanguages]);

    const popularLanguages = useMemo(() => {
        return availableLanguages.filter((l) => l.popular && l.category !== "DATABASE");
    }, [availableLanguages]);

    const moreLanguages = useMemo(() => {
        return availableLanguages.filter((l) => !l.popular && l.category !== "DATABASE");
    }, [availableLanguages]);

    const databaseLanguages = useMemo(() => {
        return availableLanguages.filter((l) => l.category === "DATABASE");
    }, [availableLanguages]);

    const filteredPopularLanguages = useMemo(() => {
        const query = searchLanguage.trim().toLowerCase();
        if (!query) return popularLanguages;
        return popularLanguages.filter(
            (item) =>
                item.label.toLowerCase().includes(query) ||
                item.value.toLowerCase().includes(query) ||
                (item.runtimeLanguage && item.runtimeLanguage.toLowerCase().includes(query))
        );
    }, [popularLanguages, searchLanguage]);

    const filteredMoreLanguages = useMemo(() => {
        const query = searchLanguage.trim().toLowerCase();
        if (!query) return moreLanguages;
        return moreLanguages.filter(
            (item) =>
                item.label.toLowerCase().includes(query) ||
                item.value.toLowerCase().includes(query) ||
                (item.runtimeLanguage && item.runtimeLanguage.toLowerCase().includes(query))
        );
    }, [moreLanguages, searchLanguage]);

    const filteredDatabaseLanguages = useMemo(() => {
        const query = searchLanguage.trim().toLowerCase();
        if (!query) return databaseLanguages;
        return databaseLanguages.filter(
            (item) =>
                item.label.toLowerCase().includes(query) ||
                item.value.toLowerCase().includes(query) ||
                (item.runtimeLanguage && item.runtimeLanguage.toLowerCase().includes(query))
        );
    }, [databaseLanguages, searchLanguage]);

    const filteredLanguages = useMemo(() => {
        return [...filteredDatabaseLanguages, ...filteredPopularLanguages, ...filteredMoreLanguages];
    }, [filteredDatabaseLanguages, filteredPopularLanguages, filteredMoreLanguages]);

    const filteredAvailableTopics = useMemo(() => {
        if (!topicSearchQuery.trim()) return availableTopics;
        const q = topicSearchQuery.trim().toLowerCase();
        return availableTopics.filter((t) => t.toLowerCase().includes(q));
    }, [availableTopics, topicSearchQuery]);

    const isProblemSolved = (probId) => {
        if (!progress) return false;
        if (Array.isArray(progress.completedProblemIds)) {
            return progress.completedProblemIds.includes(probId);
        }
        if (progress.completedProblemIds instanceof Set) {
            return progress.completedProblemIds.has(probId);
        }
        return false;
    };

    const filteredProblems = useMemo(() => {
        const query =
            problemSearch.trim().toLowerCase();

        return problems.filter((problem) => {
            const difficulty =
                String(
                    problem?.difficulty || ""
                )
                    .trim()
                    .toLowerCase();

            if (
                problemFilter !== "all" &&
                difficulty !== problemFilter
            ) {
                return false;
            }

            if (categoryFilter !== "all") {
                const cat = String(problem?.category || (problem?.sourceId?.startsWith("sql-") ? "DATABASE" : "DSA")).toUpperCase();
                if (cat !== categoryFilter.toUpperCase()) {
                    return false;
                }
            }

            if (
                topicFilter !== "all" &&
                !(Array.isArray(problem?.tags) && problem.tags.some((t) => String(t).toLowerCase() === topicFilter.toLowerCase()))
            ) {
                return false;
            }

            if (!query) {
                return true;
            }

            const title =
                String(
                    problem?.title || ""
                ).toLowerCase();

            const description =
                String(
                    problem?.description || ""
                ).toLowerCase();

            const tags =
                Array.isArray(problem?.tags)
                    ? problem.tags
                          .join(" ")
                          .toLowerCase()
                    : "";

            return (
                title.includes(query) ||
                description.includes(query) ||
                tags.includes(query)
            );
        });
    }, [
        problems,
        problemSearch,
        problemFilter,
        categoryFilter,
        topicFilter
    ]);

    const getStarterCode = (
        problem,
        currentLanguage
    ) => {
        if (!problem || !currentLanguage) {
            return "";
        }

        const configurations =
            parseLanguageConfigurations(problem);

        const configuration =
            configurations[currentLanguage];

        if (
            configuration &&
            typeof configuration === "object" &&
            typeof configuration.starterCode ===
                "string"
        ) {
            return configuration.starterCode;
        }

        const matchingConfiguration =
            Object.entries(configurations).find(
                ([key]) =>
                    normalizeLanguageValue(key) ===
                    normalizeLanguageValue(
                        currentLanguage
                    )
            );

        if (
            matchingConfiguration &&
            matchingConfiguration[1] &&
            typeof matchingConfiguration[1] ===
                "object" &&
            typeof matchingConfiguration[1]
                .starterCode === "string"
        ) {
            return matchingConfiguration[1]
                .starterCode;
        }

        const starterCodes =
            problem.starterCodes;

        if (
            starterCodes &&
            typeof starterCodes === "object" &&
            !Array.isArray(starterCodes)
        ) {
            const exact =
                starterCodes[currentLanguage];

            if (typeof exact === "string") {
                return exact;
            }

            const matchingKey =
                Object.keys(starterCodes).find(
                    (key) =>
                        normalizeLanguageValue(
                            key
                        ) ===
                        normalizeLanguageValue(
                            currentLanguage
                        )
                );

            if (matchingKey) {
                return starterCodes[matchingKey] || "";
            }
        }

        if (
            typeof problem.starterCode === "string"
        ) {
            try {
                const parsed =
                    JSON.parse(
                        problem.starterCode
                    );

                if (
                    parsed &&
                    typeof parsed === "object"
                ) {
                    if (
                        typeof parsed[
                            currentLanguage
                        ] === "string"
                    ) {
                        return parsed[
                            currentLanguage
                        ];
                    }

                    const matchingKey =
                        Object.keys(parsed).find(
                            (key) =>
                                normalizeLanguageValue(
                                    key
                                ) ===
                                normalizeLanguageValue(
                                    currentLanguage
                                )
                        );

                    if (matchingKey) {
                        return (
                            parsed[matchingKey] || ""
                        );
                    }
                }
            } catch {
                return problem.starterCode;
            }
        }

        if (registeredLanguages.all && registeredLanguages.all.length > 0) {
            const registered = registeredLanguages.all.find(
                (item) => normalizeLanguageValue(item.key) === normalizeLanguageValue(currentLanguage)
            );
            if (registered?.starterCode) {
                return registered.starterCode;
            }
        }

        return (
            getProblemLanguages(problem).find(
                (item) =>
                    normalizeLanguageValue(
                        item.value
                    ) ===
                    normalizeLanguageValue(
                        currentLanguage
                    )
            )?.starterCode || ""
        );
    };

    const getCodeKey = (
        problemId,
        currentLanguage
    ) => {
        return `${problemId}_${currentLanguage}`;
    };

    const getCurrentCode = () => {
        if (!selectedProblem || !language) {
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
        if (isDatabaseProblem || normalizeLanguageValue(language) === "mysql" || normalizeLanguageValue(language) === "sql") {
            return "sql";
        }

        const selected =
            availableLanguages.find(
                (item) =>
                    normalizeLanguageValue(
                        item.value
                    ) ===
                    normalizeLanguageValue(
                        language
                    )
            );

        return (
            selected?.monacoLanguage ||
            getLanguageDefaults(language)
                .monacoLanguage ||
            "plaintext"
        );
    };

    const getFilteredSelectedIndex = () => {
        if (!selectedProblem) {
            return -1;
        }

        return filteredProblems.findIndex(
            (problem) =>
                problem.id ===
                selectedProblem.id
        );
    };

    const selectProblemById = async (
        problemId
    ) => {
        const index =
            problems.findIndex(
                (problem) =>
                    problem.id === problemId
            );

        if (index === -1) {
            return;
        }

        await handleProblemSelect(index);
    };

    const loadPistonRuntimes = async () => {
        try {
            setRuntimesLoading(true);

            const response =
                await getCodingRuntimes();

            setRuntimes(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );
            setRuntimeError("");
        } catch (requestError) {
            console.error(
                "Failed to load Piston runtimes",
                requestError
            );

            setRuntimes([]);
            setRuntimeError(
                getApiErrorMessage(requestError) ||
                "Live code execution is unavailable because Piston could not be reached."
            );
        } finally {
            setRuntimesLoading(false);
        }
    };

    const loadProblemPage = async (
        page = 0,
        append = false,
        search = problemSearch,
        difficulty = problemFilter === "all" ? "" : problemFilter,
        tag = topicFilter === "all" ? "" : topicFilter,
        category = categoryFilter === "all" ? "" : categoryFilter
    ) => {
        if (problemListLoading) {
            return;
        }

        try {
            setProblemListLoading(true);

            const response = await getCodingProblems(
                page,
                50,
                search,
                difficulty,
                tag,
                category
            );

            const result = response.data || {};
            const content = Array.isArray(result.content)
                ? result.content
                : [];

            setProblems((previous) =>
                append ? [...previous, ...content] : content
            );
            setProblemPage(page);
            setProblemTotal(Number(result.totalElements) || content.length);
        } catch (requestError) {
            console.error("Failed to load coding problem page", requestError);
            setError(
                requestError?.response?.data?.message ||
                "Unable to load coding problems."
            );
        } finally {
            setProblemListLoading(false);
        }
    };

    const loadCodingArena = async () => {
        try {
            setLoading(true);
            setError("");

            const [problemsResponse, progressResponse, tagsResponse, languagesResponse] =
                await Promise.all([
                    getCodingProblems(0, 50, "", "", ""),
                    getCodingProgress(),
                    getCodingProblemTags().catch(() => ({ data: [] })),
                    getCodingLanguages().catch(() => ({ data: { all: [], popular: [] } }))
                ]);

            if (Array.isArray(tagsResponse?.data)) {
                setAvailableTopics(tagsResponse.data);
            }

            if (languagesResponse?.data && Array.isArray(languagesResponse.data.all)) {
                setRegisteredLanguages(languagesResponse.data);
            }

            const backendProblems = Array.isArray(
                problemsResponse.data?.content
            )
                ? problemsResponse.data.content
                : [];

            const backendProgress =
                progressResponse.data || null;

            setProblems(backendProblems);
            setProblemPage(0);
            setProblemTotal(
                Number(problemsResponse.data?.totalElements) ||
                    backendProblems.length
            );
            setProgress(backendProgress);

            if (
                backendProblems.length === 0
            ) {
                setSelectedProblemIndex(null);
                return;
            }

            const currentProblemId =
                backendProgress
                    ?.currentProblem?.id;

            const lastSelectedProblemId =
                backendProgress
                    ?.lastSelectedProblem?.id;

            const restoredProblemId =
                currentProblemId ||
                lastSelectedProblemId;

            let restoredIndex =
                restoredProblemId
                    ? backendProblems.findIndex(
                          (problem) =>
                              problem.id ===
                              restoredProblemId
                      )
                    : -1;

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

            let restoredProblemDetails = restoredProblem;

            if (restoredProblem?.id) {
                const detailResponse = await getCodingProblem(
                    restoredProblem.id
                );
                restoredProblemDetails = detailResponse.data;
                setSelectedProblemDetails(restoredProblemDetails);
            }

            const restoredLanguages =
                getProblemLanguages(
                    restoredProblemDetails
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
                    : restoredLanguages[0]
                          ?.value || "";

            const initialLanguageData =
                restoredLanguages.find(
                    (item) =>
                        normalizeLanguageValue(
                            item.value
                        ) ===
                        normalizeLanguageValue(
                            initialLanguage
                        )
                );

            setLanguage(initialLanguage);

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

                setCodeMap((previous) => ({
                    ...previous,
                    [key]:
                        backendProgress.lastCode
                }));
            }
        } catch (requestError) {
            console.error(
                "Failed to load Coding Arena",
                requestError
            );

            setError(
                requestError?.response?.data
                    ?.message ||
                    requestError?.response?.data
                        ?.error ||
                    "Unable to load Coding Arena."
            );

            setProblems([]);
            setSelectedProblemIndex(null);
        } finally {
            setLoading(false);
        }
    };

    const loadGitHubStatus = async () => {
        try {
            const token = localStorage.getItem("token");
            if (!token) return;
            const data = await getGitHubRepository();
            if (data && typeof data === "object") {
                setGitHubInfo(data);
            }
        } catch (err) {
            // Optional integration, ignore error
        }
    };

    useEffect(() => {
        loadCodingArena();
        loadPistonRuntimes();
        loadGitHubStatus();

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

        if (availableLanguages.length === 0) {
            setLanguage("");
            setSearchLanguage("");
            return;
        }

        if (isDatabaseProblem) {
            if (language !== "mysql") {
                setLanguage("mysql");
                const mysqlData = availableLanguages.find((item) => item.value === "mysql") || availableLanguages[0];
                setSearchLanguage(mysqlData?.label || "MySQL");
            }
            return;
        }

        const currentExists =
            language !== "mysql" &&
            availableLanguages.some(
                (item) =>
                    normalizeLanguageValue(
                        item.value
                    ) ===
                    normalizeLanguageValue(
                        language
                    )
            );

        if (!currentExists || language === "mysql") {
            const progressLanguage =
                progress?.lastLanguage;

            const progressExists =
                progressLanguage &&
                progressLanguage !== "mysql" &&
                availableLanguages.some(
                    (item) =>
                        normalizeLanguageValue(
                            item.value
                        ) ===
                        normalizeLanguageValue(
                            progressLanguage
                        )
                );

            const nextLanguage =
                progressExists
                    ? progressLanguage
                    : availableLanguages[0]
                          .value;

            const nextLanguageData =
                availableLanguages.find(
                    (item) =>
                        normalizeLanguageValue(
                            item.value
                        ) ===
                        normalizeLanguageValue(
                            nextLanguage
                        )
                );

            setLanguage(nextLanguage);

            setSearchLanguage(
                nextLanguageData?.label ||
                    nextLanguage
            );
        }
    }, [
        selectedProblem,
        isDatabaseProblem,
        availableLanguages,
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
                topicDropdownRef.current &&
                !topicDropdownRef.current.contains(
                    event.target
                )
            ) {
                setShowTopicMenu(false);
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

        const handleKeyDown = (event) => {
            if (event.key === "Escape") {
                setShowLanguages(false);
                setShowProblemMenu(false);
                setShowTopicMenu(false);
                setShowAiHint(false);
            }
        };

        document.addEventListener(
            "mousedown",
            handleOutsideClick
        );
        document.addEventListener(
            "keydown",
            handleKeyDown
        );

        return () => {
            document.removeEventListener(
                "mousedown",
                handleOutsideClick
            );
            document.removeEventListener(
                "keydown",
                handleKeyDown
            );
        };
    }, []);

    const handleProblemSelect = async (
        index
    ) => {
        const problem = problems[index];

        if (!problem) {
            return;
        }

        let problemDetails = problem;
        setSelectedProblemDetails(null);

        try {
            const response = await getCodingProblem(problem.id);
            problemDetails = response.data;
            setSelectedProblemDetails(problemDetails);
        } catch (requestError) {
            console.error("Failed to load coding problem details", requestError);
            setError("Unable to load the selected coding problem.");
            return;
        }

        setSelectedProblemIndex(index);
        setShowProblemMenu(false);

        setExecutionResult(null);
        setLastSuccessfulRun(null);
        setAiHint("");
        setAiHintError("");
        setShowAiHint(false);

        const problemLanguages =
            getProblemLanguages(problemDetails);

        const isDbProblem = Boolean(
            problemDetails.category === "DATABASE" ||
            problemDetails.category === "SQL" ||
            problemDetails.sourceId?.startsWith("sql-")
        );

        let nextLanguage = "";
        if (isDbProblem) {
            nextLanguage = "mysql";
        } else {
            const currentLanguageExists =
                language !== "mysql" &&
                problemLanguages.some(
                    (item) =>
                        normalizeLanguageValue(
                            item.value
                        ) ===
                        normalizeLanguageValue(
                            language
                        )
                );

            nextLanguage =
                currentLanguageExists
                    ? language
                    : (progress?.lastLanguage && progress.lastLanguage !== "mysql")
                        ? progress.lastLanguage
                        : problemLanguages[0]?.value || "";
        }

        const nextLanguageData =
            problemLanguages.find(
                (item) =>
                    normalizeLanguageValue(
                        item.value
                    ) ===
                    normalizeLanguageValue(
                        nextLanguage
                    )
            );

        setLanguage(nextLanguage);

        setSearchLanguage(
            nextLanguageData?.label ||
                (isDbProblem ? "MySQL" : nextLanguage)
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
            setCodeMap((previous) => ({
                ...previous,
                [key]:
                        getStarterCode(
                        problemDetails,
                        nextLanguage
                    )
            }));
        }

        try {
            const response =
                await selectCodingProblem(
                    problem.id
                );

            setProgress(response.data);
        } catch (requestError) {
            console.error(
                "Failed to save selected problem",
                requestError
            );
        }
    };

    const handlePreviousProblem = async () => {
        const currentIndex =
            getFilteredSelectedIndex();

        if (currentIndex <= 0) {
            return;
        }

        await selectProblemById(
            filteredProblems[
                currentIndex - 1
            ].id
        );
    };

    const handleNextProblem = async () => {
        const currentIndex =
            getFilteredSelectedIndex();

        if (
            currentIndex === -1 ||
            currentIndex >=
                filteredProblems.length - 1
        ) {
            return;
        }

        await selectProblemById(
            filteredProblems[
                currentIndex + 1
            ].id
        );
    };

    const handleProblemSearchChange = (value) => {
        setProblemSearch(value);

        if (problemSearchTimerRef.current) {
            clearTimeout(problemSearchTimerRef.current);
        }

        problemSearchTimerRef.current = setTimeout(() => {
            loadProblemPage(
                0,
                false,
                value,
                problemFilter === "all" ? "" : problemFilter,
                topicFilter === "all" ? "" : topicFilter,
                categoryFilter === "all" ? "" : categoryFilter
            );
        }, 300);
    };

    const handleProblemFilterChange = (value) => {
        setProblemFilter(value);
        loadProblemPage(
            0,
            false,
            problemSearch,
            value === "all" ? "" : value,
            topicFilter === "all" ? "" : topicFilter,
            categoryFilter === "all" ? "" : categoryFilter
        );
    };

    const handleCategoryFilterChange = (value) => {
        setCategoryFilter(value);
        loadProblemPage(
            0,
            false,
            problemSearch,
            problemFilter === "all" ? "" : problemFilter,
            topicFilter === "all" ? "" : topicFilter,
            value === "all" ? "" : value
        );
    };

    const handleTopicFilterChange = (value) => {
        setTopicFilter(value);
        loadProblemPage(
            0,
            false,
            problemSearch,
            problemFilter === "all" ? "" : problemFilter,
            value === "all" ? "" : value,
            categoryFilter === "all" ? "" : categoryFilter
        );
    };

    const handleProblemListScroll = (event) => {
        const element = event.currentTarget;

        if (
            element.scrollTop + element.clientHeight >=
                element.scrollHeight - 80 &&
            !problemListLoading &&
            problems.length < problemTotal
        ) {
            loadProblemPage(problemPage + 1, true);
        }
    };

    const handleCodeChange = (value) => {
        if (!selectedProblem || !language) {
            return;
        }

        const nextCode = value || "";

        const key =
            getCodeKey(
                selectedProblem.id,
                language
            );

        setCodeMap((previous) => ({
            ...previous,
            [key]: nextCode
        }));

        setLastSuccessfulRun(null);

        if (saveTimerRef.current) {
            clearTimeout(
                saveTimerRef.current
            );
        }

        saveTimerRef.current =
            setTimeout(async () => {
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
            }, 800);
    };

    const handleLanguageSelect = (selectedLanguage) => {
        if (!selectedLanguage || selectedLanguage === language) {
            setShowLanguages(false);
            return;
        }

        const currentCode = getCurrentCode();
        const starter = getStarterCode(selectedProblem, language);
        const isModified = currentCode && starter && currentCode.trim() !== starter.trim();

        if (isModified) {
            setPendingLanguage(selectedLanguage);
            setShowLanguageModal(true);
            setShowLanguages(false);
            return;
        }

        applyLanguageChange(selectedLanguage);
    };

    const applyLanguageChange = (selectedLanguage) => {
        if (!selectedLanguage) {
            return;
        }

        if (selectedProblem) {
            const key = getCodeKey(selectedProblem.id, selectedLanguage);

            if (!Object.prototype.hasOwnProperty.call(codeMap, key)) {
                setCodeMap((previous) => ({
                    ...previous,
                    [key]: getStarterCode(selectedProblem, selectedLanguage)
                }));
            }
        }

        setLanguage(selectedLanguage);

        const selected = availableLanguages.find(
            (item) =>
                normalizeLanguageValue(item.value) ===
                normalizeLanguageValue(selectedLanguage)
        );

        setSearchLanguage(selected?.label || selectedLanguage);

        setShowLanguages(false);
        setExecutionResult(null);
        setLastSuccessfulRun(null);
        setAiHint("");
        setAiHintError("");
        setShowAiHint(false);
        setPendingLanguage(null);
        setShowLanguageModal(false);
    };

    const handleLanguageChange = (selectedLanguage) => {
        handleLanguageSelect(selectedLanguage);
    };

    const startCooldownTimer = (seconds = 6) => {
        setHintCooldown(seconds);
        if (hintCooldownTimerRef.current) {
            clearInterval(hintCooldownTimerRef.current);
        }
        hintCooldownTimerRef.current = setInterval(() => {
            setHintCooldown((previous) => {
                if (previous <= 1) {
                    clearInterval(hintCooldownTimerRef.current);
                    return 0;
                }
                return previous - 1;
            });
        }, 1000);
    };

    const getHintLevelName = (lvl) => {
        switch (lvl) {
            case 1:
                return "Concept";
            case 2:
                return "Observation";
            case 3:
                return "Algorithm";
            case 4:
                return "Edge Cases";
            default:
                return "Guidance";
        }
    };

    const fetchHintForLevel = async (targetLevel) => {
        if (
            !selectedProblem ||
            !language ||
            isGeneratingHint
        ) {
            return;
        }

        const code = getCurrentCode();

        setIsGeneratingHint(true);
        setAiHintError("");
        setShowAiHint(true);

        try {
            const response =
                await getCodingHint(
                    selectedProblem.title,
                    selectedProblem.description,
                    language,
                    code,
                    targetLevel
                );

            const data = response.data || {};
            const hint = data.hint ||
                (typeof data === "string"
                    ? data
                    : data.message || "");

            if (!String(hint).trim()) {
                throw new Error(
                    "AI returned an empty hint."
                );
            }

            setAiHint(String(hint).trim());
            setHintLevel(Number(data.level) || targetLevel);
            setHintLevelName(data.levelName || getHintLevelName(targetLevel));
            startCooldownTimer(6);
        } catch (requestError) {
            console.error(
                "AI hint generation failed",
                requestError
            );

            setAiHintError(
                getApiErrorMessage(requestError) ||
                    requestError?.message ||
                    "Unable to generate AI hint."
            );
        } finally {
            setIsGeneratingHint(false);
        }
    };

    const handleAiHint = async () => {
        if (!showAiHint || !aiHint) {
            await fetchHintForLevel(hintLevel || 1);
        } else {
            setShowAiHint(true);
        }
    };

    const handleNextHint = async () => {
        if (hintLevel >= 4 || isGeneratingHint || hintCooldown > 0) {
            return;
        }
        await fetchHintForLevel(hintLevel + 1);
    };

    const handleResetHint = async () => {
        setHintLevel(1);
        await fetchHintForLevel(1);
    };

    const buildExecutionError = (
        requestError,
        fallbackMessage
    ) => ({
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
            requestError?.response?.data
                ?.message ||
            requestError?.response?.data
                ?.error ||
            fallbackMessage,
        message: fallbackMessage,
        testCases: []
    });

    const getApiErrorMessage = (requestError) => {
        const data = requestError?.response?.data;

        if (typeof data === "string" && data.trim()) {
            return data.trim();
        }

        return data?.message || data?.error || "";
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

        const code = getCurrentCode();

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
                error: "Code cannot be empty.",
                message:
                    "Please write some code before running.",
                testCases: []
            });

            return;
        }

        try {
            setIsExecuting(true);
            setExecutionResult(null);
            setLastSuccessfulRun(null);

            const response =
                await executeCodingCode(
                    selectedProblem.id,
                    language,
                    code
                );

            setExecutionResult(
                response.data
            );

            if (response.data?.passed === true) {
                setLastSuccessfulRun({
                    problemId: selectedProblem.id,
                    language,
                    code,
                    success: true,
                    allPublicTestsPassed: true,
                    timestamp: Date.now()
                });
            } else {
                setLastSuccessfulRun(null);
            }
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
            setIsExecuting(false);
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

        const code = getCurrentCode();

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
                error: "Code cannot be empty.",
                message:
                    "Please write some code before submitting.",
                testCases: []
            });

            return;
        }

        try {
            setIsSubmitting(true);
            setExecutionResult(null);

            const response =
                await submitCodingCode(
                    selectedProblem.id,
                    language,
                    code
                );

            const result =
                response.data;

            setExecutionResult(result);

            if (result?.passed === true) {
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

                if (result?.gitHubSync?.connected && result?.gitHubSync?.repositoryUrl) {
                    setGitHubInfo(prev => ({
                        ...(prev || {}),
                        connected: true,
                        repositoryUrl: result.gitHubSync.repositoryUrl,
                        pushAuthorized: true
                    }));
                }
            }

            setLastSuccessfulRun(null);
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
            setIsSubmitting(false);
        }
    };

    const handleRetryGitHubSync = async () => {
        if (!hasSelectedProblem || !language || isRetryingSync) {
            return;
        }
        const code = getCurrentCode();
        if (!code || !code.trim()) {
            return;
        }

        try {
            setIsRetryingSync(true);
            const response = await retryGitHubSync(
                selectedProblem.id,
                language,
                code
            );
            const syncResult = response.data;
            setExecutionResult(prev => prev ? {
                ...prev,
                gitHubSync: syncResult
            } : prev);

            if (syncResult?.connected && syncResult?.repositoryUrl) {
                setGitHubInfo(prev => ({
                    ...(prev || {}),
                    connected: true,
                    repositoryUrl: syncResult.repositoryUrl,
                    pushAuthorized: true
                }));
            }
        } catch (syncError) {
            console.error("Failed to retry GitHub sync", syncError);
            const errMsg = syncError?.response?.data?.message || syncError?.message || "Failed to retry GitHub sync.";
            setExecutionResult(prev => prev ? {
                ...prev,
                gitHubSync: {
                    ...(prev.gitHubSync || {}),
                    connected: true,
                    synced: false,
                    alreadySynced: false,
                    error: errMsg,
                    message: "GitHub sync failed: " + errMsg
                }
            } : prev);
        } finally {
            setIsRetryingSync(false);
        }
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

        const totalTests =
            executionResult.totalTests || 0;

        const passedTests =
            executionResult.passedTests || 0;

        const progressWidth =
            totalTests > 0
                ? `${Math.min(
                      100,
                      (passedTests /
                          totalTests) *
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
                            {executionResult.status ||
                                "RESULT"}
                        </strong>

                        <span>
                            {executionResult.message ||
                                ""}
                        </span>
                    </div>

                    <div>
                        {executionResult.runtime !==
                        undefined
                            ? `${executionResult.runtime} ms`
                            : ""}
                    </div>
                </div>

                {totalTests > 0 && (
                    <div className="run-progress">
                        <div
                            className="run-fill"
                            style={{
                                width:
                                    progressWidth
                            }}
                        />
                    </div>
                )}

                <div className="execution-summary">
                    <span>
                        Passed: {passedTests}
                    </span>

                    <span>
                        Failed:{" "}
                        {executionResult.failedTests ||
                            0}
                    </span>

                    <span>
                        Total: {totalTests}
                    </span>
                </div>

                {executionResult.error && (
                    <div className="execution-error">
                        {executionResult.error}
                    </div>
                )}

                {executionResult.gitHubSync && (
                    <div className={`coding-github-sync-card ${
                        executionResult.gitHubSync.synced
                            ? "sync-success"
                            : !executionResult.gitHubSync.connected
                            ? "sync-unlinked"
                            : "sync-failed"
                    }`}>
                        <div className="sync-card-icon-col">
                            <FiGithub className="sync-card-gh-icon" />
                        </div>
                        <div className="sync-card-main-col">
                            <div className="sync-card-header-row">
                                <div className="sync-card-title">
                                    {executionResult.gitHubSync.synced ? (
                                        executionResult.gitHubSync.alreadySynced ? (
                                            <>
                                                <FiCheckCircle className="sync-status-icon success" />
                                                <span>Solution Already Synced to GitHub</span>
                                            </>
                                        ) : (
                                            <>
                                                <FiCheckCircle className="sync-status-icon success" />
                                                <span>
                                                    {executionResult.gitHubSync.solutionNumber > 1
                                                        ? `Solution ${executionResult.gitHubSync.solutionNumber} Synced to GitHub`
                                                        : "Solution Synced to GitHub"}
                                                </span>
                                            </>
                                        )
                                    ) : !executionResult.gitHubSync.connected ? (
                                        <>
                                            <FiAlertCircle className="sync-status-icon unlinked" />
                                            <span>GitHub Not Connected</span>
                                        </>
                                    ) : (
                                        <>
                                            <FiAlertCircle className="sync-status-icon failed" />
                                            <span>GitHub Sync Failed</span>
                                        </>
                                    )}
                                </div>

                                {executionResult.gitHubSync.connected && !executionResult.gitHubSync.synced && (
                                    <button
                                        type="button"
                                        className="sync-retry-btn"
                                        onClick={handleRetryGitHubSync}
                                        disabled={isRetryingSync}
                                    >
                                        <FiRefreshCw className={isRetryingSync ? "spin" : ""} />
                                        <span>{isRetryingSync ? "Syncing..." : "Retry Sync"}</span>
                                    </button>
                                )}

                                {!executionResult.gitHubSync.connected && (
                                    <a
                                        href="/profile"
                                        className="sync-connect-link"
                                    >
                                        Connect in Profile
                                    </a>
                                )}
                            </div>

                            <p className="sync-card-msg">
                                {executionResult.gitHubSync.message ||
                                 executionResult.gitHubSync.error ||
                                 "Your solution is tracked in GitHub."}
                            </p>

                            {executionResult.gitHubSync.synced && (
                                <div className="sync-card-meta-links">
                                    {executionResult.gitHubSync.fileUrl ? (
                                        <a
                                            href={executionResult.gitHubSync.fileUrl}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="sync-meta-link"
                                        >
                                            <span>{executionResult.gitHubSync.filePath}</span>
                                            <FiExternalLink />
                                        </a>
                                    ) : (
                                        <span className="sync-meta-path">
                                            {executionResult.gitHubSync.filePath}
                                        </span>
                                    )}

                                    {executionResult.gitHubSync.commitUrl && (
                                        <a
                                            href={executionResult.gitHubSync.commitUrl}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="sync-meta-link commit"
                                        >
                                            <span>Commit {executionResult.gitHubSync.commitSha?.substring(0, 7)}</span>
                                            <FiExternalLink />
                                        </a>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {testCases.length > 0 && (
                    <div className="execution-testcases">
                        {testCases.map(
                            (testCase) => (
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
                                            Test Case{" "}
                                            {
                                                testCase.testCaseNumber
                                            }
                                        </strong>

                                        <span>
                                            {testCase.passed
                                                ? "Passed"
                                                : "Failed"}
                                        </span>
                                    </div>

                                    <div className="execution-testcase-content">
                                        {testCase.input !==
                                            null &&
                                            testCase.input !==
                                                undefined && (
                                                <p>
                                                    <strong>
                                                        Input:
                                                    </strong>{" "}
                                                    {
                                                        testCase.input
                                                    }
                                                </p>
                                            )}

                                        {testCase.expectedOutput !==
                                            null &&
                                            testCase.expectedOutput !==
                                                undefined && (
                                                <p>
                                                    <strong>
                                                        Expected:
                                                    </strong>{" "}
                                                    {
                                                        testCase.expectedOutput
                                                    }
                                                </p>
                                            )}

                                        {testCase.actualOutput !==
                                            null &&
                                            testCase.actualOutput !==
                                                undefined && (
                                                <p>
                                                    <strong>
                                                        Output:
                                                    </strong>{" "}
                                                    {
                                                        testCase.actualOutput
                                                    }
                                                </p>
                                            )}

                                        {testCase.error && (
                                            <p>
                                                <strong>
                                                    Error:
                                                </strong>{" "}
                                                {
                                                    testCase.error
                                                }
                                            </p>
                                        )}
                                    </div>
                                </div>
                            )
                        )}
                    </div>
                )}
            </div>
        );
    };

    const filteredCurrentIndex =
        getFilteredSelectedIndex();

    return (
        <section className="coding-page">
            <div className="coding-topbar">
                <div
                    className="problem-head"
                    ref={problemMenuRef}
                >
                    <span
                        className="coding-problem-trigger"
                        onClick={() =>
                            setShowProblemMenu(
                                (previous) =>
                                    !previous
                            )
                        }
                    >
                        PROBLEM
                        {selectedProblem
                            ? ` · ${selectedProblem.difficulty}`
                            : ""}
                    </span>

                    <h1
                        className="coding-problem-trigger"
                        onClick={() =>
                            setShowProblemMenu(
                                (previous) =>
                                    !previous
                            )
                        }
                    >
                        {selectedProblem ? (
                            <>
                                <span className={`coding-nav-cat-pill ${isDatabaseProblem ? "db" : "dsa"}`}>
                                    {isDatabaseProblem ? "SQL" : "DSA"}
                                </span>
                                {`${selectedProblem.id}. ${selectedProblem.title}`}
                            </>
                        ) : (
                            "Your Next Challenge Awaits"
                        )}
                    </h1>

                    {showProblemMenu && (
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

                            <div className="coding-problem-controls">
                                <div className="coding-problem-search">
                                    <FiSearch className="problem-search-icon" />
                                    <input
                                        type="text"
                                        value={problemSearch}
                                        placeholder="Search by title, number, or keyword..."
                                        onChange={(event) =>
                                            handleProblemSearchChange(
                                                event.target.value
                                            )
                                        }
                                    />
                                    {problemSearch && (
                                        <button
                                            type="button"
                                            className="problem-search-clear"
                                            onClick={() =>
                                                handleProblemSearchChange("")
                                            }
                                            title="Clear search"
                                        >
                                            <FiX />
                                        </button>
                                    )}
                                </div>

                                <div className="coding-problem-category-row">
                                    <div className="coding-category-tabs">
                                        <button
                                            type="button"
                                            className={`category-tab ${categoryFilter === "all" ? "active" : ""}`}
                                            onClick={() => handleCategoryFilterChange("all")}
                                        >
                                            All Problems
                                        </button>
                                        <button
                                            type="button"
                                            className={`category-tab ${categoryFilter === "DSA" ? "active dsa" : ""}`}
                                            onClick={() => handleCategoryFilterChange("DSA")}
                                        >
                                            <span className="category-pill-icon dsa">DSA</span>
                                            Algorithms (5,060)
                                        </button>
                                        <button
                                            type="button"
                                            className={`category-tab ${categoryFilter === "DATABASE" ? "active database" : ""}`}
                                            onClick={() => handleCategoryFilterChange("DATABASE")}
                                        >
                                            <span className="category-pill-icon database">SQL</span>
                                            Database (1,200)
                                        </button>
                                    </div>
                                </div>

                                <div className="coding-problem-filter-row">
                                    <div className="coding-problem-filters">
                                        <button
                                            type="button"
                                            className={
                                                problemFilter === "all"
                                                    ? "problem-filter active"
                                                    : "problem-filter"
                                            }
                                            onClick={() =>
                                                handleProblemFilterChange("all")
                                            }
                                        >
                                            <FiList />
                                            All
                                        </button>

                                        <button
                                            type="button"
                                            className={
                                                problemFilter === "easy"
                                                    ? "problem-filter active easy-filter"
                                                    : "problem-filter easy-filter"
                                            }
                                            onClick={() =>
                                                handleProblemFilterChange("easy")
                                            }
                                        >
                                            <span className="filter-dot dot-easy" />
                                            Easy
                                        </button>

                                        <button
                                            type="button"
                                            className={
                                                problemFilter === "medium"
                                                    ? "problem-filter active medium-filter"
                                                    : "problem-filter medium-filter"
                                            }
                                            onClick={() =>
                                                handleProblemFilterChange("medium")
                                            }
                                        >
                                            <span className="filter-dot dot-medium" />
                                            Medium
                                        </button>

                                        <button
                                            type="button"
                                            className={
                                                problemFilter === "hard"
                                                    ? "problem-filter active hard-filter"
                                                    : "problem-filter hard-filter"
                                            }
                                            onClick={() =>
                                                handleProblemFilterChange("hard")
                                            }
                                        >
                                            <span className="filter-dot dot-hard" />
                                            Hard
                                        </button>
                                    </div>

                                    {availableTopics.length > 0 && (
                                        <div className="coding-topic-wrapper" ref={topicDropdownRef}>
                                            <button
                                                type="button"
                                                className={`coding-topic-dropdown-trigger ${topicFilter !== "all" ? "topic-active" : ""}`}
                                                onClick={() => setShowTopicMenu((prev) => !prev)}
                                                aria-label="Filter problems by topic"
                                            >
                                                <FiTag className="topic-icon" />
                                                <span className="topic-label">
                                                    {topicFilter === "all" ? `All Topics (${availableTopics.length})` : topicFilter}
                                                </span>
                                                {topicFilter !== "all" && (
                                                    <span
                                                        className="topic-clear-btn"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleTopicFilterChange("all");
                                                        }}
                                                        title="Clear topic filter"
                                                    >
                                                        <FiX />
                                                    </span>
                                                )}
                                                <FiChevronDown className={`topic-caret ${showTopicMenu ? "rotated" : ""}`} />
                                            </button>

                                            {showTopicMenu && (
                                                <div className="coding-topic-popover">
                                                    <div className="topic-popover-search">
                                                        <FiSearch />
                                                        <input
                                                            type="text"
                                                            placeholder="Search topics..."
                                                            value={topicSearchQuery}
                                                            onChange={(e) => setTopicSearchQuery(e.target.value)}
                                                            autoFocus
                                                        />
                                                        {topicSearchQuery && (
                                                            <button
                                                                type="button"
                                                                className="topic-search-clear"
                                                                onClick={() => setTopicSearchQuery("")}
                                                            >
                                                                <FiX />
                                                            </button>
                                                        )}
                                                    </div>

                                                    <div className="topic-popover-list">
                                                        <button
                                                            type="button"
                                                            className={`topic-popover-item ${topicFilter === "all" ? "selected" : ""}`}
                                                            onClick={() => {
                                                                handleTopicFilterChange("all");
                                                                setShowTopicMenu(false);
                                                                setTopicSearchQuery("");
                                                            }}
                                                        >
                                                            <span>All Topics ({availableTopics.length})</span>
                                                            {topicFilter === "all" && <FiCheck className="topic-check" />}
                                                        </button>

                                                        {filteredAvailableTopics.map((topic) => (
                                                            <button
                                                                type="button"
                                                                key={topic}
                                                                className={`topic-popover-item ${topicFilter === topic ? "selected" : ""}`}
                                                                onClick={() => {
                                                                    handleTopicFilterChange(topic);
                                                                    setShowTopicMenu(false);
                                                                    setTopicSearchQuery("");
                                                                }}
                                                            >
                                                                <span>{topic}</span>
                                                                {topicFilter === topic && <FiCheck className="topic-check" />}
                                                            </button>
                                                        ))}

                                                        {filteredAvailableTopics.length === 0 && (
                                                            <div className="topic-popover-empty">No matching topics</div>
                                                        )}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div className="coding-problem-list-header">

                                <span className="col-status">Status</span>
                                <span className="col-id"> </span>
                                <span className="col-title">Title</span>
                                <span className="col-topics">Topics</span>
                                <span className="col-difficulty">Difficulty</span>
                            </div>

                            <div
                                className="coding-problem-list"
                                ref={problemListRef}
                                onScroll={handleProblemListScroll}
                            >
                                {filteredProblems.length > 0 ? (
                                    filteredProblems.map((problem) => {
                                        const index = problems.findIndex(
                                            (item) => item.id === problem.id
                                        );
                                        const isSelected = index === selectedProblemIndex || selectedProblem?.id === problem.id;
                                        const solved = isProblemSolved(problem.id);

                                        return (
                                            <div
                                                key={problem.id}
                                                className={`coding-problem-row ${
                                                    isSelected ? "active-problem-row" : ""
                                                } ${solved ? "solved-problem-row" : ""}`}
                                                onClick={() => handleProblemSelect(index)}
                                                role="button"
                                                tabIndex={0}
                                                onKeyDown={(e) => {
                                                    if (e.key === "Enter" || e.key === " ") {
                                                        e.preventDefault();
                                                        handleProblemSelect(index);
                                                    }
                                                }}
                                            >
                                                <div className="problem-row-status">
                                                    {solved ? (
                                                        <FiCheckCircle className="status-icon-solved" title="Solved" />
                                                    ) : (
                                                        <span className="status-icon-unsolved" />
                                                    )}
                                                </div>

                                                <div className="problem-row-id">
                                                    <span className={`category-tag-mini ${problem.category === 'DATABASE' || problem.sourceId?.startsWith('sql-') ? 'db' : 'dsa'}`}>
                                                        {problem.category === 'DATABASE' || problem.sourceId?.startsWith('sql-') ? 'SQL' : 'DSA'}
                                                    </span>
                                                    #{problem.id}
                                                </div>

                                                <div className="problem-row-title-wrap">
                                                    <span className="problem-row-title">
                                                        {problem.title}
                                                    </span>
                                                </div>

                                                <div className="problem-row-topics">
                                                    {Array.isArray(problem.tags) && problem.tags.length > 0 ? (
                                                        problem.tags.slice(0, 2).map((tag, tIdx) => (
                                                            <span key={tIdx} className="problem-tag-pill">
                                                                {tag}
                                                            </span>
                                                        ))
                                                    ) : (
                                                        <span className="problem-tag-empty">—</span>
                                                    )}
                                                </div>

                                                <div className="problem-row-difficulty">
                                                    <span
                                                        className={`difficulty-pill ${String(
                                                            problem.difficulty || "easy"
                                                        ).toLowerCase()}`}
                                                    >
                                                        {problem.difficulty || "Easy"}
                                                    </span>
                                                </div>
                                            </div>
                                        );
                                    })
                                ) : (
                                    <div className="coding-problem-empty">
                                        {loading ? (
                                            "Loading problems..."
                                        ) : problemSearch || topicFilter !== "all" || problemFilter !== "all" ? (
                                            <div className="empty-filters-box">
                                                <p>No problems match your current filters.</p>
                                                <button
                                                    type="button"
                                                    className="reset-filters-btn"
                                                    onClick={() => {
                                                        handleProblemFilterChange("all");
                                                        handleTopicFilterChange("all");
                                                        handleProblemSearchChange("");
                                                    }}
                                                >
                                                    Reset All Filters
                                                </button>
                                            </div>
                                        ) : (
                                            "No problems found."
                                        )}
                                    </div>
                                )}

                                {problemListLoading && problems.length > 0 && (
                                    <div className="coding-problem-loading-more">
                                        <div className="mini-spinner" />
                                        <span>Loading more problems...</span>
                                    </div>
                                )}
                            </div>

                            <div className="coding-problem-pagination">
                                <div className="pagination-info">
                                    Showing <strong>{problemTotal > 0 ? (problemPage * 50) + 1 : 0}</strong>–<strong>{Math.min((problemPage + 1) * 50, problemTotal)}</strong> of <strong>{problemTotal.toLocaleString()}</strong> problems
                                </div>
                                <div className="pagination-actions">
                                    <button
                                        type="button"
                                        className="pagination-btn"
                                        disabled={problemPage === 0 || problemListLoading}
                                        onClick={() => loadProblemPage(0, false)}
                                        title="First Page"
                                    >
                                        « First
                                    </button>
                                    <button
                                        type="button"
                                        className="pagination-btn"
                                        disabled={problemPage === 0 || problemListLoading}
                                        onClick={() => loadProblemPage(problemPage - 1, false)}
                                        title="Previous Page"
                                    >
                                        ‹ Prev
                                    </button>
                                    <span className="pagination-page-indicator">
                                        Page {problemPage + 1} of {Math.max(1, Math.ceil(problemTotal / 50))}
                                    </span>
                                    <button
                                        type="button"
                                        className="pagination-btn"
                                        disabled={(problemPage + 1) * 50 >= problemTotal || problemListLoading}
                                        onClick={() => loadProblemPage(problemPage + 1, false)}
                                        title="Next Page"
                                    >
                                        Next ›
                                    </button>
                                    <button
                                        type="button"
                                        className="pagination-btn"
                                        disabled={(problemPage + 1) * 50 >= problemTotal || problemListLoading}
                                        onClick={() => loadProblemPage(Math.max(0, Math.ceil(problemTotal / 50) - 1), false)}
                                        title="Last Page"
                                    >
                                        Last »
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                <div className="coding-actions">
                    <button
                        className="coding-mobile-problem-btn"
                        type="button"
                        onClick={
                            handlePreviousProblem
                        }
                        disabled={
                            filteredCurrentIndex <=
                            0
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
                            filteredCurrentIndex ===
                                -1 ||
                            filteredCurrentIndex >=
                                filteredProblems.length -
                                    1
                        }
                    >
                        Next
                        <FiArrowRight />
                    </button>

                    {gitHubInfo?.connected && gitHubInfo?.repositoryUrl ? (
                        <a
                            href={gitHubInfo.repositoryUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="coding-github-badge connected"
                            title={`GitHub Sync Active: ${gitHubInfo.repositoryUrl}`}
                        >
                            <FiGithub className="github-icon" />
                            <span className="github-badge-text">
                                {gitHubInfo.repositoryUrl.replace(/^https?:\/\/github\.com\//i, "")}
                            </span>
                            <FiExternalLink className="github-ext-icon" />
                        </a>
                    ) : (
                        <a
                            href="/profile"
                            className="coding-github-badge not-connected"
                            title="Connect GitHub repository in Profile to automatically push your accepted solutions"
                        >
                            <FiGithub className="github-icon" />
                            <span className="github-badge-text">Sync to GitHub</span>
                        </a>
                    )}

                    <button
                        className="run-btn"
                        type="button"
                        disabled={
                            !hasSelectedProblem ||
                            !language ||
                            isExecuting ||
                            isSubmitting ||
                            !getCurrentCode().trim()
                        }
                        onClick={handleRun}
                    >
                        <FiPlay />
                        {isExecuting
                            ? "Running..."
                            : "Run"}
                    </button>

                    <button
                        className="submit-btn"
                        type="button"
                        disabled={
                            !hasSelectedProblem ||
                            !language ||
                            isExecuting ||
                            isSubmitting ||
                            !getCurrentCode().trim()
                        }
                        onClick={handleSubmit}
                    >
                        {isSubmitting
                            ? "Submitting..."
                            : "Submit"}
                    </button>
                </div>
            </div>

            <div className="coding-grid">
                <div className="problem-card">
                    {loading ? (
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

                                {Array.isArray(
                                    selectedProblem.tags
                                ) &&
                                    selectedProblem.tags.map(
                                        (
                                            tag,
                                            index
                                        ) => (
                                            <span
                                                key={`${tag}-${index}`}
                                                className="tag-blue"
                                            >
                                                {
                                                    tag
                                                }
                                            </span>
                                        )
                                    )}
                            </div>

                            <div className="problem-description">
                                <p>
                                    {
                                        selectedProblem.description
                                    }
                                </p>
                            </div>

                            {(selectedProblem.inputExample ||
                                selectedProblem.outputExample) && (
                                <div className="example-box">
                                    {selectedProblem.inputExample && (
                                        <p>
                                            <strong>
                                                Input:
                                            </strong>{" "}
                                            {
                                                selectedProblem.inputExample
                                            }
                                        </p>
                                    )}

                                    {selectedProblem.outputExample && (
                                        <p>
                                            <strong>
                                                Output:
                                            </strong>{" "}
                                            {
                                                selectedProblem.outputExample
                                            }
                                        </p>
                                    )}
                                </div>
                            )}

                            {Array.isArray(
                                selectedProblem.constraints
                            ) &&
                                selectedProblem
                                    .constraints
                                    .length >
                                    0 && (
                                    <div className="constraints-box">
                                        <h3>
                                            Constraints
                                        </h3>

                                        <ul>
                                            {selectedProblem.constraints.map(
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
                                            )}
                                        </ul>
                                    </div>
                                )}
                        </>
                    ) : (
                        <div className="coding-empty-problem">
                            {error ||
                                "Your Next Challenge Awaits"}
                        </div>
                    )}
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
                                className="coding-lang-selector-wrapper"
                                ref={dropdownRef}
                            >
                                <button
                                    type="button"
                                    className="coding-lang-trigger"
                                    onClick={() => {
                                        if (availableLanguages.length > 0) {
                                            setShowLanguages((prev) => !prev);
                                            setTimeout(() => langSearchInputRef.current?.focus(), 50);
                                        }
                                    }}
                                    disabled={!hasSelectedProblem || availableLanguages.length === 0}
                                    aria-expanded={showLanguages}
                                >
                                    <FiCode className="coding-lang-icon" />
                                    <span className="coding-lang-name">
                                        {availableLanguages.find(
                                            (item) =>
                                                normalizeLanguageValue(item.value) ===
                                                normalizeLanguageValue(language)
                                        )?.label || language || "Select Language"}
                                    </span>
                                    <FiChevronDown
                                        className={`coding-lang-chevron ${
                                            showLanguages ? "rotated" : ""
                                        }`}
                                    />
                                </button>

                                {showLanguages && (
                                    <div className="coding-lang-dropdown">
                                        <div className="coding-lang-search-box">
                                            <FiSearch className="lang-search-icon" />
                                            <input
                                                ref={langSearchInputRef}
                                                type="text"
                                                value={searchLanguage}
                                                placeholder="Search languages..."
                                                onChange={(e) => {
                                                    setSearchLanguage(e.target.value);
                                                    setActiveLangIndex(0);
                                                }}
                                                onKeyDown={(e) => {
                                                    if (e.key === "Escape") {
                                                        setShowLanguages(false);
                                                    } else if (e.key === "ArrowDown") {
                                                        e.preventDefault();
                                                        setActiveLangIndex((prev) =>
                                                            Math.min(prev + 1, Math.max(0, filteredLanguages.length - 1))
                                                        );
                                                    } else if (e.key === "ArrowUp") {
                                                        e.preventDefault();
                                                        setActiveLangIndex((prev) => Math.max(prev - 1, 0));
                                                    } else if (e.key === "Enter") {
                                                        e.preventDefault();
                                                        if (filteredLanguages[activeLangIndex]) {
                                                            handleLanguageSelect(filteredLanguages[activeLangIndex].value);
                                                        } else if (filteredLanguages.length > 0) {
                                                            handleLanguageSelect(filteredLanguages[0].value);
                                                        }
                                                    }
                                                }}
                                            />
                                            {searchLanguage && (
                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        setSearchLanguage("")
                                                    }
                                                    className="lang-clear-btn"
                                                    title="Clear search"
                                                >
                                                    <FiX />
                                                </button>
                                            )}
                                        </div>

                                        <div className="coding-lang-scroll-area">
                                            {filteredDatabaseLanguages.length > 0 && (
                                                <div className="coding-lang-section">
                                                    <div className="coding-lang-section-label coding-lang-section-db">
                                                        <span>Database / SQL</span>
                                                        <span className="lang-section-sub">MySQL Sandbox</span>
                                                    </div>
                                                    {filteredDatabaseLanguages.map((item) => {
                                                        const isSelected =
                                                            normalizeLanguageValue(item.value) ===
                                                            normalizeLanguageValue(language);
                                                        return (
                                                            <button
                                                                type="button"
                                                                key={item.value}
                                                                className={`coding-lang-option ${
                                                                    isSelected ? "selected" : ""
                                                                }`}
                                                                onClick={() =>
                                                                    handleLanguageChange(item.value)
                                                                }
                                                            >
                                                                <span className="lang-option-name">
                                                                    {item.label}
                                                                </span>
                                                                <span className="lang-option-badge db-badge">SQL Sandbox</span>
                                                                {isSelected && (
                                                                    <FiCheck className="lang-option-check" />
                                                                )}
                                                            </button>
                                                        );
                                                    })}
                                                </div>
                                            )}

                                            {filteredPopularLanguages.length > 0 && (
                                                <div className="coding-lang-section">
                                                    <div className="coding-lang-section-label">
                                                        Popular Languages
                                                    </div>
                                                    {filteredPopularLanguages.map((item) => {
                                                        const isSelected =
                                                            normalizeLanguageValue(item.value) ===
                                                            normalizeLanguageValue(language);
                                                        return (
                                                            <button
                                                                type="button"
                                                                key={item.value}
                                                                className={`coding-lang-option ${
                                                                    isSelected ? "selected" : ""
                                                                }`}
                                                                onClick={() =>
                                                                    handleLanguageChange(item.value)
                                                                }
                                                            >
                                                                <span className="lang-option-name">
                                                                    {item.label}
                                                                </span>
                                                                {isSelected && (
                                                                    <FiCheck className="lang-option-check" />
                                                                )}
                                                            </button>
                                                        );
                                                    })}
                                                </div>
                                            )}

                                            {filteredMoreLanguages.length > 0 && (
                                                <div className="coding-lang-section">
                                                    <div className="coding-lang-section-label">
                                                        More Languages ({filteredMoreLanguages.length})
                                                    </div>
                                                    {filteredMoreLanguages.map((item) => {
                                                        const isSelected =
                                                            normalizeLanguageValue(item.value) ===
                                                            normalizeLanguageValue(language);
                                                        return (
                                                            <button
                                                                type="button"
                                                                key={item.value}
                                                                className={`coding-lang-option ${
                                                                    isSelected ? "selected" : ""
                                                                }`}
                                                                onClick={() =>
                                                                    handleLanguageChange(item.value)
                                                                }
                                                            >
                                                                <span className="lang-option-name">
                                                                    {item.label}
                                                                </span>
                                                                {isSelected && (
                                                                    <FiCheck className="lang-option-check" />
                                                                )}
                                                            </button>
                                                        );
                                                    })}
                                                </div>
                                            )}

                                            {filteredLanguages.length === 0 && (
                                                <div className="coding-lang-empty">
                                                    No languages found
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <button
                                type="button"
                                className={`ai-hint ${isGeneratingHint ? "thinking" : ""}`}
                                disabled={
                                    !hasSelectedProblem ||
                                    !language ||
                                    isGeneratingHint
                                }
                                onClick={handleAiHint}
                            >
                                <FiCpu />
                                {isGeneratingHint
                                    ? "Thinking..."
                                    : "✦ AI Hint"}
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

                    {showAiHint && (
                        <div className="coding-ai-hint-popover">
                            <div className="ai-hint-header">
                                <div className="ai-hint-title-wrap">
                                    <span className="ai-hint-sparkle">✦</span>
                                    <div>
                                        <h4 className="ai-hint-title">AI Coding Mentor</h4>
                                        <span className="ai-hint-level-tag">
                                            Level {hintLevel} · {hintLevelName}
                                        </span>
                                    </div>
                                </div>
                                <button
                                    type="button"
                                    className="ai-hint-close-btn"
                                    onClick={() => setShowAiHint(false)}
                                    title="Close hint panel"
                                >
                                    <FiX />
                                </button>
                            </div>

                            <div className="ai-hint-level-stepper">
                                {[
                                    { lvl: 1, name: "Concept" },
                                    { lvl: 2, name: "Observation" },
                                    { lvl: 3, name: "Algorithm" },
                                    { lvl: 4, name: "Edge Cases" }
                                ].map((step) => (
                                    <div
                                        key={step.lvl}
                                        className={`stepper-step ${
                                            hintLevel >= step.lvl ? "completed" : ""
                                        } ${hintLevel === step.lvl ? "current" : ""}`}
                                    >
                                        <span className="step-number">{step.lvl}</span>
                                        <span className="step-name">{step.name}</span>
                                    </div>
                                ))}
                            </div>

                            <div className="ai-hint-body">
                                {isGeneratingHint ? (
                                    <div className="ai-hint-loading-state">
                                        <div className="hint-spinner" />
                                        <p>Generating Level {hintLevel} guidance...</p>
                                    </div>
                                ) : aiHintError ? (
                                    <div className="ai-hint-error-state">
                                        <p>{aiHintError}</p>
                                        <button
                                            type="button"
                                            className="ai-hint-retry-btn"
                                            onClick={() => fetchHintForLevel(hintLevel)}
                                        >
                                            <FiRefreshCw /> Retry
                                        </button>
                                    </div>
                                ) : aiHint ? (
                                    <div className="ai-hint-content">
                                        <p>{aiHint}</p>
                                    </div>
                                ) : (
                                    <p className="ai-hint-prompt">
                                        Click AI Hint to receive progressive guidance.
                                    </p>
                                )}
                            </div>

                            <div className="ai-hint-footer">
                                <div className="ai-hint-footer-left">
                                    {hintLevel > 1 && (
                                        <button
                                            type="button"
                                            className="ai-hint-sub-btn"
                                            onClick={handleResetHint}
                                            disabled={isGeneratingHint}
                                        >
                                            Reset to Level 1
                                        </button>
                                    )}
                                </div>
                                <div className="ai-hint-footer-right">
                                    {hintLevel < 4 && (
                                        <button
                                            type="button"
                                            className="ai-hint-action-btn"
                                            onClick={handleNextHint}
                                            disabled={isGeneratingHint || hintCooldown > 0}
                                        >
                                            {hintCooldown > 0 ? (
                                                `Wait ${hintCooldown}s`
                                            ) : (
                                                `Another Hint (Level ${hintLevel + 1})`
                                            )}
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="testcase-card">
                        <div className="testcase-top">
                            <h3>
                                Test cases
                            </h3>

                            <span>
                                {executionResult
                                    ? `${executionResult.passedTests || 0}/${executionResult.totalTests || 0} passed`
                                    : hasSelectedProblem
                                      ? `${
                                            Array.isArray(
                                                selectedProblem.testCases
                                            )
                                                ? selectedProblem
                                                      .testCases
                                                      .length
                                                : 0
                                        } public cases`
                                      : "Select a problem"}
                            </span>
                        </div>

                        {executionResult &&
                        Array.isArray(
                            executionResult.testCases
                        ) &&
                        executionResult
                            .testCases.length >
                            0 ? (
                            executionResult.testCases.map(
                                (testCase) => (
                                    <div
                                        className="case-item"
                                        key={
                                            testCase.testCaseNumber
                                        }
                                    >
                                        <p>
                                            <strong>
                                                Test Case{" "}
                                                {
                                                    testCase.testCaseNumber
                                                }
                                            </strong>{" "}
                                            ·{" "}
                                            {testCase.passed
                                                ? "Passed"
                                                : "Failed"}
                                        </p>

                                        <span>
                                            {testCase.passed
                                                ? "✓"
                                                : "✕"}
                                        </span>
                                    </div>
                                )
                            )
                        ) : selectedProblem &&
                          Array.isArray(
                              selectedProblem.testCases
                          ) &&
                          selectedProblem
                              .testCases.length >
                              0 ? (
                            selectedProblem.testCases.map(
                                (testCase) => (
                                    <div
                                        className="case-item"
                                        key={
                                            testCase.testCaseNumber
                                        }
                                    >
                                        <p>
                                            Test Case{" "}
                                            {
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
                                        Input:{" "}
                                        {selectedProblem.inputExample ||
                                            "Sample input"}
                                    </p>
                                </div>

                                <div className="case-item">
                                    <p>
                                        Expected:{" "}
                                        {selectedProblem.outputExample ||
                                            "Sample output"}
                                    </p>
                                </div>
                            </>
                        ) : (
                            <div className="coding-empty-problem">
                                Select a problem to view test cases
                            </div>
                        )}
                    </div>

                    {renderExecutionResult()}
                </div>
            </div>

            {showLanguageModal && (
                <div className="coding-modal-overlay" onClick={() => {
                    setShowLanguageModal(false);
                    setPendingLanguage(null);
                }}>
                    <div className="coding-confirm-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="confirm-modal-header">
                            <h3>Switch Language</h3>
                            <button
                                type="button"
                                className="confirm-modal-close-btn"
                                onClick={() => {
                                    setShowLanguageModal(false);
                                    setPendingLanguage(null);
                                }}
                            >
                                <FiX />
                            </button>
                        </div>
                        <p className="confirm-modal-body">
                            Switching language will replace the current code.
                        </p>
                        <div className="confirm-modal-actions">
                            <button
                                type="button"
                                className="confirm-modal-cancel-btn"
                                onClick={() => {
                                    setShowLanguageModal(false);
                                    setPendingLanguage(null);
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                type="button"
                                className="confirm-modal-submit-btn"
                                onClick={() => applyLanguageChange(pendingLanguage)}
                            >
                                Switch Language
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
}
