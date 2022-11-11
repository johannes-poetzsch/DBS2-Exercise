package de.hpi.dbs2.exercise1

import de.hpi.dbs2.dbms.*

abstract class SortOperation(
    override val blockManager: BlockManager,
    val sortColumnIndex: Int
) : UnaryOperation {
    override fun execute(inputRelation: Relation, outputRelation: OutputRelation) {
        sort(inputRelation, outputRelation)
    }

    /**
     * @param output Outputs all tuples in the output block to the next operation.
     * The block will be [Block.clear]ed after calling this method.
     */
    abstract fun sort(
        relation: Relation,
        output: BlockOutput
    )

    /**
     * Relation is larger than the given sort algorithm can handle.
     */
    class RelationSizeExceedsCapacityException : IllegalArgumentException()
}
