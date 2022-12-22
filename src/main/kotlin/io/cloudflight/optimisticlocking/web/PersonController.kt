package io.cloudflight.optimisticlocking.web

import io.cloudflight.optimisticlocking.api.PersonApi
import io.cloudflight.optimisticlocking.dto.PersonDto
import io.cloudflight.optimisticlocking.service.PersonService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.*
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.OptimisticLockException

@RestController
class PersonController(
    private val personService: PersonService
) : PersonApi {
    companion object {
        private val LOG = KotlinLogging.logger { }
    }

    override fun getPerson(id: UUID): PersonDto = personService.getPerson(id)

    override fun createPerson(personDto: PersonDto): PersonDto = personService.createPerson(personDto)

    override fun updatePerson(personDto: PersonDto): PersonDto = personService.updatePerson(personDto)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: Exception) {
        LOG.debug(ex) { "EntityNotFoundException handled in Controller" }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(value = [OptimisticLockException::class, ObjectOptimisticLockingFailureException::class])
    fun handleOptimisticLockException(ex: Exception) {
        LOG.debug(ex) { "OptimisticLockException handled in Controller" }
    }
}
