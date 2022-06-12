package io.cloudflight.optimisticlocking.service.impl

import io.cloudflight.optimisticlocking.dto.PersonDto
import io.cloudflight.optimisticlocking.entity.Person
import io.cloudflight.optimisticlocking.repository.PersonRepository
import io.cloudflight.optimisticlocking.service.PersonService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*
import javax.persistence.EntityNotFoundException
import javax.persistence.OptimisticLockException
import javax.transaction.Transactional

@Service
@Transactional
class PersonServiceImpl(
    private val personRepository: PersonRepository
) : PersonService {
    override fun getPerson(id: UUID): PersonDto {
        val person = getOrThrow(id)
        return map(person)
    }

    override fun createPerson(personDto: PersonDto): PersonDto {
        val person = Person(personDto.id, personDto.name, personDto.address)
        return saveAndMap(person)
    }

    override fun updatePerson(personDto: PersonDto): PersonDto {
        val person = getOrThrow(personDto.id).also {
            if (it.version != personDto.version) {
                throw OptimisticLockException(it)
            }
            it.name = personDto.name
            it.address = personDto.address
        }

        return saveAndMap(person)
    }

    private fun getOrThrow(id: UUID) = personRepository.findByIdOrNull(id) ?: throw EntityNotFoundException()

    private fun map(person: Person) = PersonDto(person.id, person.name, person.address, person.version)

    // flush is necessary to get new version for dto mapping
    private fun saveAndMap(person: Person) = map(personRepository.saveAndFlush(person))
}