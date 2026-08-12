package com.ryanhallberg.restaurant.menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    List<MenuCategory> findAllByOrderByDisplayOrderAsc();
}
