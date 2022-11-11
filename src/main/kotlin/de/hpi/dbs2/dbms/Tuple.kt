package de.hpi.dbs2.dbms

interface Tuple : Iterable<Any?> {
    val columnCount: Int

    operator fun get(columnIndex: Int): Any?

    /**
     * @throws IllegalArgumentException if value type does not match column type
     */
    operator fun set(columnIndex: Int, value: Any?)

    /**
     * iterator for iterating over each column's value
     */
    override fun iterator(): Iterator<Any?>

    /**
     * copies values of all columns to other tuple
     */
    fun copyInto(other: Tuple)
}
