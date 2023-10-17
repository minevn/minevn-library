package net.minevn.libs

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.streams.asSequence

fun http(
    url: String,
    method: String,
    contentType: String = "application/x-www-form-urlencoded",
    body: String? = null,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null
) : String {
    val content = body ?: parameters?.map { it.key + "=" + it.value }?.joinToString("&") { it }
    val httpsCon = (URL(url).openConnection() as HttpURLConnection).apply {
        doOutput = true
        doInput = true
        instanceFollowRedirects = false
        requestMethod = method
        setRequestProperty("Content-Type", contentType)
        setRequestProperty("charset", "utf-8")
        if (content != null) {
            setRequestProperty("Content-Length", content.byteInputStream().readAllBytes().size.toString())
        }
        headers?.forEach { (k, v) ->
            setRequestProperty(k, v)
        }

        useCaches = false
        connect()
    }

    if (content != null) {
        DataOutputStream(httpsCon.outputStream).apply {
            write(content.byteInputStream().readAllBytes())
            close()
        }
    }

    val result = BufferedReader(InputStreamReader(httpsCon.inputStream)).lines().asSequence().joinToString("\n")
    httpsCon.disconnect()
    return result
}

fun get(
    url: String,
    contentType: String = "application/x-www-form-urlencoded",
    body: String? = null,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null,
) = http(url, "GET", contentType, body, parameters, headers)

fun post(
    url: String,
    contentType: String = "application/x-www-form-urlencoded",
    body: String? = null,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null
) = http(url, "POST", contentType, body, parameters, headers)