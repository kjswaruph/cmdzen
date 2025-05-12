package io.cmdzen.cli;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@Log4j2
@CommandScan
public class CmdzenCliApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmdzenCliApplication.class, args);
	}

}
