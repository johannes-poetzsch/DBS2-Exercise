package exercise1

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.dbms.*
import de.hpi.dbs2.dbms.utils.BlockSorter
import de.hpi.dbs2.exercise1.SortOperation
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@ChosenImplementation(true)
class TPMMSKotlin(
    manager: BlockManager,
    sortColumnIndex: Int
) : SortOperation(manager, sortColumnIndex) {
    override fun estimatedIOCost(relation: Relation): Int = TODO()

    override fun sort(relation: Relation, output: BlockOutput) {
        val maxBlocksPerList = blockManager.freeBlocks
        // one block has to be reserved for the output block
        val maxSortedLists = blockManager.freeBlocks - 1

        if (maxBlocksPerList * maxSortedLists < relation.estimatedSize) {
            throw RelationSizeExceedsCapacityException();
        }

        val listCount = min(maxSortedLists, relation.estimatedSize)
        val blocksPerList = ceil(relation.estimatedSize / listCount.toFloat()).toInt()

        val iterator = relation.iterator()
        val comparator = relation.columns.getColumnComparator(sortColumnIndex)
        val lists : List<MutableList<Block>> = List<MutableList<Block>>(listCount) { mutableListOf<Block>() }

        for(listIndex : Int in 0 until listCount) {
            val currentList = lists[listIndex]
            repeat(blocksPerList) {
                if (iterator.hasNext()) currentList.add(blockManager.load(iterator.next()))
            }
            BlockSorter.sort(relation, currentList, comparator)

            for (blockIndex in 0 until currentList.size) {
                currentList[blockIndex] = blockManager.release(currentList[blockIndex], true)!!
            }
        }

        val listIterators = (0 until listCount).map { lists[it].iterator() }
        val listIndices : MutableSet<Int> = (0 until listCount).toMutableSet()

        var outputBlock : Block = blockManager.allocate(true)
        val loadedBlocks = ((0 until listCount).map { blockManager.load(listIterators[it].next()) }).toMutableList()
        val blockIterators = ((0 until listCount).map { loadedBlocks[it].iterator() }).toMutableList()
        val currentTuples = mutableMapOf<Tuple, Int>()
        for(index in 0 until listCount) currentTuples[blockIterators[index].next()] = index

        while (listIndices.isNotEmpty()) {
            val minTuple = currentTuples.minOfWith(comparator) { entry -> entry.key }
            val index = currentTuples[minTuple]!!
            currentTuples.remove(minTuple)

            outputBlock.append(minTuple)
            if(outputBlock.isFull()) output.output(outputBlock)

            if(!blockIterators[index].hasNext()) {
                blockManager.release(loadedBlocks[index], false)

                if(listIterators[index].hasNext()) {
                    loadedBlocks[index] = blockManager.load(listIterators[index].next())
                    blockIterators[index] = loadedBlocks[index].iterator()
                } else {
                    listIndices.remove(index)
                    continue
                }
            }
            currentTuples[blockIterators[index].next()] = index
        }
        if(!outputBlock.isEmpty()) output.output(outputBlock)
        blockManager.release(outputBlock, false)
    }
}
