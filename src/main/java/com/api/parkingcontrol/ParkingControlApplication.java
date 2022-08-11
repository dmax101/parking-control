package com.api.parkingcontrol;

import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ParkingControlApplication {
	Logger logger = org.apache.logging.log4j.LogManager.getLogger(ParkingControlApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ParkingControlApplication.class, args);
	}
}
