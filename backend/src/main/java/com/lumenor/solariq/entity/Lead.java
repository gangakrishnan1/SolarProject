package com.lumenor.solariq.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "property_type", nullable = false, length = 20)
    private String propertyType;

    @Column(name = "monthly_bill", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyBill;

    @Column(name = "monthly_units", precision = 12, scale = 2)
    private BigDecimal monthlyUnits;

    @Column(name = "roof_area_sqft", precision = 12, scale = 2)
    private BigDecimal roofAreaSqft;

    @Column(name = "roof_type", length = 20)
    private String roofType;

    @Column(name = "ownership", length = 10)
    private String ownership;

    @Column(name = "system_size_kw", precision = 8, scale = 2)
    private BigDecimal systemSizeKw;

    @Column(name = "install_cost", precision = 14, scale = 2)
    private BigDecimal installCost;

    @Column(name = "monthly_savings", precision = 12, scale = 2)
    private BigDecimal monthlySavings;

    @Column(name = "payback_years", precision = 5, scale = 2)
    private BigDecimal paybackYears;

    @Column(name = "co2_offset_kg", precision = 12, scale = 2)
    private BigDecimal co2OffsetKg;

    @Column(name = "subsidy_amount", precision = 14, scale = 2)
    private BigDecimal subsidyAmount;

    @Column(name = "net_cost", precision = 14, scale = 2)
    private BigDecimal netCost;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "lead_score")
    private Integer leadScore;

    @Column(name = "ai_tips", columnDefinition = "TEXT")
    private String aiTips;

    @Column(name = "projection_25yr", columnDefinition = "TEXT")
    private String projection25yr;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "new";

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) {
            status = "new";
        }
    }
}
