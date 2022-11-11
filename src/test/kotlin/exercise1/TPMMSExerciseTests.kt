package exercise1

import de.hpi.dbs2.dbms.*
import de.hpi.dbs2.exercise1.SortOperation
import de.hpi.dbs2.exerciseframework.getChosenImplementation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TPMMSExerciseTests {
    private fun getImplementation(blockManager: BlockManager, sortColumnIndex: Int): SortOperation = getChosenImplementation(
        TPMMSJava(blockManager, sortColumnIndex),
        TPMMSKotlin(blockManager, sortColumnIndex)
    )

    @Test
    fun `TPMMS sorts test file by column 0`() {
        val columnDefinition = ColumnDefinition(
            ColumnDefinition.ColumnType.INTEGER,
            ColumnDefinition.ColumnType.STRING,
            ColumnDefinition.ColumnType.DOUBLE,
        )

        with(DBMS(
            totalBlocks = 3,
            blockCapacity = 2
        )) {
            val inputRelation = loadRelation(
                blockManager, columnDefinition,
                TPMMSExerciseTests::class.java.getResourceAsStream("input.csv")!!,
            )
            val outputRelation = createOutputRelation(
                blockManager, columnDefinition
            )

            val cost = trackIOCost {
                val sortOperation = getImplementation(blockManager, 0)

                assert(blockManager.usedBlocks == 0)
                sortOperation.execute(inputRelation, outputRelation)
                assert(blockManager.usedBlocks == 0)
            }

            val controlRelation = loadRelation(
                blockManager, columnDefinition,
                TPMMSExerciseTests::class.java.getResourceAsStream("sorted_by_col0.output.csv")!!,
            )
            assertEquals(controlRelation.joinToString(), outputRelation.joinToString())

            assertEquals(3*6, cost.ioCost)
        }
    }

    @Test
    fun `TPMMS sorts test file by column 2`() {
        val columnDefinition = ColumnDefinition(
            ColumnDefinition.ColumnType.INTEGER,
            ColumnDefinition.ColumnType.STRING,
            ColumnDefinition.ColumnType.DOUBLE,
        )

        with(DBMS(
            totalBlocks = 3,
            blockCapacity = 2
        )) {
            val inputRelation = loadRelation(
                blockManager, columnDefinition,
                TPMMSExerciseTests::class.java.getResourceAsStream("input.csv")!!,
            )
            val outputRelation = createOutputRelation(
                blockManager, columnDefinition
            )

            val cost = trackIOCost {
                val sortOperation = getImplementation(blockManager, 2)

                assert(blockManager.usedBlocks == 0)
                sortOperation.execute(inputRelation, outputRelation)
                assert(blockManager.usedBlocks == 0)
            }

            val controlRelation = loadRelation(
                blockManager, columnDefinition,
                TPMMSExerciseTests::class.java.getResourceAsStream("sorted_by_col2.output.csv")!!,
            )
            assertEquals(controlRelation.joinToString(), outputRelation.joinToString())

            assertEquals(3*6, cost.ioCost)
        }
    }

    @Test
    fun `TPMMS returns error when relation is too large to sort`() {
        val columnDefinition = ColumnDefinition(
            ColumnDefinition.ColumnType.INTEGER,
        )

        with(DBMS(
            totalBlocks = 3,
            blockCapacity = 2
        )) {
            val inputRelation = object : Relation {
                val blocks = Array(13){
                    blockManager.allocate(false)
                }
                override val estimatedSize: Int = blocks.size

                override val columns: ColumnDefinition = columnDefinition
                override fun createTuple(): Tuple = TODO()
                override fun clear() = TODO()
                override fun iterator(): Iterator<Block> = blocks.iterator()
            }
            val outputRelation = createOutputRelation(
                blockManager, columnDefinition
            )

            val sortOperation = getImplementation(blockManager, 0)

            assertFailsWith<SortOperation.RelationSizeExceedsCapacityException> {
                sortOperation.execute(inputRelation, outputRelation)
            }
        }
    }
}
