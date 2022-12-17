package exercise3

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.dbms.BlockManager
import de.hpi.dbs2.dbms.Relation
import de.hpi.dbs2.exercise3.InnerJoinOperation
import de.hpi.dbs2.exercise3.JoinAttributePair

@ChosenImplementation(false)
class HashEquiInnerJoinKotlin(
    blockManager: BlockManager,
    leftColumnIndex: Int,
    rightColumnIndex: Int,
) : InnerJoinOperation(
    blockManager,
    JoinAttributePair.EquiJoinAttributePair(
        leftColumnIndex,
        rightColumnIndex
    )
) {
    override fun estimatedIOCost(
        leftInputRelation: Relation,
        rightInputRelation: Relation
    ): Int {
        TODO()
    }

    override fun join(
        leftInputRelation: Relation,
        rightInputRelation: Relation,
        outputRelation: Relation
    ) {
        val bucketCount: Int = 0
        // TODO:
        //  - calculate a sensible bucket count
        //  - hash relation
        //  - join hashed blocks

        TODO()
    }
}
