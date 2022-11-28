package com.neil.miruhiru.data

data class SearchResponse(
    val attribution: String = "",
    val features: List<Feature> = listOf(),
    val query: List<String> = listOf(),
    val type: String = ""
)