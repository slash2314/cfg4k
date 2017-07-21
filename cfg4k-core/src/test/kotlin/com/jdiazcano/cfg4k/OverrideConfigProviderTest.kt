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

package com.jdiazcano.cfg4k

import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.Providers.overriden
import com.jdiazcano.cfg4k.providers.bind
import com.jdiazcano.cfg4k.providers.get
import com.winterbe.expekt.should
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class OverrideConfigProviderTest : Spek({
    val provider = overriden(
            DefaultConfigProvider(
                    PropertyConfigLoader(javaClass.classLoader.getResource("overridetest.properties"))
            ),
            DefaultConfigProvider(
                    PropertyConfigLoader(javaClass.classLoader.getResource("test.properties"))
            )
    )
    describe("An overriding provider") {
        it("if the property exist in the first, should not go to the second loader") {
            provider.get("a", String::class.java).should.be.equal("overrideb")
            provider.get("a", String::class.java).should.be.equal("overrideb") // cached property!
            provider.get("c", String::class.java).should.be.equal("overrided")
        }

        it("if the property does not exist, then the second one should be tested") {
            provider.get<Int>("integerProperty").should.be.equal(1)
        }
    }
})