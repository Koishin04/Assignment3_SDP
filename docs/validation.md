# Validation status

## Executed during preparation

| Check | Result |
| --- | --- |
| Application compilation with `javac "@sources.txt"` | Passed |
| Compilation target | Java 17 (`--release 17`; class-file major version 61) |
| Runtime available for verification | OpenJDK 21.0.11, Linux |
| Independent Java assertion checks | 110 passed |
| Console integration scenarios | 11 passed |
| Java source and test files checked for comments | 17 files; no comments |

The independent Java checks exercised both search rules, all three sources, source switching, delegation, translated legacy status errors, malformed records, non-leaking messages/causes, empty results, and extension on both axes. They were executed using a separate preparation harness, not JUnit. That temporary harness is not part of the submitted application.

Console scenarios covered normal roommate and tenant searches, the adapted source, an unknown city, invalid numeric input, negative budget, an unknown search, an unknown catalog, a blank city, numeric overflow, and end-of-input.

## Not executed in this environment

The 41 JUnit 5 test methods were written but were not executed by the real JUnit engine. The official standalone JAR download failed because external artifact downloads were unavailable. No imitation JUnit library is included. Download the official dependency and use the commands in README.md before claiming a successful JUnit run or submitting the work.

The application was not run on the user's exact Microsoft OpenJDK 17.0.20.1 build or in their IntelliJ installation. Compilation used the Java 17 language/API target, while runtime checks used the locally available OpenJDK 21.
