package exercise1

import de.hpi.dbs2.dbms.ColumnDefinition
import de.hpi.dbs2.dbms.DBMS
import de.hpi.dbs2.dbms.utils.RelationUtils
import de.hpi.dbs2.dbms.utils.RelationUtils.loadCSV
import de.hpi.dbs2.dbms.utils.RelationUtils.printlnAllBlocks
import java.io.File

fun main() {
    val columnDefinition = ColumnDefinition(
        ColumnDefinition.ColumnType.INTEGER,
        ColumnDefinition.ColumnType.STRING,
        ColumnDefinition.ColumnType.DOUBLE,
    )

    with(
        DBMS(
            totalBlocks = 3,
            blockCapacity = 2
        )
    ) {
        val inputRelation = File(".\\src\\test\\resources\\exercise1\\input.csv")
            .also { println("Using \"$it\" as input relation") }.inputStream()
            .use {
                createRelation(blockManager, columnDefinition)
                    .apply { loadCSV(blockManager, it) }
            }
        val outputRelation = createRelation(
            blockManager, columnDefinition
        )

        println("Input relation:")
        inputRelation.printlnAllBlocks()

        val sortColumnIndex = 0
        val sortOperation = TPMMSKotlin(blockManager, sortColumnIndex)

//         sortOperation.execute(inputRelation, outputRelation)

        println("Output relation:")
        outputRelation.printlnAllBlocks()
    }
}
