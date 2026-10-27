package ru.wires.alpha;

import org.springframework.boot.SpringApplication;

public class TestAlphaApplication {

	public static void main(String[] args) {
		SpringApplication.from(AlphaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
