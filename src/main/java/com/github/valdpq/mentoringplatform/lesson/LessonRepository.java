package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT l FROM Lesson l JOIN FETCH l.mentor JOIN FETCH l.student WHERE l.mentor = :mentor")
    Page<Lesson> findAllByMentor(@Param("mentor") Mentor mentor, Pageable pageable);

    @Query("SELECT l FROM Lesson l JOIN FETCH l.mentor JOIN FETCH l.student WHERE l.student = :student")
    Page<Lesson> findAllByStudent(@Param("student") Student student, Pageable pageable);
}
