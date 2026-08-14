import "../styles/codingarena.css";

import { useState, useRef, useEffect } from "react";

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
    saveLastSelectedProblem,
    saveCodingState,
    completeCodingProblem,
    updateCodingSubmission
} from "../services/codingService";

export default function CodingArena() {

    const starterCodes = {
        javascript: `function solve() {

}`,

        typescript: `function solve(): void {

}`,

        python: `def solve():

    pass`,

        java: `class Solution {

    public static void main(String[] args) {

    }
}`,

        cpp: `#include <iostream>
using namespace std;

int main() {

    return 0;
}`,

        c: `#include <stdio.h>

int main() {

    return 0;
}`,

        go: `package main

func main() {

}`,

        rust: `fn main() {

}`,

        php: `<?php

echo "Hello World";

?>`,

        kotlin: `fun main() {

}`
    };

    const languages = [
        {
            label: "JavaScript",
            value: "javascript"
        },
        {
            label: "TypeScript",
            value: "typescript"
        },
        {
            label: "Python",
            value: "python"
        },
        {
            label: "Java",
            value: "java"
        },
        {
            label: "C++",
            value: "cpp"
        },
        {
            label: "C",
            value: "c"
        },
        {
            label: "Go",
            value: "go"
        },
        {
            label: "Rust",
            value: "rust"
        },
        {
            label: "PHP",
            value: "php"
        },
        {
            label: "Kotlin",
            value: "kotlin"
        }
    ];

    const [language, setLanguage] =
        useState("javascript");

    const [searchLanguage, setSearchLanguage] =
        useState("JavaScript");

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
        useState(starterCodes);

    const [showRunResult, setShowRunResult] =
        useState(false);

    const [showSubmitResult, setShowSubmitResult] =
        useState(false);

    const [showProblemMenu, setShowProblemMenu] =
        useState(false);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    const dropdownRef = useRef(null);

    const optionRefs = useRef([]);

    const problemMenuRef = useRef(null);

    const saveTimerRef = useRef(null);

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

    const filteredLanguages =
        languages.filter((item) =>
            item.label
                .toLowerCase()
                .includes(
                    searchLanguage.toLowerCase()
                )
        );

    useEffect(() => {

        loadCodingArena();

        return () => {

            if (saveTimerRef.current) {
                clearTimeout(saveTimerRef.current);
            }

        };

    }, []);

    useEffect(() => {

        const handleOutsideClick = (event) => {

            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(event.target)
            ) {
                setShowLanguages(false);
            }

            if (
                problemMenuRef.current &&
                !problemMenuRef.current.contains(event.target)
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
                progressResponse.data || null;

            setProblems(backendProblems);
            setProgress(backendProgress);

            if (backendProblems.length === 0) {

                setSelectedProblemIndex(null);
                return;

            }

            const currentProblemId =
                backendProgress?.currentProblem?.id;

            const lastSelectedProblemId =
                backendProgress?.lastSelectedProblem?.id;

            let restoredProblemId =
                currentProblemId ||
                lastSelectedProblemId;

            if (
                backendProgress?.completed &&
                !currentProblemId
            ) {

                restoredProblemId = null;

            }

            if (restoredProblemId) {

                const restoredIndex =
                    backendProblems.findIndex(
                        (problem) =>
                            problem.id ===
                            restoredProblemId
                    );

                if (restoredIndex !== -1) {

                    setSelectedProblemIndex(
                        restoredIndex
                    );

                } else {

                    setSelectedProblemIndex(null);

                }

            } else {

                setSelectedProblemIndex(null);

            }

            if (
                backendProgress?.lastLanguage &&
                languages.some(
                    (item) =>
                        item.value ===
                        backendProgress.lastLanguage
                )
            ) {

                setLanguage(
                    backendProgress.lastLanguage
                );

                const savedLanguage =
                    languages.find(
                        (item) =>
                            item.value ===
                            backendProgress.lastLanguage
                    );

                if (savedLanguage) {

                    setSearchLanguage(
                        savedLanguage.label
                    );

                }

            }

            if (
                backendProgress?.lastCode &&
                backendProgress?.lastLanguage
            ) {

                setCodeMap(
                    (previous) => ({
                        ...previous,
                        [backendProgress.lastLanguage]:
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
                "Unable to load Coding Arena."
            );

            setProblems([]);
            setSelectedProblemIndex(null);

        } finally {

            setLoading(false);

        }

    };

    const getStarterCode = (
        problem,
        currentLanguage
    ) => {

        if (
            problem?.starterCode &&
            typeof problem.starterCode ===
                "object"
        ) {

            return (
                problem.starterCode[
                    currentLanguage
                ] ||
                starterCodes[currentLanguage] ||
                ""
            );

        }

        if (
            problem?.starterCode &&
            typeof problem.starterCode ===
                "string"
        ) {

            return problem.starterCode;

        }

        return (
            starterCodes[currentLanguage] ||
            ""
        );

    };

    const handleProblemSelect = async (
        index
    ) => {

        const problem =
            problems[index];

        if (!problem) {
            return;
        }

        setSelectedProblemIndex(index);
        setShowProblemMenu(false);
        setShowRunResult(false);
        setShowSubmitResult(false);

        const savedCode =
            progress?.lastSelectedProblem?.id ===
                problem.id &&
            progress?.lastLanguage === language &&
            progress?.lastCode
                ? progress.lastCode
                : getStarterCode(
                    problem,
                    language
                );

        setCodeMap(
            (previous) => ({
                ...previous,
                [language]: savedCode
            })
        );

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

        if (
            !hasSelectedProblem ||
            isFirstProblem
        ) {
            return;
        }

        const nextIndex =
            selectedProblemIndex - 1;

        await handleProblemSelect(
            nextIndex
        );

    };

    const handleNextProblem = async () => {

        if (
            !hasSelectedProblem ||
            isLastProblem
        ) {
            return;
        }

        const nextIndex =
            selectedProblemIndex + 1;

        await handleProblemSelect(
            nextIndex
        );

    };

    const handleCodeChange = (
        value
    ) => {

        if (!hasSelectedProblem) {
            return;
        }

        const nextCode =
            value || "";

        setCodeMap(
            (previous) => ({
                ...previous,
                [language]: nextCode
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

        const existingCode =
            codeMap[
                selectedLanguage
            ];

        if (
            !existingCode &&
            selectedProblem
        ) {

            setCodeMap(
                (previous) => ({
                    ...previous,
                    [selectedLanguage]:
                        getStarterCode(
                            selectedProblem,
                            selectedLanguage
                        )
                })
            );

        }

        setLanguage(
            selectedLanguage
        );

        const selected =
            languages.find(
                (item) =>
                    item.value ===
                    selectedLanguage
            );

        if (selected) {

            setSearchLanguage(
                selected.label
            );

        }

        setShowLanguages(false);

    };

    const handleRun = () => {

        if (!hasSelectedProblem) {
            return;
        }

        setShowRunResult(true);
        setShowSubmitResult(false);

    };

    const handleSubmit = async () => {

        if (!hasSelectedProblem) {
            return;
        }

        try {

            const response =
                await completeCodingProblem(
                    selectedProblem.id
                );

            setProgress(
                response.data
            );

            await updateCodingSubmission(
                true
            );

            setShowSubmitResult(true);
            setShowRunResult(false);

        } catch (requestError) {

            console.error(
                "Failed to submit coding problem",
                requestError
            );

            try {

                await updateCodingSubmission(
                    false
                );

            } catch (submissionError) {

                console.error(
                    "Failed to update submission",
                    submissionError
                );

            }

        }

    };

    const handleOpenProblemMenu = () => {

        setShowProblemMenu(
            (previous) => !previous
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
                            !hasSelectedProblem
                        }
                        onClick={
                            handleRun
                        }
                    >

                        <FiPlay />

                        Run

                    </button>

                    <button
                        className="submit-btn"
                        type="button"
                        disabled={
                            !hasSelectedProblem
                        }
                        onClick={
                            handleSubmit
                        }
                    >

                        Submit

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
                                        selectedProblem.tags
                                            .map(
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
                                    placeholder="Search language..."
                                    className="language-search"
                                    onFocus={() => {

                                        setShowLanguages(
                                            true
                                        );

                                        setSelectedIndex(
                                            0
                                        );

                                    }}
                                    onClick={() =>
                                        setShowLanguages(
                                            true
                                        )
                                    }
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
                                                (
                                                    previous
                                                ) => {

                                                    const nextIndex =
                                                        previous <
                                                        filteredLanguages.length -
                                                            1
                                                            ? previous +
                                                              1
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
                                                (
                                                    previous
                                                ) => {

                                                    const nextIndex =
                                                        previous >
                                                        0
                                                            ? previous -
                                                              1
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

                                            if (
                                                selected
                                            ) {

                                                handleLanguageChange(
                                                    selected.value
                                                );

                                            }

                                        }

                                    }}
                                />

                                <FiChevronDown
                                    className="dropdown-arrow"
                                    onClick={() =>
                                        setShowLanguages(
                                            !showLanguages
                                        )
                                    }
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
                                                                        Boilerplate snippet
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

                            <div className="ai-hint">

                                <FiCpu />

                                AI Hint

                            </div>

                        </div>

                        <div className="monaco-wrapper">

                            <Editor
                                height="100%"
                                theme="vs-dark"
                                language={
                                    language
                                }
                                value={
                                    hasSelectedProblem
                                        ? (
                                            codeMap[
                                                language
                                            ] ||
                                            getStarterCode(
                                                selectedProblem,
                                                language
                                            )
                                        )
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
                                        !hasSelectedProblem
                                }}
                            />

                        </div>

                    </div>

                    <div className="testcase-card">

                        <div className="testcase-top">

                            <h3>
                                Test cases
                            </h3>

                            <span>
                                {
                                    hasSelectedProblem
                                        ? "Ready"
                                        : "Select a problem"
                                }
                            </span>

                        </div>

                        {
                            selectedProblem ? (

                                <>

                                    <div className="case-item">

                                        <p>

                                            Input: {
                                                selectedProblem.inputExample ||
                                                "Sample input"
                                            }

                                        </p>

                                        <span>
                                            ✓
                                        </span>

                                    </div>

                                    <div className="case-item">

                                        <p>

                                            Expected: {
                                                selectedProblem.outputExample ||
                                                "Sample output"
                                            }

                                        </p>

                                        <span>
                                            ✓
                                        </span>

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
                        showRunResult && (

                            <div className="run-result-card">

                                <div className="run-top">

                                    <span>
                                        Runtime
                                    </span>

                                    <span>
                                        68 ms
                                    </span>

                                </div>

                                <div className="run-progress">

                                    <div className="run-fill"></div>

                                </div>

                                <p>
                                    All test cases passed successfully.
                                </p>

                            </div>

                        )
                    }

                    {
                        showSubmitResult && (

                            <div className="accepted-card">

                                <div className="accepted-icon">
                                    ✓
                                </div>

                                <h2>
                                    Accepted
                                </h2>

                                <p>
                                    Submission saved successfully.
                                </p>

                            </div>

                        )
                    }

                </div>

            </div>

        </section>

    );

}