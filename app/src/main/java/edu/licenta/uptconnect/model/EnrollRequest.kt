package edu.licenta.uptconnect.model

data class EnrollRequest(
    val courseId: String,
    val courseEnrollRequestStatus: String,
    val studentId: String,
    val studentName: String,
    val courseName: String
) {
    constructor(): this("", "","","","")
}