package exercise3

import de.hpi.dbs2.dbms.ColumnDefinition
import de.hpi.dbs2.dbms.DBMS
import de.hpi.dbs2.dbms.Relation
import de.hpi.dbs2.dbms.utils.RelationUtils.loadCSV
import de.hpi.dbs2.dbms.utils.RelationUtils.printlnAllBlocks
import de.hpi.dbs2.exercise3.JoinOperation
import de.hpi.dbs2.exercise3.NestedLoopEquiInnerJoin
import java.io.File

fun main() {
    val leftColumnDefinition = ColumnDefinition(
        ColumnDefinition.ColumnType.STRING,
        ColumnDefinition.ColumnType.STRING
    )
    val rightColumnDefinition = ColumnDefinition(
        ColumnDefinition.ColumnType.STRING,
        ColumnDefinition.ColumnType.STRING,
        ColumnDefinition.ColumnType.STRING,
        ColumnDefinition.ColumnType.STRING
    )

    with(
        DBMS(
            totalBlocks = 5,
            blockCapacity = 10
        )
    ) {
        val leftInputRelation = File("./src/test/resources/exercise3/title.basics.sample.tsv")
            .also { println("Using \"$it\" as input relation 1") }.inputStream()
            .use {
                createRelation(blockManager, leftColumnDefinition)
                    .apply {
                        loadCSV(
                            blockManager,
                            it,
                            columnIndices = listOf(0, 2),
                            delimiter = "\t",
                            containsHeader = true
                        )
                    }
            }
        val rightInputRelation = File("./src/test/resources/exercise3/title.principals.sample.tsv")
            .also { println("Using \"$it\" as input relation 2") }.inputStream()
            .use {
                createRelation(blockManager, rightColumnDefinition)
                    .apply {
                        loadCSV(
                            blockManager,
                            it,
                            columnIndices = listOf(0, 3, 4, 5),
                            delimiter = "\t",
                            containsHeader = true
                        )
                    }
            }

        println("Input relation 1:")
        leftInputRelation.printlnAllBlocks()
        println()
        println("Input relation 2:")
        rightInputRelation.printlnAllBlocks()
        println()

        val leftColumnIndex = 0
        val rightColumnIndex = 0
        val nleij: JoinOperation =
            NestedLoopEquiInnerJoin(blockManager, leftColumnIndex, rightColumnIndex)
        val hashBucketCount = 10
        val heij: JoinOperation =
            HashEquiInnerJoinJava(blockManager, leftColumnIndex, rightColumnIndex)

        val outputColumnDefinition = nleij.buildOutputColumns(leftInputRelation, rightInputRelation)
        // val outputColumnDefinition = heij.buildOutputColumns(leftInputRelation, rightInputRelation);
        val outputRelation: Relation = createRelation(blockManager, outputColumnDefinition)

        nleij.join(leftInputRelation, rightInputRelation, outputRelation)
        // heij.join(inputRelation1, inputRelation2, outputRelation)

        println("OutputRelation:")
        outputRelation.printlnAllBlocks()
    }
}
