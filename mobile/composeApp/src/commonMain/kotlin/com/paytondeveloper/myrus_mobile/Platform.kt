package com.paytondeveloper.myrus_mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform