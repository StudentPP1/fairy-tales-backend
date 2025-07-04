package dev.project.bedtimestory.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Data
public class PageWrapper<T> implements Serializable {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private boolean first;
    private boolean last;
    private int numberOfElements;

    public static <T> PageWrapper<T> of(Page<T> page) {
        PageWrapper<T> wrapper = new PageWrapper<>();
        wrapper.content = page.getContent();
        wrapper.totalPages = page.getTotalPages();
        wrapper.totalElements = page.getTotalElements();
        wrapper.size = page.getSize();
        wrapper.number = page.getNumber();
        wrapper.first = page.isFirst();
        wrapper.last = page.isLast();
        wrapper.numberOfElements = page.getNumberOfElements();
        return wrapper;
    }
}