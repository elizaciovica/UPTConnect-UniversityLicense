package edu.licenta.uptconnect.model

data class Document(
    var documentUrl: String,
    val documentName: String,
    val fileType: FileType
)
