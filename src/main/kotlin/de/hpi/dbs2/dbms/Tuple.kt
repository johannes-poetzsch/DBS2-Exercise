package de.hpi.dbs2.dbms

import java.util.Arrays

/**
 * A tuple is a data construct with [columnCount] entries comparable to an array.
 *
 * You can use [ColumnDefinition.createTuple] to create a typed tuple which checks that
 * the inserted values match their respective column data types.
 */
open class Tuple(
    val columnCount: Int
) : Iterable<Any?> {
    private val values: Array<Any?> = arrayOfNulls(columnCount)

    constructor(columnCount: Int, vararg values: Any?) : this(columnCount) {
        require(values.size <= columnCount)
        ColumnRange.fromColumnCount(columnCount).forEach{ columnIndex ->
            this.values[columnIndex] = values[columnIndex]
        }
    }

    /**
     * @throws IndexOutOfBoundsException if the [columnIndex] is invalid
     */
    operator fun get(columnIndex: Int): Any? =
        values[columnIndex]

    /**
     * @throws IndexOutOfBoundsException if the [columnIndex] is invalid.
     */
    open operator fun set(columnIndex: Int, value: Any?) {
        values[columnIndex] = value
    }

    /**
     * @return an iterator over each column's value.
     */
    override fun iterator(): Iterator<Any?> = values.iterator()

    /**
     * Copies all values of [sourceRange] columns from this tuple
     * into the [targetRange] columns of the other tuple.
     */
    fun copyInto(
        other: Tuple,
        sourceRange: ColumnRange = ColumnRange.fromColumnCount(columnCount),
        targetRange: ColumnRange = ColumnRange.fromColumnCount(columnCount),
    ) {
        require(sourceRange.columnCount() == targetRange.columnCount())
        sourceRange.forEach{ columnIndex ->
            other[targetRange.startColumnIndex + columnIndex] = this[columnIndex]
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tuple) return false

        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }

    override fun toString(): String =
        "Tuple[${values.joinToString(separator = ", ")}]"
}

