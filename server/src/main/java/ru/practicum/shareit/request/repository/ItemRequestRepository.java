package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequestorOrderByCreatedDesc(User requestor);

    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId);

    @EntityGraph(attributePaths = {"items"})
    Optional<ItemRequest> findById(Long id);
}