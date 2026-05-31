package org.example.projectrealizationanalyticsservice;

import com.influxdb.client.InfluxDBClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ProjectRealizationAnalyticsServiceApplicationTests {

	@MockBean
	private InfluxDBClient influxDBClient;

	@Test
	void contextLoads() {
	}

}
