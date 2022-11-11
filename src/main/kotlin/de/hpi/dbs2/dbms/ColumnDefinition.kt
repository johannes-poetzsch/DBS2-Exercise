package de.hpi.dbs2.dbms

class ColumnDefinition(
    private vararg val columnTypes: ColumnType
) {
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
    }

    val columnCount: Int
        get() = columnTypes.size

    fun getColumnType(columnIndex: Int): ColumnType {
        return columnTypes[columnIndex]
    }

    fun getColumnComparator(columnIndex: Int): Comparator<Tuple> {
        val type = getColumnType(columnIndex)
        return compareBy { tuple ->
            type.toComparable(tuple[columnIndex])
        }
    }
}
