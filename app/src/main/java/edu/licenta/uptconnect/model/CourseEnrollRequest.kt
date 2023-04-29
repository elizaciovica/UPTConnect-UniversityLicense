package edu.licenta.uptconnect.model

data class CourseEnrollRequest(
    val id: String,
    val name: String,
    val section: String,
    val year: String,
    val mandatory: Boolean,
    val examination: String,
    val teachingWay: Any,
    val studentId: String,
    val studentName: String,
    val requestStatus: CourseEnrollRequestStatus
) {
    constructor() : this("", "", "", "",false,"","","", "" +
            "",CourseEnrollRequestStatus.INITIAL)
}