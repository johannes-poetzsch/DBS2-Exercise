package de.hpi.dbs2.dbms

class ColumnDefinition(
    val columnTypes: List<ColumnType>
) {
    constructor(vararg columnTypes: ColumnType) : this(columnTypes.toList())

    enum class ColumnType(
        val clazz: Class<*>,
        val toComparable: (value: Any?) -> Comparable<*>,
        val fromString: (input: String) -> Any,
    ) {
        INTEGER(
            Int::class.javaObjectType,
            { it as Int },
            { it.toInt() }
        ),
        DOUBLE(
            Double::class.javaObjectType,
            { it as Double },
            { it.toDouble() }
        ),
        STRING(
            String::class.java,
            { it as String },
            { it }
        );

        fun isCompatible(value: Any?): Boolean =
            when(value) {
                null -> true
                else -> clazz.isInstance(value)
            }

        /**
         * @throws IllegalArgumentException if the value does not match the columns data type.
         */
        fun requireCompatibility(value: Any?) {
            require(isCompatible(value)) {
                "column type \"$this\" does not match object type \"${value?.javaClass?.simpleName}\""
            }
        }
    }

    val columnCount: Int
        get() = columnTypes.size

    fun getColumnType(columnIndex: Int): ColumnType {
        return columnTypes[columnIndex]
    }

    /**
     * @return a reusable comparator for comparing tuples by the given [columnIndex].
     */
    fun getColumnComparator(columnIndex: Int): Comparator<Tuple> {
        val type = getColumnType(columnIndex)
        return compareBy { tuple ->
            type.toComparable(tuple[columnIndex])
        }
    }

    /**
     * Creates a new tuple which checks that the inserted values conform to the column data types.
     */
    fun createTuple(): Tuple = TypedTuple(this)

    class TypedTuple(
        val columnDefinition: ColumnDefinition
    ) : Tuple(columnDefinition.columnCount) {
        /**
         * @throws IndexOutOfBoundsException if the [columnIndex] is invalid.
         * @throws IllegalArgumentException if the value's type does not match the column type.
         */
        override operator fun set(columnIndex: Int, value: Any?) {
            columnDefinition.getColumnType(columnIndex).requireCompatibility(value)
            super.set(columnIndex, value)
        }
    }

    /**
     * @return a new [ColumnDefinition] which is a concatenation of this and the [other] column definition.
     */
    operator fun plus(other: ColumnDefinition): ColumnDefinition =
        ColumnDefinition(columnTypes + other.columnTypes)

    override fun toString(): String = "Columns[${columnTypes.joinToString(separator = ", ")}]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColumnDefinition) return false

        if (columnTypes != other.columnTypes) return false

        return true
    }

    override fun hashCode(): Int {
        return columnTypes.hashCode()
    }
}
