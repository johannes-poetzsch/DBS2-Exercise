package de.hpi.dbs2.dbms

import com.google.common.collect.Sets
import de.hpi.dbs2.dbms.utils.IOCostTracker
import java.io.InputStream

class DBMS(
    val totalBlocks: Int,
    val blockCapacity: Int,
) {
    private val BLOCK_MANAGER = BlockManagerImpl()
    val blockManager: BlockManager get() = BLOCK_MANAGER

    fun createRelation(
        blockManager: BlockManager,
        columnDefinition: ColumnDefinition,
    ): Relation = RelationImpl(
        blockManager,
        columnDefinition,
    )

    /**
     * Creates a new relation and fills it with tuples from the given file
     * by appending block references (not loaded in memory!) for them.
     *
     * The given file will not be modified.
     *
     * The relation's blocks can be loaded into memory using the BlockManager.
     */
    fun loadRelation(
        blockManager: BlockManager,
        columnDefinition: ColumnDefinition,
        inputStream: InputStream,
    ): Relation = RelationImpl(
        blockManager,
        columnDefinition,
    ).apply {
        discoverTuples(inputStream)
    }

    fun createOutputRelation(
        blockManager: BlockManager,
        columnDefinition: ColumnDefinition,
    ): OutputRelation = RelationImpl(
        blockManager,
        columnDefinition,
    )

    var ioCostTracker = object : IOCostTracker {}
    class IOCostTrackerImpl: IOCostTracker {
        override var inputCost: Int = 0
        override var outputCost: Int = 0

        override fun doInput() {
            inputCost++
        }
        override fun doOutput() {
            outputCost++
        }
    }
    fun trackIOCost(context: IOCostTracker.() -> Unit): IOCostTracker
        = IOCostTrackerImpl().also {
            val prevTracker = ioCostTracker
            ioCostTracker = it
            it.context()
            ioCostTracker = prevTracker
        }

    private open inner class BlockManagerImpl : BlockManager {
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
                if (isLoaded())
                    tuples.joinToString(
                        ",",
                        prefix = "Block[loaded=true]{",
                        postfix = "\n}"
                    ) {
                        "\n  $it"
                    }
                else "Block[loaded=false]"
        }
    }

    context(Relation)
    private class TupleImpl : Tuple {
        private val values: Array<Any?> = arrayOfNulls(columns.columnCount)

        override val columnCount: Int get() = values.size

        override operator fun get(columnIndex: Int): Any? = values[columnIndex]

        override operator fun set(columnIndex: Int, value: Any?) {
            require(columns.getColumnType(columnIndex).clazz.isInstance(value)) {
                "column type \"${columns.getColumnType(columnIndex).clazz.simpleName}\" does not match object type \"${value?.javaClass?.simpleName}\""
            }
            values[columnIndex] = value
        }

        override fun iterator(): Iterator<Any?> = values.iterator()

        override fun copyInto(other: Tuple) {
            require(other.columnCount == this.columnCount)
            repeat(columnCount) { columnIndex ->
                other[columnIndex] = this[columnIndex]
            }
        }

        override fun toString(): String {
            return values.contentToString()
        }
    }

    private inner class RelationImpl(
        val blockManager: BlockManager,
        override val columns: ColumnDefinition,
    ) : OutputRelation {
        fun discoverTuples(
            inputStream: InputStream
        ) {
            fun appendNewBlock() = blockManager.allocate(false)
                .also {
                    blocks.add(it)
                    blockManager.load(it)
                }

            var currentBlock =
                if (blocks.isEmpty()) appendNewBlock()
                else blocks.last()
                    .also { blockManager.load(it) }
            inputStream.bufferedReader().use { reader ->
                reader.lineSequence().forEach { line ->
                    if (currentBlock.isFull()) {
                        currentBlock.close()
                        currentBlock = appendNewBlock()
                    }
                    val tuple = createTuple().also {
                        val values = line.split(",")
                        for (i in values.indices) {
                            it[i] = columns.getColumnType(i).fromString(values[i].trim())
                        }
                    }
                    currentBlock.append(tuple)
                }
            }
            currentBlock.close()
        }

        override fun output(outputBlock: Block) {
            blocks.add((blockManager as BlockManagerImpl).copyBlock(outputBlock))
            outputBlock.clear()
        }

        override fun createTuple(): Tuple = TupleImpl()

        private val blocks = mutableListOf<Block>()

        override fun clear() {
            blocks.forEach {
                it.close()
            }
            blocks.clear()
        }

        override fun iterator(): Iterator<Block> = blocks.iterator()

        override val estimatedSize: Int get() = blocks.size
    }
}
