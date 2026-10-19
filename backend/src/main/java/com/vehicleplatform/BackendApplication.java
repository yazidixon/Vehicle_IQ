package com.vehicleplatform;

import com.vehicleplatform.config.AppProperties;
import me.paulschwarz.springdotenv.spring.DotenvApplicationInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class BackendApplication {

	public static void main(String[] args) {
		// Force a fixed, always-valid JVM timezone before any JDBC connection is
		// opened: the Postgres JDBC driver sends TimeZone.getDefault() to the
		// server on connect, and some minimal/embedded Postgres builds don't
		// recognize every IANA zone id the host OS might report.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		SpringApplication app = new SpringApplication(BackendApplication.class);
		// spring-dotenv has no auto-registration file; it must be added explicitly.
		app.addInitializers(new DotenvApplicationInitializer());
		app.run(args);
	}

}
