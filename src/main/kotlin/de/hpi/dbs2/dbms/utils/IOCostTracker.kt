package de.hpi.dbs2.dbms.utils

interface IOCostTracker {
    val inputCost: Int get() = 0
    val outputCost: Int get() = 0

    val ioCost: Int get() = inputCost + outputCost

    fun doInput() {
        // NOP
    }
    fun doOutput() {
        // NOP
    }
}
