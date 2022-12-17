package de.hpi.dbs2.exercise3

import de.hpi.dbs2.dbms.Tuple

interface JoinAttributePair {
    val leftColumnIndex: Int
    val rightColumnIndex: Int

    fun matches(
        leftTuple: Tuple,
        rightTuple: Tuple,
    ): Boolean

    data class EquiJoinAttributePair(
        override val leftColumnIndex: Int,
        override val rightColumnIndex: Int,
    ) : JoinAttributePair {
        override fun matches(
            leftTuple: Tuple,
            rightTuple: Tuple,
        ): Boolean = leftTuple[leftColumnIndex] == rightTuple[rightColumnIndex]
    }
}
