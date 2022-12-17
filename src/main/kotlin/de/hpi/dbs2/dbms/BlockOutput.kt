package de.hpi.dbs2.dbms

/**
 * An interface for moving tuples from a given block to the next operation or
 * into a backing data structure (typically a relation from which this BlockOutput has been obtained).
 */
interface BlockOutput {
    /**
     * Moves the tuples of the given block to the next operation or into a backing data structure.
     *
     * The given block **may not be used anymore in further operations** after
     * the execution of this method and will be [BlockManager.release]d from active memory.
     */
    fun move(outputBlock: Block)
}
