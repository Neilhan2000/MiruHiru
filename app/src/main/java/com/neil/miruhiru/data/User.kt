package com.neil.miruhiru.data

data class User(
    val name: String = "",
    val id: String = "",
    val blockList: List<String> = listOf(),
    val currentEvent: String = "",
    val icon: String = ""
)
