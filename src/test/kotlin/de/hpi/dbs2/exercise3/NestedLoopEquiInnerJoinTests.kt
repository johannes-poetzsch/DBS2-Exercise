package de.hpi.dbs2.exercise3

import de.hpi.dbs2.dbms.ColumnDefinition
import de.hpi.dbs2.dbms.DBMS
import de.hpi.dbs2.dbms.Tuple
import de.hpi.dbs2.dbms.utils.RelationUtils
import de.hpi.dbs2.dbms.utils.RelationUtils.fill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NestedLoopEquiInnerJoinTests {
    @Test
    fun `NLEIJoin joins test relations`() {
        val columnDefinition1 = ColumnDefinition(
            ColumnDefinition.ColumnType.INTEGER,
            ColumnDefinition.ColumnType.STRING,
        )
        val columnDefinition2 = ColumnDefinition(
            ColumnDefinition.ColumnType.STRING,
            ColumnDefinition.ColumnType.INTEGER,
        )

        with(
            DBMS(
                totalBlocks = 3,
                blockCapacity = 2
            )
        ) {
            val leftInputRelation = createRelation(blockManager, columnDefinition1).apply {
                fill(blockManager) {
                    it.add(Tuple(2, 10, "a")) // block 1
                    it.add(Tuple(2, 12, "b"))
                    it.add(Tuple(2, 14, "c")) // block 2
                    it.add(Tuple(2, 16, "d"))
                    it.add(Tuple(2, 18, "e")) // block 3
                    it.add(Tuple(2, 20, "f"))
                    it.add(Tuple(2, 22, "g")) // block 4
                    it.add(Tuple(2, 24, "h"))
                }
            }
            val rightInputRelation = createRelation(blockManager, columnDefinition2).apply {
                fill(blockManager) {
                    it.add(Tuple(2, "b",  9)) // block 1
                    it.add(Tuple(2, "d", 10))
                    it.add(Tuple(2, "f", 11)) // block 2
                    it.add(Tuple(2, "h", 12))
                }
            }

            val join1 = NestedLoopEquiInnerJoin(
                blockManager,
                0, 1,
            )
            val outputColumnDefinition1 =
                join1.buildOutputColumns(leftInputRelation, rightInputRelation)
            val outputRelation1 = createRelation(blockManager, outputColumnDefinition1)
            val cost1 = trackIOCost {
                assert(blockManager.usedBlocks == 0)
                join1.join(leftInputRelation, rightInputRelation, outputRelation1)
                assert(blockManager.usedBlocks == 0)
            }

            assertEquals(10, join1.estimatedIOCost(leftInputRelation, rightInputRelation))
            assertEquals(10, cost1.ioCost)

            val expectedColumnDefinition1 = ColumnDefinition(
                ColumnDefinition.ColumnType.INTEGER,
                ColumnDefinition.ColumnType.STRING,
                ColumnDefinition.ColumnType.STRING,
                ColumnDefinition.ColumnType.INTEGER,
            )
            assertEquals(expectedColumnDefinition1, outputColumnDefinition1)
            val expectedOutputRelation1 = createRelation(blockManager, columnDefinition1).apply {
                fill(blockManager) {
                    it.add(Tuple(4, 10, "a", "d", 10))
                    it.add(Tuple(4, 12, "b", "h", 12))
                }
            }
            assertTrue(RelationUtils.equalContent(blockManager, expectedOutputRelation1, outputRelation1))

            val join2 = NestedLoopEquiInnerJoin(
                blockManager,
                0, 1,
            )
            val outputColumnDefinition2 = join2.buildOutputColumns(rightInputRelation, leftInputRelation)
            val outputRelation2 = createRelation(blockManager, outputColumnDefinition2)
            val cost2 = trackIOCost {
                assert(blockManager.usedBlocks == 0)
                join2.join(rightInputRelation, leftInputRelation, outputRelation2)
                assert(blockManager.usedBlocks == 0)
            }

            assertEquals(10, join2.estimatedIOCost(rightInputRelation, leftInputRelation))
            assertEquals(10, cost2.ioCost)

            val expectedColumnDefinition2 = ColumnDefinition(
                ColumnDefinition.ColumnType.STRING,
                ColumnDefinition.ColumnType.INTEGER,
                ColumnDefinition.ColumnType.INTEGER,
                ColumnDefinition.ColumnType.STRING,
            )
            assertEquals(expectedColumnDefinition2, outputColumnDefinition2)
            val expectedOutputRelation2 = createRelation(blockManager, columnDefinition2).apply {
                fill(blockManager) {
                    it.add(Tuple(4, "b", 9, 12, "b"))
                    it.add(Tuple(4, "d", 10, 16, "d"))
                    it.add(Tuple(4, "f", 11, 20, "f"))
                    it.add(Tuple(4, "h", 12, 24, "h"))
                }
            }
            assertTrue(RelationUtils.equalContent(blockManager, expectedOutputRelation2, outputRelation2))
        }
    }
}
