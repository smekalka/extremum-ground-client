package io.extremum.ground.client

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan
@PropertySource("classpath:ground.client.properties")
@ConditionalOnProperty(prefix = "extremum.ground.client", name = ["autoconfiguration"], havingValue = "true", matchIfMissing = true)
class GroundClientConfiguration