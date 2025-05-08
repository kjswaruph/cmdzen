plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "io.cmdzen"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "1.0.0-M7"
extra["springShellVersion"] = "3.4.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.shell:spring-shell-starter")
//	implementation("org.springframework.boot:spring-boot-starter-webflux")
//	testImplementation("io.projectreactor:reactor-test")
//	implementation("org.springframework.ai:spring-ai-starter-model-ollama")
	implementation("ch.qos.logback:logback-classic:1.5.18")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation("io.micrometer:micrometer-core")
	implementation("io.micrometer:micrometer-registry-prometheus")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.shell:spring-shell-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
