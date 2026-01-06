package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    @Query("""
                select i from Item i
                where i.available = true
                  and (
                       lower(i.name) like lower(concat('%', :text, '%'))
                    or lower(i.description) like lower(concat('%', :text, '%'))
                  )
                order by i.id
            """)
    List<Item> search(@Param("text") String text);

    @Query("""
       select i from Item i
       where i.requestId in :requestIds
       order by i.id asc
       """)
    List<Item> findAllByRequestIdIn(@Param("requestIds") List<Long> requestIds);

    @Query("""
       select i from Item i
       where i.requestId = :requestId
       order by i.id asc
       """)
    List<Item> findAllByRequestId(@Param("requestId") Long requestId);

}
