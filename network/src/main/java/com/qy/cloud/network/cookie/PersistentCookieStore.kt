package com.qy.cloud.network.cookie

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.HashSet
import java.util.Locale

class PersistentCookieStore(context: Context) : CookieStore {

    // This map here will store all domain to cookies bindings
    private val allCookies: CookieMap = CookieMap()

    private val cookiePrefs: SharedPreferences = context.getSharedPreferences(COOKIE_PREFS, 0)

    init {

        // Load any previously stored domains into the store
        val storedCookieDomains = cookiePrefs.getString(COOKIE_DOMAINS_STORE, null)
        if (storedCookieDomains != null) {
            val storedCookieDomainsArray = TextUtils.split(storedCookieDomains, ",")
            // split this domains and get cookie names stored for each domain
            for (domain in storedCookieDomainsArray) {
                val storedCookiesNames = cookiePrefs.getString(
                    COOKIE_DOMAIN_PREFIX + domain,
                    null,
                )
                // so now we have these cookie names
                if (storedCookiesNames != null) {
                    // split these cookie names and get serialized cookie stored
                    val storedCookieNamesArray = TextUtils.split(storedCookiesNames, ",")
                    if (storedCookieNamesArray != null) {
                        // in this list we store all cookies under one URI
                        val cookies: MutableList<HttpCookie> = ArrayList()
                        for (cookieName in storedCookieNamesArray) {
                            val encCookie = cookiePrefs.getString(
                                COOKIE_NAME_PREFIX + domain + cookieName,
                                null,
                            )
                            // now we deserialize or unserialize (whatever you call it) this cookie
                            // and get HttpCookie out of it and pass it to List
                            if (encCookie != null) {
                                val decodeCookie = decodeCookie(encCookie)
                                if (decodeCookie != null) {
                                    cookies.add(decodeCookie)
                                }
                            }
                        }
                        allCookies[URI.create(domain)] = cookies
                    }
                }
            }
        }
    }

    @Synchronized
    override fun add(
        uri: URI?,
        cookie: HttpCookie?,
    ) {
        if (cookie == null || uri == null) return

        // Dirty correction of cookie's domain for vestiairecollective.xand domain
        val domain = cookie.domain
        if (!domain.startsWith(".") && domain.contains("vestiairecollective")) {
            val modifiedDomain = domain.substring(domain.indexOf("."))
            cookie.domain = modifiedDomain
        }
        val innerUri = cookiesUri(uri)
        var cookies = allCookies[innerUri]
        if (cookies == null) {
            cookies = mutableListOf()
            allCookies[innerUri] = cookies
        } else {
            cookies.remove(cookie)
        }
        cookies.add(cookie)

        // Save cookie into persistent store
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.putString(COOKIE_DOMAINS_STORE, TextUtils.join(",", allCookies.keys))
        val names: MutableSet<String?> = HashSet()
        for (cookie2 in cookies) {
            names.add(cookie2.name)
            val persistentCookie = PersistentCookie(cookie2)
            val encodeCookie = encodeCookie(persistentCookie)
            prefsWriter.putString(COOKIE_NAME_PREFIX + uri + cookie2.name, encodeCookie)
        }
        prefsWriter.putString(COOKIE_DOMAIN_PREFIX + uri, TextUtils.join(",", names))
        prefsWriter.apply()
    }

    @Synchronized
    override fun get(uri: URI?): List<HttpCookie> {
        if (uri == null) return emptyList()
        val result: MutableList<HttpCookie> = ArrayList()
        // get cookies associated with given URI. If none, returns an empty list
        val cookiesForUri = allCookies[uri]
        if (cookiesForUri != null) {
            val iterator = cookiesForUri.iterator()
            while (iterator.hasNext()) {
                val cookie = iterator.next()
                if (cookie.hasExpired()) {
                    iterator.remove() // remove expired cookies
                } else {
                    result.add(cookie)
                }
            }
        }
        // get all cookies that domain matches the URI
        for ((key, entryCookies) in allCookies) {
            if (uri == key) {
                continue // skip the given URI; we've already handled it
            }
            val iterator = entryCookies?.iterator()
            while (iterator?.hasNext() == true) {
                val cookie = iterator.next()
                if (!HttpCookie.domainMatches(cookie.domain, uri.host)) {
                    continue
                }
                if (cookie.hasExpired()) {
                    iterator.remove() // remove expired cookies or null cookies in cookie store
                } else if (!result.contains(cookie)) {
                    result.add(cookie)
                }
            }
        }
        return result
    }

    @Synchronized
    override fun getCookies(): List<HttpCookie> {
        val result: MutableList<HttpCookie> = ArrayList()
        for (list in allCookies.values) {
            val iterator = list?.iterator()
            while (iterator?.hasNext() == true) {
                val cookie = iterator.next()
                if (cookie.hasExpired()) {
                    iterator.remove() // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie)
                }
            }
        }
        return result
    }

    @Synchronized
    override fun getURIs(): List<URI> {
        val result = allCookies.allURIs.toList()
        return result
    }

    @Synchronized
    override fun remove(
        uri: URI,
        cookie: HttpCookie?,
    ): Boolean {
        if (cookie == null) return false

        if (allCookies.removeCookie(uri, cookie)) {
            val prefsWriter = cookiePrefs.edit()
            prefsWriter.putString(
                COOKIE_DOMAIN_PREFIX + uri,
                TextUtils.join(",", allCookies.getAllCookieNames(uri)),
            )
            prefsWriter.remove(COOKIE_NAME_PREFIX + uri + cookie.name)
            prefsWriter.apply()
            return true
        }
        return false
    }

    @Synchronized
    override fun removeAll(): Boolean {
        // Clear cookies from persistent store
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.apply()

        // Clear cookies from local store
        val result = !allCookies.isEmpty()
        allCookies.clear()
        return result
    }

    /**
     * Serializes HttpCookie object into String
     *
     * @param cookie cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    private fun encodeCookie(cookie: PersistentCookie?): String? {
        if (cookie == null) return null

        val os = ByteArrayOutputStream()
        try {
            val outputStream = ObjectOutputStream(os)
            outputStream.writeObject(cookie)
        } catch (e: IOException) {
            return null
        } catch (e: RuntimeException) {
            return null
        }
        return byteArrayToHexString(os.toByteArray())
    }

    /**
     * Returns HttpCookie decoded from cookie string
     *
     * @param cookieString string of cookie as returned from http request
     * @return decoded cookie or null if exception occured
     */
    private fun decodeCookie(cookieString: String): HttpCookie? {
        val bytes = hexStringToByteArray(cookieString)
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        return try {
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            (objectInputStream.readObject() as? PersistentCookie?)?.cookie
        } catch (e: IOException) {
            null
        } catch (e: ClassNotFoundException) {
            null
        } catch (e: RuntimeException) {
            null
        }
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't
     * have to rely on any large Base64 libraries. Can be overridden if you
     * like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    private fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (element in bytes) {
            val v: Int = element.toInt() and 0xff
            if (v < 16) {
                sb.append('0')
            }
            sb.append(Integer.toHexString(v))
        }
        return sb.toString().toUpperCase(Locale.US)
    }

    /**
     * Converts hex values from strings to byte arra
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = (
                (Character.digit(hexString[i], 16) shl 4) + Character
                    .digit(hexString[i + 1], 16)
            ).toByte()
            i += 2
        }
        return data
    }

    /**
     * Utility function to male sure that every time you get consistent URI
     *
     * @param uri
     * @return
     */
    private fun cookiesUri(uri: URI): URI {
        return try {
            URI(uri.scheme, uri.host, null, null)
        } catch (e: URISyntaxException) {
            uri
        }
    }

    companion object {
        private const val COOKIE_PREFS = "qly.cloud_cookieprefs"
        private const val COOKIE_DOMAINS_STORE = "qly.cloud.CookieStore.domain"
        private const val COOKIE_DOMAIN_PREFIX = "qly.cloud.CookieStore.domain_"
        private const val COOKIE_NAME_PREFIX = "qly.cloud.CookieStore.cookie_"
    }
}
