package com.wiredbraindcoffee.productapifunctional.handler;


import com.mongodb.internal.connection.Server;
import com.wiredbraindcoffee.productapifunctional.model.Product;
import com.wiredbraindcoffee.productapifunctional.model.ProductEvents;
import com.wiredbraindcoffee.productapifunctional.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {
    private ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest request){
        Flux<Product> products = repository.findAll();

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }
    public Mono<ServerResponse> getProduct(ServerRequest request){
        String id = request.pathVariable("id");

        Mono<Product> productMono = this.repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono.flatMap(product -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(product))
                .switchIfEmpty(notFound)
        );
    }

    public Mono<ServerResponse> saveProduct(ServerRequest request){
        Mono<Product> productMono = request.bodyToMono(Product.class);

        return productMono.flatMap(product -> ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(repository.save(product), Product.class)
        );
     }

     public Mono<ServerResponse> updateProduct(ServerRequest request){
        String id = request.pathVariable("id");
        Mono<Product> existingProductMono = repository.findById(id);
        Mono<Product> productMono = request.bodyToMono(Product.class);


        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return  productMono.zipWith(existingProductMono, (product, existingProduct) ->
                new Product(existingProduct.getId(), product.getName(), product.getPrice())
        ).flatMap(product -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(repository.save(product), Product.class)
        ).switchIfEmpty(notFound);
     }

    public Mono<ServerResponse> deleteProduct(ServerRequest request){
        String id = request.pathVariable("id");

        Mono<Product> productMono = repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return  productMono.flatMap(foundProduct ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .build(repository.delete(foundProduct)))
                .switchIfEmpty(notFound);
        }

        public Mono<ServerResponse> deleteAllProducts(ServerRequest request){
            return ServerResponse.ok()
                    .build(repository.deleteAll());
        }

        public Mono<ServerResponse> getProductEvents(ServerRequest request){
        Flux<ProductEvents> eventsFlux = Flux.interval(Duration.ofSeconds(1)).
                map(val -> new ProductEvents(val, "Product Type"));

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventsFlux, ProductEvents.class);
        }
    }
