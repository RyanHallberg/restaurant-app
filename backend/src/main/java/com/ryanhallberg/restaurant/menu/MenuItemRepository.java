package com.ryanhallberg.restaurant.menu;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Page<MenuItem> findByAvailableTrue(Pageable pageable);

    Page<MenuItem> findByAvailableTrueAndCategoryId(Long categoryId, Pageable pageable);

    Page<MenuItem> findByCategoryId(Long categoryId, Pageable pageable);
}
