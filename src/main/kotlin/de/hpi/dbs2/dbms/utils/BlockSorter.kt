package de.hpi.dbs2.dbms.utils

import de.hpi.dbs2.dbms.Block
import de.hpi.dbs2.dbms.Relation
import de.hpi.dbs2.dbms.Tuple

/**
- sort all tuples in given blocks by column index
 */
object BlockSorter {
    /**
     * sorts the given blocks' tuples "in-place" by the given column index
     */
    fun sort(relation: Relation, blocks: List<Block>, tupleComparator: Comparator<Tuple>) {
        val blockSize = blocks.first().size
        blocks
            .flatten()
            .sortedWith(tupleComparator)
            .chunked(blockSize)
            .forEachIndexed { i, tupleList ->
                blocks[i].apply {
                    clear()
                    tupleList.forEach {
                        append(it)
                    }
                }
            }
    }
}
