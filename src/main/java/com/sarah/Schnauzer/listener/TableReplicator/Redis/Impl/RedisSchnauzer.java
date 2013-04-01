/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sarah.Schnauzer.listener.TableReplicator.Redis.Impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.*;

/**
 * 
 * @author SarahCla
 */
public class RedisSchnauzer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisSchnauzer.class);	
	
	private RedisStructure type = null;
	
	private List<RepField> fullfields = new CopyOnWriteArrayList<RepField>();	
	private List<CheckField> conffields = new CopyOnWriteArrayList<CheckField>();
	private List<MemberField> memfields = new CopyOnWriteArrayList<MemberField>();
	private List<ScoreField> scorefields = new CopyOnWriteArrayList<ScoreField>();
	
	
	
}
