package io.cloudflight.optimisticlocking.entity

import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Version

@Entity
class Person(
    @Id @JdbcTypeCode(SqlTypes.CHAR) var id: UUID,
    var name: String,
    var address: String
) {
    @Version
    var version: Long? = null
}