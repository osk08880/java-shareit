package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;

    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            log.warn("Попытка преобразовать null Comment в CommentDto");
            return null;
        }

        log.debug("Начало маппинга Comment в CommentDto: {}", comment);

        String authorName = null;
        if (comment.getAuthorId() != null) {
            User author = userRepository.findById(comment.getAuthorId()).orElse(null);
            authorName = author != null ? author.getName() : null;
            log.debug("Найден автор для комментария: authorId={}, authorName={}", comment.getAuthorId(), authorName);
        }

        CommentDto dto = CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(authorName)
                .created(comment.getCreated())
                .build();

        log.debug("Завершен маппинг Comment в CommentDto: {}", dto);
        return dto;
    }

    public Comment toComment(CommentDto dto, Long itemId, Long authorId) {
        if (dto == null) {
            log.warn("Попытка преобразовать null CommentDto в Comment");
            return null;
        }

        log.debug("Начало маппинга CommentDto в Comment: dto={}, itemId={}, authorId={}", dto, itemId, authorId);

        Comment comment = Comment.builder()
                .text(dto.getText())
                .itemId(itemId)
                .authorId(authorId)
                .created(LocalDateTime.now())
                .build();

        log.debug("Завершен маппинг CommentDto в Comment: {}", comment);
        return comment;
    }
}
