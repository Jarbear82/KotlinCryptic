package org.tau.cryptic

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform