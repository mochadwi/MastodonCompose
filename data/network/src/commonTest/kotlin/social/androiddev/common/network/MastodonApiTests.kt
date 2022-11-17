/*
 * This file is part of MastodonX.
 *
 * MastodonX is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MastodonX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MastodonX. If not, see <https://www.gnu.org/licenses/>.
 */
package social.androiddev.common.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charsets
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import social.androiddev.common.readBinaryResource

@OptIn(ExperimentalCoroutinesApi::class)
class MastodonApiTests {
    @Test
    fun `Instance request should fail with invalid response`() = runTest {
        // given
        val byteArray: ByteArray = readBinaryResource("src/commonTest/resources/response_instance_invalid.json")
        println(byteArray.decodeToString())

        val mastodonApi = MastodonApiImpl(
            httpClient = createMockClient(
                statusCode = HttpStatusCode.Unauthorized, content = ByteReadChannel(text = byteArray.decodeToString())
            )
        )

        // when
        val result = mastodonApi.getInstance("")

        // then
        assertFalse(actual = result.isSuccess)
        assertNull(actual = result.getOrNull()?.uri)
    }

    @Test
    fun `Instance request should succeed with required field response`() = runTest {
        // given
        val byteArray: ByteArray = readBinaryResource("src/commonTest/resources/response_instance_valid.json")
        println(byteArray.decodeToString())

        val mastodonApi = MastodonApiImpl(
            httpClient = createMockClient(
                statusCode = HttpStatusCode.Unauthorized, content = ByteReadChannel(text = byteArray.decodeToString())
            )
        )

        // when
        val result = mastodonApi.getInstance("")

        // then
        assertTrue(actual = result.isSuccess)
        assertNotNull(actual = result.getOrNull()?.uri)
    }

    private fun createMockClient(
        statusCode: HttpStatusCode = HttpStatusCode.OK,
        content: ByteReadChannel
    ): HttpClient {
        return HttpClient(
            MockEngine {
                respond(
                    content = content, status = statusCode, headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                    contentType = ContentType.Application.Json
                )
            }
        }
    }
}
