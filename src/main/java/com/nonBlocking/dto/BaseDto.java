package com.nonBlocking.dto;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseDto {
    @Column(name = "CREATEDT", updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "UPDATEDT")
    @LastModifiedDate
    private LocalDateTime modifiedDate;
}
