package edu.licenta.uptconnect.model

enum class CourseEnrollRequestStatus(val status: Int) {
    SENT(1),
    ACCEPTED(2),
    REJECTED(3),
    CANCELED(4),
    INITIAL(5)
}