package io.cloudflight.optimisticlocking.dto

import java.util.*

class PersonDto(
    val id: UUID,
    val name: String,
    val address: String,
    val version: Long? = null
)