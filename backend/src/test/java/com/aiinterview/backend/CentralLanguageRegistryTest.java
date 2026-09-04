package com.aiinterview.backend;

import com.aiinterview.backend.config.CentralLanguageRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CentralLanguageRegistryTest {

    @Test
    void testLanguageRegistryCompleteness() {
        List<CentralLanguageRegistry.LanguageSpec> all = CentralLanguageRegistry.getAllLanguages();
        assertEquals(51, all.size(), "Registry should contain exactly 51 verified languages");

        List<CentralLanguageRegistry.LanguageSpec> popular = CentralLanguageRegistry.getPopularLanguages();
        assertEquals(5, popular.size(), "Should contain 5 popular programming languages");

        List<CentralLanguageRegistry.LanguageSpec> more = CentralLanguageRegistry.getMoreLanguages();
        assertEquals(45, more.size(), "Should contain 45 more programming languages");

        List<CentralLanguageRegistry.LanguageSpec> database = CentralLanguageRegistry.getDatabaseLanguages();
        assertEquals(1, database.size(), "Should contain 1 database language");
        assertEquals("mysql", database.get(0).key());

        List<CentralLanguageRegistry.LanguageSpec> programming = CentralLanguageRegistry.getProgrammingLanguages();
        assertEquals(50, programming.size(), "Should contain 50 programming languages");

        // Verify key languages exist
        String[] requiredLanguages = {
                "python", "java", "cpp", "javascript", "typescript",
                "c", "csharp", "go", "rust", "swift",
                "php", "ruby", "scala", "bash", "lua",
                "elixir", "erlang", "perl", "haskell",
                "dart", "racket", "r", "groovy",
                "julia", "d", "cobol", "ocaml", "nim",
                "pascal", "raku", "vlang", "zig", "fortran",
                "prolog", "basic.net", "clojure", "crystal",
                "lisp", "coffeescript", "octave", "pwsh",
                "smalltalk", "sqlite3", "awk", "dash",
                "freebasic", "forth", "emacs", "bqn", "dragon",
                "mysql"
        };
        for (String lang : requiredLanguages) {
            CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(lang);
            assertNotNull(spec, "Missing language: " + lang);
            assertNotNull(spec.displayName(), "Missing displayName for " + lang);
            assertNotNull(spec.runtimeLanguage(), "Missing runtimeLanguage for " + lang);
            assertNotNull(spec.starterCode(), "Missing starterCode for " + lang);
            assertFalse(spec.starterCode().isBlank(), "Starter code is blank for " + lang);
            assertNotNull(spec.fileExtension(), "Missing fileExtension for " + lang);
            assertNotNull(spec.icon(), "Missing icon for " + lang);
            assertTrue(spec.enabled(), "Language should be enabled: " + lang);
        }

        // Verify aliases work
        assertEquals("python", CentralLanguageRegistry.get("py").key());
        assertEquals("cpp", CentralLanguageRegistry.get("c++").key());
        assertEquals("javascript", CentralLanguageRegistry.get("js").key());
        assertEquals("csharp", CentralLanguageRegistry.get("c#").key());
        assertEquals("go", CentralLanguageRegistry.get("golang").key());
        assertEquals("elixir", CentralLanguageRegistry.get("exs").key());
        assertEquals("erlang", CentralLanguageRegistry.get("erl").key());
        assertEquals("perl", CentralLanguageRegistry.get("pl").key());
        assertEquals("haskell", CentralLanguageRegistry.get("hs").key());
        assertEquals("racket", CentralLanguageRegistry.get("rkt").key());
        assertEquals("r", CentralLanguageRegistry.get("rscript").key());
        assertEquals("groovy", CentralLanguageRegistry.get("gvy").key());
        assertEquals("julia", CentralLanguageRegistry.get("jl").key());
        assertEquals("d", CentralLanguageRegistry.get("dlang").key());
        assertEquals("cobol", CentralLanguageRegistry.get("cob").key());
        assertEquals("ocaml", CentralLanguageRegistry.get("ml").key());
        assertEquals("pascal", CentralLanguageRegistry.get("pas").key());
        assertEquals("raku", CentralLanguageRegistry.get("perl6").key());
        assertEquals("vlang", CentralLanguageRegistry.get("v").key());
        assertEquals("mysql", CentralLanguageRegistry.get("sql").key());
    }
}
