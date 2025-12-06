package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryItemHistoryStorageTest {

    private InMemoryItemHistoryStorage historyStorage;

    @BeforeEach
    void setUp() {
        historyStorage = new InMemoryItemHistoryStorage();
    }

    @Test
    void addViewAndGetHistory_tracksItemsInOrder() {
        historyStorage.addView(1L, 100L);
        historyStorage.addView(1L, 101L);
        historyStorage.addView(1L, 102L);

        List<Long> history = historyStorage.getHistory(1L);
        assertThat(history).containsExactly(100L, 101L, 102L);
    }

    @Test
    void addView_existingItemMovesToEnd() {
        historyStorage.addView(1L, 100L);
        historyStorage.addView(1L, 101L);
        historyStorage.addView(1L, 100L); // добавляем повторно

        List<Long> history = historyStorage.getHistory(1L);
        assertThat(history).containsExactly(101L, 100L);
    }

    @Test
    void getHistory_noHistory_returnsEmptyList() {
        List<Long> history = historyStorage.getHistory(999L);
        assertThat(history).isEmpty();
    }

    @Test
    void addView_nullUserOrItem_doesNotThrow() {
        historyStorage.addView(null, 1L);
        historyStorage.addView(1L, null);
        historyStorage.addView(null, null);

        List<Long> history = historyStorage.getHistory(null);
        assertThat(history).isEmpty();
    }
}
