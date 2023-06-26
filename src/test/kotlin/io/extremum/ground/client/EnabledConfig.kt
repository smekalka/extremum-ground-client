package io.extremum.ground.client

object EnabledConfig {

    fun groundApiEnabled() = false
    fun storageApiEnabled() = false
}

const val ENABLED_GROUND_EXPRESSION = "#{T(io.extremum.ground.client.EnabledConfig).INSTANCE.groundApiEnabled()}"
const val ENABLED_STORAGE_EXPRESSION = "#{T(io.extremum.ground.client.EnabledConfig).INSTANCE.storageApiEnabled()}"