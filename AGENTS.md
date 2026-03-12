# AGENTS.md - Development Guide for WSON

## Build & Test Commands

### Gradle Tasks
```bash
./gradlew build                    # Build and test all modules
./gradlew test                     # Run all tests
./gradlew test --tests "<pattern>" # Run specific tests (e.g., "*.JSONTest")
./gradlew check                    # Run all verification tasks
./gradlew clean                    # Clean build outputs
./gradlew jacocoTestReport         # Generate code coverage report
```

### Module-Specific Commands
```bash
./gradlew :wson-cli:envelopeJar      # Create executable JAR
./gradlew :wson-cli:nativeImage      # Build GraalVM native image
./gradlew :benchmark:jmh             # Run JMH benchmarks
./gradlew :antlr:generateGrammarSource   # Generate ANTLR parsers
./gradlew :wcfg:generateGrammarSource    # Generate WCFG parsers
./gradlew javadoc                    # Generate API documentation
./gradlew sourcesJar                 # Build sources JAR
./gradlew publish                    # Publish to Maven repository
```

### Running a Single Test
```bash
./gradlew test --tests "net.woggioni.wson.serialization.binary.JBONTest"
./gradlew test --tests "*.JSONTest"                    # By class name
./gradlew test --tests "net.woggioni.wson.*"           # By package
./gradlew test --tests "*Test.consistencyTest"         # By method name
./gradlew :wson-cli:test --tests "CliTest"             # Module-specific
```

### Test Options
```bash
./gradlew test --fail-fast        # Stop after first failure
./gradlew test --rerun            # Force re-run even if up-to-date
./gradlew test --info             # Show detailed test output
```

## Project Structure
- `src/main/java/net/woggioni/wson/` - Main library code
- `src/test/java/net/woggioni/wson/` - Test code
- `test-utils/` - Shared test utilities (e.g., JsonBomb for DoS testing)
- `antlr/` - ANTLR-based JSON parser implementation
- `wson-cli/` - CLI with GraalVM native image support
- `wcfg/` - Configuration file parser (ANTLR-based)
- `benchmark/` - JMH benchmarks
- `wson-w3c-json/` - Jakarta JSON-P provider

## Code Style Guidelines

### Java Version & Toolchain
- **Target**: Java 25 (GraalVM)
- **Release**: Java 17 compatibility
- **Module System**: JPMS enabled (`module-info.java`)

### Imports Organization
```java
// 1. Lombok annotations (if used)
import lombok.Builder;
import lombok.EqualsAndHashCode;

// 2. Project imports (net.woggioni.wson)
import net.woggioni.wson.exception.TypeException;
import net.woggioni.wson.value.NullValue;

// 3. Standard Java imports (java.*, org.*, com.*)
import java.util.Map;
import java.util.ArrayList;
```

### Naming Conventions
- **Packages**: `net.woggioni.wson.<function>` (e.g., `value`, `serialization`, `traversal`, `exception`, `xface`)
- **Interfaces**: Descriptive names (`Value`, `Parser`, `Dumper`, `ValueVisitor`)
- **Implementations**: `<Format><Role>` pattern (`JSONParser`, `JBONDumper`, `JSONDumper`)
- **Value types**: `<Type>Value` pattern (`StringValue`, `IntegerValue`, `ObjectValue`)
- **Exceptions**: `<Name>Exception` extending `WsonException`
- **Tests**: `<ClassName>Test.java` in parallel test directory structure

### Formatting
- **Indentation**: 4 spaces
- **Braces**: K&R style (opening brace on same line)
- **Line length**: ~120 characters
- **Blank lines**: Separate logical blocks between methods and classes

### Lombok Usage
Heavy use of Lombok for reducing boilerplate:
- `@EqualsAndHashCode` - Value objects
- `@NoArgsConstructor` / `@AllArgsConstructor` - Constructors
- `@Getter` - Simple getters
- `@Builder` - Configuration classes and builders
- `@Slf4j` - Logging
- `@SneakyThrows` - Test code only (avoid in production)
- `@NonNull` - Null-safety annotations
- `@RequiredArgsConstructor` - Constructor injection

### Error Handling
All exceptions extend `WsonException` (RuntimeException):
```java
// Exception hierarchy
WsonException (base)
├── ParseException (includes line/column info)
├── TypeException (type conversion errors)
├── IOException (I/O errors)
├── NotImplementedException (unreachable code)
└── MaxDepthExceededException (DoS protection)
```

Pattern for parser errors (include position info):
```java
private <T extends RuntimeException> T error(Function<String, T> constructor,
                                              String fmt, Object... args) {
    return constructor.apply(String.format(
        "Error at line %d column %d: %s",
        currentLine, currentColumn, String.format(fmt, args)));
}
```

### Design Patterns
- **Interface/Implementation**: Define contracts in `xface` package, multiple implementations
- **Factory methods**: Configuration-driven instantiation
- **Visitor pattern**: `ValueVisitor` for tree traversal
- **Builder pattern**: `Value.Configuration` with Lombok `@Builder`

### Testing Patterns
- Use JUnit 5 (`org.junit.jupiter.api.*`)
- `@TempDir` for temporary test files
- Try-with-resources for streams
- Common tests: `consistencyTest` (round-trip), `comparativeTest` (vs Jackson)
- Test resources in `src/test/resources/`

### Logging
- Use SLF4J via Lombok `@Slf4j`
- Logging framework: slf4j-simple (test/runtime)

## Module System (JPMS)
Exports from `module-info.java`:
```java
module net.woggioni.wson {
    requires static lombok;
    requires net.woggioni.jwo;
    
    exports net.woggioni.wson.xface;
    exports net.woggioni.wson.value;
    exports net.woggioni.wson.exception;
    exports net.woggioni.wson.serialization;
    exports net.woggioni.wson.serialization.json;
    exports net.woggioni.wson.serialization.binary;
    exports net.woggioni.wson.traversal;
}
```

## CI/CD (Jenkins)
Pipeline stages:
1. `./gradlew build` - Build and test
2. Archive JUnit results: `**/build/test-results/test/*.xml`
3. Generate JaCoCo coverage report
4. Archive Javadoc and JAR artifacts
5. `./gradlew publish` - Deploy to Gitea Maven repository

## Versioning
- Scheme: `YYYY.MM.DD` (e.g., `2026.03.12`)
- Configured in `gradle.properties`: `wson.version`
- Dependency catalog: `lys-catalog:2026.02.19`

## Environment Setup
```properties
# gradle.properties
org.gradle.caching=true
wson.version = 2026.03.12
lys.version = 2026.02.19
gitea.maven.url = https://gitea.woggioni.net/api/packages/woggioni/maven
```

## Build Notes
- GraalVM 25 required for native image builds
- Java 17 release compatibility for produced artifacts
- Gradle 9.4.0+
- Use `--warning-mode all` to see deprecation notices
- Configuration cache can be enabled for faster builds
