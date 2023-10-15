package net.minevn.libs

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

fun http(
    url: String,
    method: String,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null
) : String {
    val body = parameters?.map { it.key + "=" + it.value }?.joinToString("&") ?: ""
    val httpsCon = (URL(url).openConnection() as HttpURLConnection).apply {
        doOutput = true
        doInput = true
        instanceFollowRedirects = false
        requestMethod = method
        setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        setRequestProperty("charset", "utf-8")
        setRequestProperty("Content-Length", body.byteInputStream().readAllBytes().size.toString())
        headers?.forEach { (k, v) ->
            setRequestProperty(k, v)
        }

        useCaches = false
        connect()
    }

    DataOutputStream(httpsCon.outputStream).apply {
        write(body.byteInputStream().readAllBytes())
        close()
    }

    val result = StringBuilder()
    val reader = BufferedReader(InputStreamReader(httpsCon.inputStream))
    reader.lines().forEach {
        result.append(it)
    }

    httpsCon.disconnect()
    return result.toString()
}

fun get(
    url: String,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null
) = http(url, "GET", parameters, headers)

fun post(
    url: String,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null
) = http(url, "POST", parameters, headers)