package com.meta.foremeal.VitalData;

import com.meta.foremeal.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "VitalData")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class VitalData_domain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vital_id")
    private Long vitalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "measured_at")
    private LocalDateTime measuredAt;

    @Column(name = "source_type")
    private String sourceType; // watch, mobile, manual

    private Integer pulse; // 맥박

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
