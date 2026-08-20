package com.github.valdpq.mentoringplatform.mentor;

import com.github.valdpq.mentoringplatform.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mentors")
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String bio;

    private String specialization;

    @Column(name = "price_per_hour")
    private BigDecimal pricePerHour;

    @Column(name = "avg_rating", nullable = false)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(name = "reviews_count", nullable = false)
    @Builder.Default
    private Integer reviewsCount = 0;
}
