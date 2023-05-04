package io.extremum.ground.client

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@ConditionalOnProperty(prefix = "extremum.ground.client", name = ["autoconfiguration"], havingValue = "true", matchIfMissing = true)
class GroundClientConfiguration