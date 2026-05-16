package com.meta.foremeal.pantry.domain;

import com.meta.foremeal.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name="PantryScan")
@Getter
@NoArgsConstructor

public class PantryScan {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="scan_id")
    private Long scanId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="image_url", length=255)
    private String imageUrl;

    @Column(name="scanned_date")
    private LocalDateTime scannedDate;

    @Column(name="scan_result", columnDefinition="JSON")
    private String scanResult; // AI 에이전트 인식 원본 데이터

    @Column(name="status", length=50)
    private String status; //PROCESSING, SUCCESS, FAILED

    @Column(name="error_message", columnDefinition="TEXT")
    private String errorMessage;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Builder
    public PantryScan(User user, String imageUrl, LocalDateTime scannedDate, String scanResult,
                      String status, String errorMessage, LocalDateTime createdAt){
        this.user = user;
        this.imageUrl = imageUrl;
        this.scannedDate = scannedDate;
        this.scanResult = scanResult;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    public void updateScanResult(String status, String scanResult, String errorMessage){
        this.status=status;
        this.scanResult=scanResult;
        this.errorMessage=errorMessage;
    }
}
