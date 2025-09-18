package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BmApplication

fun main(args: Array<String>) {
	runApplication<BmApplication>(*args)
}
