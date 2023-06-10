package edu.licenta.uptconnect.model

data class New(
    val title: String,
    val content: String,
    val time: String,
    val courseId: String,
    val createdBy: String
) {
    constructor(): this( "", "", "", "", "")
}
