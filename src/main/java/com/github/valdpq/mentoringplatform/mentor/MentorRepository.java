package com.github.valdpq.mentoringplatform.mentor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    Page<Mentor> findAllBySpecializationContainingIgnoreCase(String specialization, Pageable pageable);
}
