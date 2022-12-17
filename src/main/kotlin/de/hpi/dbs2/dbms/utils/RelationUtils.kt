package de.hpi.dbs2.dbms.utils

import de.hpi.dbs2.dbms.Block
import de.hpi.dbs2.dbms.BlockManager
import de.hpi.dbs2.dbms.BlockOutput
import de.hpi.dbs2.dbms.Relation
import de.hpi.dbs2.dbms.Tuple
import java.io.InputStream
import java.io.PrintStream
import java.util.function.Consumer

object RelationUtils {
    @JvmStatic
    @JvmOverloads
    fun Relation.printlnAllBlocks(
        outputStream: PrintStream = System.out
    ) {
        forEach(outputStream::println)
    }

    /**
     * Creates a new relation and fills it with tuples from the given file
     * by appending block references (not loaded in memory!) for them.
     *
     * The given file will not be modified.
     *
     * Only loads the columns in [columnIndices] and maps them by their order into tuples -
     * additional columns are ignored.
     *
     * The relation's blocks can be loaded into memory using the BlockManager.
     */
    @JvmStatic
    @JvmOverloads
    fun Relation.loadCSV(
        blockManager: BlockManager,
        csvInputStream: InputStream,
        columnIndices: List<Int> = (0 until columns.columnCount).toList(),
        delimiter: String = ",",
        containsHeader: Boolean = false,
    ): Int {
        var lines = 0
        csvInputStream.bufferedReader().use { reader ->
            fill(blockManager) {
                reader.lineSequence().forEach { line ->
                    lines++
                    if (lines == 1 && containsHeader) return@forEach
                    val tuple = Tuple(columns.columnCount).also { tuple ->
                        val values = line.split(delimiter)
                        columnIndices.forEachIndexed { i, columnIndex ->
                            tuple[i] =
                                columns.getColumnType(i).fromString(values[columnIndex].trim())
                        }
                    }
                    it.add(tuple)
                }
            }
        }
        return lines
    }

    /**
     * You are _NOT_ allowed to use this method in the exercise.
     * You are allowed to take inspiration from this implementation.
     *
     * Creates a context in which a relation can be filled with new tuples by calling [RelationFiller.add].
     * While the RelationFiller context is active, a singular block will be allocated to which
     * the tuples are appended. Full blocks are appended to the relation.
     * The final block might be appended without being full.
     */
    @JvmStatic
    fun Relation.fill(blockManager: BlockManager, filler: Consumer<RelationFiller>) {
        RelationFiller(
            { blockManager.allocate(true) },
            getBlockOutput()
        ).use { filler.accept(it) }
    }

    class RelationFiller(
        private val allocateBlock: () -> Block,
        private val blockOutput: BlockOutput
    ) : AutoCloseable {
        private var currentBlock: Block = allocateBlock()

        fun add(tuple: Tuple) {
            if (currentBlock.isFull()) {
                blockOutput.move(currentBlock)
                currentBlock = allocateBlock()
            }
            currentBlock.append(tuple)
        }

        override fun close() {
            if (!currentBlock.isEmpty()) {
                blockOutput.move(currentBlock)
            } else {
                currentBlock.close()
            }
        }
    }

    /**
     * You are _NOT_ allowed to use this method in the exercise.
     * You are allowed to take inspiration from this implementation.
     *
     * Creates an iterator which loads each block one at a time and iterates the loaded tuples in them.
     * @return an iterator over all tuples in this relation.
     */
    @JvmStatic
    fun Relation.tupleIterator(blockManager: BlockManager): Iterator<Tuple> = iterator {
        forEach {
            blockManager.load(it).use { loadedBlock ->
                loadedBlock.forEach { tuple ->
                    yield(tuple)
                }
            }
        }
    }

    /**
     * You are _NOT_ allowed to use this method in the exercise.
     * You are allowed to take inspiration from this implementation.
     *
     * Compares both relation's tuples and their order for equality.
     * Will load blocks of each relation and iterate their tuples.
     * @return true if both relations contain the same tuples in the same order
     */
    @JvmStatic
    fun equalContent(
        blockManager: BlockManager,
        relation1: Relation,
        relation2: Relation
    ): Boolean {
        val it1 = relation1.tupleIterator(blockManager)
        val it2 = relation2.tupleIterator(blockManager)
        while (it1.hasNext() && it2.hasNext()) {
            if (it1.next() != it2.next()) return false
        }
        if (it1.hasNext()) return false
        if (it2.hasNext()) return false
        return true
    }
}
