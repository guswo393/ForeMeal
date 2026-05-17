package com.meta.foremeal.pantry.domain;

import com.meta.foremeal.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pantry_file")
@Getter
@NoArgsConstructor
public class PantryScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scan_id")
    private Long scanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "raw_result_json", columnDefinition = "JSON", insertable = false, updatable = false)
    private String rawResultJson;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public PantryScan(User user, String imageUrl, LocalDateTime scannedAt, String rawResultJson,
                      String status, String errorMessage, LocalDateTime createdAt) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.scannedAt = scannedAt;
        this.rawResultJson = rawResultJson;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    public void updateScanResult(String status, String rawResultJson, String errorMessage) {
        this.status = status;
        this.rawResultJson = rawResultJson;
        this.errorMessage = errorMessage;
    }
}
