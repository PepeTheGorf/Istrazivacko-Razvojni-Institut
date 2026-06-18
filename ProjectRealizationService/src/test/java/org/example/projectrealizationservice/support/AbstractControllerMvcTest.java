package org.example.projectrealizationservice.support;

import org.example.projectrealizationservice.security.JwtService;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Base for {@code @WebMvcTest} controller tests. SecurityConfig requires JwtAuthenticationFilter,
 * which depends on JwtService — not loaded in the sliced test context, so we provide a mock.
 */
public abstract class AbstractControllerMvcTest {

    @MockBean
    protected JwtService jwtService;
}
