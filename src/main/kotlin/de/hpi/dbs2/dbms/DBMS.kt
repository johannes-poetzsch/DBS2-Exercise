package de.hpi.dbs2.dbms

import com.google.common.collect.Sets
import de.hpi.dbs2.dbms.utils.IOCostTracker

class DBMS(
    val totalBlocks: Int,
    val blockCapacity: Int,
) {
    private val BLOCK_MANAGER = BlockManagerImpl()
    val blockManager: BlockManager get() = BLOCK_MANAGER

    /**
     * @return a new and empty relation with the given [columnDefinition].
     */
    fun createRelation(
        blockManager: BlockManager,
        columnDefinition: ColumnDefinition,
    ): Relation = RelationImpl(
        blockManager,
        columnDefinition,
    )

    var ioCostTracker = object : IOCostTracker {}

    class IOCostTrackerImpl : IOCostTracker {
        override var inputCost: Int = 0
        override var outputCost: Int = 0

        override fun doInput() {
            inputCost++
        }

        override fun doOutput() {
            outputCost++
        }

        override fun toString(): String = "CostTracker[i=$inputCost,o=$outputCost,io=$ioCost]"
    }

    fun trackIOCost(context: IOCostTracker.() -> Unit): IOCostTracker =
        IOCostTrackerImpl().also {
            val prevTracker = ioCostTracker
            ioCostTracker = it
            it.context()
            ioCostTracker = prevTracker
        }

    private inner class BlockManagerImpl : BlockManager {
        override val usedBlocks: Int get() = activeMap.size
        override val freeBlocks: Int get() = totalBlocks - usedBlocks

        override fun allocate(inMemory: Boolean): Block =
            BlockImpl().also {
                if (inMemory)
                    setActive(it)
            }

        override fun release(block: Block, saveToDisk: Boolean): Block? {
            check(block.isLoaded()) { "block is not loaded in memory" }
            activeMap -= block
            return if (saveToDisk) {
                ioCostTracker.doOutput()
                block
            } else null
        }

        override fun load(blockReference: Block): Block {
            check(!blockReference.isLoaded()) { "block is already loaded in memory" }
            return setActive(blockReference).also {
                ioCostTracker.doInput()
            }
        }

        override fun toString(): String = "BlockManager[free=${freeBlocks}/${totalBlocks}]"

        private val activeMap: MutableSet<Block> = Sets.newIdentityHashSet()
        private fun isActive(block: Block): Boolean = block in activeMap
        private fun setActive(block: Block): Block = block.also {
            if (freeBlocks <= 0)
                throw BlockManager.OutOfMemoryException()
            activeMap += it
        }

        fun copyBlock(block: Block): Block = (block as BlockImpl).copy()

        private inner class BlockImpl(
            override val capacity: Int = blockCapacity,
            private val _tuples: MutableList<Tuple> = mutableListOf()
        ) : Block {
            fun copy(): Block = BlockImpl(
                capacity = this.capacity,
                _tuples = this._tuples.toMutableList()
            )

            override fun isLoaded(): Boolean = isActive(this)

            private val tuples: MutableList<Tuple>
                get() {
                    check(isLoaded()) { "block is not loaded in memory" }
                    return _tuples
                }

            override val size: Int get() = tuples.size
            override fun isEmpty(): Boolean = size <= 0
            override fun isFull(): Boolean = size >= capacity

            override fun get(tupleIndex: Int): Tuple = tuples[tupleIndex]
            override fun clear() = tuples.clear()
            override fun append(tuple: Tuple): Tuple = tuple.also {
                check(!isFull()) { "block is full" }
                tuples.add(it)
            }

            override fun insert(tupleIndex: Int, tuple: Tuple): Tuple = tuple.also {
                check(!isFull()) { "block is full" }
                tuples.add(tupleIndex, it)
            }

            override fun iterator(): Iterator<Tuple> = tuples.iterator()

            override fun close() {
                release(this, false)
            }

            override fun toString(): String =
                _tuples.joinToString(
                    ",",
                    prefix = "Block[loaded=${isLoaded()}]{",
                    postfix = "\n}"
                ) {
                    "\n  $it"
                }
        }
    }

    private inner class RelationImpl(
        val blockManager: BlockManager,
        override val columns: ColumnDefinition,
    ) : Relation {
        private val blocks = mutableListOf<Block>()

        override fun clear() {
            blocks.forEach {
                it.close()
            }
            blocks.clear()
        }

        override fun estimatedBlockCount(): Int = blocks.size
        override fun iterator(): Iterator<Block> = blocks.iterator()

        private val blockOutput = BlockOutputImpl()
        override fun getBlockOutput(): BlockOutput = blockOutput
        private inner class BlockOutputImpl : BlockOutput {
            override fun move(outputBlock: Block) {
                blocks.add(outputBlock)
                blockManager.release(outputBlock, false)
            }
        }
    }
}
