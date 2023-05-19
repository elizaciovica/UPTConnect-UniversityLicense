package edu.licenta.uptconnect.model

data class Body(
    val bodyName: String,
    val floor: MutableList<Floor>
) {
    constructor() : this("", mutableListOf())
}
