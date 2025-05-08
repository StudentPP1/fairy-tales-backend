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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class SearchStoryRepositoryImpl implements SearchStoryRepository {

    private final EntityManager entityManager;

    /**
     * Build predicate for searching
     *
     * @param builder CriteriaBuilder
     * @param root    root for query
     * @param query   query text
     * @return Predicate
     */
    private Predicate buildSearchPredicate(CriteriaBuilder builder, Root<Story> root, String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        Predicate titlePredicate = builder.like(builder.lower(root.get("title")), pattern);
        Predicate descPredicate = builder.like(builder.lower(root.get("description")), pattern);
        Predicate textPredicate = builder.like(builder.lower(root.get("text")), pattern);
        return builder.or(titlePredicate, descPredicate, textPredicate);
    }

    /**
     * Build list of orders for sorting by specification
     *
     * @param builder CriteriaBuilder
     * @param root    root for query
     * @param sort    sort object from pageable
     * @return List<Order>
     */
    private List<Order> buildOrders(CriteriaBuilder builder, Root<Story> root, Sort sort) {
        return sort.stream()
                .map(order -> order.isAscending()
                        ? builder.asc(root.get(order.getProperty()))
                        : builder.desc(root.get(order.getProperty())))
                .toList();
    }

    @Override
    public Page<StoryDto> searchStories(String query, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<StoryDto> criteriaQuery = builder.createQuery(StoryDto.class);
        Root<Story> root = criteriaQuery.from(Story.class);
        Predicate predicate = buildSearchPredicate(builder, root, query);
        criteriaQuery.select(builder.construct(
                StoryDto.class,
                root.get("id"),
                root.get("title"),
                root.get("description"),
                root.get("imgUrl"),
                root.get("likedCount")
        )).where(predicate);

        if (pageable.getSort().isSorted()) {
            criteriaQuery.orderBy(buildOrders(builder, root, pageable.getSort()));
        }

        TypedQuery<StoryDto> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<StoryDto> content = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Story> countRoot = countQuery.from(Story.class);
        Predicate countPredicate = buildSearchPredicate(builder, countRoot, query);
        countQuery.select(builder.count(countRoot)).where(countPredicate);
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}