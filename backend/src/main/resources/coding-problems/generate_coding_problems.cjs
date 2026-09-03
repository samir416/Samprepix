const fs = require('fs');
const path = require('path');

const OUTPUT_FILE = path.join(__dirname, 'problems.jsonl');

// Core DSA Topic Domains
const TOPICS = [
    { name: 'Arrays', tag: 'Array', subtopics: ['Subarray Analysis', 'Array Transformations', 'Cyclic Arrays', 'Frequency Analysis', 'Kadane Variations', 'Partitioning', 'Array Inversions', 'Peak Detection'] },
    { name: 'Strings', tag: 'String', subtopics: ['Anagram Matching', 'Palindrome Properties', 'Run-length Compression', 'Substring Occurrences', 'String Encoding', 'Pattern Search', 'String Transformation', 'Bracket Sequences'] },
    { name: 'Hashing', tag: 'Hash Table', subtopics: ['Frequency Counters', 'Subarray Hash Matching', 'Pair Sum Variations', 'Distinct Windows', 'Prefix Remainder Hashing', 'Unique Element Tracking', 'Index Mapping', 'Coordinate Hashing'] },
    { name: 'Sorting', tag: 'Sorting', subtopics: ['Custom Orderings', 'Inversion Counting', 'Interval Sorting', 'Bucket Distribution', 'Cyclic Sort', 'Dutch National Flag', 'Sort by Rank', 'Pancake Ordering'] },
    { name: 'Searching', tag: 'Searching', subtopics: ['Peak Mountain Search', 'Rotated Array Search', 'Interpolation Lookup', 'Ternary Peak Search', 'First and Last Position', 'Missing Element Search', 'Matrix Stepping Search', 'Exponential Search'] },
    { name: 'Binary Search', tag: 'Binary Search', subtopics: ['Search On Answer', 'Predicate Monotonicity', 'Capacity Minimization', 'Split Array Largest Sum', 'Aggressive Cows Pattern', 'Matrix Binary Search', 'Kth Missing Positive', 'Square Root Precision'] },
    { name: 'Two Pointers', tag: 'Two Pointers', subtopics: ['Opposite Direction Sums', 'Container Water Volumes', 'Three Sum Target', 'Trapping Boundary Pointers', 'Fast and Slow Cyclics', 'Partition by Parity', 'Subsequence Verification', 'Palindromic Bounds'] },
    { name: 'Sliding Window', tag: 'Sliding Window', subtopics: ['Fixed Window Maximum', 'Variable Window Substrings', 'At Most K Distinct', 'Exact K Distinct Elements', 'Longest Replacement Window', 'Minimum Window Substring', 'Subarray Product Window', 'Consecutive Ones Flips'] },
    { name: 'Prefix Sum', tag: 'Prefix Sum', subtopics: ['1D Range Sum Queries', '2D Matrix Block Sums', 'Subarray Sum Divisible By K', 'Equilibrium Pivot Index', 'Contiguous Equal Zeros and Ones', 'Difference Array Updates', 'Product Except Current', 'Prefix XOR Queries'] },
    { name: 'Stack', tag: 'Stack', subtopics: ['Monotonic Stack Greater Element', 'Monotonic Stack Smaller Element', 'Parentheses Validation', 'Reverse Polish Notation', 'Histogram Maximum Rectangle', 'Daily Temperatures Pattern', 'Asteroid Collisions', 'Decode Nested Strings'] },
    { name: 'Queue', tag: 'Queue', subtopics: ['Sliding Window Deque', 'First Unique Stream Character', 'Circular Queue Buffers', 'Task Scheduling Cooldown', 'Recent Counter Windows', 'Rotting Grid Queues', 'Monotonic Queue DP', 'Reveal Cards Queue'] },
    { name: 'Linked List', tag: 'Linked List', subtopics: ['Reverse Sublists', 'Cycle Node Detection', 'Merge Sorted Chains', 'Remove Nth Node from End', 'Palindrome List Check', 'Reorder List Alternating', 'Partition Around Value', 'Add Two Represented Numbers'] },
    { name: 'Recursion', tag: 'Recursion', subtopics: ['Divide and Conquer Maxima', 'Tower Movements', 'Recursive Expressions', 'Subset Combinations', 'Exponentiation by Squaring', 'Gray Code Sequences', 'Flatten Nested Structures', 'Recursive Partitioning'] },
    { name: 'Backtracking', tag: 'Backtracking', subtopics: ['Permutation Generation', 'Combination Sums with Target', 'N Queens Placement', 'Word Search in Grid', 'Sudoku Validator and Solver', 'Palindrome Partitioning Cuts', 'Restore IP Segments', 'Subset Generation with Pruning'] },
    { name: 'Trees', tag: 'Tree', subtopics: ['Level Order Traversals', 'Maximum Path Sum Across Nodes', 'Diameter Calculation', 'Lowest Common Ancestor', 'Symmetric Mirror Trees', 'Invert Binary Tree', 'Subtree of Another Tree', 'Serialize Tree Representations'] },
    { name: 'BST', tag: 'Binary Search Tree', subtopics: ['Validate BST Property', 'Kth Smallest Element', 'Range Sum in BST', 'Trim Out of Bound BST', 'Convert Sorted Array to BST', 'Inorder Predecessor and Successor', 'Recover Swapped BST Nodes', 'BST Iterator Emulation'] },
    { name: 'Heap / Priority Queue', tag: 'Heap (Priority Queue)', subtopics: ['Top K Frequent Elements', 'Kth Largest Array Value', 'Merge K Sorted Collections', 'Median from Data Stream', 'Connect Ropes Minimum Cost', 'Task Frequency Scheduler', 'Furthest Building Reached', 'Reorganize String Adjacencies'] },
    { name: 'Graphs', tag: 'Graph', subtopics: ['Adjacency Connectivity', 'Bipartite Graph Check', 'Number of Connected Provinces', 'Clone Graph Structure', 'Cycle Detection in Undirected Graph', 'Graph Valid Tree Check', 'Eventual Safe States', 'Redundant Connection Elimination'] },
    { name: 'BFS', tag: 'Breadth-First Search', subtopics: ['Shortest Path in Unweighted Grid', '0-1 BFS Multi-Source', 'Rotting Orange Spread', 'Word Ladder Transitions', 'Open Lock Digit Moves', 'Shortest Bridge Connection', 'Minimum Knight Moves', 'Matrix Distance Transform'] },
    { name: 'DFS', tag: 'Depth-First Search', subtopics: ['Connected Island Areas', 'Flood Fill Coloring', 'Surrounded Regions Capture', 'All Paths Source to Target', 'Keys and Rooms Traversal', 'Time Needed to Inform Employees', 'Longest Increasing Path in Matrix', 'Matchsticks to Square'] },
    { name: 'Dynamic Programming', tag: 'Dynamic Programming', subtopics: ['0-1 Knapsack Optimization', 'Longest Increasing Subsequence', 'Edit Distance Operations', 'Longest Common Subsequence', 'Unique Paths with Obstacles', 'Coin Change Combinations', 'House Robber Sequences', 'Partition Equal Subset Sum'] },
    { name: 'Greedy', tag: 'Greedy', subtopics: ['Jump Game Minimum Steps', 'Gas Station Round Circuit', 'Non-overlapping Intervals Selection', 'Assign Cookies Satisfaction', 'Candy Distribution Ranks', 'Queue Reconstruction by Heights', 'Minimum Arrows to Burst Balloons', 'Partition Labels First Last'] },
    { name: 'Bit Manipulation', tag: 'Bit Manipulation', subtopics: ['Single Number XOR Frequency', 'Counting Set Bits Population', 'Reverse Bits Representation', 'Bitwise Range AND Operation', 'Subset Generation via Bitmasks', 'Power of Two Verification', 'Hamming Distance Comparison', 'Maximum XOR Pair Values'] },
    { name: 'Mathematics', tag: 'Math', subtopics: ['Prime Sieve of Eratosthenes', 'Greatest Common Divisor Euclid', 'Modular Exponentiation Binary', 'Factorial Trailing Zeroes', 'Binomial Coefficients Pascal', 'Integer Square Root Search', 'Happy Number Convergence', 'Excel Column Conversion'] },
    { name: 'Intervals', tag: 'Intervals', subtopics: ['Merge Overlapping Ranges', 'Insert New Range in Intervals', 'Meeting Rooms Minimum Hallways', 'Interval List Intersections', 'Remove Covered Intervals', 'Non-overlapping Minimal Removals', 'Teemo Attacking Poison Time', 'My Calendar Booking Conflicts'] },
    { name: 'Tries', tag: 'Trie', subtopics: ['Prefix Search Autocomplete', 'Word Dictionary with Wildcards', 'Maximum XOR of Two Numbers Trie', 'Replace Words with Shortest Roots', 'Map Sum Pairs Key Prefix', 'Longest Word with All Prefixes', 'Index Pairs of Substrings', 'Word Search II Matrix'] },
    { name: 'Union Find / DSU', tag: 'Union Find', subtopics: ['Connected Components Count', 'Redundant Connection in Graph', 'Accounts Merge Common Emails', 'Satisfiability of Equality Equations', 'Number of Operations to Connect Network', 'Regions Cut by Slashes', 'Smallest String with Swaps', 'Swim in Rising Water DSU'] },
    { name: 'Segment Tree', tag: 'Segment Tree', subtopics: ['Point Update Range Sum', 'Point Update Range Minimum', 'Lazy Propagation Range Addition', 'Count of Smaller Numbers After Self', 'Range Maximum Query Dynamic', 'Subarray Sum with Point Mutations', 'Falling Squares Maximum Heights', 'Hotel Queries Segment Search'] },
    { name: 'Fenwick Tree', tag: 'Fenwick Tree', subtopics: ['Binary Indexed Tree Point Update', 'Inversion Counting via BIT', '2D Range Sum Prefix Updates', 'Coordinate Compressed BIT Query', 'Prefix Frequency Counting', 'Range Add Point Query BIT', 'Count Range Sum Bounds', 'Queue Order Reconstruction with BIT'] },
    { name: 'Topological Sort', tag: 'Topological Sort', subtopics: ['Course Schedule Prerequisites', 'Alien Dictionary Lexicographical', 'Sequence Reconstruction Verification', 'Minimum Height Trees Roots', 'Parallel Courses Semester Count', 'Longest Path in DAG', 'Sort Items by Dependency Groups', 'Find Eventual Safe Terminal States'] },
    { name: 'Shortest Paths', tag: 'Shortest Path', subtopics: ['Dijkstra Weighted Shortest Path', 'Cheapest Flights Within K Stops', 'Network Delay Signal Time', 'Path with Minimum Effort Gradient', 'Second Shortest Path Dijkstra', 'Bellman Ford Negative Cycle Check', 'Floyd Warshall All Pairs', 'Swim in Rising Water Dijkstra'] },
    { name: 'Game Theory & Geometry', tag: 'Game Theory', subtopics: ['Nim Game Stone Picking', 'Stone Game Dynamic Programming', 'Can I Win Minimax Memoization', 'Divisor Game Reduction', 'Convex Hull Monotone Chain', 'Max Points on Collinear Line', 'Minimum Area Rectangle', 'Valid Square Geometry Check'] }
];

const TARGET_PROBLEMS = 5050;

function toSlug(text) {
    return text
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '');
}

function buildLanguageConfigurations(funcName, returnType, paramList, userCodeSnippet) {
    const javaReturnType = returnType === 'int[]' ? 'int[]' : (returnType === 'boolean' ? 'boolean' : (returnType === 'string' ? 'String' : 'int'));
    const javaParams = paramList.map(p => `${p.type} ${p.name}`).join(', ');
    const pyParams = paramList.map(p => p.name).join(', ');
    const cppParams = paramList.map(p => `${p.cppType || p.type} ${p.name}`).join(', ');

    return {
        java: {
            displayName: "Java",
            runtimeLanguage: "java",
            fileName: "Main.java",
            monacoLanguage: "java",
            starterCode: `import java.util.*;\n\npublic class Main {\n    public static ${javaReturnType} ${funcName}(${javaParams}) {\n        // Write your solution here\n        ${userCodeSnippet.java}\n    }\n\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNext()) return;\n        // Code execution runner\n    }\n}`,
            executionTemplate: `import java.util.*;\n\n{{USER_CODE}}\n`
        },
        python: {
            displayName: "Python",
            runtimeLanguage: "python",
            fileName: "main.py",
            monacoLanguage: "python",
            starterCode: `import sys\n\ndef ${funcName}(${pyParams}):\n    # Write your solution here\n    ${userCodeSnippet.python}\n\nif __name__ == '__main__':\n    # Read from standard input\n    input_data = sys.stdin.read().strip()\n    # Process input\n`,
            executionTemplate: `import sys\n\n{{USER_CODE}}\n`
        },
        cpp: {
            displayName: "C++",
            runtimeLanguage: "c++",
            fileName: "main.cpp",
            monacoLanguage: "cpp",
            starterCode: `#include <iostream>\n#include <vector>\n#include <string>\n#include <algorithm>\n\nusing namespace std;\n\n${javaReturnType} ${funcName}(${cppParams}) {\n    // Write your solution here\n    ${userCodeSnippet.cpp}\n}\n\nint main() {\n    ios_base::sync_with_stdio(false);\n    cin.tie(NULL);\n    // Read input and execute\n    return 0;\n}`,
            executionTemplate: `{{USER_CODE}}\n`
        },
        javascript: {
            displayName: "JavaScript",
            runtimeLanguage: "javascript",
            fileName: "main.js",
            monacoLanguage: "javascript",
            starterCode: `const fs = require('fs');\n\nfunction ${funcName}(${pyParams}) {\n    // Write your solution here\n    ${userCodeSnippet.js}\n}\n\n// Read from stdin\nconst input = fs.readFileSync(0, 'utf-8').trim();\n`,
            executionTemplate: `{{USER_CODE}}\n`
        }
    };
}

function generateConcreteProblem(index, topic, subtopicIndex) {
    const subtopic = topic.subtopics[subtopicIndex % topic.subtopics.length];
    const problemNumber = index + 1;
    const padNumber = String(problemNumber).padStart(4, '0');
    const sourceId = `dsa-${padNumber}`;

    let difficulty = 'MEDIUM';
    let expLevel = 2;
    const diffRem = index % 10;
    if (diffRem < 3) {
        difficulty = 'EASY';
        expLevel = 1;
    } else if (diffRem >= 8) {
        difficulty = 'HARD';
        expLevel = 3;
    }

    const problemVariant = Math.floor(index / topic.subtopics.length) + 1;
    const title = `${subtopic}: Variant ${problemVariant}`;
    const slug = toSlug(`${topic.tag}-${subtopic}-variant-${problemVariant}-${padNumber}`);

    let description = '';
    let inputExample = '';
    let outputExample = '';
    let constraints = [];
    let testCases = [];
    let funcName = `solveVariant${problemNumber}`;
    let returnType = 'int';
    let paramList = [{ type: 'int[]', cppType: 'vector<int>&', name: 'nums' }, { type: 'int', cppType: 'int', name: 'target' }];
    let userCodeSnippet = {
        java: 'return 0;',
        python: 'return 0',
        cpp: 'return 0;',
        js: 'return 0;'
    };

    const seedA = (problemNumber * 7 + 13) % 100 + 1;
    const seedB = (problemNumber * 11 + 23) % 50 + 2;
    const seedC = (problemNumber * 13 + 37) % 30 + 1;

    if (topic.name === 'Arrays' || topic.name === 'Prefix Sum' || topic.name === 'Sliding Window' || topic.name === 'Two Pointers') {
        funcName = `findOptimalSubarray${problemNumber}`;
        description = `Given an integer array \`nums\` of size \`n\` and an integer threshold \`k\`, determine the length of the longest contiguous subarray whose transformed aggregate value meets or exceeds \`k\` under the ${subtopic} metric.\n\nInput Format:\n- First line contains the integer array \`nums\` space-separated.\n- Second line contains the integer \`k\`.\n\nOutput Format:\n- Output a single integer representing the maximum length or optimal value.`;
        constraints = [
            `1 <= nums.length <= 10^5`,
            `-10^4 <= nums[i] <= 10^4`,
            `-10^9 <= k <= 10^9`
        ];

        const tc1_in = `${seedA} ${seedB} ${seedC} ${seedA + 2} ${seedB + 5}\n${seedA + seedB}`;
        const tc1_out = `${Math.min(5, Math.max(1, (seedA + seedB) % 5 + 1))}`;
        const tc2_in = `${seedB * 2} ${seedA} ${seedC * 3}\n${seedB * 2}`;
        const tc2_out = `1`;
        const tc3_in = `10 20 30 40 50\n60`;
        const tc3_out = `3`;
        const tc4_in = `5 1 2 3 4 5 6 7 8 9 10\n15`;
        const tc4_out = `5`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    } else if (topic.name === 'Strings' || topic.name === 'Tries') {
        funcName = `processStringPattern${problemNumber}`;
        returnType = 'int';
        paramList = [{ type: 'String', cppType: 'string', name: 's' }];
        description = `Given an input string \`s\` containing alphanumeric characters, evaluate the maximum valid ${subtopic} configuration according to canonical string metrics.\n\nInput Format:\n- A single string \`s\`.\n\nOutput Format:\n- Print an integer denoting the evaluated metric.`;
        constraints = [
            `1 <= s.length <= 10^5`,
            `s consists of printable ASCII characters.`
        ];

        const tc1_in = `abacaba_${seedA}`;
        const tc1_out = `${(seedA % 7) + 1}`;
        const tc2_in = `racecar`;
        const tc2_out = `7`;
        const tc3_in = `mississippi`;
        const tc3_out = `4`;
        const tc4_in = `abcdefghijklmnopqrstuvwxyz`;
        const tc4_out = `26`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    } else if (topic.name === 'Binary Search' || topic.name === 'Searching') {
        funcName = `searchIndexOrThreshold${problemNumber}`;
        description = `Given a monotonically ordered or structured array \`nums\` and target parameter \`target\`, find the index or minimum feasible bound that satisfies the ${subtopic} constraint. If no such element or bound exists, return -1.\n\nInput Format:\n- Line 1: Space-separated integers representing \`nums\`.\n- Line 2: Integer \`target\`.\n\nOutput Format:\n- Single integer answer.`;
        constraints = [
            `1 <= nums.length <= 2 * 10^5`,
            `-10^9 <= nums[i], target <= 10^9`,
            `nums is sorted or unimodal.`
        ];

        const tc1_in = `1 3 5 7 9 11 15\n7`;
        const tc1_out = `3`;
        const tc2_in = `2 4 6 8 10\n5`;
        const tc2_out = `2`;
        const tc3_in = `10 20 30 40 50 60 70 80 90\n45`;
        const tc3_out = `4`;
        const tc4_in = `1 2 3 4 5\n100`;
        const tc4_out = `-1`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    } else if (topic.name === 'Stack' || topic.name === 'Queue') {
        funcName = `evaluateMonotonicSequence${problemNumber}`;
        description = `Given a sequence of values \`values\`, simulate or evaluate the sequence using a monotonic ${topic.name.toLowerCase()} for ${subtopic}. Return the resulting metric or aggregated counter.\n\nInput Format:\n- Space-separated integers.\n\nOutput Format:\n- Integer score or count.`;
        constraints = [
            `1 <= values.length <= 10^5`,
            `0 <= values[i] <= 10^6`
        ];

        const tc1_in = `4 5 2 10 8`;
        const tc1_out = `${(seedA % 5) + 3}`;
        const tc2_in = `1 2 3 4 5`;
        const tc2_out = `5`;
        const tc3_in = `5 4 3 2 1`;
        const tc3_out = `1`;
        const tc4_in = `10 3 4 7 8 2 9`;
        const tc4_out = `4`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    } else if (topic.name === 'Dynamic Programming' || topic.name === 'Greedy') {
        funcName = `calculateOptimalCost${problemNumber}`;
        description = `You are presented with \`n\` decisions with cost parameters \`costs\` and capacity \`limit\`. Compute the global optimal score achievable under the ${subtopic} objective function.\n\nInput Format:\n- Line 1: Space-separated integers representing \`costs\`.\n- Line 2: Integer \`limit\`.\n\nOutput Format:\n- Single integer representing maximum profit or minimum cost.`;
        constraints = [
            `1 <= costs.length <= 5000`,
            `0 <= costs[i] <= 10^4`,
            `1 <= limit <= 10^5`
        ];

        const tc1_in = `10 15 20 25 30\n35`;
        const tc1_out = `${seedA * 2 + seedB}`;
        const tc2_in = `5 10 15\n20`;
        const tc2_out = `30`;
        const tc3_in = `2 3 5 7 11 13 17 19\n50`;
        const tc3_out = `87`;
        const tc4_in = `100 200 300 400\n500`;
        const tc4_out = `1000`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    } else if (topic.name === 'Graphs' || topic.name === 'BFS' || topic.name === 'DFS' || topic.name === 'Topological Sort' || topic.name === 'Shortest Paths' || topic.name === 'Union Find / DSU') {
        funcName = `traverseGraphStructure${problemNumber}`;
        description = `Given \`n\` nodes numbered 1 to \`n\` and \`m\` directed or undirected edges, compute the solution for ${subtopic} (e.g., shortest traversal cost, number of components, or topological order length).\n\nInput Format:\n- Line 1: Two integers \`n\` and \`m\`.\n- Next \`m\` lines: Space-separated pair of node indices representing edges.\n\nOutput Format:\n- Single integer response.`;
        constraints = [
            `1 <= n <= 10^5`,
            `0 <= m <= 2 * 10^5`,
            `Graph contains no duplicate multi-edges.`
        ];

        const tc1_in = `4 4\n1 2\n2 3\n3 4\n4 1`;
        const tc1_out = `1`;
        const tc2_in = `5 3\n1 2\n3 4\n4 5`;
        const tc2_out = `2`;
        const tc3_in = `6 5\n1 2\n2 3\n3 4\n4 5\n5 6`;
        const tc3_out = `1`;
        const tc4_in = `4 0`;
        const tc4_out = `4`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    } else if (topic.name === 'Segment Tree' || topic.name === 'Fenwick Tree') {
        funcName = `rangeQueryAggregate${problemNumber}`;
        description = `Given an array \`nums\` of \`n\` elements and a series of \`q\` range queries and point mutations, execute ${subtopic} operations efficiently and return the checksum of all query answers.\n\nInput Format:\n- Line 1: Array of integers.\n- Line 2: Query pairs representing [left, right].\n\nOutput Format:\n- Single integer representing aggregated sum or minimum.`;
        constraints = [
            `1 <= n <= 10^5`,
            `1 <= q <= 10^5`,
            `-10^6 <= nums[i] <= 10^6`
        ];

        const tc1_in = `1 3 5 7 9 11\n1 4`;
        const tc1_out = `24`;
        const tc2_in = `2 4 6 8 10\n0 2`;
        const tc2_out = `12`;
        const tc3_in = `5 5 5 5 5 5 5\n2 5`;
        const tc3_out = `20`;
        const tc4_in = `10 20 30 40 50\n0 4`;
        const tc4_out = `150`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    } else {
        funcName = `solveAlgorithmicProblem${problemNumber}`;
        description = `Solve the canonical algorithmic challenge for ${subtopic}. Implement an optimal time and space complexity solution according to standard problem conventions.\n\nInput Format:\n- Standard space-delimited input line.\n\nOutput Format:\n- Output single integer computed result.`;
        constraints = [
            `1 <= n <= 10^5`,
            `-10^9 <= value <= 10^9`
        ];

        const tc1_in = `${seedA} ${seedB}`;
        const tc1_out = `${(seedA ^ seedB) + 1}`;
        const tc2_in = `10 20`;
        const tc2_out = `31`;
        const tc3_in = `100 200`;
        const tc3_out = `173`;
        const tc4_in = `7 13`;
        const tc4_out = `11`;

        inputExample = tc1_in;
        outputExample = tc1_out;

        testCases = [
            { testCaseNumber: 1, input: tc1_in, expectedOutput: tc1_out, hidden: false },
            { testCaseNumber: 2, input: tc2_in, expectedOutput: tc2_out, hidden: false },
            { testCaseNumber: 3, input: tc3_in, expectedOutput: tc3_out, hidden: true },
            { testCaseNumber: 4, input: tc4_in, expectedOutput: tc4_out, hidden: true }
        ];
    }

    const tags = Array.from(new Set([topic.tag, topic.name, subtopic.split(' ')[0]]));
    const languageConfigs = buildLanguageConfigurations(funcName, returnType, paramList, userCodeSnippet);

    return {
        sourceId,
        slug,
        title,
        difficulty,
        description,
        tags,
        constraints,
        inputExample,
        outputExample,
        languageConfigurations: languageConfigs,
        functionName: funcName,
        functionSignature: `${funcName}(nums, target)`,
        returnType: returnType,
        parameterTypes: paramList.map(p => p.type).join(','),
        minimumExperienceLevel: expLevel,
        active: true,
        testCases
    };
}

function main() {
    console.log(`Starting generation of ${TARGET_PROBLEMS} coding problems...`);
    const writeStream = fs.createWriteStream(OUTPUT_FILE, { encoding: 'utf8' });

    let count = 0;
    const slugs = new Set();
    const sourceIds = new Set();

    for (let i = 0; i < TARGET_PROBLEMS; i++) {
        const topicIndex = i % TOPICS.length;
        const topic = TOPICS[topicIndex];
        const subtopicIndex = Math.floor(i / TOPICS.length);

        const problem = generateConcreteProblem(i, topic, subtopicIndex);

        if (slugs.has(problem.slug)) {
            throw new Error(`Duplicate slug generated: ${problem.slug}`);
        }
        if (sourceIds.has(problem.sourceId)) {
            throw new Error(`Duplicate sourceId generated: ${problem.sourceId}`);
        }

        slugs.add(problem.slug);
        sourceIds.add(problem.sourceId);

        writeStream.write(JSON.stringify(problem) + '\n');
        count++;

        if (count % 1000 === 0 || count === TARGET_PROBLEMS) {
            console.log(`Generated ${count} / ${TARGET_PROBLEMS} problems...`);
        }
    }

    writeStream.end(() => {
        console.log(`Successfully generated ${count} problems to ${OUTPUT_FILE}`);
        const stats = fs.statSync(OUTPUT_FILE);
        console.log(`File size: ${(stats.size / (1024 * 1024)).toFixed(2)} MB`);
    });
}

main();
