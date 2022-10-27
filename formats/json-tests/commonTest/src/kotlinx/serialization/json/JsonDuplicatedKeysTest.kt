package kotlinx.serialization.json

import kotlinx.serialization.SerializationException
import kotlinx.serialization.test.assertFailsWithMessage
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonDuplicatedKeysTest {
    @kotlinx.serialization.Serializable
    data class Pet(val name: String)

    @kotlinx.serialization.Serializable
    data class Person(val name: String, val pets: List<Pet>)

    /**
     * Ensure that, even if there are two keys with the same name, as long as they are in different objects,
     * no exception is thrown
     */
    @Test
    fun shouldParsePersonWithoutDuplicatedKeyError() {
        // Given
        val validPersonJson = """{"name": "Lucky Luke", "pets": [{"name": "Rantanplan"}, {"name":"Jolly Jumper"}]}"""
        val json = Json { allowDuplicatedKeys = false }

        // When
        val decodedJson = json.decodeFromString(
            Person.serializer(),
            validPersonJson
        )

        // Then
        with(decodedJson) {
            assertEquals(name, "Lucky Luke")
            assertEquals(pets, listOf(Pet("Rantanplan"), Pet("Jolly Jumper")))
        }
    }

    private val jsonWithDuplicatedPersonNameKey = """{"name": "John", "name":"Jane" "pets": []}"""

    @Test
    fun shouldThrowErrorOnDuplicateKeyAtObjectRoot() {
        // Given
        val json = Json { allowDuplicatedKeys = false }

        // When / Then
        assertFailsWithMessage<SerializationException>("allowDuplicatedKeys") {
            json.decodeFromString(
                Person.serializer(),
                jsonWithDuplicatedPersonNameKey
            )
        }
    }

    @Test
    fun shouldNotThrowErrorOnDuplicateKeyAtObjectRoot() {
        // Given
        val json = Json { allowDuplicatedKeys = true }

        // When
        val decodedJson = json.decodeFromString(
            Person.serializer(),
            jsonWithDuplicatedPersonNameKey
        )

        // Then
        with(decodedJson) {
            assertEquals(name, "Jane")
            assertEquals(pets, listOf())
        }
    }

    private val jsonWithDuplicatedPetNameKey = """{"name":"Lucky Luke", "pets": [{"name":"Rantanplan", "name":"Jolly Jumper"}]}"""

    @Test
    fun shouldThrowErrorOnDuplicateKeyInChildObject() {
        // Given
        val json = Json { allowDuplicatedKeys = false }

        // When / Then
        assertFailsWithMessage<SerializationException>("allowDuplicatedKeys") {
            json.decodeFromString(
                Person.serializer(),
                jsonWithDuplicatedPetNameKey
            )
        }
    }

    @Test
    fun shouldNotThrowErrorOnDuplicateKeyInChildObject() {
        // Given
        val json = Json { allowDuplicatedKeys = true }

        // When
        val decodedJson = json.decodeFromString(
            Person.serializer(),
            jsonWithDuplicatedPetNameKey
        )

        // Then
        with(decodedJson) {
            assertEquals(name, "Lucky Luke")
            assertEquals(pets, listOf(Pet("Jolly Jumper")))
        }
    }
}