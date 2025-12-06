package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private User owner;
    private Item item;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        author = userRepository.save(User.builder()
                .name("Alice")
                .email("alice@example.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .build());

        comment = commentRepository.save(Comment.builder()
                .text("Great drill!")
                .authorId(author.getId())
                .itemId(item.getId())
                .created(LocalDateTime.now())
                .build());
    }

    @Test
    void findByItemIdOrderByCreatedDesc_returnsComments() {
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());

        assertThat(comments).hasSize(1);
        Comment savedComment = comments.get(0);

        assertThat(savedComment.getText()).isEqualTo("Great drill!");
        assertThat(savedComment.getItemId()).isEqualTo(item.getId());
        assertThat(savedComment.getAuthorId()).isEqualTo(author.getId());
    }
}
