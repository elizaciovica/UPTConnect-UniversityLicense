package edu.licenta.uptconnect.model

data class EnrollRequest(
    val id: String,
    val courseId: String,
    val courseEnrollRequestStatus: String,
    val studentId: String,
    val studentName: String,
    val courseName: String
)