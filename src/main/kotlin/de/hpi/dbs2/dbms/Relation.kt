package de.hpi.dbs2.dbms

/**
 * A relation is a collection of blocks which might be loaded or not.
 * A relation is typically created by reading its file from disk using [DBMS.loadRelation].
 */
interface Relation : Iterable<Block> {
    val columns: ColumnDefinition

    /**
     * an estimation of the number of blocks used by the relation
     */
    val estimatedSize: Int

    /**
     * create a new tuple which is not yet saved in any block
     */
    fun createTuple(): Tuple

    /**
     * remove all tuples from this relation and unload all their blocks
     */
    fun clear()

    /**
     * iterator for iterating over all contained block references
     */
    override fun iterator(): Iterator<Block>
}

interface OutputRelation : Relation, BlockOutput

fun interface BlockOutput {
    /**
     * Creates a block reference in which all tuples from the output block are copied into.
     * The output block will be [Block.clear]ed after calling this method.
     */
    fun output(outputBlock: Block)
}
