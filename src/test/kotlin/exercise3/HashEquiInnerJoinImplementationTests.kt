package exercise3

import de.hpi.dbs2.dbms.BlockManager
import de.hpi.dbs2.dbms.ColumnDefinition
import de.hpi.dbs2.dbms.DBMS
import de.hpi.dbs2.dbms.Operation
import de.hpi.dbs2.dbms.utils.RelationUtils.loadCSV
import de.hpi.dbs2.dbms.utils.RelationUtils.tupleIterator
import de.hpi.dbs2.exercise3.JoinOperation
import de.hpi.dbs2.exercise3.NestedLoopEquiInnerJoin
import de.hpi.dbs2.exerciseframework.getChosenImplementation
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HashEquiInnerJoinImplementationTests {
    private fun getImplementation(
        blockManager: BlockManager,
        leftColumnIndex: Int,
        rightColumnIndex: Int
    ): JoinOperation =
        getChosenImplementation(
            HashEquiInnerJoinJava(blockManager, leftColumnIndex, rightColumnIndex),
            HashEquiInnerJoinKotlin(blockManager, leftColumnIndex, rightColumnIndex)
        )

    @Test
    fun testJoin() {
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
                totalBlocks = 12,
                blockCapacity = 10
            )
        ) {
            val leftInputRelation = createRelation(
                blockManager, leftColumnDefinition,
            ).apply {
                loadCSV(
                    blockManager,
                    HashEquiInnerJoinImplementationTests::class.java.getResourceAsStream("title.basics.sample.tsv")!!,
                    columnIndices =  listOf(0, 2),
                    delimiter =  "\t",
                    containsHeader = true
                )
            }
            val rightInputRelation = createRelation(
                blockManager, rightColumnDefinition,
            ).apply {
                loadCSV(
                    blockManager,
                    HashEquiInnerJoinImplementationTests::class.java.getResourceAsStream("title.principals.sample.tsv")!!,
                    columnIndices =  listOf(0, 3, 4, 5),
                    delimiter =  "\t",
                    containsHeader = true
                )
            }

            val leftColumnIndex = 0
            val rightColumnIndex = 0
            val nleij: JoinOperation = NestedLoopEquiInnerJoin(blockManager, leftColumnIndex, rightColumnIndex)

            val nleijOutputRelation = createRelation(
                blockManager, nleij.buildOutputColumns(leftInputRelation, rightInputRelation)
            )
            assert(blockManager.usedBlocks == 0)
            nleij.join(leftInputRelation, rightInputRelation, nleijOutputRelation)
            assert(blockManager.usedBlocks == 0)

            val heij: JoinOperation = getImplementation(blockManager, leftColumnIndex, rightColumnIndex)
            val heijOutputRelation = createRelation(
                blockManager, heij.buildOutputColumns(leftInputRelation, rightInputRelation)
            )
            val cost = trackIOCost {
                assert(blockManager.usedBlocks == 0)
                heij.join(leftInputRelation, rightInputRelation, heijOutputRelation)
                assert(blockManager.usedBlocks == 0)
            }
            assertEquals(3300, heij.estimatedIOCost(leftInputRelation, rightInputRelation))
            assertTrue(cost.inputCost <= 3300)
            assertTrue(cost.outputCost <= 1650)

            assertEquals(nleijOutputRelation.estimatedBlockCount(), heijOutputRelation.estimatedBlockCount())
            assertEquals(
                nleijOutputRelation.tupleIterator(blockManager).asSequence().toSet(),
                heijOutputRelation.tupleIterator(blockManager).asSequence().toSet(),
            )
        }
    }

    @Test
    fun testJoinThrowsExceptionIfNotEnoughMemory() {
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
                totalBlocks = 11,
                blockCapacity = 10
            )
        ) {
            val leftInputRelation = createRelation(
                blockManager, leftColumnDefinition,
            ).apply {
                loadCSV(
                    blockManager,
                    HashEquiInnerJoinImplementationTests::class.java.getResourceAsStream("title.basics.sample.tsv")!!,
                    columnIndices =  listOf(0, 2),
                    delimiter =  "\t",
                    containsHeader = true
                )
            }
            val rightInputRelation = createRelation(
                blockManager, rightColumnDefinition,
            ).apply {
                loadCSV(
                    blockManager,
                    HashEquiInnerJoinImplementationTests::class.java.getResourceAsStream("title.principals.sample.tsv")!!,
                    columnIndices =  listOf(0, 3, 4, 5),
                    delimiter =  "\t",
                    containsHeader = true
                )
            }

            val leftColumnIndex = 0
            val rightColumnIndex = 0
            val heij: JoinOperation = getImplementation(blockManager, leftColumnIndex, rightColumnIndex)
            val heijOutputRelation = createRelation(
                blockManager, heij.buildOutputColumns(leftInputRelation, rightInputRelation)
            )
            assertThrows<Operation.RelationSizeExceedsCapacityException> {
                heij.join(leftInputRelation, rightInputRelation, heijOutputRelation)
            }
            assert(blockManager.usedBlocks == 0)
        }
    }
}
