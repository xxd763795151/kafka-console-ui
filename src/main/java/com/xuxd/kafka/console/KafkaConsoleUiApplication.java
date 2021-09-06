package com.xuxd.kafka.console;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.xuxd.kafka.console.dao")
@SpringBootApplication
@EnableScheduling
public class KafkaConsoleUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaConsoleUiApplication.class, args);
	}

}
