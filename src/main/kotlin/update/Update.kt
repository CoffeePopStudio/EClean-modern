package top.e404.eclean.update

import com.google.gson.JsonParser
import org.bukkit.Bukkit
import top.e404.eclean.config.Config
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

object Update {
    private const val GITHUB_API = "https://api.github.com/repos/CoffeePopStudio/EClean-modern/releases"
    private const val GITHUB_URL = "https://github.com/CoffeePopStudio/EClean-modern"

    fun register() {
        if (!Config.current.global.updateCheck) return
        val plugin = top.e404.eclean.PL
        plugin.server.asyncScheduler.runAtFixedRate(
            plugin,
            { _ -> check() },
            20L,
            6L,
            TimeUnit.HOURS,
        )
    }

    private fun check() {
        try {
            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API))
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return
            val json = JsonParser.parseString(response.body()).asJsonArray
            if (json.isEmpty) return
            val latest = json[0].asJsonObject.get("tag_name").asString
            val current = top.e404.eclean.PL.pluginMeta.version
            if (latest != current) {
                Bukkit.getConsoleSender().sendMessage(
                    "§6[EClean-Modern] §e新版本可用: §b$latest §e(当前: §7$current§e) → §a$GITHUB_URL"
                )
            }
        } catch (_: Exception) {
        }
    }
}
