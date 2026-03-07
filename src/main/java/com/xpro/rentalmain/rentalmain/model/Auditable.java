package com.xpro.rentalmain.rentalmain.model;

import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.ZonedDateTime;

public  abstract class Auditable {

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private ZonedDateTime lastModifiedAt;

    @PrePersist
    protected void onCreate() {
        // Runs once, the first time the record is saved
        this.createdAt = ZonedDateTime.now();
        this.lastModifiedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        // Runs every time 'save' is called on an existing record
        this.lastModifiedAt = ZonedDateTime.now();
    }
}
