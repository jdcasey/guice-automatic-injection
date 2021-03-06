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
package de.devsurf.injection.guice.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Qualifier;

import com.google.inject.Module;
import com.google.inject.Singleton;

import de.devsurf.injection.guice.install.InstallationContext.BindingStage;
import de.devsurf.injection.guice.scanner.features.BindingScannerFeature;

/**
 * Annotate a Module with the GuiceModule-Annotation and it will be installed
 * automatically.
 * 
 * @author Daniel Manzke
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@GuiceAnnotation
@Target( { ElementType.TYPE })
public @interface GuiceModule {
	BindingStage stage() default BindingStage.BUILD;

	@Singleton
	public class ModuleBindingFeature extends BindingScannerFeature {
		private Logger _logger = Logger.getLogger(ModuleBindingFeature.class.getName());

		@Override
		public BindingStage accept(Class<Object> annotatedClass, Map<String, Annotation> annotations) {
			if (annotations.containsKey(GuiceModule.class.getName())) {
				GuiceModule module = (GuiceModule) annotations.get(GuiceModule.class.getName());
				return module.stage();
			}
			return BindingStage.IGNORE;
		}

		@Override
		public void process(final Class<Object> annotatedClass, Map<String, Annotation> annotations) {
			if (_logger.isLoggable(Level.INFO)) {
				_logger.info("Installing Module: " + annotatedClass.getName());
			}
			synchronized (_binder) {
				_binder.install((Module) injector.getInstance(annotatedClass));
			}
		}
	}
}
