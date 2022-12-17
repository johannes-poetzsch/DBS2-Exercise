package de.hpi.dbs2.exercise3

import de.hpi.dbs2.dbms.Block
import de.hpi.dbs2.dbms.BlockManager
import de.hpi.dbs2.dbms.ColumnDefinition
import de.hpi.dbs2.dbms.Relation
import de.hpi.dbs2.dbms.Tuple
import de.hpi.dbs2.dbms.ColumnRange
import java.util.function.Consumer

abstract class InnerJoinOperation(
    override val blockManager: BlockManager,
    /**
     * The attribute on which the two relations should be joined.
     */
    override val joinAttributePair: JoinAttributePair,
) : JoinOperation {

    /**
     * Use this [ColumnDefinition] for creating your output relation.
     * @return the [ColumnDefinition] of the result of merging both input relations.
     */
    override fun buildOutputColumns(
        leftInputRelation: Relation,
        rightInputRelation: Relation
    ): ColumnDefinition =
        leftInputRelation.columns + rightInputRelation.columns

    /**
     * Joins all tuples of the left and the right block using the set [joinAttributePairs].
     * Joined tuples are created using the given [outputColumns] definition
     * and are returned in the [tupleConsumer] for further processing (e.g. saving in a block).
     *
     * Make sure that both blocks are loaded when calling this method!
     */
    fun joinBlocks(
        leftBlock: Block,
        rightBlock: Block,
        outputColumns: ColumnDefinition,
        tupleConsumer: Consumer<Tuple>
    ) {
        assert(leftBlock.isLoaded())
        assert(rightBlock.isLoaded())

        leftBlock.forEach { leftTuple ->
            rightBlock.forEach { rightTuple ->
                if (joinAttributePair.matches(leftTuple, rightTuple)) {
                    tupleConsumer.accept(constructJoinedTuple(leftTuple, rightTuple, outputColumns))
                }
            }
        }
    }

    /**
     * @return a joined tuple with the columns from the left tuple followed by the columns of the right tuple.
     */
    fun constructJoinedTuple(
        leftTuple: Tuple,
        rightTuple: Tuple,
        outputColumns: ColumnDefinition,
    ): Tuple = outputColumns.createTuple().apply {
        leftTuple.copyInto(
            this,
            targetRange = ColumnRange.fromColumnCount(leftTuple.columnCount)
        )
        rightTuple.copyInto(
            this,
            targetRange = ColumnRange.fromColumnCount(rightTuple.columnCount) + leftTuple.columnCount
        )
    }
}
