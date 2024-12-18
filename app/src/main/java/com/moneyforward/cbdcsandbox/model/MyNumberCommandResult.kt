package com.moneyforward.cbdcsandbox.model

data class MyNumberCommandResult(
    val loginSignature: String,
    val loginCertificate: String,
    val documentCertificate: String,
    val documentSignatures: List<DocumentSignature>
)

data class DocumentSignature(
    val documentSignature: String,
    val documentDigestValue: String
)