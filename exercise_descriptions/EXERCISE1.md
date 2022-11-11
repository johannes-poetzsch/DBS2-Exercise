# Exercise 0

You will find the relevant files at:
 - for Java: `./src/main/java/exercise1/`
 - for Kotlin: `./src/main/kotlin/exercise1/`

Relevant files / classes:
 - `src/main/java/exercise1/TPMMSJava.java` / `src/main/kotlin/exercise1/TPMMSKotlin.kt`
 - `src/test/kotlin/exercise1/TPMMSExerciseTests.kt`
 - `src/test/resources/exercise1/*.csv`

## Your tasks

0. Choose your programming language by setting the value of the `@ChosenImplementation` annotation on the given class to `true`.
1. Implement the missing methods `estimatedIOCost` and `sort` of the TPMMS algorithm.
2. Test your implementation by running `gradle test --tests "exercise1.TPMMSExerciseTests"` or using your IDE's test runner.
3. Pack your exercise with `gradle packExercise1` and upload it to Moodle.

## Notes

- you will find the interfaces for our DBMS framework in `src/main/kotlin/de/hpi/dbs2/dbms/*`
  - read their documentation carefully
  - only focus on the interfaces
  - if you have questions about methods, feel free to ask in the Moodle
- for your own tests: the `DBMS` class is your entry point in our testing framework
  - it contains methods for loading `Relation`s and getting the `BlockManager`
  - see the existing tests for examples on how to use it
- blocks exist in two states: loaded into memory and referenced on disk
  - see the `BlockManager`'s documentation for usage details
- the `BlockSorter` is capable of sorting lists of loaded blocks
  - use the `ColumnDefinition` to get the required tuple `Comparator`
- after the operation finishes, the memory should be empty again
