package com.qy.cloud.network.cookie

import java.net.HttpCookie
import java.net.URI
import java.util.HashMap
import java.util.HashSet

/**
 * A implementation of [Map] for utility class for storing URL cookie map
 */
class CookieMap : MutableMap<URI, MutableList<HttpCookie>?> {

    private val map: MutableMap<URI, MutableList<HttpCookie>?> = HashMap()

    override fun put(
        key: URI,
        value: MutableList<HttpCookie>?,
    ): MutableList<HttpCookie>? {
        return map.put(key, value)
    }

    override fun putAll(from: Map<out URI, MutableList<HttpCookie>?>) {
        this.map.putAll(from)
    }

    /**
     * List all URIs for which cookies are stored in map
     *
     * @return
     */
    val allURIs: Collection<URI>
        get() = map.keys

    /**
     * Get all cookies names stored for given URI
     *
     * @param uri
     */
    fun getAllCookieNames(uri: URI): Collection<String> {
        val cookies: List<HttpCookie>? = map[uri]
        val cookieNames: MutableSet<String> = HashSet()
        if (cookies != null) {
            for (cookie in cookies) {
                cookieNames.add(cookie.name)
            }
        }
        return cookieNames
    }

    /**
     * Removes requested [HttpCookie] `httpCookie` from given `uri` value
     *
     * @param uri
     * @param httpCookie
     */
    fun removeCookie(
        uri: URI,
        httpCookie: HttpCookie,
    ): Boolean {
        return if (map.containsKey(uri)) {
            map[uri]?.remove(httpCookie) ?: false
        } else {
            false
        }
    }

    override val size: Int
        get() = map.size

    override fun containsKey(key: URI): Boolean = map.containsKey(key)

    override fun containsValue(value: MutableList<HttpCookie>?): Boolean = map.containsValue(value)

    override fun get(key: URI): MutableList<HttpCookie>? = map[key]

    override val entries: MutableSet<MutableMap.MutableEntry<URI, MutableList<HttpCookie>?>>
        get() = map.entries

    override val keys: MutableSet<URI>
        get() = map.keys

    override val values: MutableCollection<MutableList<HttpCookie>?>
        get() = map.values

    override fun remove(key: URI): MutableList<HttpCookie>? = map.remove(key)

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun clear() = map.clear()
}
