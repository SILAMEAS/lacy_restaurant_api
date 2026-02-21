package com.sila.modules.category.services;

import com.sila.modules.category.dto.CategoryRequest;
import com.sila.modules.category.dto.CategoryResponse;
import com.sila.modules.category.model.Category;
import com.sila.modules.chat.dto.MessageResponse;
import com.sila.share.dto.req.PaginationRequest;
import com.sila.share.pagination.EntityResponseHandler;

import java.util.List;

public interface CategoryService {
    Category create(CategoryRequest request);

    List<Category> getsByResId(Long restaurantId);

    Category update(CategoryRequest request, Long categoryId);

    Category getById(Long categoryId);

    MessageResponse delete(Long categoryId);

    List<CategoryResponse> all();

    EntityResponseHandler<CategoryResponse> gets(PaginationRequest request);

    String deleteAllCategories();

    Long count();


}
