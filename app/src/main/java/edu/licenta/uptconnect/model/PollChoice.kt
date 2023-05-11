package edu.licenta.uptconnect.model

data class PollChoice(
    val pollChoiceId: String,
    val timeOfVote: String,
    val votedBy: String,
    val optionOrder: HashMap<String, Int>
)
