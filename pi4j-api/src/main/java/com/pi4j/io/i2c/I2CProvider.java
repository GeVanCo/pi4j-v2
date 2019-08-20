package com.pi4j.io.i2c;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (API)
 * FILENAME      :  I2CProvider.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.provider.Provider;

public interface I2CProvider extends Provider<I2CProvider, I2C, I2CConfig> {

    default <T extends I2C> T create(I2CConfigBuilder builder) throws Exception {
        return (T)create(builder.build());
    }

    default <T extends I2C> T create(Integer bus, Integer device) throws Exception {
        var config = I2C.newConfigBuilder()
                .bus(bus)
                .device(device)
                .build();
        return (T)create(config);
    }

    default <T extends I2C> T create(Integer bus, Integer device, String id) throws Exception {
        var config = I2C.newConfigBuilder()
                .bus(bus)
                .device(device)
                .id(id)
                .build();
        return (T)create(config);
    }

    default <T extends I2C> T create(Integer bus, Integer device, String id, String name) throws Exception {
        var config = I2C.newConfigBuilder()
                .bus(bus)
                .device(device)
                .id(id)
                .name(name)
                .build();
        return (T)create(config);
    }

    default <T extends I2C> T create(Integer bus, Integer device, String id, String name, String description) throws Exception {
        var config = I2C.newConfigBuilder()
                .bus(bus)
                .device(device)
                .id(id)
                .name(name)
                .description(description)
                .build();
        return (T)create(config);
    }

}
