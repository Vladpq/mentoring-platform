package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.mentor.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("""
            SELECT COUNT (l) > 0 FROM Lesson l
            WHERE l.mentor = :mentor
            AND l.status = 'SCHEDULED'
            AND l.startTime < :end
            AND l.endTime > :start
            """)
    boolean existsOverlappingLesson(@Param("mentor") Mentor mentor,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);
}
