package com.github.valdpq.mentoringplatform.review;

import com.github.valdpq.mentoringplatform.lesson.Lesson;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsReviewByLesson(Lesson lesson);

    @Query("SELECT COUNT (r) FROM Review r WHERE r.lesson.mentor = :mentor")
    long countReviewByMentor(@Param("mentor") Mentor mentor);

    @Query("SELECT AVG (r.rating) FROM Review r WHERE r.lesson.mentor = :mentor")
    BigDecimal calculateAvgRating(@Param("mentor") Mentor mentor);

    @Query("SELECT r FROM Review r JOIN FETCH r.lesson l JOIN FETCH l.student WHERE l.mentor = :mentor")
    Page<Review> getAllByLessonMentor(@Param("mentor") Mentor mentor, Pageable pageable);
}
