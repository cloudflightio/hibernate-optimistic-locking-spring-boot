package io.cloudflight.optimisticlocking

import com.ninjasquad.springmockk.SpykBean
import io.cloudflight.optimisticlocking.FeignTestClientFactory.createClientApi
import io.cloudflight.optimisticlocking.api.PersonApi
import io.cloudflight.optimisticlocking.dto.PersonDto
import io.cloudflight.optimisticlocking.repository.PersonRepository
import io.cloudflight.optimisticlocking.service.PersonService
import io.mockk.clearAllMocks
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.openfeign.FeignClientBuilder
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ExtendWith(OutputCaptureExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests(
    @LocalServerPort private val localServerPort: Int,
    @Autowired applicationContext: ApplicationContext,
    @Autowired private val personRepository: PersonRepository
) {
    @SpykBean
    private lateinit var personService: PersonService

    private val personApi = createClientApi(PersonApi::class.java, localServerPort, applicationContext)

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
        personRepository.deleteAllInBatch()
    }

    @Test
    fun `sunshine case create`() {
        // given
        val createDto =
            PersonDto(UUID.fromString("a426dbe8-e711-45cc-a2f7-5651dc2ea124"), "John Doe", "Doe Street 1")

        // when
        val createdDto = personApi.createPerson(createDto)

        // then
        assertThat(createdDto).usingRecursiveComparison().ignoringFields("version").isEqualTo(createDto)
        assertThat(createdDto.version).isEqualTo(0)
    }

    @Test
    fun `sunshine case update`() {
        // given
        val createDto =
            PersonDto(UUID.fromString("a426dbe8-e711-45cc-a2f7-5651dc2ea124"), "John Doe", "Doe Street 1")
        val createdDto = personApi.createPerson(createDto)

        // when
        val updateDto =
            PersonDto(createdDto.id, "${createdDto.name} update", "${createdDto.address} update", 0)
        val updatedDto = personApi.updatePerson(updateDto)

        // then
        assertThat(updatedDto).usingRecursiveComparison().ignoringFields("version").isEqualTo(updateDto)
        assertThat(updatedDto.version).isEqualTo(1)
    }

    @Test
    fun `concurrent long conversations`() {
        // given
        val createDto =
            PersonDto(UUID.fromString("a426dbe8-e711-45cc-a2f7-5651dc2ea124"), "John Doe", "Doe Street 1")
        val createdDto = personApi.createPerson(createDto)

        // when
        val updateDto =
            PersonDto(createdDto.id, "${createdDto.name} update", "${createdDto.address} update", 0)
        personApi.updatePerson(updateDto)

        // then
        assertThatThrownBy { personApi.updatePerson(updateDto) } // version is 1 in the meantime!
            .hasMessageContaining("${HttpStatus.UNPROCESSABLE_ENTITY.value()}")
    }

    @Test
    fun `concurrent database transactions`(output: CapturedOutput) {
        // given
        val createDto =
            PersonDto(UUID.fromString("a426dbe8-e711-45cc-a2f7-5651dc2ea124"), "John Doe", "Doe Street 1")
        val createdDto = personApi.createPerson(createDto)

        // when
        val executor = Executors.newFixedThreadPool(5)
        for (i in 1..5) {
            val updateDto =
                PersonDto(createdDto.id, "${createdDto.name} update $i", "${createdDto.address} update $i", 0)
            executor.execute {
                try {
                    personApi.updatePerson(updateDto)
                } catch (_: Exception) {
                    // ignore optimistic lock exceptions
                }
            }
        }
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        // then
        assertThat(personApi.getPerson(createDto.id).version).isEqualTo(1)
        verify(exactly = 5) { personService.updatePerson(any()) }
        assertThat(output.all).contains("ObjectOptimisticLockingFailureException: " +
                "Row was updated or deleted by another transaction")
    }
}

object FeignTestClientFactory {
    fun <T> createClientApi(apiClass: Class<T>, port: Int, clientContext: ApplicationContext): T {
        return FeignClientBuilder(clientContext)
            .forType(apiClass, apiClass.canonicalName)
            .url("http://localhost:$port")
            .build()
    }
}
