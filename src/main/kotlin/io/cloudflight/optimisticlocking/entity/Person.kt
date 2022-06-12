package io.cloudflight.optimisticlocking.entity

import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Version

@Entity
class Person(
    @Id @Type(type = "uuid-char") var id: UUID,
    var name: String,
    var address: String
) {
    @Version
    var version: Long? = null
}