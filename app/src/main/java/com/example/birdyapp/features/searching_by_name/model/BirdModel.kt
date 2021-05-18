package com.example.birdyapp.features.searching_by_name.model

data class BirdModel(
    val name: String,
    val description: String,
    val photo: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BirdModel

        if (name != other.name) return false
        if (description != other.description) return false
        if (!photo.contentEquals(other.photo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + photo.contentHashCode()
        return result
    }
}