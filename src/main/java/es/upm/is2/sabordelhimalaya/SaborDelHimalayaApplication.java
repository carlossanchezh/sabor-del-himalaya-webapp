package es.upm.is2.sabordelhimalaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SaborDelHimalayaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaborDelHimalayaApplication.class, args);
	}

}
