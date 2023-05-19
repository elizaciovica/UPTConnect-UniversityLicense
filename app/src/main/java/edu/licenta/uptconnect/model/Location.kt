package edu.licenta.uptconnect.model

data class Location(
    var Name: String,
    val bodies: MutableList<Body>
) {
    constructor() : this("", mutableListOf())
}
