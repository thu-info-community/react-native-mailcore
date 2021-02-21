package com.reactlibrary

import com.facebook.react.bridge.ReadableMap

class UserCredential(obj: ReadableMap) {
    val username: String = obj.getString("username")!!
    val password: String = obj.getString("password")!!
    val hostname: String = obj.getString("hostname")!!
    val port: Int = obj.getInt("port")
    val authType: Int = obj.getInt("authType")
}