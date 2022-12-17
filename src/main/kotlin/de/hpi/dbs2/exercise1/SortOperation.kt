package de.hpi.dbs2.exercise1

import de.hpi.dbs2.dbms.*

abstract class SortOperation(
    override val blockManager: BlockManager,
    /**
     * The column index by which to sort the tuples.
     */
    val sortColumnIndex: Int
) : UnaryOperation {
    override fun execute(
        inputRelation: Relation,
        outputRelation: Relation
    ) {
        sort(inputRelation, outputRelation)
    }

    /**
     * Sorts all tuples in the given relation by their values in column [sortColumnIndex].
     */
    abstract fun sort(
        inputRelation: Relation,
        outputRelation: Relation
    )
}
