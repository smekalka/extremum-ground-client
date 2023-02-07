package io.extremum.ground.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GroundClientApplication

fun main(args: Array<String>) {
    runApplication<GroundClientApplication>(*args)
}
