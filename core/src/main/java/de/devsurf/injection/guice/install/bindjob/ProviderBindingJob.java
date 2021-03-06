/**
 * Copyright (C) 2010 Daniel Manzke <daniel.manzke@googlemail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devsurf.injection.guice.install.bindjob;

import java.lang.annotation.Annotation;

import com.google.inject.Provider;
import com.google.inject.Scope;


public class ProviderBindingJob extends BindingJob{

	public ProviderBindingJob(Scope scoped, Provider<?> provided, Annotation annotated, String interfaceName) {
		super(scoped, provided, annotated, null, interfaceName);
	}

	@Override
	public String toString() {
		return "Provider"+super.toString();
	}
}
