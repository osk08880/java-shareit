package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemCommentModelTest {

    @Test
    void commentModel_gettersAndSettersWork() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Nice item");
        comment.setItemId(10L);
        comment.setAuthorId(20L);
        comment.setCreated(LocalDateTime.now());

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Nice item");
        assertThat(comment.getItemId()).isEqualTo(10L);
        assertThat(comment.getAuthorId()).isEqualTo(20L);
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void itemModel_gettersAndSettersWork() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(100L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setComments(List.of());

        assertThat(item.getId()).isEqualTo(100L);
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.getDescription()).isEqualTo("Powerful drill");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getComments()).isEmpty();
    }
}
