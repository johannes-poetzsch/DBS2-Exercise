package de.hpi.dbs2.dbms

interface Operation {
    val blockManager: BlockManager

    fun estimatedIOCost(relation: Relation): Int
}

interface UnaryOperation : Operation {
    fun execute(inputRelation: Relation, outputRelation: OutputRelation)
}
