package io.cloudflight.optimisticlocking.service

import io.cloudflight.optimisticlocking.dto.PersonDto
import java.util.*

interface PersonService {
    fun getPerson(id: UUID): PersonDto;
    fun createPerson(personDto: PersonDto): PersonDto
    fun updatePerson(personDto: PersonDto): PersonDto
}