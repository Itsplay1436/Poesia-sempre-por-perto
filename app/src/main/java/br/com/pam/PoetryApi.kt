package br.com.pam

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.IOException

/** API sem chaves. Todas as chamadas são executadas fora da thread de interface. */
class PoetryApi {
    private val cache = mutableMapOf<String, Any>()
    private fun get(path: String, cached: Boolean = true): Any {
        if (cached) cache[path]?.let { return it }
        val connection = URL("https://poetrydb.org/$path").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15000
            connection.readTimeout = 25000
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status == 404) return JSONArray()
            if (status !in 200..299) throw IOException("A API respondeu com erro $status. Tente novamente.")
            val json = connection.inputStream.bufferedReader(Charsets.UTF_8).use { JSONTokener(it.readText()).nextValue() }
            if (json is JSONObject && json.has("status")) {
                if (json.optInt("status") == 404) return JSONArray()
                throw IOException("A API não conseguiu concluir a consulta.")
            }
            if (cached) cache[path] = json
            return json
        } finally { connection.disconnect() }
    }
    private fun encode(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")
    private fun array(path: String, cached: Boolean = true) = get(path, cached) as? JSONArray ?: throw IOException("Resposta inesperada da API.")
    fun authors(): List<String> {
        val root = get("author") as? JSONObject ?: throw IOException("Lista de autores indisponível.")
        return strings(root.getJSONArray("authors")).sorted()
    }
    fun titles(): List<Poem> {
        val root = get("title") as? JSONObject ?: throw IOException("Lista de títulos indisponível.")
        return strings(root.getJSONArray("titles")).sorted().map { Poem(it) }
    }
    fun author(name: String): Pair<JSONObject?, List<Poem>> {
        val poems = poems(array("author/${encode(name)}:abs/title,author,linecount"))
        // A lista de poemas continua disponível se a biografia não for retornada.
        val bio = try { array("author/${encode(name)}:abs/info").optJSONObject(0) } catch (_: IOException) { null }
        return Pair(bio, poems)
    }
    fun random(count: Int) = poems(array("random/$count/title,author,linecount", false))
    fun featured() = poems(array("random/1", false)).firstOrNull()
    fun detail(title: String, author: String): List<Poem> = poems(array("title/${encode(title)}:abs")).let { all ->
        if (author.isEmpty()) all else all.filter { it.author == author }
    }
    fun search(filter: Filters): List<Poem> {
        filter.validate()?.let { throw IllegalArgumentException(it) }
        val criteria = mutableListOf<Pair<String, String>>()
        if (filter.title.isNotBlank()) criteria += "title" to filter.title.trim()
        if (filter.author.isNotBlank()) criteria += "author" to filter.author.trim()
        if (filter.lines.isNotBlank()) criteria += "lines" to filter.lines.trim()
        if (filter.min.isNotBlank() || filter.max.isNotBlank()) criteria += "linecount" to "${filter.min}-${filter.max}"
        if (criteria.isEmpty()) return titles()
        // Consultas independentes evitam que ';' no texto seja interpretado como separador da API.
        var found: List<Poem>? = null
        for ((field, term) in criteria) {
            val next = poems(array("$field/${encode(term)}/title,author,linecount"))
            val keys = next.map { it.title to it.author }.toSet()
            found = found?.filter { (it.title to it.author) in keys } ?: next
            if (found.isEmpty()) break
        }
        return found.orEmpty()
    }
    private fun poems(a: JSONArray) = (0 until a.length()).map { i ->
        val p = a.getJSONObject(i)
        Poem(p.getString("title"), p.optString("author"), p.optString("linecount").toIntOrNull(), p.optJSONArray("lines")?.let(::strings))
    }
    private fun strings(a: JSONArray) = (0 until a.length()).map { a.getString(it) }
}
data class Poem(val title: String, val author: String = "", val count: Int? = null, val lines: List<String>? = null) : java.io.Serializable
