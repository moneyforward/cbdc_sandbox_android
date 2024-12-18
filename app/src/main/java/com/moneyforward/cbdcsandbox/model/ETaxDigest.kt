package com.moneyforward.cbdcsandbox.model

data class ETaxDigest(
    val loginDigestInfo: String,
    val documentDigests: List<DocumentDigest>
)

data class DocumentDigest(
    val documentDigestInfo: String,
    val documentDigestValue: String
)