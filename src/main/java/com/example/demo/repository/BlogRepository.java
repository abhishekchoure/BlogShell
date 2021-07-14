package com.example.demo.repository;

import com.example.demo.model.Blog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BlogRepository extends PagingAndSortingRepository<Blog,Long> {
    public List<Blog> findByUserIdOrderByDateTimeDesc(Long id);
    public List<Blog> findAllByOrderByDateTimeDesc();
    public List<Blog> findAllByCategoryOrderByDateTimeDesc(String category);
    public Blog findDistinctById(Long id);
}
