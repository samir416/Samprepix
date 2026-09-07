export const APTITUDE_TRACKS = [
    {
        id: "quantitative",
        title: "Quantitative Aptitude",
        shortTitle: "Quant",
        badge: "10 Core Topics",
        frequency: "High Frequency",
        description: "Master foundational arithmetic, numerical calculation, and speed problem-solving techniques essential for campus placement screenings.",
        topics: [
            {
                id: "quant-1",
                title: "Percentages, Profit & Loss",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Master cost price, selling price, marked price, margin calculations, and successive discount problems.",
                formula: "Profit % = (Profit / CP) × 100 | Successive Discount = a + b - (a × b) / 100",
                questions: [
                    {
                        id: "q1-1",
                        prompt: "A merchant marks an article 40% above the cost price and then allows a discount of 15% on the marked price. What is the merchant's net profit percentage?",
                        options: [
                            { id: "A", text: "16%" },
                            { id: "B", text: "19%" },
                            { id: "C", text: "21%" },
                            { id: "D", text: "25%" }
                        ],
                        correctOption: "B",
                        explanation: "Let Cost Price (CP) = 100. Marked Price (MP) = 100 + 40 = 140. Discount = 15% of 140 = 21. Selling Price (SP) = 140 - 21 = 119. Profit = SP - CP = 119 - 100 = 19. Profit % = (19 / 100) × 100 = 19%."
                    },
                    {
                        id: "q1-2",
                        prompt: "If the cost price of 16 items is equal to the selling price of 12 items, find the profit percentage.",
                        options: [
                            { id: "A", text: "25%" },
                            { id: "B", text: "30%" },
                            { id: "C", text: "33.33%" },
                            { id: "D", text: "36.67%" }
                        ],
                        correctOption: "C",
                        explanation: "Given: 16 × CP = 12 × SP. Therefore, SP / CP = 16 / 12 = 4 / 3. Let CP = 3 units, then SP = 4 units. Profit = 4 - 3 = 1 unit. Profit % = (1 / 3) × 100 = 33.33%."
                    },
                    {
                        id: "q1-3",
                        prompt: "A shopkeeper offers two successive discounts of 20% and 10% on a laptop. What is the single equivalent discount percentage?",
                        options: [
                            { id: "A", text: "28%" },
                            { id: "B", text: "30%" },
                            { id: "C", text: "32%" },
                            { id: "D", text: "26%" }
                        ],
                        correctOption: "A",
                        explanation: "Using the formula for successive discounts: Equivalent Discount = a + b - (ab / 100) = 20 + 10 - (20 × 10 / 100) = 30 - 2 = 28%."
                    }
                ]
            },
            {
                id: "quant-2",
                title: "Time, Speed & Distance",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Solve relative speed, trains crossing platforms/poles, and boat/stream upstream and downstream problems.",
                formula: "Speed = Distance / Time | Relative Speed (opposite) = S1 + S2 | Relative Speed (same) = |S1 - S2|",
                questions: [
                    {
                        id: "q2-1",
                        prompt: "A train 180 meters long running at 54 km/h crosses an electric pole. How much time does it take to cross the pole?",
                        options: [
                            { id: "A", text: "10 seconds" },
                            { id: "B", text: "12 seconds" },
                            { id: "C", text: "15 seconds" },
                            { id: "D", text: "18 seconds" }
                        ],
                        correctOption: "B",
                        explanation: "Convert speed from km/h to m/s: 54 × (5 / 18) = 15 m/s. Distance to cover = length of train = 180 m. Time = Distance / Speed = 180 / 15 = 12 seconds."
                    },
                    {
                        id: "q2-2",
                        prompt: "A person travels from city A to city B at 60 km/h and returns at 40 km/h along the same route. What is the average speed for the entire journey?",
                        options: [
                            { id: "A", text: "48 km/h" },
                            { id: "B", text: "50 km/h" },
                            { id: "C", text: "52 km/h" },
                            { id: "D", text: "45 km/h" }
                        ],
                        correctOption: "A",
                        explanation: "When distances are equal, Average Speed = (2 × S1 × S2) / (S1 + S2) = (2 × 60 × 40) / (60 + 40) = 4800 / 100 = 48 km/h."
                    }
                ]
            },
            {
                id: "quant-3",
                title: "Time & Work, Pipes & Cisterns",
                difficulty: "Hard",
                frequency: "High Frequency",
                summary: "Calculate combined work efficiency, alternate day tasks, and cistern inlet/outlet tank filling rates.",
                formula: "Efficiency = Total Work / Days | Combined Rate = 1/A + 1/B",
                questions: [
                    {
                        id: "q3-1",
                        prompt: "A can complete a task in 12 days and B can complete it in 18 days. If they work together, in how many days will the task be completed?",
                        options: [
                            { id: "A", text: "6.8 days" },
                            { id: "B", text: "7.2 days" },
                            { id: "C", text: "7.5 days" },
                            { id: "D", text: "8.0 days" }
                        ],
                        correctOption: "B",
                        explanation: "LCM of 12 and 18 = 36 (assume total work is 36 units). A's 1-day work = 36 / 12 = 3 units. B's 1-day work = 36 / 18 = 2 units. Combined 1-day work = 3 + 2 = 5 units. Days required = 36 / 5 = 7.2 days."
                    }
                ]
            },
            {
                id: "quant-4",
                title: "Ratio, Proportion & Mixtures",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Analyze direct/inverse proportion, partnership profit-sharing, and alligation replacement mixtures.",
                formula: "Alligation: (Cheaper Qty / Dearer Qty) = (Dearer Price - Mean) / (Mean - Cheaper)",
                questions: [
                    {
                        id: "q4-1",
                        prompt: "In a mixture of 60 liters, the ratio of milk to water is 2:1. How much water must be added to make the ratio 1:2?",
                        options: [
                            { id: "A", text: "40 liters" },
                            { id: "B", text: "50 liters" },
                            { id: "C", text: "60 liters" },
                            { id: "D", text: "80 liters" }
                        ],
                        correctOption: "C",
                        explanation: "Initial milk = (2/3) × 60 = 40 liters. Initial water = (1/3) × 60 = 20 liters. Let added water = x. New ratio: Milk / Water = 40 / (20 + x) = 1 / 2. Cross-multiplying: 80 = 20 + x, so x = 60 liters."
                    }
                ]
            },
            {
                id: "quant-5",
                title: "Simple & Compound Interest",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Compute SI, CI compounded annually/half-yearly, and differences between CI and SI over 2 and 3 years.",
                formula: "SI = (P × R × T) / 100 | CI Difference for 2 yrs = P × (R / 100)²",
                questions: [
                    {
                        id: "q5-1",
                        prompt: "The difference between CI and SI on a certain sum of money for 2 years at 10% per annum is $45. Find the principal sum.",
                        options: [
                            { id: "A", text: "$4,000" },
                            { id: "B", text: "$4,500" },
                            { id: "C", text: "$5,000" },
                            { id: "D", text: "$5,500" }
                        ],
                        correctOption: "B",
                        explanation: "Difference for 2 years = P × (R / 100)². 45 = P × (10 / 100)² = P × 0.01. Therefore, P = 45 / 0.01 = $4,500."
                    }
                ]
            },
            {
                id: "quant-6",
                title: "Number Systems & Divisibility",
                difficulty: "Easy",
                frequency: "High Frequency",
                summary: "Understand prime factors, LCM/HCF relations, remainders, cyclicity of units digits, and divisibility rules.",
                formula: "Product of numbers = LCM × HCF | Cyclicity of digits (2, 3, 7, 8 = 4)",
                questions: [
                    {
                        id: "q6-1",
                        prompt: "What is the remainder when 7^84 is divided by 342?",
                        options: [
                            { id: "A", text: "1" },
                            { id: "B", text: "7" },
                            { id: "C", text: "49" },
                            { id: "D", text: "341" }
                        ],
                        correctOption: "A",
                        explanation: "Notice that 7³ = 343. So 7^84 = (7³)^28 = (343)^28. Since 343 = 342 + 1, (342 + 1)^28 mod 342 = 1^28 = 1."
                    }
                ]
            },
            {
                id: "quant-7",
                title: "Permutations, Combinations & Probability",
                difficulty: "Hard",
                frequency: "Core Placement",
                summary: "Master arrangements with constraints, team selections, and independent/conditional event probabilities.",
                formula: "nPr = n! / (n - r)! | nCr = n! / [r! × (n - r)!] | P(E) = n(E) / n(S)",
                questions: [
                    {
                        id: "q7-1",
                        prompt: "In how many different ways can the letters of the word 'LEADER' be arranged?",
                        options: [
                            { id: "A", text: "360" },
                            { id: "B", text: "720" },
                            { id: "C", text: "180" },
                            { id: "D", text: "120" }
                        ],
                        correctOption: "A",
                        explanation: "The word 'LEADER' has 6 letters: L (1), E (2), A (1), D (1), R (1). Total arrangements = 6! / 2! = 720 / 2 = 360."
                    }
                ]
            },
            {
                id: "quant-8",
                title: "Averages, Alligations & Ages",
                difficulty: "Easy",
                frequency: "Standard",
                summary: "Solve problems on average speed, weighted averages, and linear equations based on age ratios.",
                formula: "Average = Sum / Count | Weighted Avg = (w1·x1 + w2·x2) / (w1 + w2)",
                questions: [
                    {
                        id: "q8-1",
                        prompt: "The average age of a class of 30 students is 14 years. If the teacher's age is included, the average increases by 1 year. What is the teacher's age?",
                        options: [
                            { id: "A", text: "42 years" },
                            { id: "B", text: "45 years" },
                            { id: "C", text: "46 years" },
                            { id: "D", text: "44 years" }
                        ],
                        correctOption: "B",
                        explanation: "Total age of 30 students = 30 × 14 = 420. Total age with teacher (31 persons) = 31 × 15 = 465. Teacher's age = 465 - 420 = 45 years."
                    }
                ]
            },
            {
                id: "quant-9",
                title: "Progressions, Sequences & Series",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Arithmetic progressions (AP), geometric progressions (GP), and harmonic series sums.",
                formula: "AP: Tn = a + (n - 1)d, Sn = (n/2)[2a + (n - 1)d] | GP: Tn = a·r^(n-1)",
                questions: [
                    {
                        id: "q9-1",
                        prompt: "Find the sum of all natural numbers between 100 and 200 that are divisible by 3.",
                        options: [
                            { id: "A", text: "4,950" },
                            { id: "B", text: "4,850" },
                            { id: "C", text: "5,000" },
                            { id: "D", text: "5,150" }
                        ],
                        correctOption: "A",
                        explanation: "First term > 100 divisible by 3 is a = 102. Last term < 200 is l = 198. Common difference d = 3. Number of terms n = [(198 - 102) / 3] + 1 = (96 / 3) + 1 = 33. Sum = (n / 2) × (a + l) = (33 / 2) × 300 = 33 × 150 = 4,950."
                    }
                ]
            },
            {
                id: "quant-10",
                title: "Mensuration & Geometry Fundamentals",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Calculate perimeter, area, surface area, and volume of 2D/3D figures (cylinders, cones, spheres).",
                formula: "Cylinder Vol = πr²h | Sphere Vol = (4/3)πr³ | Cone Vol = (1/3)πr²h",
                questions: [
                    {
                        id: "q10-1",
                        prompt: "If the radius of a circle is increased by 20%, by what percentage does its area increase?",
                        options: [
                            { id: "A", text: "40%" },
                            { id: "B", text: "44%" },
                            { id: "C", text: "48%" },
                            { id: "D", text: "50%" }
                        ],
                        correctOption: "B",
                        explanation: "Area is proportional to r². If r increases by 20% (multiplier 1.2), area multiplier = (1.2)² = 1.44. Percentage increase = (1.44 - 1) × 100 = 44%."
                    }
                ]
            }
        ]
    },
    {
        id: "logical",
        title: "Logical Reasoning",
        shortTitle: "Logical",
        badge: "8 Core Topics",
        frequency: "High Frequency",
        description: "Develop deductive logic, sequential pattern recognition, structural relations, and analytical thinking for technical assessments.",
        topics: [
            {
                id: "logic-1",
                title: "Syllogisms & Deductive Logic",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Evaluate statements with universal/particular premises and determine valid conclusions.",
                formula: "All A are B + All B are C => All A are C | Some A are B != All A are B",
                questions: [
                    {
                        id: "l1-1",
                        prompt: "Statements:\n1. All cars are vehicles.\n2. Some vehicles are electric.\nConclusions:\nI. Some cars are electric.\nII. All vehicles are cars.",
                        options: [
                            { id: "A", text: "Only conclusion I follows" },
                            { id: "B", text: "Only conclusion II follows" },
                            { id: "C", text: "Neither I nor II follows" },
                            { id: "D", text: "Both I and II follow" }
                        ],
                        correctOption: "C",
                        explanation: "The electric vehicles may not overlap with cars; conclusion I is not definitely true. Also, all vehicles are not cars (cars are a subset); conclusion II is false. Thus, neither follows."
                    }
                ]
            },
            {
                id: "logic-2",
                title: "Coding, Decoding & Letter Series",
                difficulty: "Easy",
                frequency: "Core Placement",
                summary: "Identify letter substitution patterns, alphabetical shifts, and reverse numeric mapping.",
                formula: "A=1, Z=26 | Opposite pairs: A-Z, B-Y, C-X, D-W (Sum = 27)",
                questions: [
                    {
                        id: "l2-1",
                        prompt: "If 'SYSTEM' is coded as 'SYSMET' and 'NEARER' is coded as 'AENRER', how will 'FRACTION' be coded?",
                        options: [
                            { id: "A", text: "CARFNOIT" },
                            { id: "B", text: "CRAFITON" },
                            { id: "C", text: "ARFCNOIT" },
                            { id: "D", text: "CARFTION" }
                        ],
                        correctOption: "A",
                        explanation: "Split the 8-letter word into two halves: 'FRAC' and 'TION'. Reverse each half: 'FRAC' reversed is 'CARF', 'TION' reversed is 'NOIT'. Combining gives 'CARFNOIT'."
                    }
                ]
            },
            {
                id: "logic-3",
                title: "Blood Relations & Family Trees",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Trace generational relationships, maternal/paternal lineages, and coded relation symbols.",
                formula: "Generational levels: Grandparents (+2) -> Parents (+1) -> Self/Siblings (0) -> Children (-1)",
                questions: [
                    {
                        id: "l3-1",
                        prompt: "Pointing to a photograph, Rohit said, 'She is the daughter of my grandfather's only son.' How is the girl in the photograph related to Rohit?",
                        options: [
                            { id: "A", text: "Mother" },
                            { id: "B", text: "Sister" },
                            { id: "C", text: "Cousin" },
                            { id: "D", text: "Aunt" }
                        ],
                        correctOption: "B",
                        explanation: "Grandfather's only son is Rohit's father. The daughter of Rohit's father is Rohit's sister."
                    }
                ]
            },
            {
                id: "logic-4",
                title: "Direction Sense & Spatial Navigation",
                difficulty: "Easy",
                frequency: "Standard",
                summary: "Calculate net displacement and cardinal heading (N, S, E, W) using Pythagorean theorem.",
                formula: "Displacement = √(Δx² + Δy²) | North = +y, South = -y, East = +x, West = -x",
                questions: [
                    {
                        id: "l4-1",
                        prompt: "A man walks 6 km North, turns right and walks 8 km. How far is he from his starting point and in which direction?",
                        options: [
                            { id: "A", text: "10 km North-East" },
                            { id: "B", text: "14 km North-East" },
                            { id: "C", text: "10 km East" },
                            { id: "D", text: "12 km North-East" }
                        ],
                        correctOption: "A",
                        explanation: "He moved 6 km North (+y) and 8 km East (+x). Distance = √(6² + 8²) = √(36 + 64) = √100 = 10 km in the North-East direction."
                    }
                ]
            },
            {
                id: "logic-5",
                title: "Seating Arrangements (Linear & Circular)",
                difficulty: "Hard",
                frequency: "Core Placement",
                summary: "Deduce arrangements of entities facing center/outwards in circle or single/double parallel rows.",
                formula: "Facing center: Right is counter-clockwise, Left is clockwise.",
                questions: [
                    {
                        id: "l5-1",
                        prompt: "Five friends A, B, C, D, E are sitting in a circle facing the center. A is to the immediate left of B. E is between C and D. If C is to the immediate left of A, who is sitting to the immediate right of B?",
                        options: [
                            { id: "A", text: "D" },
                            { id: "B", text: "E" },
                            { id: "C", text: "C" },
                            { id: "D", text: "A" }
                        ],
                        correctOption: "A",
                        explanation: "Arrangement clockwise: B, A, C, E, D. Sitting to the immediate right of B (counter-clockwise) is D."
                    }
                ]
            },
            {
                id: "logic-6",
                title: "Statement, Assumptions & Conclusions",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Evaluate premises to determine whether an unstated assumption is implicitly taken for granted.",
                formula: "Assumption is an unstated premise that MUST be true for the argument to hold.",
                questions: [
                    {
                        id: "l6-1",
                        prompt: "Statement: 'The company has decided to grant work-from-home privilege on Fridays to improve employee productivity.'\nAssumption I: Employees prefer working from home on Fridays.\nAssumption II: Working from home on Fridays will not adversely impact core company deliverables.",
                        options: [
                            { id: "A", text: "Only assumption I is implicit" },
                            { id: "B", text: "Only assumption II is implicit" },
                            { id: "C", text: "Both assumptions I and II are implicit" },
                            { id: "D", text: "Neither is implicit" }
                        ],
                        correctOption: "C",
                        explanation: "For the initiative to achieve its goal of improving productivity, the company assumes both that employees welcome it and that operational output won't suffer."
                    }
                ]
            },
            {
                id: "logic-7",
                title: "Clocks, Calendars & Binary Logic",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Angle between clock hands, leap year day calculations, and truth-teller / liar puzzles.",
                formula: "Clock angle = |(30 × H) - (11/2 × M)| | Ordinary year = 1 odd day, Leap year = 2 odd days",
                questions: [
                    {
                        id: "l7-1",
                        prompt: "What is the angle between the minute hand and the hour hand of a clock at 3:40 PM?",
                        options: [
                            { id: "A", text: "120°" },
                            { id: "B", text: "130°" },
                            { id: "C", text: "140°" },
                            { id: "D", text: "125°" }
                        ],
                        correctOption: "B",
                        explanation: "Angle = |(30 × 3) - (11/2 × 40)| = |90 - 220| = |-130| = 130°."
                    }
                ]
            },
            {
                id: "logic-8",
                title: "Data Sufficiency & Critical Puzzles",
                difficulty: "Hard",
                frequency: "Core Placement",
                summary: "Determine whether given statements alone or combined are sufficient to answer a problem.",
                formula: "Check statement (1) alone -> (2) alone -> both combined.",
                questions: [
                    {
                        id: "l8-1",
                        prompt: "Question: Is integer x positive?\nStatement (1): x² = 25\nStatement (2): 2x > 6",
                        options: [
                            { id: "A", text: "Statement (1) ALONE is sufficient" },
                            { id: "B", text: "Statement (2) ALONE is sufficient" },
                            { id: "C", text: "BOTH statements together are needed" },
                            { id: "D", text: "Statements (1) and (2) together are NOT sufficient" }
                        ],
                        correctOption: "B",
                        explanation: "Statement (1) gives x = 5 or -5 (not uniquely positive). Statement (2) gives 2x > 6 => x > 3, which guarantees x is definitely positive. Therefore statement (2) ALONE is sufficient."
                    }
                ]
            }
        ]
    },
    {
        id: "verbal",
        title: "Verbal Ability",
        shortTitle: "Verbal",
        badge: "6 Core Topics",
        frequency: "High Frequency",
        description: "Strengthen reading comprehension, contextual vocabulary, sentence correction, and verbal reasoning precision.",
        topics: [
            {
                id: "verb-1",
                title: "Reading Comprehension & Inferences",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Extract central themes, identify tone, differentiate facts from inferences in passage extracts.",
                formula: "Main Idea = Topic + Author's Stance | Avoid extreme answer choices (always, never)",
                questions: [
                    {
                        id: "v1-1",
                        prompt: "Passage extract: 'While artificial intelligence accelerates repetitive automation, true strategic innovation still requires human contextual empathy and interdisciplinary synthesis.'\n\nWhich of the following can be inferred?",
                        options: [
                            { id: "A", text: "AI will replace all knowledge work within a decade." },
                            { id: "B", text: "Strategic breakthroughs remain dependent on human capabilities." },
                            { id: "C", text: "Automation is incapable of handling repetitive tasks." },
                            { id: "D", text: "Interdisciplinary thinking is an artificial intelligence capability." }
                        ],
                        correctOption: "B",
                        explanation: "The passage directly asserts that 'true strategic innovation still requires human contextual empathy and interdisciplinary synthesis', directly supporting option B."
                    }
                ]
            },
            {
                id: "verb-2",
                title: "Sentence Correction & Grammatical Errors",
                difficulty: "Easy",
                frequency: "Core Placement",
                summary: "Master subject-verb agreement, misplaced modifiers, parallelism, and pronoun references.",
                formula: "Singular subject = singular verb | Parallel structures must maintain identical grammatical forms.",
                questions: [
                    {
                        id: "v2-1",
                        prompt: "Identify the grammatically correct sentence:",
                        options: [
                            { id: "A", text: "Neither the manager nor the engineers was present at the deployment." },
                            { id: "B", text: "Neither the manager nor the engineers were present at the deployment." },
                            { id: "C", text: "Neither the manager or the engineers was present at the deployment." },
                            { id: "D", text: "Neither the manager nor the engineers is present at the deployment." }
                        ],
                        correctOption: "B",
                        explanation: "In 'Neither... nor' constructions, the verb agrees with the subject closest to it. Here, 'engineers' is plural, requiring the plural verb 'were'."
                    }
                ]
            },
            {
                id: "verb-3",
                title: "Vocabulary, Synonyms & Antonyms",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Contextual definitions, high-frequency collegiate placement words, and nuance discrimination.",
                formula: "Root words: Bene (good), Mal (bad), Chron (time), Dict (speak), Fract (break).",
                questions: [
                    {
                        id: "v3-1",
                        prompt: "Select the word closest in meaning to 'PRAGMATIC':",
                        options: [
                            { id: "A", text: "Theoretical" },
                            { id: "B", text: "Practical" },
                            { id: "C", text: "Idealistic" },
                            { id: "D", text: "Erratic" }
                        ],
                        correctOption: "B",
                        explanation: "'Pragmatic' means dealing with things sensibly and realistically based on practical considerations rather than theoretical ones."
                    }
                ]
            },
            {
                id: "verb-4",
                title: "Para Jumbles & Coherent Sequencing",
                difficulty: "Hard",
                frequency: "Core Placement",
                summary: "Reconstruct logical narrative sequences using opening statements, transitions, and pronouns.",
                formula: "Look for independent opening noun sentence -> link with pronouns (he, it, they) -> conclusion.",
                questions: [
                    {
                        id: "v4-1",
                        prompt: "Arrange sentences P, Q, R, S in logical order:\nP: It has consequently transformed global trade corridors.\nQ: Containerization revolutionized modern maritime shipping.\nR: This standardized system drastically lowered cargo loading costs.\nS: Goods can now be transported seamlessly between ships and trains.",
                        options: [
                            { id: "A", text: "Q - R - S - P" },
                            { id: "B", text: "P - Q - R - S" },
                            { id: "C", text: "R - S - Q - P" },
                            { id: "D", text: "Q - S - P - R" }
                        ],
                        correctOption: "A",
                        explanation: "Q introduces the primary subject (Containerization). R explains 'This standardized system' and cost reduction. S describes seamless transport. P concludes with the consequential impact on trade corridors."
                    }
                ]
            },
            {
                id: "verb-5",
                title: "Fill in the Blanks & Cloze Test",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Select grammatically and contextually appropriate words to restore cohesion in narrative text.",
                formula: "Check parts of speech required (noun, verb, adjective) + tone of the surrounding text.",
                questions: [
                    {
                        id: "v5-1",
                        prompt: "The candidate's explanation was so ______ that the interview panel immediately approved her proposal without further scrutiny.",
                        options: [
                            { id: "A", text: "lucid" },
                            { id: "B", text: "ambiguous" },
                            { id: "C", text: "tentative" },
                            { id: "D", text: "convoluted" }
                        ],
                        correctOption: "A",
                        explanation: "'Lucid' means expressed clearly and easy to understand, matching the context of immediate panel approval."
                    }
                ]
            },
            {
                id: "verb-6",
                title: "Critical Reasoning & Argument Flaws",
                difficulty: "Hard",
                frequency: "Core Placement",
                summary: "Strengthen/weaken arguments, identify logical fallacies, and evaluate underlying assumptions.",
                formula: "Identify Conclusion -> Identify Evidence -> Spot the causal leap or unstated assumption.",
                questions: [
                    {
                        id: "v6-1",
                        prompt: "Argument: 'Cities with more bicycle lanes report lower cardiovascular disease rates. Therefore, building bicycle lanes directly causes improved cardiovascular health.'\n\nWhich of the following points out the primary flaw in the reasoning?",
                        options: [
                            { id: "A", text: "It confuses correlation with direct causation." },
                            { id: "B", text: "It fails to define bicycle lanes clearly." },
                            { id: "C", text: "It assumes that all citizens own bicycles." },
                            { id: "D", text: "It ignores the economic cost of lane construction." }
                        ],
                        correctOption: "A",
                        explanation: "The author observes a correlation between bike lanes and lower disease rates and erroneously infers direct causation, ignoring confounding lifestyle factors."
                    }
                ]
            }
        ]
    },
    {
        id: "data_interpretation",
        title: "Data Interpretation",
        shortTitle: "DI",
        badge: "6 Core Topics",
        frequency: "High Frequency",
        description: "Analyze quantitative datasets, multi-variable charts, tabular comparisons, and business caselets under timed conditions.",
        topics: [
            {
                id: "di-1",
                title: "Tabular Data & Multi-Row Calculations",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Perform rapid column sums, percentage growths, and comparative market share calculations.",
                formula: "% Growth = [(Final - Initial) / Initial] × 100",
                questions: [
                    {
                        id: "d1-1",
                        prompt: "Table Data: Product Sales in Units\nYear 2023: Company X = 400, Company Y = 600\nYear 2024: Company X = 520, Company Y = 750\n\nWhat is the percentage growth in total combined sales from 2023 to 2024?",
                        options: [
                            { id: "A", text: "25%" },
                            { id: "B", text: "27%" },
                            { id: "C", text: "30%" },
                            { id: "D", text: "32%" }
                        ],
                        correctOption: "B",
                        explanation: "Total 2023 = 400 + 600 = 1,000. Total 2024 = 520 + 750 = 1,270. Absolute growth = 1,270 - 1,000 = 270. Percentage growth = (270 / 1,000) × 100 = 27%."
                    }
                ]
            },
            {
                id: "di-2",
                title: "Bar Charts & Comparative Trends",
                difficulty: "Easy",
                frequency: "Core Placement",
                summary: "Interpret single and grouped vertical/horizontal bars comparing performance across quarters.",
                formula: "Ratio = Bar1 Height / Bar2 Height | Average = Total Sum / Number of Bars",
                questions: [
                    {
                        id: "d2-1",
                        prompt: "A quarterly revenue bar chart shows:\nQ1: $20M | Q2: $25M | Q3: $30M | Q4: $45M\n\nWhat fraction of total annual revenue was generated in Q4?",
                        options: [
                            { id: "A", text: "3/8" },
                            { id: "B", text: "9/24" },
                            { id: "C", text: "1/3" },
                            { id: "D", text: "5/12" }
                        ],
                        correctOption: "A",
                        explanation: "Total Annual Revenue = 20 + 25 + 30 + 45 = $120M. Q4 fraction = 45 / 120 = 9 / 24 = 3 / 8."
                    }
                ]
            },
            {
                id: "di-3",
                title: "Pie Charts & Percentage Share",
                difficulty: "Medium",
                frequency: "Core Placement",
                summary: "Convert central angles (360 degrees) to percentage shares and compute actual segment values.",
                formula: "Angle (degrees) = (Percentage / 100) × 360° | Value = (Angle / 360°) × Total",
                questions: [
                    {
                        id: "d3-1",
                        prompt: "In a company budget pie chart, the sector representing R&D corresponds to a central angle of 54°. If the total annual budget is $8,000,000, how much is allocated to R&D?",
                        options: [
                            { id: "A", text: "$1,000,000" },
                            { id: "B", text: "$1,200,000" },
                            { id: "C", text: "$1,500,000" },
                            { id: "D", text: "$1,800,000" }
                        ],
                        correctOption: "B",
                        explanation: "Fraction allocated = 54° / 360° = 3 / 20 = 15%. R&D budget = 0.15 × $8,000,000 = $1,200,000."
                    }
                ]
            },
            {
                id: "di-4",
                title: "Line Graphs & Rate of Growth",
                difficulty: "Medium",
                frequency: "Standard",
                summary: "Track continuous trends over multi-year timelines and identify periods of maximum slope/decline.",
                formula: "Slope = Δy / Δx = (Value2 - Value1) / (Year2 - Year1)",
                questions: [
                    {
                        id: "d4-1",
                        prompt: "A line chart shows active subscribers over 4 years: 2020: 100k, 2021: 150k, 2022: 210k, 2023: 315k. Which single-year interval recorded the highest percentage growth?",
                        options: [
                            { id: "A", text: "2020 to 2021" },
                            { id: "B", text: "2021 to 2022" },
                            { id: "C", text: "2022 to 2023" },
                            { id: "D", text: "All intervals were identical" }
                        ],
                        correctOption: "C",
                        explanation: "2020-2021: (50/100) = 50.0%. 2021-2022: (60/150) = 40.0%. 2022-2023: (105/210) = 50.0%. Both 2020-2021 and 2022-2023 had 50%, with 2022-2023 adding the largest absolute number of subscribers (105k)."
                    }
                ]
            },
            {
                id: "di-5",
                title: "Radar & Mixed Multi-Variable Charts",
                difficulty: "Hard",
                frequency: "Standard",
                summary: "Evaluate multidimensional performance across skills, departments, and composite metrics.",
                formula: "Composite Score = Σ (Weight_i × Score_i)",
                questions: [
                    {
                        id: "d5-1",
                        prompt: "A candidate is evaluated on 3 competencies: Coding (weight 50%), System Design (weight 30%), and Behavioral (weight 20%). If she scores 80 in Coding, 70 in System Design, and 90 in Behavioral, what is her overall weighted score?",
                        options: [
                            { id: "A", text: "78" },
                            { id: "B", text: "79" },
                            { id: "C", text: "80" },
                            { id: "D", text: "82" }
                        ],
                        correctOption: "B",
                        explanation: "Weighted Score = (0.50 × 80) + (0.30 × 70) + (0.20 × 90) = 40 + 21 + 18 = 79."
                    }
                ]
            },
            {
                id: "di-6",
                title: "Caselet Data Interpretation & Sets",
                difficulty: "Hard",
                frequency: "Core Placement",
                summary: "Synthesize paragraph-based quantitative descriptions into tabular datasets to answer multi-part questions.",
                formula: "Construct matrix table first before calculating individual question ratios.",
                questions: [
                    {
                        id: "d6-1",
                        prompt: "Caselet: Out of 500 college graduates, 60% received IT offers, 40% received Non-IT offers, and 15% received both. How many graduates received ONLY IT offers?",
                        options: [
                            { id: "A", text: "200" },
                            { id: "B", text: "225" },
                            { id: "C", text: "250" },
                            { id: "D", text: "275" }
                        ],
                        correctOption: "B",
                        explanation: "Total IT offers = 60% of 500 = 300. Both IT and Non-IT = 15% of 500 = 75. Only IT = Total IT - Both = 300 - 75 = 225."
                    }
                ]
            }
        ]
    }
];

