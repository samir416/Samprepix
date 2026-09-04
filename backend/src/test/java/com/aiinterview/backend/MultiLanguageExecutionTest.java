package com.aiinterview.backend;

import com.aiinterview.backend.config.CentralLanguageRegistry;
import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.service.coding.CodeExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultiLanguageExecutionTest {

    @Autowired
    private CodingProblemRepository codingProblemRepository;

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Test
    void testAllExposedLanguagesRunSuccessfully() {
        CodingProblem problem = codingProblemRepository.findBySourceId("dsa-0001")
                .orElseGet(() -> codingProblemRepository.findAll().get(15));

        assertNotNull(problem, "Test problem must exist");

        // Solutions for each language that read input and print "3" for test case 1
        Map<String, String> languagePrograms = new LinkedHashMap<>();

        // 1. Python
        languagePrograms.put("python", """
                import sys
                lines = sys.stdin.read().strip().split()
                if lines:
                    print(3)
                """);

        // 2. Java
        languagePrograms.put("java", """
                import java.io.*;
                public class Solution {
                    public static void main(String[] args) throws Exception {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String line = br.readLine();
                        System.out.println(3);
                    }
                }
                """);

        // 3. C++
        languagePrograms.put("cpp", """
                #include <iostream>
                #include <string>
                using namespace std;
                int main() {
                    string s;
                    if (cin >> s) {
                        cout << 3 << endl;
                    }
                    return 0;
                }
                """);

        // 4. JavaScript
        languagePrograms.put("javascript", """
                const fs = require('fs');
                const input = fs.readFileSync(0, 'utf-8').trim();
                if (input) {
                    console.log(3);
                }
                """);

        // 5. TypeScript
        languagePrograms.put("typescript", """
                declare var require: any;
                const fs = require('fs');
                const input: string = fs.readFileSync(0, 'utf-8').trim();
                if (input) {
                    console.log(3);
                }
                """);

        // 6. C
        languagePrograms.put("c", """
                #include <stdio.h>
                int main() {
                    char s[100];
                    if (scanf("%s", s) == 1) {
                        printf("3\\n");
                    }
                    return 0;
                }
                """);

        // 7. C#
        languagePrograms.put("csharp", """
                using System;
                class Program {
                    static void Main() {
                        string s = Console.ReadLine();
                        if (s != null) {
                            Console.WriteLine(3);
                        }
                    }
                }
                """);

        // 8. Go
        languagePrograms.put("go", """
                package main
                import "fmt"
                func main() {
                    var s string
                    if fmt.Scan(&s); len(s) > 0 {
                        fmt.Println(3)
                    }
                }
                """);

        // 9. Rust
        languagePrograms.put("rust", """
                use std::io::{self, Read};
                fn main() {
                    let mut s = String::new();
                    if io::stdin().read_to_string(&mut s).is_ok() {
                        println!("3");
                    }
                }
                """);

        // 10. Swift
        languagePrograms.put("swift", """
                if let s = readLine() {
                    print(3)
                }
                """);

        // 11. PHP
        languagePrograms.put("php", """
                <?php
                $s = trim(fgets(STDIN));
                if (!empty($s)) {
                    echo "3\\n";
                }
                """);

        // 12. Ruby
        languagePrograms.put("ruby", """
                s = gets
                if s
                    puts 3
                end
                """);

        // 13. Scala
        languagePrograms.put("scala", """
                import scala.io.StdIn.readLine
                @main def run() = {
                    val s = readLine()
                    if (s != null) println(3)
                }
                """);

        // 14. Lua
        languagePrograms.put("lua", """
                local s = io.read("*line")
                if s then
                    print(3)
                end
                """);

        // 15. Bash
        languagePrograms.put("bash", """
                read s
                echo 3
                """);

        // 16. Elixir
        languagePrograms.put("elixir", """
                defmodule Solution do
                  def solve do
                    _ = IO.read(:stdio, :line)
                    IO.puts(3)
                  end
                end
                Solution.solve()
                """);

        // 17. Erlang
        languagePrograms.put("erlang", """
                -module(solution).
                -export([main/1]).
                main(_) ->
                    _ = io:get_line(""),
                    io:format("3~n").
                """);

        // 18. Perl
        languagePrograms.put("perl", """
                my $s = <STDIN>;
                print "3\\n";
                """);

        // 19. Haskell
        languagePrograms.put("haskell", """
                main :: IO ()
                main = do
                    _ <- getLine
                    putStrLn "3"
                """);

        // 20. Dart
        languagePrograms.put("dart", """
                import 'dart:io';
                void main() {
                    String? line = stdin.readLineSync();
                    print(3);
                }
                """);

        // 21. Racket
        languagePrograms.put("racket", """
                #lang racket
                (define line (read-line))
                (displayln 3)
                """);

        // 22. R
        languagePrograms.put("r", """
                f <- file("stdin")
                open(f)
                lines <- readLines(f, n = 1)
                cat(3, "\\n", sep="")
                """);

        // 23. Groovy
        languagePrograms.put("groovy", """
                def reader = System.in.newReader()
                def line = reader.readLine()
                println(3)
                """);


        // 25. Julia
        languagePrograms.put("julia", """
                line = readline()
                println(3)
                """);

        // 24. D
        languagePrograms.put("d", """
                import std.stdio;
                void main() {
                    string line = readln();
                    writeln(3);
                }
                """);

        // 25. COBOL
        languagePrograms.put("cobol", """
               IDENTIFICATION DIVISION.
               PROGRAM-ID. SOLUTION.
               PROCEDURE DIVISION.
                   DISPLAY 3.
                   STOP RUN.
                """);

        // 26. OCaml
        languagePrograms.put("ocaml", """
                let () =
                  let _ = try read_line () with End_of_file -> "" in
                  print_endline "3"
                """);

        // 27. Nim
        languagePrograms.put("nim", """
                import std/rdstdin
                let line = readLine(stdin)
                echo 3
                """);

        // 28. Pascal
        languagePrograms.put("pascal", """
                program Solution;
                var line: string;
                begin
                  readln(line);
                  writeln(3);
                end.
                """);

        // 29. Raku
        languagePrograms.put("raku", """
                my $line = lines[0];
                say 3;
                """);

        // 30. V
        languagePrograms.put("vlang", """
                import os
                fn main() {
                    _ := os.get_line()
                    println(3)
                }
                """);

        System.out.println("=== Starting Execution Test for " + languagePrograms.size() + " Languages ===");

        for (Map.Entry<String, String> entry : languagePrograms.entrySet()) {
            String lang = entry.getKey();
            String code = entry.getValue();

            CodeExecutionResponse response = codeExecutionService.execute(
                    CodeExecutionRequest.builder()
                            .problemId(problem.getId())
                            .language(lang)
                            .code(code)
                            .build()
            );

            assertNotNull(response, "Response must not be null for " + lang);
            assertNotEquals("COMPILATION_ERROR", response.getStatus(),
                    "Language " + lang + " failed compilation: " + response.getError());
            assertNotEquals("RUNTIME_ERROR", response.getStatus(),
                    "Language " + lang + " failed with runtime error: " + response.getError());

            System.out.printf("Language %-12s: status=%-10s passedTests=%d/%d%n",
                    lang, response.getStatus(), response.getPassedTests(), response.getTotalTests());

            // Check that at least test case 1 passed (which expected "3")
            assertTrue(response.getPassedTests() >= 1,
                    "Language " + lang + " should have at least 1 test passed, but got: " + response.getPassedTests()
                            + " with error: " + response.getError());
        }

        System.out.println("=== All " + languagePrograms.size() + " Languages Executed Successfully! ===");
    }

    @Test
    void testCompileErrorHandling() {
        CodingProblem problem = codingProblemRepository.findBySourceId("dsa-0001")
                .orElseGet(() -> codingProblemRepository.findAll().get(15));

        assertNotNull(problem, "Test problem must exist");

        // Broken C++ code that cannot compile
        String invalidCppCode = """
                #include <iostream>
                int main() {
                    this_is_an_intentional_syntax_error_12345;
                    return 0;
                }
                """;

        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(problem.getId())
                        .language("cpp")
                        .code(invalidCppCode)
                        .build()
        );

        assertNotNull(response, "Response must not be null");
        assertEquals("COMPILE_ERROR", response.getStatus(), "Status must be COMPILE_ERROR");
        assertFalse(response.isPassed(), "Compilation failure must not pass");
        assertNotNull(response.getError(), "Error must be populated for compile error");
        assertTrue(response.getError().contains("this_is_an_intentional_syntax_error_12345")
                || response.getError().toLowerCase().contains("error"),
                "Error output should contain compiler diagnostic message");
    }

    @Test
    void testRuntimeErrorHandling() {
        CodingProblem problem = codingProblemRepository.findBySourceId("dsa-0001")
                .orElseGet(() -> codingProblemRepository.findAll().get(15));

        assertNotNull(problem, "Test problem must exist");

        // Python code that crashes at runtime with ZeroDivisionError
        String crashingPythonCode = """
                import sys
                x = 1 / 0
                """;

        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(problem.getId())
                        .language("python")
                        .code(crashingPythonCode)
                        .build()
        );

        assertNotNull(response, "Response must not be null");
        assertEquals("RUNTIME_ERROR", response.getStatus(), "Status must be RUNTIME_ERROR");
        assertFalse(response.isPassed(), "Runtime failure must not pass");
        assertNotNull(response.getError(), "Error must be populated for runtime error");
        assertTrue(response.getError().contains("ZeroDivisionError"),
                "Error output should mention ZeroDivisionError: " + response.getError());
    }

    @Test
    void testHiddenTestPrivacyInExecutionResponse() {
        CodingProblem problem = codingProblemRepository.findBySourceId("dsa-0001")
                .orElseGet(() -> codingProblemRepository.findAll().get(15));

        assertNotNull(problem, "Test problem must exist");

        // Execute code
        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(problem.getId())
                        .language("python")
                        .code("print(3)")
                        .build()
        );

        assertNotNull(response);
        assertNotNull(response.getTestCases(), "Test cases list must be returned");

        // Verify that any hidden test cases in the response do not expose input or expected output
        for (var tc : response.getTestCases()) {
            if (tc.getInput() == null) {
                // If input is null, it means it's a hidden test case protected by visibleValue()
                assertNull(tc.getExpectedOutput(), "Hidden test case expectedOutput must be null");
                assertNull(tc.getActualOutput(), "Hidden test case actualOutput must be null");
            }
        }
    }
}

