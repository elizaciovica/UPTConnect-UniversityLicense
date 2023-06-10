package edu.licenta.uptconnect.util

import edu.licenta.uptconnect.model.PollChoice
import edu.licenta.uptconnect.model.ScheduleData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CoursePlacesAssignmentUtilTest {

    @Test
    fun pollResultsAlgorithm_1() {
        val mock = CoursePlacesAssignmentUtil()
        val pollChoices = createPollChoices()
        val schedules = createSchedules()

        val result = mock.executePollResultsAlgorithm(pollChoices, schedules)

        val expected = createExpectedResult(schedules)
        assertEquals(expected, result)
    }

    private fun createExpectedResult(schedules: List<ScheduleData>): HashMap<ScheduleData, MutableList<String>> {
        return hashMapOf(
            Pair(schedules[1], mutableListOf("Florin345", "Alex234")),
            Pair(schedules[0], mutableListOf("Eliza123"))
        )
    }

    private fun createPollChoices(): MutableList<PollChoice> {
        val mondaySchedule = "Mon 10 12"
        val tuesdaySchedule = "Tue 8 10"

        val optionsEliza = hashMapOf(
            Pair(mondaySchedule, 0),
            Pair(tuesdaySchedule, 1)
        )

        val optionsAlex = hashMapOf(
            Pair(mondaySchedule, 0),
            Pair(tuesdaySchedule, 1)
        )

        val optionsFlorin = hashMapOf(
            Pair(tuesdaySchedule, 0),
            Pair(mondaySchedule, 1)
        )

        return mutableListOf(
            PollChoice("999", "0", "Florin345", optionsFlorin),
            PollChoice("999", "2", "Alex234", optionsAlex),
            PollChoice("999", "1", "Eliza123", optionsEliza)
        )
    }

    private fun createSchedules(): MutableList<ScheduleData> {
        return mutableListOf(
            ScheduleData("Mon", "10", "12", "1"),
            ScheduleData("Tue", "8", "10", "2")
        )
    }
}