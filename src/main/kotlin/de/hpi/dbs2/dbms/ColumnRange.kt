package de.hpi.dbs2.dbms

data class ColumnRange(
    /**
     * inclusive
     */
    val startColumnIndex: Int,
    /**
     * inclusive
     */
    val endColumnIndex: Int,
) : Iterable<Int> {
    companion object {
        fun fromColumnCount(columnCount: Int): ColumnRange =
            ColumnRange(0, columnCount - 1)
    }

    /**
     * @return how many columns this range spans.
     */
    fun columnCount(): Int = endColumnIndex + 1 - startColumnIndex

    fun iterate(tuple: Tuple): Iterator<Any?> = iterator {
        forEach { i ->
            yield(tuple[i])
        }
    }

    operator fun plus(displacement: Int): ColumnRange =
        ColumnRange(displacement + startColumnIndex, displacement + endColumnIndex)

    override fun iterator(): Iterator<Int> = (startColumnIndex..endColumnIndex).iterator()
}
