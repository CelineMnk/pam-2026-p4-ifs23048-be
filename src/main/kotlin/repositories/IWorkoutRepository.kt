package org.delcom.repositories

import org.delcom.entities.Workout

interface IWorkoutRepository {
    suspend fun getWorkouts(search: String): List<Workout>
    suspend fun getWorkoutById(id: String): Workout?
    suspend fun getWorkoutByName(nama: String): Workout?
    suspend fun addWorkout(workout: Workout): String
    suspend fun updateWorkout(id: String, workout: Workout): Boolean
    suspend fun removeWorkout(id: String): Boolean
}