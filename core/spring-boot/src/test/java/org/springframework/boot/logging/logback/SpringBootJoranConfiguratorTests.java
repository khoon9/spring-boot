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

package org.springframework.boot.logging.logback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.testsupport.classpath.ClassPathExclusions;
import org.springframework.boot.testsupport.classpath.resources.WithResource;
import org.springframework.boot.testsupport.system.CapturedOutput;
import org.springframework.boot.testsupport.system.OutputCaptureExtension;
import org.springframework.context.aot.AbstractAotProcessor;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringBootJoranConfigurator}.
 *
 * @author Phillip Webb
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 */
@ExtendWith(OutputCaptureExtension.class)
class SpringBootJoranConfiguratorTests {

	private MockEnvironment environment;

	private LoggingInitializationContext initializationContext;

	private JoranConfigurator configurator;

	private LoggerContext context;

	private Logger logger;

	private CapturedOutput output;

	@BeforeEach
	void setup(CapturedOutput output) {
		this.output = output;
		this.environment = new MockEnvironment();
		this.initializationContext = new LoggingInitializationContext(this.environment);
		this.configurator = new SpringBootJoranConfigurator(this.initializationContext);
		this.context = (LoggerContext) LoggerFactory.getILoggerFactory();
		this.logger = this.context.getLogger(getClass());
	}

	@AfterEach
	void reset() {
		this.context.stop();
		new BasicConfigurator().configure((LoggerContext) LoggerFactory.getILoggerFactory());
		this.context.start();
	}

	@Test
	@WithProductionProfileXmlResource
	void profileActive() throws Exception {
		this.environment.setActiveProfiles("production");
		initialize("production-profile.xml");
		this.logger.trace("Hello");
		assertThat(this.output).contains("Hello");
	}

	@Test
	@WithResource(name = "profile-in-include.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<include file="${resourceRoot}/include-with-profile.xml"/>
			</configuration>
			""")
	@WithResource(name = "include-with-profile.xml", content = """
			<included>
				<springProfile name="production">
					<logger name="org.springframework.boot.logging.logback" level="TRACE"/>
				</springProfile>
			</included>
			""")
	void profileInIncludeActive() throws Exception {
		this.environment.setActiveProfiles("production");
		initialize("profile-in-include.xml");
		this.logger.trace("Hello");
		assertThat(this.output).contains("Hello");
	}

	@Test
	@WithMultiProfileNamesXmlResource
	void multipleNamesFirstProfileActive() throws Exception {
		this.environment.setActiveProfiles("production");
		initialize("multi-profile-names.xml");
		this.logger.trace("Hello");
		assertThat(this.output).contains("Hello");
	}

	@Test
	@WithMultiProfileNamesXmlResource
	void multipleNamesSecondProfileActive() throws Exception {
		this.environment.setActiveProfiles("test");
		initialize("multi-profile-names.xml");
		this.logger.trace("Hello");
		assertThat(this.output).contains("Hello");
	}

	@Test
	@WithProductionProfileXmlResource
	void profileNotActive() throws Exception {
		initialize("production-profile.xml");
		this.logger.trace("Hello");
		assertThat(this.output).doesNotContain("Hello");
	}

	@Test
	@WithProfileExpressionXmlResource
	void profileExpressionMatchFirst() throws Exception {
		this.environment.setActiveProfiles("production");
		initialize("profile-expression.xml");
		this.logger.trace("Hello");
		assertThat(this.output).contains("Hello");
	}

	@Test
	@WithProfileExpressionXmlResource
	void profileExpressionMatchSecond() throws Exception {
		this.environment.setActiveProfiles("test");
		initialize("profile-expression.xml");
		this.logger.trace("Hello");
		assertThat(this.output).contains("Hello");
	}

	@Test
	@WithProfileExpressionXmlResource
	void profileExpressionNoMatch() throws Exception {
		this.environment.setActiveProfiles("development");
		initialize("profile-expression.xml");
		this.logger.trace("Hello");
		assertThat(this.output).doesNotContain("Hello");
	}

	@Test
	@WithNestedXmlResource
	void profileNestedActiveActive() throws Exception {
		doTestNestedProfile(true, "outer", "inner");
	}

	@Test
	@WithNestedXmlResource
	void profileNestedActiveNotActive() throws Exception {
		doTestNestedProfile(false, "outer");
	}

	@Test
	@WithNestedXmlResource
	void profileNestedNotActiveActive() throws Exception {
		doTestNestedProfile(false, "inner");
	}

	@Test
	@WithNestedXmlResource
	void profileNestedNotActiveNotActive() throws Exception {
		doTestNestedProfile(false);
	}

	@Test
	@WithPropertyXmlResource
	void springProperty() throws Exception {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(this.environment, "my.example-property=test");
		initialize("property.xml");
		assertThat(this.context.getProperty("MINE")).isEqualTo("test");
	}

	@Test
	@WithPropertyXmlResource
	void relaxedSpringProperty() throws Exception {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(this.environment, "my.EXAMPLE_PROPERTY=test");
		ConfigurationPropertySources.attach(this.environment);
		initialize("property.xml");
		assertThat(this.context.getProperty("MINE")).isEqualTo("test");
	}

	@Test
	@WithPropertyXmlResource
	void springPropertyNoValue() throws Exception {
		initialize("property.xml");
		assertThat(this.context.getProperty("SIMPLE")).isNull();
	}

	@Test
	@WithPropertyXmlResource
	void relaxedSpringPropertyNoValue() throws Exception {
		initialize("property.xml");
		assertThat(this.context.getProperty("MINE")).isNull();
	}

	@Test
	@WithPropertyDefaultValueXmlResource
	void springPropertyWithDefaultValue() throws Exception {
		initialize("property-default-value.xml");
		assertThat(this.context.getProperty("SIMPLE")).isEqualTo("foo");
	}

	@Test
	@WithPropertyDefaultValueXmlResource
	void relaxedSpringPropertyWithDefaultValue() throws Exception {
		initialize("property-default-value.xml");
		assertThat(this.context.getProperty("MINE")).isEqualTo("bar");
	}

	@Test
	@WithPropertyInIfXmlResource
	void springPropertyInIfWhenTrue() throws Exception {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(this.environment, "my.example-property=true");
		initialize("property-in-if.xml");
		assertThat(this.context.getProperty("MYCHECK")).isEqualTo("i-was-included");
	}

	@Test
	@WithPropertyInIfXmlResource
	void springPropertyInIfWhenFalse() throws Exception {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(this.environment, "my.example-property=false");
		initialize("property-in-if.xml");
		assertThat(this.context.getProperty("MYCHECK")).isNull();
	}

	@Test
	@WithResource(name = "property-in-include.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<include file="${resourceRoot}/include-with-property.xml"/>
			</configuration>
			""")
	@WithResource(name = "include-with-property.xml", content = """
			<included>
				<springProperty scope="context" name="MINE" source="my.example-property" defaultValue="default-test"/>
			</included>
			""")
	@ClassPathExclusions
	void springPropertyInInclude() throws Exception {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(this.environment, "my.example-property=test");
		initialize("property-in-include.xml");
		assertThat(this.context.getProperty("MINE")).isEqualTo("test");
	}

	@Test
	@WithPropertyXmlResource
	void addsAotContributionToContextDuringAotProcessing() throws Exception {
		withSystemProperty(AbstractAotProcessor.AOT_PROCESSING, "true", () -> {
			initialize("property.xml");
			Object contribution = this.context.getObject(BeanFactoryInitializationAotContribution.class.getName());
			assertThat(contribution).isNotNull();
		});
	}

	private void withSystemProperty(String name, String value, Action action) throws Exception {
		System.setProperty(name, value);
		try {
			action.perform();
		}
		finally {
			System.clearProperty(name);
		}
	}

	private void doTestNestedProfile(boolean expected, String... profiles) throws JoranException {
		this.environment.setActiveProfiles(profiles);
		initialize("nested.xml");
		this.logger.trace("Hello");
		if (expected) {
			assertThat(this.output).contains("Hello");
		}
		else {
			assertThat(this.output).doesNotContain("Hello");
		}

	}

	private void initialize(String config) throws JoranException {
		this.configurator.setContext(this.context);
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		this.configurator.doConfigure(contextClassLoader.getResource(config));
	}

	private interface Action {

		void perform() throws Exception;

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@WithResource(name = "property-default-value.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<springProperty scope="context" name="SIMPLE" source="testpropertyfoobar" defaultValue="foo"/>
				<springProperty scope="context" name="MINE" source="my.example-property" defaultValue="bar"/>
			</configuration>
			""")
	private @interface WithPropertyDefaultValueXmlResource {

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@WithResource(name = "property-in-if.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<springProperty scope="context" name="MINE" source="my.example-property"/>
				<if condition='property("MINE").contains("true")'>
					<then>
						<variable scope="context" name="MYCHECK" value="i-was-included"/>
					</then>
				</if>
			</configuration>
			""")
	private @interface WithPropertyInIfXmlResource {

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@WithResource(name = "property.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<springProperty scope="context" name="SIMPLE" source="testpropertyfoobar"/>
				<springProperty scope="context" name="MINE" source="my.example-property"/>
			</configuration>
			""")
	private @interface WithPropertyXmlResource {

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@WithResource(name = "profile-expression.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<springProfile name="production | test">
					<logger name="org.springframework.boot.logging.logback" level="TRACE"/>
				</springProfile>
			</configuration>
			""")
	private @interface WithProfileExpressionXmlResource {

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@WithResource(name = "production-profile.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<springProfile name="production">
					<logger name="org.springframework.boot.logging.logback" level="TRACE"/>
				</springProfile>
			</configuration>
			""")
	private @interface WithProductionProfileXmlResource {

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@WithResource(name = "nested.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<springProfile name="outer">
					<springProfile name="inner">
						<logger name="org.springframework.boot.logging.logback" level="TRACE"/>
					</springProfile>
				</springProfile>
			</configuration>
			""")
	private @interface WithNestedXmlResource {

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@WithResource(name = "multi-profile-names.xml", content = """
			<?xml version="1.0" encoding="UTF-8"?>
			<configuration>
				<include resource="org/springframework/boot/logging/logback/base.xml"/>
				<springProfile name="production, test">
					<logger name="org.springframework.boot.logging.logback" level="TRACE"/>
				</springProfile>
			</configuration>
			""")
	private @interface WithMultiProfileNamesXmlResource {

	}

}
