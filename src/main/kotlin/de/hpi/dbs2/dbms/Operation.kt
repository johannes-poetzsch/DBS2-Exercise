package de.hpi.dbs2.dbms

/**
 * extending n-argument operations should contain the following methods:
 * - execute(n input relations, output relation)
 * - estimatedIOCost(n input relations): Int
 */
interface Operation {
    val blockManager: BlockManager

    /**
     * Relation is larger than the given sort algorithm can handle.
     */
    class RelationSizeExceedsCapacityException : IllegalArgumentException()
}

interface UnaryOperation : Operation {
    fun execute(
        inputRelation: Relation,
        outputRelation: Relation,
    )

    fun estimatedIOCost(inputRelation: Relation): Int
}

interface BinaryOperation : Operation {
    fun execute(
        leftInputRelation: Relation,
        rightInputRelation: Relation,
        outputRelation: Relation
    )

    fun estimatedIOCost(
        leftInputRelation: Relation,
        rightInputRelation: Relation,
    ): Int
}
