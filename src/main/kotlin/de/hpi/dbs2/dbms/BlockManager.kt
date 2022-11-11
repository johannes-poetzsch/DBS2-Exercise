package de.hpi.dbs2.dbms

interface BlockManager {
    /**
     * allocated block count
     */
    val usedBlocks: Int

    /**
     * available block count
     */
    val freeBlocks: Int

    /**
     * Allocate an empty block.
     * @param inMemory If true this block will be allocated in memory;
     *                 otherwise this will return a block reference which can be loaded using [load].
     */
    fun allocate(inMemory: Boolean): Block

    /**
     * Free a block from memory.
     * @param block The given block must be loaded in memory.
     * @param saveToDisk If true, this counts as 1 disk write operation.
     * @return If saveToDisk is true, a reference to this block, which is not in memory;
     *         otherwise null.
     */
    fun release(block: Block, saveToDisk: Boolean): Block?

    /**
     * Load the given block into memory. This counts as 1 disk read operation.
     * @param blockReference a block which is not loaded in memory.
     *        This blockReference should not be used anymore afterwards.
     * @return The loaded block in memory.
     */
    fun load(blockReference: Block): Block

    class OutOfMemoryException : Exception()
}
