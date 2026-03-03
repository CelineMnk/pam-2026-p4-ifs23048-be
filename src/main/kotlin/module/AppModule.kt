package org.delcom.module

import org.delcom.repositories.IPlantRepository
import org.delcom.repositories.PlantRepository
import org.delcom.repositories.IWorkoutRepository
import org.delcom.repositories.WorkoutRepository
import org.delcom.services.PlantService
import org.delcom.services.WorkoutService
import org.delcom.services.ProfileService
import org.koin.dsl.module

val appModule = module {
    // Plant
    single<IPlantRepository> { PlantRepository() }
    single { PlantService(get()) }

    // Workout
    single<IWorkoutRepository> { WorkoutRepository() }
    single { WorkoutService(get()) }

    // Profile
    single { ProfileService() }
}