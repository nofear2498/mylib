package io.myfirst.library.storage.persistroom.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test")
data class Link(
    @PrimaryKey var uid: Int,
    @ColumnInfo(name = "link")
    var link: String?
)