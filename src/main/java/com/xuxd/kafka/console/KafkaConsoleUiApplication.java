package com.xuxd.kafka.console;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 晓东哥哥
 */
@MapperScan("com.xuxd.kafka.console.dao")
@SpringBootApplication
@EnableScheduling
@ServletComponentScan
public class KafkaConsoleUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaConsoleUiApplication.class, args);
	}

}
