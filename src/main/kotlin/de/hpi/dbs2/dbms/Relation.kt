package de.hpi.dbs2.dbms

/**
 * A relation is a collection of blocks which might be loaded or not.
 * A relation is typically created by reading its file from disk using [DBMS.loadRelation].
 * You can use [RelationUtils] for additional functionality.
 */
interface Relation : Iterable<Block> {
    val columns: ColumnDefinition

    /**
     * @return an estimation of the number of blocks used by the relation.
     */
    fun estimatedBlockCount(): Int

    /**
     * Removes all tuples from this relation and unload all their blocks.
     */
    fun clear()

    /**
     * @return an iterator over all contained block references.
     */
    override fun iterator(): Iterator<Block>

    /**
     * @return a block output which appends given block references to this relation.
     */
    fun getBlockOutput(): BlockOutput
}
