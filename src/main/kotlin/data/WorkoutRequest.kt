package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Workout

@Serializable
data class WorkoutRequest(
    var nama: String = "",
    var durasi: Int = 0,
    var jenis: String = "",
    var lokasi: String = "",
    var pathGambar: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "nama" to nama,
        "durasi" to durasi,
        "jenis" to jenis,
        "lokasi" to lokasi,
        "pathGambar" to pathGambar
    )

    fun toEntity(): Workout = Workout(
        nama = nama,
        durasi = durasi,
        jenis = jenis,
        lokasi = lokasi,
        pathGambar = pathGambar,
    )
}