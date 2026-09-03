# Coding Problem Dataset Import

The optional importer reads UTF-8 JSON Lines when `coding.problems.import-resource` is configured, for example:

`coding.problems.import-resource=classpath:coding-problems/problems.jsonl`

Each line must contain a unique `sourceId`, lowercase kebab-case `slug`, unique title, `difficulty` (`EASY`, `MEDIUM`, or `HARD`), non-empty `tags`, `constraints`, `description`, a non-empty `languageConfigurations` object, and `testCases` containing at least one public and one hidden case. Each test case requires a unique positive `testCaseNumber`, `input`, and `expectedOutput`.

The importer is opt-in, deterministic, duplicate-safe, and preserves existing problem IDs and user progress. It reports total records, imported records, duplicate skips, and invalid records at startup. Place only original or legally redistributable data here and retain its license/attribution documentation alongside the dataset.
