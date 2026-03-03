package org.delcom.repositories

import org.delcom.dao.WorkoutDAO
import org.delcom.entities.Workout
import org.delcom.helpers.daoToWorkoutModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.WorkoutTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class WorkoutRepository : IWorkoutRepository {

    override suspend fun getWorkouts(search: String): List<Workout> = suspendTransaction {
        if (search.isBlank()) {
            WorkoutDAO.all()
                .orderBy(WorkoutTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToWorkoutModel)
        } else {
            val keyword = "%${search.lowercase()}%"
            WorkoutDAO
                .find { WorkoutTable.nama.lowerCase() like keyword }
                .orderBy(WorkoutTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToWorkoutModel)
        }
    }

    override suspend fun getWorkoutById(id: String): Workout? = suspendTransaction {
        WorkoutDAO
            .find { WorkoutTable.id eq UUID.fromString(id) }
            .limit(1)
            .map(::daoToWorkoutModel)
            .firstOrNull()
    }

    override suspend fun getWorkoutByName(nama: String): Workout? = suspendTransaction {
        WorkoutDAO
            .find { WorkoutTable.nama eq nama }
            .limit(1)
            .map(::daoToWorkoutModel)
            .firstOrNull()
    }

    override suspend fun addWorkout(workout: Workout): String = suspendTransaction {
        val dao = WorkoutDAO.new {
            this.nama = workout.nama
            this.durasi = workout.durasi
            this.jenis = workout.jenis
            this.lokasi = workout.lokasi
            this.pathGambar = workout.pathGambar
            this.createdAt = workout.createdAt
            this.updatedAt = workout.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun updateWorkout(id: String, workout: Workout): Boolean = suspendTransaction {
        val dao = WorkoutDAO
            .find { WorkoutTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.nama = workout.nama
            dao.durasi = workout.durasi
            dao.jenis = workout.jenis
            dao.lokasi = workout.lokasi
            dao.pathGambar = workout.pathGambar
            dao.updatedAt = workout.updatedAt
            true
        } else false
    }

    override suspend fun removeWorkout(id: String): Boolean = suspendTransaction {
        val rowsDeleted = WorkoutTable.deleteWhere {
            WorkoutTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }
}