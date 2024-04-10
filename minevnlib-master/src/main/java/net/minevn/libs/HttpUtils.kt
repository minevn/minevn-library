package net.minevn.libs

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import kotlin.streams.asSequence

fun http(
    url: String,
    method: String,
    contentType: String = "application/x-www-form-urlencoded",
    body: String? = null,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    setCookie: Map<String, String>? = null,
    getCookie: MutableMap<String, String>? = null,
    proxy: Proxy? = null,
): String {
    val content = body ?: parameters?.map { it.key + "=" + it.value }?.joinToString("&") { it }
    val httpsCon = (URL(url).run { proxy?.let { openConnection(it) } ?: openConnection() } as HttpURLConnection).apply {
        doOutput = true
        doInput = true
        instanceFollowRedirects = false
        requestMethod = method
        setRequestProperty("Content-Type", contentType)
        setRequestProperty("charset", "utf-8")
        if (content != null) {
            setRequestProperty("Content-Length", ByteArrayInputStream(content.toByteArray()).readBytes().size.toString())
        }
        headers?.forEach { (k, v) ->
            setRequestProperty(k, v)
        }
        setCookie?.forEach {
            setRequestProperty("Cookie", "${it.key}=${it.value}")
        }

        useCaches = false
        connect()
    }

    if (content != null) {
        DataOutputStream(httpsCon.outputStream).apply {
            write(ByteArrayInputStream(content.toByteArray()).readBytes())
            close()
        }
    }

    if (getCookie != null) {
        // Extract cookies from the response headers
        httpsCon.headerFields
            .filter { it.key != null && it.key.equals("Set-Cookie", ignoreCase = true) }
            .flatMap { it.value }
            .map { it.split(";")[0] }
            .groupBy({ it.substringBefore('=') }, { it.substringAfter('=') })
            .forEach() { (k, v) ->
                getCookie[k] = v.lastOrNull() ?: ""
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
    setCookie: Map<String, String>? = null,
    getCookie: MutableMap<String, String>? = null,
    proxy: Proxy? = null,
) = http(url, "GET", contentType, body, parameters, headers, setCookie, getCookie, proxy)

fun post(
    url: String,
    contentType: String = "application/x-www-form-urlencoded",
    body: String? = null,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    setCookie: Map<String, String>? = null,
    getCookie: MutableMap<String, String>? = null,
    proxy: Proxy? = null,
) = http(url, "POST", contentType, body, parameters, headers, setCookie, getCookie, proxy)