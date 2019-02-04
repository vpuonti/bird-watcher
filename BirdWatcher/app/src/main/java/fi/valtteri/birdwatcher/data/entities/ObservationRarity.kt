package fi.valtteri.birdwatcher.data.entities

enum class ObservationRarity {
    COMMON {
        override fun toString(): String {
            return "Common"
        }
    },
    RARE {
        override fun toString(): String {
            return "Rare"
        }
    },
    EXTREMELY_RARE {
        override fun toString(): String {
            return "Extremely rare"
        }
    }
}