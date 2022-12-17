package de.hpi.dbs2.exercise3

import de.hpi.dbs2.dbms.BinaryOperation
import de.hpi.dbs2.dbms.ColumnDefinition
import de.hpi.dbs2.dbms.Relation

interface JoinOperation : BinaryOperation {
    val joinAttributePair: JoinAttributePair

    override fun execute(
        leftInputRelation: Relation,
        rightInputRelation: Relation,
        outputRelation: Relation
    ) {
        join(leftInputRelation, rightInputRelation, outputRelation)
    }

    /**
     * Joins the left and the right relation's tuples on their [joinAttributePair].
     */
    fun join(
        leftInputRelation: Relation,
        rightInputRelation: Relation,
        outputRelation: Relation
    )

    /**
     * Use this [ColumnDefinition] for creating your output relation.
     * @return the [ColumnDefinition] of the result of merging both input relations.
     */
    fun buildOutputColumns(
        leftInputRelation: Relation,
        rightInputRelation: Relation
    ): ColumnDefinition
}
