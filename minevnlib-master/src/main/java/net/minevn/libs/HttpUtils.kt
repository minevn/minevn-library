package net.minevn.libs

import java.io.*
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
    isMultipart: Boolean = false
): String {
    val boundary = "===" + System.currentTimeMillis() + "==="
    val content = body ?: parameters?.map { it.key + "=" + it.value }?.joinToString("&") { it }
    val httpsCon = (URL(url).run { proxy?.let { openConnection(it) } ?: openConnection() } as HttpURLConnection).apply {
        doOutput = true
        doInput = true
        instanceFollowRedirects = true
        requestMethod = method
        if (isMultipart) {
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        } else {
            setRequestProperty("Content-Type", contentType)
        }
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

    if (isMultipart) {
        OutputStreamWriter(httpsCon.outputStream).apply {
            parameters!!.forEach { (key, value) ->
                write("--$boundary\r\n")
                write("Content-Disposition: form-data; name=\"$key\"\r\n\r\n")
                write("$value\r\n")
            }
            write("--$boundary--\r\n")
            flush()
            close()
        }
    } else if (content != null) {
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
    isMultipart: Boolean = false
) = http(url, "GET", contentType, body, parameters, headers, setCookie, getCookie, proxy, isMultipart)

fun post(
    url: String,
    contentType: String = "application/x-www-form-urlencoded",
    body: String? = null,
    parameters: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    setCookie: Map<String, String>? = null,
    getCookie: MutableMap<String, String>? = null,
    proxy: Proxy? = null,
    isMultipart: Boolean = false
) = http(url, "POST", contentType, body, parameters, headers, setCookie, getCookie, proxy, isMultipart)
