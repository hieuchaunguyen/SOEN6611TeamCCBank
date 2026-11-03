
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * SOEN 6611 - CBank Source Lines of Code Analyzer
 *
 * Measures production Java files only.
 *
 * Physical SLOC:
 *   Nonblank lines containing source code.
 *   Comment-only lines are excluded.
 *
 * Logical SLOC:
 *   Selected Java declarations and statements,
 *   counted using the Java compiler AST.
 *
 * The counting scheme is documented in
 * Tools/SLOC/counting_scheme.md.
 *
 * Generated reports are overwritten on every run.
 *
 * Requires JDK 17 or newer.
 */
public class SLOCAnalyzer {

    private static final Path SOURCE_DIR =
            Path.of("Source", "cbank", "src", "cbank");

    private static final Path OUTPUT_DIR =
            Path.of("Tools", "SLOC");

    private static final Path CSV_FILE =
            OUTPUT_DIR.resolve("sloc_results.csv");

    private static final Path MD_FILE =
            OUTPUT_DIR.resolve("sloc_results.md");

    public static void main(String[] args) throws Exception {

        if (!Files.isDirectory(SOURCE_DIR)) {
            throw new IllegalStateException(
                    "Source directory not found: " + SOURCE_DIR
                            + "\nRun from the repository root."
            );
        }

        Files.createDirectories(OUTPUT_DIR);

        List<Path> javaFiles;

        try (var stream = Files.list(SOURCE_DIR)) {
            javaFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName().toString()
                                    .endsWith(".java"))
                    .sorted()
                    .toList();
        }

        if (javaFiles.isEmpty()) {
            throw new IllegalStateException(
                    "No Java source files found."
            );
        }

        List<Result> results = new ArrayList<>();

        int totalPhysical = 0;
        int totalLogical = 0;

        System.out.println("CBank SLOC Measurement");
        System.out.println(
                "=============================================="
        );

        for (Path file : javaFiles) {

            int physical = countPhysical(file);
            int logical = countLogical(file);

            results.add(new Result(
                    file.getFileName().toString(),
                    physical,
                    logical
            ));

            totalPhysical += physical;
            totalLogical += logical;

            System.out.printf(
                    "%-25s Physical: %4d  Logical: %4d%n",
                    file.getFileName(),
                    physical,
                    logical
            );
        }

        writeCsv(results, totalPhysical, totalLogical);
        writeMarkdown(results, totalPhysical, totalLogical);

        System.out.println(
                "=============================================="
        );
        System.out.println("Files measured : " + results.size());
        System.out.println("Physical SLOC  : " + totalPhysical);
        System.out.println("Logical SLOC   : " + totalLogical);
        System.out.println("CSV report     : " + CSV_FILE);
        System.out.println("Markdown report: " + MD_FILE);
    }

    /**
     * Counts physical source lines.
     *
     * Ignores blank lines and comment-only lines.
     * Counts lines containing both code and comments.
     *
     * Recognizes line comments, block comments,
     * string literals, character literals and text blocks.
     */
    private static int countPhysical(Path file)
            throws IOException {

        String source = Files.readString(
                file, StandardCharsets.UTF_8
        );

        boolean inBlockComment = false;
        boolean inTextBlock = false;

        int physical = 0;

        for (String line : source.split("\\R", -1)) {

            boolean hasCode = false;
            boolean inString = false;
            boolean inChar = false;

            for (int i = 0; i < line.length(); i++) {

                char c = line.charAt(i);
                char next = i + 1 < line.length()
                        ? line.charAt(i + 1)
                        : '\0';

                if (inBlockComment) {
                    if (c == '*' && next == '/') {
                        inBlockComment = false;
                        i++;
                    }
                    continue;
                }

                if (inTextBlock) {
                    if (line.startsWith("\"\"\"", i)
                            && !isEscaped(line, i)) {
                        inTextBlock = false;
                        hasCode = true;
                        i += 2;
                    } else if (!Character.isWhitespace(c)) {
                        hasCode = true;
                    }
                    continue;
                }

                if (inString) {
                    if (c == '\\') {
                        i++;
                    } else if (c == '"') {
                        inString = false;
                    }
                    continue;
                }

                if (inChar) {
                    if (c == '\\') {
                        i++;
                    } else if (c == '\'') {
                        inChar = false;
                    }
                    continue;
                }

                if (c == '/' && next == '/') {
                    break;
                }

                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }

                if (line.startsWith("\"\"\"", i)) {
                    inTextBlock = true;
                    hasCode = true;
                    i += 2;
                    continue;
                }

                if (c == '"') {
                    inString = true;
                    hasCode = true;
                    continue;
                }

                if (c == '\'') {
                    inChar = true;
                    hasCode = true;
                    continue;
                }

                if (!Character.isWhitespace(c)) {
                    hasCode = true;
                }
            }

            if (hasCode) {
                physical++;
            }
        }

        return physical;
    }

    private static boolean isEscaped(
            String text, int index) {

        int backslashes = 0;

        for (int i = index - 1;
             i >= 0 && text.charAt(i) == '\\';
             i--) {
            backslashes++;
        }

        return backslashes % 2 != 0;
    }

    /**
     * Parses the Java file using the compiler AST.
     *
     * Counts selected declarations and statements,
     * following the documented logical SLOC scheme.
     */
    private static int countLogical(Path file)
            throws IOException {

        JavaCompiler compiler =
                ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            throw new IllegalStateException(
                    "JDK compiler unavailable. Use JDK 17+."
            );
        }

        try (StandardJavaFileManager manager =
                     compiler.getStandardFileManager(
                             null,
                             null,
                             StandardCharsets.UTF_8
                     )) {

            Iterable<? extends JavaFileObject> units =
                    manager.getJavaFileObjects(
                            file.toFile()
                    );

            JavacTask task = (JavacTask) compiler.getTask(
                    null,
                    manager,
                    null,
                    List.of("-proc:none"),
                    null,
                    units
            );

            Iterable<? extends CompilationUnitTree> trees =
                    task.parse();

            StatementCounter counter =
                    new StatementCounter();

            for (CompilationUnitTree tree : trees) {
                counter.scan(tree, null);
            }

            return counter.getCount();
        }
    }

    /**
     * AST-based logical source unit counter.
     *
     * Counts:
     * - Field and local variable declarations
     * - Expression statements
     * - Return, throw, break, continue, assert
     * - If, loops, switch, try, synchronized,
     *   and labeled statements
     *
     * Excludes:
     * - Class and method declarations
     * - Method/lambda/catch parameters
     * - Standalone blocks
     * - Empty statements
     * - Package/import declarations
     * - For-loop header components
     *
     * Each loop is counted exactly once.
     */
    private static class StatementCounter
            extends TreeScanner<Void, Void> {

        private int count = 0;

        int getCount() {
            return count;
        }

        @Override
        public Void visitMethod(
                MethodTree node, Void unused) {

            // Exclude method and constructor parameters.
            // Scan the body for executable statements.
            scan(node.getBody(), unused);

            return null;
        }

        @Override
        public Void visitLambdaExpression(
                LambdaExpressionTree node,
                Void unused) {

            // Exclude lambda parameters.
            scan(node.getBody(), unused);

            return null;
        }

        @Override
        public Void visitCatch(
                CatchTree node, Void unused) {

            // Exclude the exception parameter.
            scan(node.getBlock(), unused);

            return null;
        }

        @Override
        public Void visitForLoop(
                ForLoopTree node, Void unused) {

            // Count the loop once.
            count++;

            // Exclude initializer, condition, and updates.
            // Count statements inside the body.
            scan(node.getStatement(), unused);

            return null;
        }

        @Override
        public Void visitEnhancedForLoop(
                EnhancedForLoopTree node,
                Void unused) {

            // Count the enhanced-for loop once.
            count++;

            // Exclude iteration variable and iterable
            // expression as separate logical units.
            scan(node.getStatement(), unused);

            return null;
        }

        @Override
        public Void scan(Tree tree, Void unused) {

            if (tree != null && isCounted(tree.getKind())) {
                count++;
            }

            return super.scan(tree, unused);
        }

        private boolean isCounted(Tree.Kind kind) {

            return switch (kind) {

                case VARIABLE,
                     EXPRESSION_STATEMENT,
                     RETURN,
                     THROW,
                     BREAK,
                     CONTINUE,
                     ASSERT,
                     IF,
                     WHILE_LOOP,
                     DO_WHILE_LOOP,
                     SWITCH,
                     SWITCH_EXPRESSION,
                     TRY,
                     SYNCHRONIZED,
                     LABELED_STATEMENT -> true;

                default -> false;
            };
        }
    }

    private static void writeCsv(
            List<Result> results,
            int totalPhysical,
            int totalLogical) throws IOException {

        StringBuilder csv = new StringBuilder();

        csv.append(
                "File,Physical SLOC,Logical SLOC\n"
        );

        for (Result result : results) {
            csv.append(result.fileName)
                    .append(",")
                    .append(result.physical)
                    .append(",")
                    .append(result.logical)
                    .append("\n");
        }

        csv.append("TOTAL,")
                .append(totalPhysical)
                .append(",")
                .append(totalLogical)
                .append("\n");

        Files.writeString(
                CSV_FILE,
                csv.toString(),
                StandardCharsets.UTF_8
        );
    }

    private static void writeMarkdown(
            List<Result> results,
            int totalPhysical,
            int totalLogical) throws IOException {

        StringBuilder md = new StringBuilder();

        md.append("# CBank SLOC Measurement Results\n\n");

        md.append(
                "Measurement scope: production Java source files "
                        + "in `Source/cbank/src/cbank/`.\n\n"
        );

        md.append(
                "Counting rules are documented in "
                        + "`counting_scheme.md`.\n\n"
        );

        md.append(
                "| Source File | Physical SLOC | Logical SLOC |\n"
        );

        md.append("|---|---:|---:|\n");

        for (Result result : results) {
            md.append("| ")
                    .append(result.fileName)
                    .append(" | ")
                    .append(result.physical)
                    .append(" | ")
                    .append(result.logical)
                    .append(" |\n");
        }

        md.append("| **TOTAL** | **")
                .append(totalPhysical)
                .append("** | **")
                .append(totalLogical)
                .append("** |\n\n");

        md.append("## Physical SLOC Calculation\n\n");

        md.append(joinValues(results, true))
                .append(" = **")
                .append(totalPhysical)
                .append("**\n\n");

        md.append("## Logical SLOC Calculation\n\n");

        md.append(joinValues(results, false))
                .append(" = **")
                .append(totalLogical)
                .append("**\n\n");

        Result largest = results.stream()
                .max(Comparator.comparingInt(
                        result -> result.physical))
                .orElseThrow();

        md.append("## Interpretation\n\n");

        md.append(
                "The application contains **"
                        + totalPhysical
                        + " Physical SLOC** and **"
                        + totalLogical
                        + " Logical SLOC** under the documented "
                        + "measurement conventions.\n\n"
        );

        md.append("`")
                .append(largest.fileName)
                .append("` has the highest Physical SLOC (")
                .append(largest.physical)
                .append(").\n\n");

        md.append(
                "SLOC measures source-code size. "
                        + "It does not directly establish readability "
                        + "or maintainability.\n"
        );

        Files.writeString(
                MD_FILE,
                md.toString(),
                StandardCharsets.UTF_8
        );
    }

    private static String joinValues(
            List<Result> results,
            boolean physical) {

        StringJoiner joiner = new StringJoiner(" + ");

        for (Result result : results) {
            joiner.add(String.valueOf(
                    physical
                            ? result.physical
                            : result.logical
            ));
        }

        return joiner.toString();
    }

    private static class Result {

        final String fileName;
        final int physical;
        final int logical;

        Result(String fileName, int physical, int logical) {
            this.fileName = fileName;
            this.physical = physical;
            this.logical = logical;
        }
    }
}
