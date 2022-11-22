package com.neil.miruhiru.data

import com.squareup.moshi.Json

data class Feature(
    val bbox: List<Double> = listOf(),
    val center: List<Double> = listOf(),
    val context: List<Context> = listOf(),
    val geometry: Geometry,
    val id: String = "",
    @Json(name = "matching_place_name")
    val matchPlaceName: String = "",
    @Json(name = "matching_text")
    val matchingText: String = "",
    @Json(name = "place_name")
    val placeName: String = "",
    @Json(name = "place_type")
    val placeType: List<String> = listOf(),
    val properties: Properties,
    val relevance: Double = 0.0,
    val text: String = "",
    val type: String = ""
)