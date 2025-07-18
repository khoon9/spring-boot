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

package org.springframework.boot.r2dbc.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MissingR2dbcPoolDependencyFailureAnalyzer}
 *
 * @author Andy Wilkinson
 */
class MissingR2dbcPoolDependencyFailureAnalyzerTests {

	private final MissingR2dbcPoolDependencyFailureAnalyzer failureAnalyzer = new MissingR2dbcPoolDependencyFailureAnalyzer();

	@Test
	void analyzeWhenDifferentFailureShouldReturnNull() {
		assertThat(this.failureAnalyzer.analyze(new Exception())).isNull();
	}

	@Test
	void analyzeWhenMissingR2dbcPoolDependencyShouldReturnAnalysis() {
		assertThat(this.failureAnalyzer.analyze(new MissingR2dbcPoolDependencyException())).isNotNull();
	}

}
