package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private String text;

    private String authorName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;

    public static CommentDto fallback(String text, String authorName) {
        return CommentDto.builder()
                .id(null)
                .text(text != null ? text : "")
                .authorName(authorName != null ? authorName : "")
                .created(LocalDateTime.now())
                .build();
    }
}
