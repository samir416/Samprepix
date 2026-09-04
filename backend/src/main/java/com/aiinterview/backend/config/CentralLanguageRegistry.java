package com.aiinterview.backend.config;

import java.util.*;

/**
 * Central Language Registry defining all supported programming languages,
 * their Piston runtime identifiers, versions, Monaco IDs, and idiomatic starter code.
 */
public final class CentralLanguageRegistry {

    public record LanguageSpec(
            String key,
            String displayName,
            String runtimeLanguage,
            String runtimeVersion,
            String monacoLanguage,
            String fileExtension,
            String fileName,
            String icon,
            boolean popular,
            boolean enabled,
            String executionMode,
            String stdinBehavior,
            String outputBehavior,
            String starterCode
    ) {}

    private static final Map<String, LanguageSpec> REGISTRY = new LinkedHashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        // ==========================================
        // POPULAR LANGUAGES
        // ==========================================

        // 1. Python (Popular)
        register(new LanguageSpec(
                "python",
                "Python",
                "python",
                "3.12.0",
                "python",
                ".py",
                "main.py",
                "python",
                true,
                true,
                "standard",
                "Standard input stream (sys.stdin)",
                "Standard output stream (print)",
                """
                import sys

                def main():
                    input_data = sys.stdin.read().split()
                    if not input_data:
                        return
                    # Write your solution here


                if __name__ == '__main__':
                    main()
                """
        ), "py", "python3");

        // 2. Java (Popular)
        register(new LanguageSpec(
                "java",
                "Java",
                "java",
                "15.0.2",
                "java",
                ".java",
                "Main.java",
                "java",
                true,
                true,
                "standard",
                "Standard input stream (System.in)",
                "Standard output stream (System.out)",
                """
                import java.util.*;
                import java.io.*;

                public class Main {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        // Write your solution here

                    }
                }
                """
        ));

        // 3. C++ (Popular)
        register(new LanguageSpec(
                "cpp",
                "C++",
                "c++",
                "10.2.0",
                "cpp",
                ".cpp",
                "main.cpp",
                "cpp",
                true,
                true,
                "standard",
                "Standard input stream (std::cin)",
                "Standard output stream (std::cout)",
                """
                #include <iostream>
                #include <vector>
                #include <string>
                #include <algorithm>

                using namespace std;

                int main() {
                    ios_base::sync_with_stdio(false);
                    cin.tie(NULL);

                    // Write your solution here

                    return 0;
                }
                """
        ), "c++", "g++");

        // 4. JavaScript (Popular)
        register(new LanguageSpec(
                "javascript",
                "JavaScript",
                "javascript",
                "20.11.1",
                "javascript",
                ".js",
                "main.js",
                "javascript",
                true,
                true,
                "standard",
                "Standard input stream (fs.readFileSync(0))",
                "Standard output stream (console.log)",
                """
                const fs = require('fs');

                function solve() {
                    const input = fs.readFileSync(0, 'utf-8').trim();
                    if (!input) return;

                    // Write your solution here

                }

                solve();
                """
        ), "js", "node", "nodejs");

        // 5. TypeScript (Popular)
        register(new LanguageSpec(
                "typescript",
                "TypeScript",
                "typescript",
                "5.0.3",
                "typescript",
                ".ts",
                "main.ts",
                "typescript",
                true,
                true,
                "standard",
                "Standard input stream (fs.readFileSync(0))",
                "Standard output stream (console.log)",
                """
                declare var require: any;
                const fs = require('fs');

                function solve(): void {
                    const input: string = fs.readFileSync(0, 'utf-8').trim();
                    if (!input) return;

                    // Write your solution here

                }

                solve();
                """
        ), "ts");

        // ==========================================
        // MORE LANGUAGES
        // ==========================================

        // 6. C
        register(new LanguageSpec(
                "c",
                "C",
                "c",
                "10.2.0",
                "c",
                ".c",
                "main.c",
                "c",
                false,
                true,
                "standard",
                "Standard input stream (scanf / fgets)",
                "Standard output stream (printf)",
                """
                #include <stdio.h>
                #include <stdlib.h>
                #include <string.h>

                int main() {
                    // Write your solution here

                    return 0;
                }
                """
        ), "gcc");

        // 7. C# (.NET)
        register(new LanguageSpec(
                "csharp",
                "C#",
                "csharp.net",
                "5.0.201",
                "csharp",
                ".cs",
                "Program.cs",
                "csharp",
                false,
                true,
                "standard",
                "Standard input stream (Console.ReadLine)",
                "Standard output stream (Console.WriteLine)",
                """
                using System;

                class Program {
                    static void Main(string[] args) {
                        // Write your solution here

                    }
                }
                """
        ), "c#", "cs", "dotnet", "csharp.net");

        // 8. Go
        register(new LanguageSpec(
                "go",
                "Go",
                "go",
                "1.16.2",
                "go",
                ".go",
                "main.go",
                "go",
                false,
                true,
                "standard",
                "Standard input stream (bufio.Reader)",
                "Standard output stream (fmt.Println)",
                """
                package main

                import (
                    "bufio"
                    "fmt"
                    "os"
                )

                func main() {
                    reader := bufio.NewReader(os.Stdin)
                    _ = reader
                    _ = fmt.Println
                    // Write your solution here

                }
                """
        ), "golang");

        // 9. Rust
        register(new LanguageSpec(
                "rust",
                "Rust",
                "rust",
                "1.68.2",
                "rust",
                ".rs",
                "main.rs",
                "rust",
                false,
                true,
                "standard",
                "Standard input stream (io::stdin)",
                "Standard output stream (println!)",
                """
                use std::io::{self, Read};

                fn main() {
                    let mut input = String::new();
                    io::stdin().read_to_string(&mut input).unwrap();

                    // Write your solution here

                }
                """
        ), "rs");

        // 10. Swift
        register(new LanguageSpec(
                "swift",
                "Swift",
                "swift",
                "5.3.3",
                "swift",
                ".swift",
                "main.swift",
                "swift",
                false,
                true,
                "standard",
                "Standard input stream (readLine)",
                "Standard output stream (print)",
                """
                import Foundation

                // Write your solution here

                """
        ));

        // 11. PHP
        register(new LanguageSpec(
                "php",
                "PHP",
                "php",
                "8.2.3",
                "php",
                ".php",
                "main.php",
                "php",
                false,
                true,
                "standard",
                "Standard input stream (fgets(STDIN))",
                "Standard output stream (echo)",
                """
                <?php
                // Write your solution here

                """
        ));

        // 12. Ruby
        register(new LanguageSpec(
                "ruby",
                "Ruby",
                "ruby",
                "3.0.1",
                "ruby",
                ".rb",
                "main.rb",
                "ruby",
                false,
                true,
                "standard",
                "Standard input stream (gets)",
                "Standard output stream (puts)",
                """
                # Write your solution here

                """
        ), "rb");

        // 13. Scala
        register(new LanguageSpec(
                "scala",
                "Scala",
                "scala",
                "3.2.2",
                "scala",
                ".scala",
                "Main.scala",
                "scala",
                false,
                true,
                "standard",
                "Standard input stream (scala.io.StdIn.readLine)",
                "Standard output stream (println)",
                """
                import scala.io.StdIn.readLine

                @main def run() = {
                    // Write your solution here

                }
                """
        ), "sc");

        // 14. Bash
        register(new LanguageSpec(
                "bash",
                "Bash",
                "bash",
                "5.2.0",
                "shell",
                ".sh",
                "main.sh",
                "bash",
                false,
                true,
                "standard",
                "Standard input stream (read)",
                "Standard output stream (echo)",
                """
                #!/bin/bash
                # Write your solution here

                """
        ), "sh", "shell");

        // 15. Lua
        register(new LanguageSpec(
                "lua",
                "Lua",
                "lua",
                "5.4.4",
                "lua",
                ".lua",
                "main.lua",
                "lua",
                false,
                true,
                "standard",
                "Standard input stream (io.read)",
                "Standard output stream (print)",
                """
                -- Write your solution here

                """
        ));

        // 16. Elixir
        register(new LanguageSpec(
                "elixir",
                "Elixir",
                "elixir",
                "1.11.3",
                "elixir",
                ".exs",
                "solution.exs",
                "elixir",
                false,
                true,
                "standard",
                "Standard input stream (IO.read(:stdio, :line))",
                "Standard output stream (IO.puts)",
                """
                defmodule Solution do
                  def solve do
                    # Write your solution here

                  end
                end

                Solution.solve()
                """
        ), "exs");

        // 17. Erlang
        register(new LanguageSpec(
                "erlang",
                "Erlang",
                "erlang",
                "23.0.0",
                "erlang",
                ".erl",
                "solution.erl",
                "erlang",
                false,
                true,
                "standard",
                "Standard input stream (io:get_line)",
                "Standard output stream (io:format)",
                """
                -module(solution).
                -export([main/1]).

                main(_) ->
                    % Write your solution here
                    ok.
                """
        ), "erl", "escript");

        // 18. Perl
        register(new LanguageSpec(
                "perl",
                "Perl",
                "perl",
                "5.36.0",
                "perl",
                ".pl",
                "solution.pl",
                "perl",
                false,
                true,
                "standard",
                "Standard input stream (<STDIN>)",
                "Standard output stream (print)",
                """
                use strict;
                use warnings;

                # Write your solution here

                """
        ), "pl");

        // 19. Haskell
        register(new LanguageSpec(
                "haskell",
                "Haskell",
                "haskell",
                "9.0.1",
                "haskell",
                ".hs",
                "Solution.hs",
                "haskell",
                false,
                true,
                "standard",
                "Standard input stream (getLine)",
                "Standard output stream (putStrLn)",
                """
                main :: IO ()
                main = do
                    -- Write your solution here
                    return ()
                """
        ), "hs");

        // 20. Dart
        register(new LanguageSpec(
                "dart",
                "Dart",
                "dart",
                "3.0.1",
                "dart",
                ".dart",
                "main.dart",
                "dart",
                false,
                true,
                "standard",
                "Standard input stream (stdin.readLineSync)",
                "Standard output stream (print)",
                """
                import 'dart:io';

                void main() {
                  String? input = stdin.readLineSync();
                  // Write your solution here

                }
                """
        ));

        // 21. Racket
        register(new LanguageSpec(
                "racket",
                "Racket",
                "racket",
                "8.3.0",
                "scheme",
                ".rkt",
                "main.rkt",
                "racket",
                false,
                true,
                "standard",
                "Standard input stream (read-line)",
                "Standard output stream (displayln)",
                """
                #lang racket

                (define line (read-line))
                ; Write your solution here

                """
        ), "rkt");

        // 22. R
        register(new LanguageSpec(
                "r",
                "R",
                "rscript",
                "4.1.1",
                "r",
                ".r",
                "main.r",
                "r",
                false,
                true,
                "standard",
                "Standard input stream (readLines)",
                "Standard output stream (cat)",
                """
                f <- file("stdin")
                open(f)
                lines <- readLines(f, n = 1)
                # Write your solution here

                """
        ), "rscript");

        // 23. Groovy
        register(new LanguageSpec(
                "groovy",
                "Groovy",
                "groovy",
                "3.0.7",
                "java",
                ".groovy",
                "main.groovy",
                "groovy",
                false,
                true,
                "standard",
                "Standard input stream (System.in.newReader)",
                "Standard output stream (println)",
                """
                def reader = System.in.newReader()
                def line = reader.readLine()
                // Write your solution here

                """
        ), "gvy");


        // 25. Julia
        register(new LanguageSpec(
                "julia",
                "Julia",
                "julia",
                "1.8.5",
                "julia",
                ".jl",
                "main.jl",
                "julia",
                false,
                true,
                "standard",
                "Standard input stream (readline)",
                "Standard output stream (println)",
                """
                line = readline()
                # Write your solution here

                """
        ), "jl");

        // 26. D
        register(new LanguageSpec(
                "d",
                "D",
                "d",
                "10.2.0",
                "c",
                ".d",
                "main.d",
                "d",
                false,
                true,
                "standard",
                "Standard input stream (readln)",
                "Standard output stream (writeln)",
                """
                import std.stdio;

                void main() {
                    string line = readln();
                    // Write your solution here

                }
                """
        ), "dlang", "gdc");

        // 27. COBOL
        register(new LanguageSpec(
                "cobol",
                "COBOL",
                "cobol",
                "3.1.2",
                "cobol",
                ".cob",
                "main.cob",
                "cobol",
                false,
                true,
                "standard",
                "Standard input stream (ACCEPT)",
                "Standard output stream (DISPLAY)",
                """
                       IDENTIFICATION DIVISION.
                       PROGRAM-ID. SOLUTION.
                       DATA DIVISION.
                       WORKING-STORAGE SECTION.
                       01 INPUT-BUFFER PIC X(100).
                       PROCEDURE DIVISION.
                           ACCEPT INPUT-BUFFER.
                           * Write your solution here
                           STOP RUN.
                """
        ), "cob");

        // 28. OCaml
        register(new LanguageSpec(
                "ocaml",
                "OCaml",
                "ocaml",
                "4.12.0",
                "ocaml",
                ".ml",
                "main.ml",
                "ocaml",
                false,
                true,
                "standard",
                "Standard input stream (read_line)",
                "Standard output stream (print_endline)",
                """
                let () =
                  let line = try read_line () with End_of_file -> "" in
                  (* Write your solution here *)
                  ()
                """
        ), "ml");

        // 29. Nim
        register(new LanguageSpec(
                "nim",
                "Nim",
                "nim",
                "1.6.2",
                "python",
                ".nim",
                "main.nim",
                "nim",
                false,
                true,
                "standard",
                "Standard input stream (readLine)",
                "Standard output stream (echo)",
                """
                import std/rdstdin

                let line = readLine(stdin)
                # Write your solution here

                """
        ));

        // 30. Pascal
        register(new LanguageSpec(
                "pascal",
                "Pascal",
                "pascal",
                "3.2.2",
                "pascal",
                ".pas",
                "main.pas",
                "pascal",
                false,
                true,
                "standard",
                "Standard input stream (readln)",
                "Standard output stream (writeln)",
                """
                program Solution;
                var line: string;
                begin
                  readln(line);
                  { Write your solution here }
                end.
                """
        ), "pas");

        // 31. Raku
        register(new LanguageSpec(
                "raku",
                "Raku",
                "raku",
                "6.100.0",
                "perl",
                ".raku",
                "main.raku",
                "raku",
                false,
                true,
                "standard",
                "Standard input stream (lines[0])",
                "Standard output stream (say)",
                """
                my $line = lines[0];
                # Write your solution here

                """
        ), "perl6", "p6");

        // 32. V
        register(new LanguageSpec(
                "vlang",
                "V",
                "vlang",
                "0.3.3",
                "go",
                ".v",
                "main.v",
                "v",
                false,
                true,
                "standard",
                "Standard input stream (os.get_line)",
                "Standard output stream (println)",
                """
                import os

                fn main() {
                    _ := os.get_line()
                    // Write your solution here

                }
                """
        ), "v");

        // 33. MySQL (Popular Database)
        register(new LanguageSpec(
                "mysql",
                "MySQL",
                "mysql",
                "8.0",
                "sql",
                ".sql",
                "solution.sql",
                "database",
                true,
                true,
                "database",
                "SQL Sandbox Table Setup",
                "Tabular Result Set",
                """
                -- Write your MySQL query statement below
                SELECT
                    *
                FROM
                    ;
                """
        ), "sql");

        // 34. Zig
        register(new LanguageSpec(
                "zig",
                "Zig",
                "zig",
                "0.10.1",
                "zig",
                ".zig",
                "main.zig",
                "zig",
                false,
                true,
                "standard",
                "Standard input stream",
                "Standard output stream (std.debug.print)",
                """
                const std = @import("std");

                pub fn main() !void {
                    // Write your solution here
                }
                """
        ));

        // 35. Fortran
        register(new LanguageSpec(
                "fortran",
                "Fortran",
                "fortran",
                "10.2.0",
                "fortran",
                ".f90",
                "main.f90",
                "fortran",
                false,
                true,
                "standard",
                "Standard input stream (read *)",
                "Standard output stream (print *)",
                """
                program main
                    implicit none
                    ! Write your solution here
                end program main
                """
        ));

        // 36. Prolog
        register(new LanguageSpec(
                "prolog",
                "Prolog",
                "prolog",
                "8.2.4",
                "prolog",
                ".pl",
                "main.pl",
                "prolog",
                false,
                true,
                "standard",
                "Standard input stream (read)",
                "Standard output stream (write)",
                """
                :- initialization(main).

                main :-
                    % Write your solution here
                    halt.
                """
        ));

        // 37. Visual Basic .NET
        register(new LanguageSpec(
                "vb",
                "Visual Basic",
                "basic.net",
                "5.0.201",
                "vb",
                ".vb",
                "Program.vb",
                "vb",
                false,
                true,
                "standard",
                "Standard input stream (Console.ReadLine)",
                "Standard output stream (Console.WriteLine)",
                """
                Imports System

                Module Program
                    Sub Main()
                        ' Write your solution here
                    End Sub
                End Module
                """
        ), "basic.net", "visualbasic");

        // 38. Clojure
        register(new LanguageSpec(
                "clojure",
                "Clojure",
                "clojure",
                "1.10.3",
                "clojure",
                ".clj",
                "main.clj",
                "clojure",
                false,
                true,
                "standard",
                "Standard input stream (read-line)",
                "Standard output stream (println)",
                """
                (ns main)

                ;; Write your solution here
                """
        ), "clj");

        // 39. Crystal
        register(new LanguageSpec(
                "crystal",
                "Crystal",
                "crystal",
                "1.9.2",
                "crystal",
                ".cr",
                "main.cr",
                "crystal",
                false,
                true,
                "standard",
                "Standard input stream (gets)",
                "Standard output stream (puts)",
                """
                # Write your solution here
                """
        ), "cr");

        // 40. Common Lisp
        register(new LanguageSpec(
                "lisp",
                "Common Lisp",
                "lisp",
                "2.1.2",
                "lisp",
                ".lisp",
                "main.lisp",
                "lisp",
                false,
                true,
                "standard",
                "Standard input stream (read-line)",
                "Standard output stream (format)",
                """
                ;; Write your solution here
                """
        ), "clisp");

        // 41. CoffeeScript
        register(new LanguageSpec(
                "coffeescript",
                "CoffeeScript",
                "coffeescript",
                "2.5.1",
                "coffeescript",
                ".coffee",
                "main.coffee",
                "coffeescript",
                false,
                true,
                "standard",
                "Standard input stream (process.stdin)",
                "Standard output stream (console.log)",
                """
                # Write your solution here
                """
        ), "coffee");

        // 42. GNU Octave
        register(new LanguageSpec(
                "octave",
                "GNU Octave",
                "octave",
                "8.1.0",
                "octave",
                ".m",
                "main.m",
                "octave",
                false,
                true,
                "standard",
                "Standard input stream (input)",
                "Standard output stream (disp)",
                """
                % Write your solution here
                """
        ), "matlab");

        // 43. PowerShell
        register(new LanguageSpec(
                "powershell",
                "PowerShell",
                "powershell",
                "7.1.4",
                "powershell",
                ".ps1",
                "main.ps1",
                "powershell",
                false,
                true,
                "standard",
                "Standard input stream (Read-Host)",
                "Standard output stream (Write-Output)",
                """
                # Write your solution here
                """
        ), "pwsh");

        // 44. Smalltalk
        register(new LanguageSpec(
                "smalltalk",
                "Smalltalk",
                "smalltalk",
                "3.2.3",
                "smalltalk",
                ".st",
                "main.st",
                "smalltalk",
                false,
                true,
                "standard",
                "Standard input stream",
                "Standard output stream (Transcript show)",
                """
                "Write your solution here"
                """
        ));

        // 45. SQLite
        register(new LanguageSpec(
                "sqlite3",
                "SQLite",
                "sqlite3",
                "3.36.0",
                "sql",
                ".sql",
                "main.sql",
                "database",
                false,
                true,
                "standard",
                "Standard SQL input",
                "Query results",
                """
                -- Write your SQLite query below
                """
        ), "sqlite");

        // 46. AWK
        register(new LanguageSpec(
                "awk",
                "AWK",
                "awk",
                "5.1.0",
                "awk",
                ".awk",
                "main.awk",
                "awk",
                false,
                true,
                "standard",
                "Standard line stream",
                "Standard output stream (print)",
                """
                BEGIN {
                    # Write your solution here
                }
                """
        ), "gawk");

        // 47. Dash
        register(new LanguageSpec(
                "dash",
                "Dash",
                "dash",
                "0.5.11",
                "shell",
                ".sh",
                "main.sh",
                "bash",
                false,
                true,
                "standard",
                "Standard input stream (read)",
                "Standard output stream (echo)",
                """
                #!/bin/dash
                # Write your solution here
                """
        ));

        // 48. FreeBASIC
        register(new LanguageSpec(
                "freebasic",
                "FreeBASIC",
                "freebasic",
                "1.8.0",
                "vb",
                ".bas",
                "main.bas",
                "vb",
                false,
                true,
                "standard",
                "Standard input stream (Input)",
                "Standard output stream (Print)",
                """
                ' Write your solution here
                """
        ));

        // 49. Forth
        register(new LanguageSpec(
                "forth",
                "Forth",
                "forth",
                "0.7.3",
                "forth",
                ".fs",
                "main.fs",
                "forth",
                false,
                true,
                "standard",
                "Standard stack operations",
                "Stack display",
                """
                \\ Write your solution here
                bye
                """
        ));

        // 50. Emacs Lisp
        register(new LanguageSpec(
                "emacs",
                "Emacs Lisp",
                "emacs",
                "27.1.0",
                "lisp",
                ".el",
                "main.el",
                "lisp",
                false,
                true,
                "standard",
                "Standard input stream",
                "Standard output stream (princ)",
                """
                ;; Write your solution here
                """
        ), "elisp");

        // 51. BQN
        register(new LanguageSpec(
                "bqn",
                "BQN",
                "bqn",
                "1.0.0",
                "plaintext",
                ".bqn",
                "main.bqn",
                "bqn",
                false,
                true,
                "standard",
                "Standard input stream",
                "Standard output stream (•Out)",
                """
                # Write your solution here
                """
        ));

        // 52. Dragon
        register(new LanguageSpec(
                "dragon",
                "Dragon",
                "dragon",
                "1.9.8",
                "plaintext",
                ".dgn",
                "main.dgn",
                "dragon",
                false,
                true,
                "standard",
                "Standard input stream",
                "Standard output stream (show)",
                """
                # Write your solution here
                """
        ));
    }

    private CentralLanguageRegistry() {}

    private static void register(LanguageSpec spec, String... aliases) {
        REGISTRY.put(spec.key().toLowerCase(), spec);
        ALIASES.put(spec.key().toLowerCase(), spec.key().toLowerCase());
        for (String alias : aliases) {
            ALIASES.put(alias.toLowerCase(), spec.key().toLowerCase());
        }
    }

    public static List<LanguageSpec> getAllLanguages() {
        return new ArrayList<>(REGISTRY.values());
    }

    public static List<LanguageSpec> getPopularLanguages() {
        return REGISTRY.values().stream()
                .filter(spec -> spec.popular() && !"database".equalsIgnoreCase(spec.executionMode()))
                .toList();
    }

    public static List<LanguageSpec> getMoreLanguages() {
        return REGISTRY.values().stream()
                .filter(spec -> !spec.popular() && !"database".equalsIgnoreCase(spec.executionMode()))
                .toList();
    }

    public static List<LanguageSpec> getDatabaseLanguages() {
        return REGISTRY.values().stream().filter(spec -> "database".equalsIgnoreCase(spec.executionMode())).toList();
    }

    public static List<LanguageSpec> getProgrammingLanguages() {
        return REGISTRY.values().stream().filter(spec -> !"database".equalsIgnoreCase(spec.executionMode())).toList();
    }

    public static LanguageSpec get(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }
        String normalized = language.trim().toLowerCase();
        String canonicalKey = ALIASES.getOrDefault(normalized, normalized);
        return REGISTRY.get(canonicalKey);
    }

    public static boolean isSupported(String language) {
        return get(language) != null;
    }

    public static Map<String, Object> toConfigurationMap(LanguageSpec spec) {
        if (spec == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", spec.key());
        map.put("displayName", spec.displayName());
        map.put("runtimeLanguage", spec.runtimeLanguage());
        map.put("runtimeVersion", spec.runtimeVersion());
        map.put("monacoLanguage", spec.monacoLanguage());
        map.put("fileExtension", spec.fileExtension());
        map.put("fileName", spec.fileName());
        map.put("icon", spec.icon());
        map.put("starterCode", spec.starterCode());
        map.put("executionMode", spec.executionMode());
        map.put("stdinBehavior", spec.stdinBehavior());
        map.put("outputBehavior", spec.outputBehavior());
        map.put("enabled", spec.enabled());
        map.put("popular", spec.popular());
        map.put("executionTemplate", "{{USER_CODE}}");
        return map;
    }
}
