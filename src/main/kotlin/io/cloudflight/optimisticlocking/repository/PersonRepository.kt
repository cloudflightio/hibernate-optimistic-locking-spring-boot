package io.cloudflight.optimisticlocking.repository

import io.cloudflight.optimisticlocking.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PersonRepository: JpaRepository<Person, UUID>