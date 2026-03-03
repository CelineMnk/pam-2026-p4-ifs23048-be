package org.delcom.dao

import org.delcom.tables.WorkoutTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class WorkoutDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, WorkoutDAO>(WorkoutTable)

    var nama by WorkoutTable.nama
    var durasi by WorkoutTable.durasi
    var jenis by WorkoutTable.jenis
    var lokasi by WorkoutTable.lokasi
    var pathGambar by WorkoutTable.pathGambar
    var createdAt by WorkoutTable.createdAt
    var updatedAt by WorkoutTable.updatedAt
}