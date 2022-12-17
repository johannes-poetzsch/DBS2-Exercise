# Exercise 3

You will find the relevant files at:
- for Java: `./src/main/java/exercise3/`
- for Kotlin: `./src/main/kotlin/exercise3/`

Relevant files / classes:
* For your implementation:
    - `src/main/java/exercise3/HashEquiInnerJoinJava.java` / `src/main/kotlin/exercise3/HashEquiInnerJoinKotlin.kt`
    - `src/test/kotlin/exercise3/HashEquiInnerJoinImplementationTests.kt`
* For experimentation:
    - `src/main/java/exercise3/Ex3Main.java` / `src/main/kotlin/exercise3/Ex3Main.kt`
* For information on the DBMS framework and the given Join implementation:
    - `src/main/java/de/hpi/dbs2/exercise3/NestedLoopEquiInnerJoin.java`
    - `src/main/kotlin/de/hpi/dbs2/dbms/*.kt`
    - `src/main/kotlin/de/hpi/dbs2/exercise3/*.kt`
    - `src/test/kotlin/de/hpi/dbs2/exercise3/*.kt`
    - `src/test/java/de/hpi/dbs2/dbms/*.java`

## Your tasks

1. Implement the empty method `estimatedIOCost(leftInputRelation, rightInputRelation)` and `join(leftInputRelation, rightInputRelation, outputRelation)` in HashEquiInnerJoinJava / HashEquiInnerJoinKotlin.
2. Test your implementation by running `gradle test --tests "exercise3.HashEquiInnerJoinImplementationTests"` or using your IDE's test runner.
3. Pack your exercise with `gradle packExercise3` and upload the resulting .zip file to Moodle.

## Notes

- See notes from exercise 1 for the DBMS framework.
  - Also check out the JavaDocs (`gradle dokkaJavadoc` -> `build\dokka\javadoc\index.html`)
  - We have changed the structure and some methods of the DBMS framework. Please comment out any code from exercise 1 which does not compile anymore.
- Download the example relations from moodle and put them in `src/test/resources/exercise3/`
- Java provides the `hashCode()` method to get a good hash for every `Object`.
  - Be careful. It can be negative.
