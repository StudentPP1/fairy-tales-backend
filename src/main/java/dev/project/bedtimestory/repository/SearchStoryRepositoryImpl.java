package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchStoryRepositoryImpl implements SearchStoryRepository {
    private final EntityManager entityManager;
    @Override
    public Page<StoryDto> searchStories(String query, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<StoryDto> criteriaQuery = builder.createQuery(StoryDto.class);
        Root<Story> root = criteriaQuery.from(Story.class);

        Predicate titlePredicate = builder.like(builder.lower(
                root.get("title")),
                "%" + query.toLowerCase() + "%");
        Predicate descPredicate = builder.like(builder.lower(
                        root.get("description")),
                "%" + query.toLowerCase() + "%");
        Predicate textPredicate = builder.like(builder.lower(
                        root.get("text")),
                "%" + query.toLowerCase() + "%");

        Predicate finalPredicate = builder.or(titlePredicate, descPredicate, textPredicate);

        criteriaQuery.select(builder.construct(
                StoryDto.class,
                root.get("id"),
                root.get("title"),
                root.get("description"),
                root.get("imgUrl"),
                root.get("likedCount")
        )).where(finalPredicate);

        // Sorting
        if (pageable.getSort().isSorted()) { // ! ?sort=likedCount,desc&sort=title,asc
            List<Order> orders = pageable.getSort().stream()
                    .map(order -> order.isAscending() // ! desc or asc
                            ? builder.asc(root.get(order.getProperty()))
                            : builder.desc(root.get(order.getProperty())))
                    .toList();
            criteriaQuery.orderBy(orders);
        }

        TypedQuery<StoryDto> queryResult = entityManager.createQuery(criteriaQuery);
        queryResult.setFirstResult((int) pageable.getOffset());
        queryResult.setMaxResults(pageable.getPageSize());

        List<StoryDto> content = queryResult.getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Story> countRoot = countQuery.from(Story.class);
        countQuery.select(builder.count(countRoot)).where(finalPredicate);
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
