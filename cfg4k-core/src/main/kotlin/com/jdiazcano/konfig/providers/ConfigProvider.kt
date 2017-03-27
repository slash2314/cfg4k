/*
 * Copyright 2015-2016 Javier Díaz-Cano Martín-Albo (javierdiazcanom@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdiazcano.konfig.providers

import com.jdiazcano.konfig.binders.Binder
import com.jdiazcano.konfig.utils.Typable
import com.jdiazcano.konfig.utils.typeOf
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Base interface for all the ConfigProviders, this interface defines the needed methods of a provider in order to be
 * configurable and reloadable.
 */
interface ConfigProvider {

    val binder: Binder

    /**
     * Gets a property from the loader and parses it to the correct type. Normally only primitive and custom parsers
     * will be here, generic types must be used with the Typable parameter.
     *
     * @param name Name of the property
     * @param type Class of the property, it will be parsed to it.
     */
    fun <T: Any> getProperty(name: String, type: KClass<T>, default: T? = null): T

    /**
     * Gets a property from the loader and parses it to the correct type. This is used for generics (but not only) so
     * this method will correctly parse List<Int> for example.
     *
     * @param name Name of the property
     * @param type KType of the property.
     */
    fun <T: Any> getProperty(name: String, type: KType, default: T? = null): T

    /**
     * This method will be called when there is an order to reload the properties. This can happen in different scenarios
     * for example with a timed reload strategy. This method will be called from the reload strategy.
     */
    fun reload()

    /**
     * This method will be called in order to stop the automatic reloading. This will deregister itself from the
     * Reload strategy
     */
    fun cancelReload(): Unit?

    /**
     * Adds a reload listener that will be called once the reload is performed.
     */
    fun addReloadListener(listener: () -> Unit)

    /**
     * Checks if a property exists in the provider.
     */
    fun contains(name: String): Boolean

    /**
     * Binds an interface
     *
     * This method will return an implementation of the interface with the given methods for configuration. (Will call
     * the Binder.bind method)
     *
     * @param prefix The prefix of the configuration, if this is not empty, configs starting with the prefix will be used
     * @param type The interface that will be implemented and it will be returned
     */
    fun <T: Any> bind(prefix: String, type: KClass<T>): T {
        return binder.bind(this, prefix, type)
    }
}

inline fun <reified T : Any> ConfigProvider.bind(name: String) = bind(name, T::class)
inline fun <reified T : Any> ConfigProvider.getProperty(name: String, default: T? = null) = getProperty(name, typeOf<T>(), default)
fun ConfigProvider.cache() = CachedConfigProvider(this)