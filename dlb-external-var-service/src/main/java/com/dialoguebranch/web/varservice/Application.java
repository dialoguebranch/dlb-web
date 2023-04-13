/*
 *
 *                   Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *      as outlined below. Based on original source code licensed under the following terms:
 *
 *                                            ----------
 *
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dialoguebranch.web.varservice;

import nl.rrd.utils.AppComponents;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.system.JavaVersion;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.SpringVersion;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ClassUtils;

import java.net.URL;
import java.time.Instant;

/**
 * The main entry point for the External Variable Service Dummy as a Spring Boot Application.
 * 
 * @author Harm op den Akker
 */
@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer implements
ApplicationListener<ApplicationEvent> {

	private final Logger logger =
			AppComponents.getLogger(ClassUtils.getUserClass(getClass()).getSimpleName());
	private final Configuration config;

	private final Long launchedTime = Instant.now().toEpochMilli();

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Constructs a new application. It reads service.properties and
	 * initializes the {@link Configuration Configuration} and the {@link
	 * AppComponents AppComponents}.
	 * 
	 * @throws Exception if the application can't be initialised
	 */
	public Application() throws Exception {
		// Initialize a Configuration object
		config = AppComponents.get(Configuration.class);

		// Load the values from service.properties into the Configuration
		URL propertiesUrl = getClass().getClassLoader().getResource(
				"service.properties");
		if (propertiesUrl == null) {
			throw new Exception("Can't find resource service.properties. " +
					"Did you run gradlew updateConfig?");
		}
		config.loadProperties(propertiesUrl);

		// Load the values from deployment.properties into the Configuration (app version number)
		propertiesUrl = getClass().getClassLoader().getResource(
				"deployment.properties");
		config.loadProperties(propertiesUrl);

		// By default, log uncaught exceptions to this logger
		Thread.setDefaultUncaughtExceptionHandler((t, e) ->
				logger.error("Uncaught exception: " + e.getMessage(), e)
		);
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Return the UTC timestamp of when this service was first launched.
	 * @return the UTC timestamp of when this service was first launched.
	 */
	public Long getLaunchedTime() {
		return launchedTime;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof ContextClosedEvent) {
			logger.info("Shutdown DialogueBranch External Variable Service Dummy.");
		}

		if(event instanceof ContextRefreshedEvent) {
			logger.info("========== DialogueBranch External Variable Service Dummy Startup Info ==========");
			logger.info("=== Version: " + config.get(Configuration.VERSION));
			logger.info("=== API Version: " + ProtocolVersion.getLatestVersion().versionName());
			logger.info("=== Build: " + config.getBuildTime());
			logger.info("=== Spring Version: "+ SpringVersion.getVersion());
			logger.info("=== JDK Version: "+System.getProperty("java.version"));
			logger.info("=== Java Version: "+ JavaVersion.getJavaVersion().toString());
			logger.info("=======================================================================");
		}
	}
}
