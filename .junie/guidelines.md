Code Style
----------

Focus on correctness, readability and performance.
Max line length 120.
Use final for fields, omit final for local variables uless necessary or where it increases readability.
Prefer immutable data; avoid exposing mutable internals.
All code should be threadsafe unless otherwise stated.
Prefer Optional over @Nullable for return types of user facing methods (public API); for private API, @Nullable return values are acceptable. Do not use Optional for fields or parameters.

External Dependencies
---------------------

For this project, external dependencies should be kept to the bare required minimum. Do not introduce any new dependencies unless explicitly asked to do so.

Do not use Lombok or Guava. Check the LangUtil and IoUtil classes (and others) that offer much of the functionality often imported from Lombok or Guava.

Deprecated classes and methods
------------------------------

Do not use deprecated classes like Date unless explicitly told to do so.
Prefer using Path over File, URI over URL.
Do not use functionality deprecated in newer Java versions, for example the primitive constructors that are now deprecated in Java 25 but were not in Java 21.

Java Version
------------

Prefer newer more concise language constructs. The project requires at least Java 21, some modules even Java 25. Compatibility with lower Java versions is not needed or desired.

Null-checks
-----------

The project uses JSpecify annotations. 
All modules are package‑annotated with @NullMarked.
Annotate nullable type uses with @Nullable.
Do not write manual Objects.requireNonNull or explicit null checks; the Cabe processor injects checks for method/constructor/record parameters and return values.

he Cabe processor is applied in the Gradle build for all modules. Run ./gradlew build to ensure null checks are injected.

Cabe can be configured to throw AssertionErrors instead of NullPointerExceptions so that programs fail early during development. Consider this when writing unit tests that test null arguments are detcted, i.e., assertThrows(NullPointerException.class, ...) will not work in that case. Instead, assertThrows(Throwable.class, ...) followed by an assertion on the returned exception type (should be NullPointerException or AssertionError) must be used.

Prefer non‑null elements in collections. If elements may be null, annotate the type use, e.g., List<@Nullable T>.

Logging
-------

Log4J API inn version 2.20.+ is used for logging.
Do not use JUL logging or SLF4J unless explicity told to.
Prefer parameterized messages with {} placeholders.
Use lambda/supplier for expensive computations to avoid unnecessary work.

Log levels:
- ERROR: An operation failed and requires attention. Usually accompanied by an exception. The application may not proceed correctly.
- WARN: Anomalies or degraded behavior; operation continues but may need investigation. Avoid floods.
- INFO: High‑level lifecycle events and important outcomes (startup, shutdown, configuration summary, key feature usage). Keep bounded.
- DEBUG: Diagnostic details for developers; safe to enable in production temporarily. Prefer lazy/supplier usage.
- TRACE: Very verbose, step‑by‑step tracing. Only enable when actively investigating.

Do not log sensitive data such as passwords.

Build Tool
----------

The project uses Gradle in version 9.+ as build tool. All build files use the Kotlin DSL.

Unit Tests
----------

JUnit 5 is used.

If a test unexpectedly fails, DO NOT try to change the test to make it pass, instead inform me about why you think the method under test behaves in an unexpected way. Check the method for possible errors. Explain your findings and ask how to proceed.
Use a single test class per class under test.
Remember the rule under Null-Checks, i.e., AssertionErrors might be thrown where you would expect a NullPointerException.
Use parametrized tests where it makes the code easier to maintain, increases readablitlity and coverage.

Javadoc comments
----------------

Write concise Javadoc comments. Do not include irrelevant information about details of the implementation.
Make sure to include all parameters in the correct order.
Place @param <T> tags before other @param tags.
Do not add generic type parameters that are not declared in the declaration being documented but in the enclosing entity.
When inserting an empty line for readability into the Javadoc, place a single "<p>" on that line and do not add a closing tag "</p>".
