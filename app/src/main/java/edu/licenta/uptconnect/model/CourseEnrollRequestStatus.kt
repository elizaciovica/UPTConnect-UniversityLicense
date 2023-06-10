package edu.licenta.uptconnect.model

enum class CourseEnrollRequestStatus(val status: Int) {
    SENT(1),
    CANCELED(2),
    INITIAL(3)
}