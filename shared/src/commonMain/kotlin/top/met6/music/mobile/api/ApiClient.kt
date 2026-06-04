package top.met6.music.mobile.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import top.met6.music.mobile.models.*

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
}

object ApiClient {
    private const val BASE_URL = "https://music.met6.top:444/api/web"

    suspend fun getUserPlaylists(uid: String): UserPlaylistsResponse {
        val timestamp = (0..99999999).random().toString()
        return httpClient.get("$BASE_URL/user/playlist") {
            parameter("uid", uid)
            parameter("limit", 100)
            parameter("timestamp", timestamp)
        }.body()
    }

    suspend fun getPlaylistDetail(id: String): PlaylistDetail {
        val timestamp = (0..99999999).random().toString()
        return httpClient.get("$BASE_URL/playlist/detail") {
            parameter("id", id)
            parameter("timestamp", timestamp)
        }.body()
    }

    suspend fun getPlaylistTracks(id: String, limit: Int = 500): PlaylistTracksResponse {
        val timestamp = (0..99999999).random().toString()
        return httpClient.get("$BASE_URL/playlist/track/all") {
            parameter("id", id)
            parameter("limit", limit)
            parameter("timestamp", timestamp)
        }.body()
    }

    suspend fun getSongUrl(id: String, level: String = "standard"): SongUrlResponse {
        val timestamp = (0..99999999).random().toString()
        return httpClient.get("$BASE_URL/song/url/v1") {
            parameter("id", id)
            parameter("level", level)
            parameter("timestamp", timestamp)
        }.body()
    }

    suspend fun getSongLyric(id: String): LyricResponse {
        return httpClient.get("$BASE_URL/lyric/new") {
            parameter("id", id)
        }.body()
    }
}
