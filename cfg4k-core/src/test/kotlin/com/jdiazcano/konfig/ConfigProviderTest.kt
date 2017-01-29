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

package com.jdiazcano.konfig

import com.jdiazcano.konfig.loaders.JsonConfigLoader
import com.jdiazcano.konfig.loaders.PropertyConfigLoader
import com.jdiazcano.konfig.providers.Providers.cached
import com.jdiazcano.konfig.providers.Providers.proxy
import com.jdiazcano.konfig.providers.bind
import com.jdiazcano.konfig.providers.cache
import com.jdiazcano.konfig.providers.getProperty
import com.jdiazcano.konfig.utils.SettingNotFound
import com.winterbe.expekt.should
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.test.assertFailsWith

class ConfigProviderTest: Spek({

    val providers = listOf(
            proxy(PropertyConfigLoader(javaClass.classLoader.getResource("test.properties"))),
            proxy(JsonConfigLoader(javaClass.classLoader.getResource("test.json"))),
            proxy(PropertyConfigLoader(javaClass.classLoader.getResource("test.properties"))).cache(),
            cached(proxy(JsonConfigLoader(javaClass.classLoader.getResource("test.json"))))
    )

    providers.forEachIndexed { i, provider ->
        describe("provider[$i]") {
            it("default values") {
                provider.getProperty("this.does.not.exist", 1).should.be.equal(1)
                // When having a cached provider then it will cache the "this.does.not.exist" if it has a default value
                // because the delegated provider will return the default value. Should the default value not be passed
                // and the exception caught? I think that would mean a performance impact and having exceptions into
                // account for normal logic is not right
                assertFailsWith<SettingNotFound> {
                    provider.getProperty<Int>("i.dont.extist")
                }
            }

            it("integer properties") {
                provider.getProperty("integerProperty", Int::class.java).should.be.equal(1)
                provider.getProperty("integerProperty", Integer::class.java).should.be.equal(Integer(1))
            }

            it("long properties") {
                provider.getProperty("longProperty", Long::class.java).should.be.equal(2)
            }

            it("short properties") {
                provider.getProperty("shortProperty", Short::class.java).should.be.equal(1)
            }

            it("float properties") {
                provider.getProperty("floatProperty", Float::class.java).should.be.equal(2.1F)
            }

            it("double properties") {
                provider.getProperty("doubleProperty", Double::class.java).should.be.equal(1.1)
            }

            it("byte properties") {
                provider.getProperty("byteProperty", Byte::class.java).should.be.equal(2)
            }

            it("boolean properties") {
                provider.getProperty("booleanProperty", Boolean::class.java).should.be.`true`
            }

            it("big integer properties") {
                provider.getProperty<BigInteger>("bigIntegerProperty").should.be.equal(BigInteger("1"))
            }

            it("big decimal properties") {
                provider.getProperty<BigDecimal>("bigDecimalProperty").should.be.equal(BigDecimal("1.1"))
            }

            it("binding test") {
                val testBinder = provider.bind<TestBinder>("")
                testBinder.booleanProperty().should.be.`true`
                testBinder.integerProperty().should.be.equal(1)
                testBinder.longProperty().should.be.equal(2)
                testBinder.shortProperty().should.be.equal(1)
                testBinder.floatProperty().should.be.equal(2.1F)
                testBinder.doubleProperty().should.be.equal(1.1)
                testBinder.byteProperty().should.be.equal(2)
                testBinder.a().should.be.equal("b")
                testBinder.c().should.be.equal("d")
                testBinder.list().should.be.equal(listOf(1, 2, 3))
                testBinder.floatList().should.be.equal(listOf(1.2F, 2.2F, 3.2F))
                testBinder.bigDecimalProperty().should.be.equal(BigDecimal("1.1"))
                testBinder.bigIntegerProperty().should.be.equal(BigInteger("1"))

                // toString should be the object tostring not the one that comes from the property
                testBinder.toString().should.not.be.equal("this should not be ever used")
            }
        }
    }
})
