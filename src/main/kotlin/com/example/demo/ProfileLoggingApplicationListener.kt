package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener

class ProfileLoggingApplicationListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private val logger = LoggerFactory.getLogger(ProfileLoggingApplicationListener::class.java)

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val environment = event.environment
        val activeProfiles = environment.activeProfiles
        if (activeProfiles.isEmpty()) {
            logger.info(
                "No active profile set, running with default profile(s): {}",
                environment.defaultProfiles.contentToString()
            )
        } else {
            logger.info("Active profile(s): {}", activeProfiles.joinToString())
        }
    }
}
