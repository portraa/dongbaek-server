package me.chacham.dongbaek.infra.grpc

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import me.chacham.dongbaek.domain.progress.DurationProgress
import me.chacham.dongbaek.domain.progress.ProgressRepository
import me.chacham.dongbaek.domain.progress.QuantityProgress
import me.chacham.dongbaek.domain.schedule.ScheduleId
import me.chacham.dongbaek.infra.proto.PbUtils.toPbProgress
import me.chacham.dongbaek.infra.proto.PbUtils.toPbTimestamp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.Instant

@ExtendWith(MockKExtension::class)
class ProgressGrpcServiceImplTest {
    @Test
    fun getProgressesTest(@MockK progressRepositoryMock: ProgressRepository) {
        val impl = ProgressGrpcServiceImpl(progressRepositoryMock)

        val sId1 = ScheduleId("sid1")
        val sId2 = ScheduleId("sid2")
        val progress1 = QuantityProgress(sId1, Instant.now(), null, 10)
        val progress2 = DurationProgress(sId2, Instant.now(), null, Duration.ofMinutes(10), null)
        coEvery { progressRepositoryMock.list(listOf(sId1, sId2), any()) } returns listOf(progress1, progress2)

        val time = Instant.now()
        val request = getProgressesRequest {
            scheduleIds.addAll(listOf(sId1.value, sId2.value))
            timestamp = time.toPbTimestamp()
        }
        val response = runBlocking { impl.getProgresses(request) }

        coVerify { progressRepositoryMock.list(listOf(sId1, sId2), time) }
        assertEquals(getProgressesResponse {
            progresses.addAll(listOf(progress1.toPbProgress(), progress2.toPbProgress()))
        }, response)
    }

    @Test
    fun replaceProgressTest(@MockK progressRepositoryMock: ProgressRepository) {
        val impl = ProgressGrpcServiceImpl(progressRepositoryMock)

        val sId = ScheduleId("sid")
        val p = QuantityProgress(sId, Instant.now(), Instant.now().plusSeconds(3600), 10)
        coEvery { progressRepositoryMock.save(p) } returns p.getId()

        val request = replaceProgressRequest {
            progress = p.toPbProgress()
        }
        val response = runBlocking { impl.replaceProgress(request) }

        coVerify { progressRepositoryMock.save(p) }
        assertEquals(replaceProgressResponse {
            scheduleId = p.scheduleId.value
            startTimestamp = p.startInstant.toPbTimestamp()
        }, response)
    }
}
