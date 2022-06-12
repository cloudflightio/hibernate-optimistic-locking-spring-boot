package io.cloudflight.optimisticlocking.api

import io.cloudflight.optimisticlocking.dto.PersonDto
import org.springframework.web.bind.annotation.*
import java.util.*

interface PersonApi {
    @ResponseBody
    @GetMapping("$CONTEXT_PATH/person")
    fun getPerson(@RequestParam id: UUID): PersonDto

    @ResponseBody
    @PostMapping("$CONTEXT_PATH/person")
    fun createPerson(@RequestBody personDto: PersonDto): PersonDto

    @ResponseBody
    @PutMapping("$CONTEXT_PATH/person")
    fun updatePerson(@RequestBody personDto: PersonDto): PersonDto

    companion object {
        private const val CONTEXT_PATH = "/api"
    }
}