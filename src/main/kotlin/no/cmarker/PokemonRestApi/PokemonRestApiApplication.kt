package no.cmarker.PokemonRestApi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableJpaRepositories
@EntityScan
@EnableSwagger2
class PokemonRestApiApplication {
    
    /*
        Bean used to configure Swagger documentation
     */
    @Bean
    fun swaggerApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.any())
                .build()
    }
    
    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
                .title("API for REST Pokemon")
                .description("This is a REST API containing all pokemons that exists in 1. Gen")
                .version("1.0")
                .build()
    }
    
    //http://localhost:8080/pokemon/swagger-ui.html
    
    
}

fun main(args: Array<String>) {
    runApplication<PokemonRestApiApplication>(*args)
}
