package com.neil.miruhiru.data

import com.squareup.moshi.Json

data class Context(
    val id: String = "",
    @Json(name = "short_code")
    val shortCode: String = "",
    val text: String = "",
    val wikidata: String = ""
)