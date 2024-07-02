package com.correct.correctsoc.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserData")
class User(
    @PrimaryKey
    var id: String,
    var username: String,
    var password: String,
    var phone: String,
    var token: String,
    var code: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (phone != other.phone) return false
        if (token != other.token) return false
        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + code.hashCode()
        return result
    }
}