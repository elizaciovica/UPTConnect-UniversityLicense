package edu.licenta.uptconnect.model

data class ScheduleData(
    val day: String,
    val startTime: String,
    val endTime: String,
    var maxNoOfStudents: String
)
