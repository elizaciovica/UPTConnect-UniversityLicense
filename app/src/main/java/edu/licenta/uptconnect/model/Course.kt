package edu.licenta.uptconnect.model

data class Course(
    val id: String,
    val name: String,
    val section: String,
    val year: String,
    val mandatory: Boolean,
    val examination: String,
    val teachingWay: Any
)