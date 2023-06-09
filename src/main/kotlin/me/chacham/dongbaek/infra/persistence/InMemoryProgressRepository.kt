package me.chacham.dongbaek.infra.persistence

import me.chacham.dongbaek.domain.progress.Progress
import me.chacham.dongbaek.domain.progress.ProgressId
import me.chacham.dongbaek.domain.progress.ProgressRepository
import me.chacham.dongbaek.domain.schedule.ScheduleId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class InMemoryProgressRepository : ProgressRepository {
    private val progressMap = mutableMapOf<ProgressId, Progress>()

    override suspend fun save(progress: Progress): ProgressId {
        val id = progress.getId()
        progressMap[id] = progress
        return id
    }

    override suspend fun list(scheduleIds: List<ScheduleId>, instant: Instant): List<Progress> {
        return progressMap.values.filter {
            it.scheduleId in scheduleIds
                    && !it.startInstant.isAfter(instant)
                    && (it.endInstant?.isAfter(instant) ?: true)
        }
    }
}
