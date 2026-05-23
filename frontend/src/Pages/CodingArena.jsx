import "../styles/codingarena.css";

import { useState, useRef, useEffect } from "react";

import Editor from "@monaco-editor/react";

import {
    FiPlay,
    FiCpu,
    FiChevronDown,
    FiSearch
} from "react-icons/fi";

export default function CodingArena() {

    /* =========================
       BOILERPLATES
    ========================= */

    const starterCodes = {

        javascript:
            `function solve() {

    

}`,

        typescript:
            `function solve(): void {

    

}`,

        python:
            `def solve():

    pass`,

        java:
            `class Solution {

    public static void main(String[] args) {

        

    }
}`,

        cpp:
            `#include <iostream>
using namespace std;

int main() {

    

    return 0;
}`,

        c:
            `#include <stdio.h>

int main() {

    

    return 0;
}`,

        go:
            `package main

func main() {

    

}`,

        rust:
            `fn main() {

    

}`,

        php:
            `<?php

echo "Hello World";

?>`,

        kotlin:
            `fun main() {

    

}`
    };

    /* =========================
       LANGUAGES
    ========================= */

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

    /* =========================
       STATES
    ========================= */

    const [language, setLanguage] =
        useState("javascript");

    const [searchLanguage, setSearchLanguage] =
        useState("JavaScript");

    const [showLanguages, setShowLanguages] =
        useState(false);

    const [selectedIndex, setSelectedIndex] =
        useState(0);

    const [codeMap, setCodeMap] =
        useState(starterCodes);

    const [showRunResult, setShowRunResult] =
        useState(false);

    const [showSubmitResult, setShowSubmitResult] =
        useState(false);

    const dropdownRef = useRef(null);

    const optionRefs = useRef([]);
    /* =========================
       OUTSIDE CLICK
    ========================= */

    useEffect(() => {

        const handleOutsideClick = (event) => {

            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(event.target)
            ) {

                setShowLanguages(false);
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

    /* =========================
       CODE CHANGE
    ========================= */

    const handleCodeChange = (value) => {

        setCodeMap({

            ...codeMap,

            [language]: value
        });
    };

    /* =========================
       FILTER
    ========================= */

    const filteredLanguages =
        languages.filter((item) =>
            item.label
                .toLowerCase()
                .includes(
                    searchLanguage.toLowerCase()
                )
        );

    return (

        <section className="coding-page">

            {/* TOPBAR */}

            <div className="coding-topbar">

                <div className="problem-head">

                    <span>
                        PROBLEM · EASY
                    </span>

                    <h1>
                        1. Two Sum
                    </h1>

                </div>

                {/* ACTIONS */}

                <div className="coding-actions">

                    {/* RUN */}

                    <button
                        className="run-btn"
                        onClick={() => {

                            setShowRunResult(true);

                            setShowSubmitResult(false);
                        }}
                    >

                        <FiPlay />

                        Run

                    </button>

                    {/* SUBMIT */}

                    <button
                        className="submit-btn"
                        onClick={() => {

                            setShowSubmitResult(true);

                            setShowRunResult(false);
                        }}
                    >

                        Submit

                    </button>

                </div>

            </div>

            {/* GRID */}

            <div className="coding-grid">

                {/* LEFT */}

                <div className="problem-card">

                    <div className="problem-tags">

                        <span className="tag-green">
                            Array
                        </span>

                        <span className="tag-blue">
                            Hash Map
                        </span>

                        <div className="problem-time">

                            ⏱ 15:24

                        </div>

                    </div>

                    {/* DESCRIPTION */}

                    <div className="problem-description">

                        <p>

                            Given an array of integers
                            <code> nums </code>
                            and an integer
                            <code> target </code>,
                            return indices of the two numbers such that they add up to target.

                        </p>

                    </div>

                    {/* EXAMPLE */}

                    <div className="example-box">

                        <p>

                            <strong>
                                Input:
                            </strong>

                            nums = [2,7,11,15], target = 9

                        </p>

                        <p>

                            <strong>
                                Output:
                            </strong>

                            [0,1]

                        </p>

                    </div>

                    {/* CONSTRAINTS */}

                    <div className="constraints-box">

                        <h3>
                            Constraints
                        </h3>

                        <ul>

                            <li>
                                2 ≤ nums.length ≤ 10⁴
                            </li>

                            <li>
                                −10⁹ ≤ nums[i] ≤ 10⁹
                            </li>

                            <li>
                                Only one valid answer exists.
                            </li>

                        </ul>

                    </div>

                </div>

                {/* RIGHT */}

                <div className="editor-column">

                    {/* EDITOR */}

                    <div className="editor-card">

                        {/* HEADER */}

                        <div className="editor-header">

                            {/* DOTS */}

                            <div className="editor-dots">

                                <span></span>
                                <span></span>
                                <span></span>

                            </div>

                            {/* LANGUAGE SWITCHER */}

                            <div
                                className="language-search-wrapper"
                                ref={dropdownRef}
                            >

                                <FiSearch className="search-icon" />

                                <input
                                    type="text"
                                    value={searchLanguage}
                                    placeholder="Search language..."
                                    className="language-search"
                                    onFocus={() => {

                                        setShowLanguages(true);

                                        setSelectedIndex(0);
                                    }}
                                    onClick={() =>
                                        setShowLanguages(true)
                                    }
                                    onChange={(e) => {

                                        setSearchLanguage(
                                            e.target.value
                                        );

                                        setShowLanguages(true);

                                        setSelectedIndex(0);
                                    }}
                                    onKeyDown={(e) => {

                                        if (e.key === "ArrowDown") {

                                            e.preventDefault();

                                            setSelectedIndex((prev) => {

                                                const nextIndex =

                                                    prev < filteredLanguages.length - 1
                                                        ? prev + 1
                                                        : prev;

                                                optionRefs.current[nextIndex]
                                                    ?.scrollIntoView({

                                                        block: "nearest",
                                                        behavior: "smooth"
                                                    });

                                                return nextIndex;
                                            });
                                        }

                                        if (e.key === "ArrowUp") {

                                            e.preventDefault();

                                            setSelectedIndex((prev) => {

                                                const nextIndex =

                                                    prev > 0
                                                        ? prev - 1
                                                        : 0;

                                                optionRefs.current[nextIndex]
                                                    ?.scrollIntoView({

                                                        block: "nearest",
                                                        behavior: "smooth"
                                                    });

                                                return nextIndex;
                                            });
                                        }

                                        if (e.key === "Enter") {

                                            e.preventDefault();

                                            const selected =
                                                filteredLanguages[selectedIndex];

                                            if (selected) {

                                                setLanguage(selected.value);

                                                setSearchLanguage(selected.label);

                                                setShowLanguages(false);
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

                                                filteredLanguages.length > 0

                                                    ? (

                                                        filteredLanguages.map((item, index) => (

                                                            <div
                                                                key={item.value}
                                                                ref={(el) =>
                                                                    optionRefs.current[index] = el
                                                                }
                                                                className={
                                                                    index === selectedIndex
                                                                        ? "language-item active-language"
                                                                        : "language-item"
                                                                }
                                                                onMouseEnter={() =>
                                                                    setSelectedIndex(index)
                                                                }
                                                                onClick={() => {

                                                                    setLanguage(item.value);

                                                                    setSearchLanguage(item.label);

                                                                    setShowLanguages(false);
                                                                }}
                                                            >

                                                                <div>

                                                                    <h4>
                                                                        {item.label}
                                                                    </h4>

                                                                    <p>
                                                                        Boilerplate snippet
                                                                    </p>

                                                                </div>

                                                                <span>
                                                                    {item.value}
                                                                </span>

                                                            </div>

                                                        ))

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

                            {/* AI */}

                            <div className="ai-hint">

                                <FiCpu />

                                AI Hint

                            </div>

                        </div>

                        {/* MONACO */}

                        <div className="monaco-wrapper">

                            <Editor
                                height="100%"
                                theme="vs-dark"
                                language={language}
                                value={codeMap[language]}
                                onChange={handleCodeChange}
                                options={{
                                    fontSize: 13,
                                    minimap: {
                                        enabled: false
                                    },
                                    scrollBeyondLastLine: false,
                                    automaticLayout: true
                                }}
                            />

                        </div>

                    </div>

                    {/* TEST CASES */}

                    <div className="testcase-card">

                        <div className="testcase-top">

                            <h3>
                                Test cases
                            </h3>

                            <span>
                                💾 42 MB · 68 ms
                            </span>

                        </div>

                        <div className="case-item">

                            <p>
                                case 1: [2,7,11,15], 9 → [0,1]
                            </p>

                            <span>
                                ✓
                            </span>

                        </div>

                        <div className="case-item">

                            <p>
                                case 2: [3,2,4], 6 → [1,2]
                            </p>

                            <span>
                                ✓
                            </span>

                        </div>

                        <div className="case-item">

                            <p>
                                case 3: [3,3], 6 → [0,1]
                            </p>

                            <span>
                                ✓
                            </span>

                        </div>

                    </div>

                    {/* RUN RESULT */}

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

                    {/* SUBMIT RESULT */}

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
                                    Beats 96% on runtime · 89% on memory
                                </p>

                            </div>
                        )
                    }

                </div>

            </div>

        </section>
    );
}