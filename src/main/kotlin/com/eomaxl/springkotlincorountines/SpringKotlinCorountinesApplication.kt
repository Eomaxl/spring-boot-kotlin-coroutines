package com.eomaxl.springkotlincorountines

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcRepositories(value= arrayOf("com.eomaxl.coroutines","com.eomaxl.reactor"))
@EnableJpaRepositories
class SpringKotlinCorountinesApplication

fun main(args: Array<String>) {
    runApplication<SpringKotlinCorountinesApplication>(*args)
}
