/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.autoconfigure.restdocs;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.cassandra.autoconfigure.CassandraAutoConfiguration;
import org.springframework.boot.security.autoconfigure.actuate.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.servlet.SecurityAutoConfiguration;

/**
 * Test application used with {@link AutoConfigureRestDocs @AutoConfigureRestDocs} tests.
 *
 * @author Andy Wilkinson
 */
@SpringBootApplication(exclude = { CassandraAutoConfiguration.class, SecurityAutoConfiguration.class,
		ManagementWebSecurityAutoConfiguration.class })
public class RestDocsTestApplication {

}
