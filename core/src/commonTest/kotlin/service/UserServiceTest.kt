package service

import factory.GenericFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import model.params.UserCreationParams
import model.user.User
import repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserServiceTest {

    // create mocks
    private val userRepository = mockk<UserRepository>()
    private val userFactory = mockk<GenericFactory<User, UserCreationParams>>()

    // init service with mocks
    private val userService = UserService(userRepository, userFactory)

    // BL-1 POSITIVE
    @Test
    fun `login returns User when credentials are correct`() = runTest {
        // Arrange
        val login = "testUser"
        val password = "password123"
        val expectedUser = User(id = "1", login = login, passwordHash = password.toByteArray())

        // method behavior in test
        coEvery { userRepository.findByLogin(login) } returns expectedUser

        // Act
        val actualUser = userService.login(login, password)

        // Assert
        assertEquals(expectedUser.id, actualUser?.id)
        assertEquals(expectedUser.login, actualUser?.login)
    }

    // BL-2 NEGATIVE
    @Test
    fun `login returns null when password is incorrect`() = runTest {
        // Arrange
        val login = "testUser"
        val correctPassword = "password123"
        val wrongPassword = "wrongPassword"
        val existingUser = User(id = "1", login = login, passwordHash = correctPassword.toByteArray())

        // mock method behavior in test, must return user that's being requested
        coEvery { userRepository.findByLogin(login) } returns existingUser

        // Act
        val actualUser = userService.login(login, wrongPassword)

        // Assert
        assertNull(actualUser, "User should be null when password does not match")
    }

    // BL-3 POSITIVE
    @Test
    fun `register a new user with success`() = runTest {
        // Arrange
        val login = "testUser"
        val password = "test"
        val params = UserCreationParams(login, password)
        val newUser = User(id = "1", login = login, passwordHash = password.toByteArray())

        // mock user repository findByLogin returns null (user still not registred)
        coEvery { userRepository.findByLogin(login) } returns null

        // mock factory and add
        every { userFactory.create(params) } returns newUser
        coEvery { userRepository.add(newUser) } returns Unit

        // Act
        val result = userService.register(params)

        // Assert
        // verify that true returned
        assertTrue(result, "Should return true on successful registration")
    }

    // BL-4 NEGATIVE
    @Test
    fun `register a new user with existing login`() = runTest {
        // Arrange
        val existingLogin = "testUser"
        val login = "testUser"
        val password = "test"
        val params = UserCreationParams(login, password)
        val existingUser = User(id = "1", login = existingLogin, passwordHash = password.toByteArray())

        // mock user repository findByLogin returns existingUser
        coEvery { userRepository.findByLogin(login) } returns existingUser

        // Act
        val result = userService.register(params)

        // Assert
        // verify that true returned
        assertFalse(result, "Should return false if user with this login already exists")
    }
}