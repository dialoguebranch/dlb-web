/*
 *
 *                   Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *      as outlined below. Based on original source code licensed under the following terms:
 *
 *                                            ----------
 *
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dialoguebranch.web.service.storage;

import com.dialoguebranch.execution.DLBUser;
import com.dialoguebranch.execution.DLBVariable;
import com.dialoguebranch.execution.DLBVariableStore;
import com.dialoguebranch.execution.DLBVariableStoreChange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A {@link DLBVariableStoreJSONStorageHandler} can manage reading and writing
 * {@link DLBVariableStore}s to and from JSON file representations.
 * You can instantiate an instance of a {@link DLBVariableStoreJSONStorageHandler} by providing a
 * root dataDirectory. The storage handler will assume/create a single {username}.json file for
 * every DialogueBranch User that will contain a JSON representation of the DialogueBranch Variable Store.
 *
 * @author Harm op den Akker
 */
public class DLBVariableStoreJSONStorageHandler implements DLBVariableStoreStorageHandler {

    private String dataDirectory;
    private static final Object LOCK = new Object();
    private final Logger logger =
            AppComponents.getLogger(ClassUtils.getUserClass(getClass()).getSimpleName());

    public DLBVariableStoreJSONStorageHandler(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public DLBVariableStore read(DLBUser dlbUser) throws IOException, ParseException {
        synchronized (LOCK) {
            File dataDir = new File(dataDirectory);
            FileUtils.mkdir(dataDir);
            File dataFile = new File(dataDir, dlbUser.getId() + ".json");
            if (!dataFile.exists())
                return new DLBVariableStore(dlbUser);
            ObjectMapper mapper = new ObjectMapper();

            try {
                DLBVariable[] dlbVariables = mapper.readValue(dataFile,
                        new TypeReference<DLBVariable[]>() {});
                return new DLBVariableStore(dlbUser, dlbVariables);
            } catch (JsonProcessingException ex) {
                throw new ParseException(
                        "Failed to parse variable store file: " +
                                dataFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void write(DLBVariableStore variableStore) throws IOException {
        synchronized (LOCK) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE,
                    true);

            File dataDir = new File(dataDirectory);
            FileUtils.mkdir(dataDir);
            File dataFile = new File(dataDir,
                    variableStore.getDLBUser().getId() + ".json");

            // Write the DLBVariableStore only as a list of DLBVariables
            // (for easier deserialization).
            objectMapper.writeValue(dataFile,variableStore.getDLBVariables());
        }
    }

    @Override
    public void onChange(DLBVariableStore variableStore,
                         List<DLBVariableStoreChange> changes) {
        try {
            write(variableStore);
        } catch(IOException e) {
            logger.error("Failed to write variable store changes: " +
                    e.getMessage(), e);
        }
    }
}
