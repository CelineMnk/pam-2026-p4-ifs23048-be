package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.WorkoutRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IWorkoutRepository
import java.io.File
import java.util.UUID

class WorkoutService(private val workoutRepository: IWorkoutRepository) {

    suspend fun getAllWorkouts(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val workouts = workoutRepository.getWorkouts(search)
        call.respond(DataResponse("success", "Berhasil mengambil daftar workout",
            mapOf("workouts" to workouts)))
    }

    suspend fun getWorkoutById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID workout tidak boleh kosong!")
        val workout = workoutRepository.getWorkoutById(id)
            ?: throw AppException(404, "Data workout tidak tersedia!")
        call.respond(DataResponse("success", "Berhasil mengambil data workout",
            mapOf("workout" to workout)))
    }

    private suspend fun getWorkoutRequest(call: ApplicationCall): WorkoutRequest {
        val req = WorkoutRequest()
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> req.nama = part.value.trim()
                        "durasi" -> req.durasi = part.value.trim().toIntOrNull() ?: 0
                        "jenis" -> req.jenis = part.value.trim()
                        "lokasi" -> req.lokasi = part.value.trim()
                    }
                }
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/workouts/$fileName"
                    val file = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    req.pathGambar = filePath
                }
                else -> {}
            }
            part.dispose()
        }
        return req
    }

    private fun validateWorkoutRequest(req: WorkoutRequest) {
        val validator = ValidatorHelper(req.toMap())
        validator.required("nama", "Nama tidak boleh kosong")
        validator.required("jenis", "Jenis tidak boleh kosong")
        validator.required("lokasi", "Lokasi tidak boleh kosong")
        validator.required("pathGambar", "Gambar tidak boleh kosong")
        validator.validate()

        if (req.durasi <= 0) throw AppException(400, "Durasi harus lebih dari 0 menit!")
        val file = File(req.pathGambar)
        if (!file.exists()) throw AppException(400, "Gambar workout gagal diupload!")
    }

    suspend fun createWorkout(call: ApplicationCall) {
        val req = getWorkoutRequest(call)
        validateWorkoutRequest(req)

        val exist = workoutRepository.getWorkoutByName(req.nama)
        if (exist != null) {
            File(req.pathGambar).takeIf { it.exists() }?.delete()
            throw AppException(409, "Workout dengan nama ini sudah terdaftar!")
        }

        val id = workoutRepository.addWorkout(req.toEntity())
        call.respond(DataResponse("success", "Berhasil menambahkan data workout",
            mapOf("workoutId" to id)))
    }

    suspend fun updateWorkout(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID workout tidak boleh kosong!")
        val old = workoutRepository.getWorkoutById(id)
            ?: throw AppException(404, "Data workout tidak tersedia!")

        val req = getWorkoutRequest(call)
        if (req.pathGambar.isEmpty()) req.pathGambar = old.pathGambar
        validateWorkoutRequest(req)

        if (req.nama != old.nama) {
            val exist = workoutRepository.getWorkoutByName(req.nama)
            if (exist != null) {
                File(req.pathGambar).takeIf { it.exists() }?.delete()
                throw AppException(409, "Workout dengan nama ini sudah terdaftar!")
            }
        }

        if (req.pathGambar != old.pathGambar) {
            File(old.pathGambar).takeIf { it.exists() }?.delete()
        }

        val updated = workoutRepository.updateWorkout(id, req.toEntity())
        if (!updated) throw AppException(400, "Gagal memperbarui data workout!")

        call.respond(DataResponse("success", "Berhasil mengubah data workout", null))
    }

    suspend fun deleteWorkout(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID workout tidak boleh kosong!")
        val old = workoutRepository.getWorkoutById(id)
            ?: throw AppException(404, "Data workout tidak tersedia!")

        val deleted = workoutRepository.removeWorkout(id)
        if (!deleted) throw AppException(400, "Gagal menghapus data workout!")

        File(old.pathGambar).takeIf { it.exists() }?.delete()
        call.respond(DataResponse("success", "Berhasil menghapus data workout", null))
    }

    suspend fun getWorkoutImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)
        val workout = workoutRepository.getWorkoutById(id)
            ?: return call.respond(HttpStatusCode.NotFound)
        val file = File(workout.pathGambar)
        if (!file.exists()) return call.respond(HttpStatusCode.NotFound)
        call.respondFile(file)
    }
}