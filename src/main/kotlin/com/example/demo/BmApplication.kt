package com.example.demo

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.core.env.AbstractEnvironment

@SpringBootApplication
class BmApplication

fun main(args: Array<String>) {
    val defaultProfile = "local"
    if (System.getProperty(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME).isNullOrBlank()) {
        System.setProperty(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME, defaultProfile)
    }

    SpringApplicationBuilder(BmApplication::class.java)
        .bannerMode(Banner.Mode.CONSOLE)
        .listeners(ProfileLoggingApplicationListener())
        .run(*args)
}
