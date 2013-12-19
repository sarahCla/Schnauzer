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
package com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.common.glossary.Column;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.Tags;

/**
 * 
 * @author SarahCla
 */
public class SASRDBSchnauzer extends RDBSchnauzer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SASRDBSchnauzer.class);	
	
	private List<String> profilors = new ArrayList<String>();
	
	public void addProfilor(String id) {
		if (profilors.indexOf(id)>=0) return;
		profilors.add(id);
	}
	
	public void deleteProfilor(String id) {
		if (profilors.indexOf(id)<0) return;
		profilors.remove(id);
	}
	
	public void clearProfilors() {
		profilors.clear();
	}
	
	@Override
	public boolean needReplicate(List<Column> columns) {
		if (this.checkFieldIndex<0)	return true;
		String value = columns.get(this.checkFieldIndex).toString();
		if (Tags.Verbose) LOGGER.info(Infos.CheckValue + "=" + value);
		return (profilors.contains(value));
	}
	
	
}
