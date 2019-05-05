package xyz.nowiknowmy.hogwarts.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
abstract class Auditable {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

//    public String getCreatedAt(String zoneId) {
//        ZoneId zone = ZoneId.of(zoneId);
//
//        return ZonedDateTime.of(this.getCreatedAt(), zone).format(DateTimeFormatter.ofPattern("y-M-d HH:mm z")
//            .withLocale(new Locale("en", "US")));
//    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
