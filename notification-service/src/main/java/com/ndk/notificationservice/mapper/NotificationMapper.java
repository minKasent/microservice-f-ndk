package com.ndk.notificationservice.mapper;

import com.ndk.notificationservice.dto.response.NotificationDto;
import com.ndk.notificationservice.entity.mongo.NotificationHistory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {
  NotificationDto toDto(NotificationHistory history);
}
