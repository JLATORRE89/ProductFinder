package com.productfinder.repository;

import com.productfinder.model.Search;
import com.productfinder.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchRepository extends JpaRepository<Search, Long> {
    List<Search> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<Search> findByIdAndUser(Long id, User user);
}
