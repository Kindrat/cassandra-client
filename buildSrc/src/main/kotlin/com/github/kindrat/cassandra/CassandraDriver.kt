package com.github.kindrat.cassandra

import java.math.BigInteger
import java.net.DatagramSocket
import java.net.InetAddress
import java.security.MessageDigest

object CassandraDriver {
    val VERSION = "3.10.1"
}

object Network {
    val PUBLIC_HOST: Lazy<String> = lazy {
        if (System.getenv("PUBLIC_HOST") != null) {
            System.getenv("PUBLIC_HOST")
        } else {
            DatagramSocket().use { socket ->
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
                val hostAddress = socket.localAddress.hostAddress
                check(hostAddress != "0.0.0.0") {
                    "Failed to resolve public IP address. Please provide 'PUBLIC_HOST' env property."
                }
                hostAddress
            }
        }
    }
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}