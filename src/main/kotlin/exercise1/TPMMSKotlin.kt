package exercise1

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.dbms.BlockManager
import de.hpi.dbs2.dbms.Relation
import de.hpi.dbs2.exercise1.SortOperation
import java.util.PriorityQueue
import kotlin.math.ceil

@ChosenImplementation(true)
class TPMMSKotlin(
    manager: BlockManager,
    sortColumnIndex: Int
) : SortOperation(manager, sortColumnIndex) {

    private val memorySize = blockManager.usedBlocks + blockManager.freeBlocks
    inner class SortedBlockList(
        private val sortedBlocks: MutableList<Block>
    ) {
        private var loadedBlockIndex: Int = 0
        private var currentTupleIndex: Int = 0

        fun loadFirst(): Unit {
            blockManager.load(sortedBlocks[loadedBlockIndex])
        }

        fun getCurrentTuple(): Tuple = sortedBlocks[loadedBlockIndex][currentTupleIndex]
        fun saveTuple(sortedBlockLists: MutableSet<SortedBlockList>, outputBlock: Block, output: BlockOutput): Unit {
            val currentBlock = sortedBlocks[loadedBlockIndex]

            outputBlock.append(getCurrentTuple())

            if (currentBlock.size > currentTupleIndex + 1) {
                currentTupleIndex++
            } else {
                blockManager.release(currentBlock, false)

                if (sortedBlocks.size > loadedBlockIndex + 1) {
                    currentTupleIndex = 0
                    loadedBlockIndex++
                    blockManager.load(sortedBlocks[loadedBlockIndex])
                } else {
                    sortedBlockLists.remove(this)
                }
            }

            if (outputBlock.isFull()) {
                output.output(outputBlock)
            }
        }
    }

    // if the result is saved on disk: 4 * relation.estimatedSize
    override fun estimatedIOCost(relation: Relation): Int = 3 * relation.estimatedSize

    override fun sort(relation: Relation, output: BlockOutput) {
        if (relation.estimatedSize > blockManager.freeBlocks * (blockManager.freeBlocks - 1)) {
            throw RelationSizeExceedsCapacityException();
        }

        val sortedBlockLists = mutableSetOf<SortedBlockList>()

        relation
            .chunked(blockManager.freeBlocks)
            .forEach { blockList ->
                // while there is free memory load blocks into memory
                blockList.forEach { block ->
                    blockManager.load(block)
                }

                // sort block in memory
                BlockSorter.sort(relation, blockList, relation.columns.getColumnComparator(sortColumnIndex))

                // write blocks back to storage and save reference as List inside sortedLists
                val savedBlocks = mutableListOf<Block>()
                blockList.forEach { block ->
                    savedBlocks.add(blockManager.release(block, true)!!)
                }
                sortedBlockLists.add(SortedBlockList(savedBlocks))

            }

        // load the first block of each list and start comparing the tuples
        // write everything back to memory and commit the block to output
        val outputBlock = blockManager.allocate(true)
        val comparator = relation.columns.getColumnComparator(sortColumnIndex)
        sortedBlockLists.forEach { it.loadFirst() }

        while (sortedBlockLists.size > 0) {
            var listWithMinTuple = sortedBlockLists.minWith(
                compareBy(comparator) { list : SortedBlockList -> list.getCurrentTuple() }
            )

            listWithMinTuple.saveTuple(sortedBlockLists, outputBlock, output)
        }

        blockManager.release(outputBlock, false)
    }
}