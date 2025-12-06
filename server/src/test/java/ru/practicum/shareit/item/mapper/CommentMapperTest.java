package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentMapperTest {

    private UserRepository userRepository;
    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        commentMapper = new CommentMapper(userRepository);
    }

    @Test
    void toCommentDto_withAuthor_shouldMapCorrectly() {
        Comment comment = Comment.builder().id(1L).text("Nice").authorId(2L).created(LocalDateTime.now()).build();
        User author = User.builder().id(2L).name("Bob").email("b@b.com").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));

        CommentDto dto = commentMapper.toCommentDto(comment);

        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(author.getName(), dto.getAuthorName());
    }

    @Test
    void toCommentDto_nullComment_returnsNull() {
        assertNull(commentMapper.toCommentDto(null));
    }

    @Test
    void toComment_withoutNulls_shouldMapCorrectly() {
        CommentDto dto = CommentDto.builder().text("Great").build();
        Comment comment = commentMapper.toComment(dto, 1L, 2L);

        assertEquals(dto.getText(), comment.getText());
        assertEquals(1L, comment.getItemId());
        assertEquals(2L, comment.getAuthorId());
        assertNotNull(comment.getCreated());
    }

    @Test
    void toComment_nullDto_returnsNull() {
        assertNull(commentMapper.toComment(null, 1L, 2L));
    }
}
