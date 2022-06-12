package io.cloudflight.optimisticlocking.web

import io.cloudflight.optimisticlocking.api.PersonApi
import io.cloudflight.optimisticlocking.dto.PersonDto
import io.cloudflight.optimisticlocking.service.PersonService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.persistence.EntityNotFoundException
import javax.persistence.OptimisticLockException

@RestController
class PersonController(
    private val personService: PersonService
) : PersonApi {
    override fun getPerson(id: UUID): PersonDto = personService.getPerson(id)

    override fun createPerson(personDto: PersonDto): PersonDto = personService.createPerson(personDto)

    override fun updatePerson(personDto: PersonDto): PersonDto = personService.updatePerson(personDto)

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException() {
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(OptimisticLockException::class)
    fun handleOptimisticLockException() {
    }
}
