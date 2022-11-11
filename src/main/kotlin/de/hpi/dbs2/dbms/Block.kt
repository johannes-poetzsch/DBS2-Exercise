package de.hpi.dbs2.dbms

/**
 * A block that contains tuples.
 * It depends on the loaded state of this block whether its tuples can be accessed or not.
 *
 * A block can be allocated or loaded via a [BlockManager] to which it then belongs.
 * It can also be freed via it's creating [BlockManager] or using [close].
 */
interface Block : AutoCloseable, Iterable<Tuple> {
    /**
     * true if the block is currently loaded into memory
     */
    fun isLoaded(): Boolean

    /**
     * number of tuples this block currently contains
     * @throws IllegalStateException if block is not loaded
     */
    val size: Int

    /**
     * number of tuples this block can contain
     */
    val capacity: Int

    fun isEmpty(): Boolean
    fun isFull(): Boolean

    /**
     * gets the tuple at the given index
     * @throws IllegalStateException if block is not loaded
     */
    operator fun get(tupleIndex: Int): Tuple

    /**
     * Remove all tuples from this block.
     * The block will be empty after this operation.
     */
    fun clear()

    /**
     * appends a tuple at the end of the block and returns it
     * @throws IllegalStateException if size >= capacity or if block is not loaded
     */
    fun append(tuple: Tuple): Tuple

    /**
     * inserts a tuple at the given position and returns it
     * @throws IllegalStateException if size >= capacity or if block is not loaded
     */
    fun insert(tupleIndex: Int, tuple: Tuple): Tuple

    /**
     * iterator for iterating over all contained tuples
     */
    override fun iterator(): Iterator<Tuple>

    /**
     * free this block and return it to it's [BlockManager]'s block pool
     */
    override fun close()
}
