package edu.licenta.uptconnect.model

import java.lang.reflect.Constructor

data class Floor(
    val floorNumber: String,
    val classes: MutableList<String>
) {
    constructor() : this("", mutableListOf())
}