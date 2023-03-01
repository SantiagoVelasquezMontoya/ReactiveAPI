package com.wiredbraindcoffee.productapifunctional;

import com.wiredbraindcoffee.productapifunctional.handler.ProductHandler;
import com.wiredbraindcoffee.productapifunctional.model.Product;
import com.wiredbraindcoffee.productapifunctional.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;

@SpringBootApplication
public class ProductApiFunctionalApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProductApiFunctionalApplication.class, args);
	}

	@Bean
	CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository repository){
		return args -> {
			Flux<Product> productFlux = Flux.just(
							new Product(null, "Keyboard", 39.99),
							new Product(null, "Mouse", 29.99),
							new Product(null, "Monitor", 79.99))
					.flatMap(repository::save);

			productFlux
					.thenMany(repository.findAll())
					.subscribe(System.out::println);

			//If running a real MongoDB You can run code below to delete any collection if Exists
//			operations.collectionExists(Product.class)
//					.flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(exists))
//					.thenMany(v -> operations.createCollection(Product.class))
//					.thenMany(productFlux)
//					.thenMany(repository.findAll())
//					.subscribe(System.out::println);
		};
	}

	@Bean
	RouterFunction<ServerResponse> routes(ProductHandler handler){
		return route()
				.GET("/products/events", accept(MediaType.TEXT_EVENT_STREAM), handler::getProductEvents)
				.GET("/products/id", accept(MediaType.APPLICATION_JSON), handler::getProduct)
				.GET("/products", accept(MediaType.APPLICATION_JSON), handler::getAllProducts)
				.PUT("/products/{id}", accept(MediaType.APPLICATION_JSON), handler::updateProduct)
				.POST("/products", accept(MediaType.APPLICATION_JSON), handler::saveProduct)
				.DELETE("/products/{id}", accept(MediaType.APPLICATION_JSON), handler::deleteProduct)
				.DELETE("/products", accept(MediaType.APPLICATION_JSON), handler::deleteAllProducts)
				.build();

//		return route()
//				.path("/products",
//						builder -> builder
//								.nest(accept(MediaType.APPLICATION_JSON)
//								.or(contentType(MediaType.APPLICATION_JSON))
//								.or(accept(MediaType.TEXT_EVENT_STREAM)),
//										nestedBuilder -> nestedBuilder
//												.GET("/events", handler::getProductEvents)
//												.GET("/{id}", handler::getProduct)
//												.GET(handler::getAllProducts)
//												.PUT("/{id}", handler::updateProduct)
//												.POST("/products", handler::saveProduct)
//								)
//								.DELETE("/{id}", handler::deleteProduct)
//								.DELETE(handler::deleteAllProducts)).build();
	}

}
