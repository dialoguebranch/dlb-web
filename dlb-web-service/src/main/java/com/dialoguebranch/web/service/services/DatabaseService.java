/*
 *
 *                Copyright (c) 2023-2026 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023-2026 Fruit Tree Labs (www.fruittreelabs.com)
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

package com.dialoguebranch.web.service.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.rrd.utils.AppComponents;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseService {

	@Bean
	public DataSource dataSource() {
		com.dialoguebranch.web.service.Configuration cfg =
				com.dialoguebranch.web.service.Configuration.getInstance();

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mariadb://" + cfg.getMariadbHost() + ":" + cfg.getMariadbPort() +
				"/" + cfg.getMariadbDatabase() + "?createDatabaseIfNotExist=true");
		config.setUsername(cfg.getMariadbUser());
		config.setPassword(cfg.getMariadbPassword());

		Logger logger = AppComponents.getLogger(getClass().getSimpleName());
		int retryCount = 0;
		while (true) {
			try {
				return new HikariDataSource(config);
			} catch (Exception ex) {
				if (retryCount++ < 30) {
					logger.warn("Failed to connect to database; retrying in 10 seconds ...");
					sleep(10000);
				} else {
					throw ex;
				}
			}
		}
	}

	private void sleep(int ms) {
		long now = System.currentTimeMillis();
		long end = now + ms;
		try {
			while (now < end) {
				Thread.sleep(end - now);
				now = System.currentTimeMillis();
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
