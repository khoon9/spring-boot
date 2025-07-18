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

package org.springframework.boot.jpa.autoconfigure.hibernate.mapping;

/**
 * A non annotated entity that is handled by a custom "mapping-file".
 *
 * @author Stephane Nicoll
 */
public class NonAnnotatedEntity {

	private Long id;

	private String item;

	protected NonAnnotatedEntity() {
	}

	public NonAnnotatedEntity(String item) {
		this.item = item;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getItem() {
		return this.item;
	}

	public void setItem(String value) {
		this.item = value;
	}

}
