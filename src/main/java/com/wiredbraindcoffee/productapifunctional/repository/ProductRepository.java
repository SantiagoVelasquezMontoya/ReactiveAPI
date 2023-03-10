package com.wiredbraindcoffee.productapifunctional.repository;

import com.wiredbraindcoffee.productapifunctional.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
