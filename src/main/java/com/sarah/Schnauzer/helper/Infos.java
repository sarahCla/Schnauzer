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
package com.sarah.Schnauzer.helper;

/**
 * 
 * @author SarahCla
 */
public interface Infos {
	String Failed = "失败";
	String OK = "成功";
	String IllegalAttr = "无效属性:";
	String IllegalTag = "无效标签:";
	String Illegal = "无效";
	String ConMaster = "连接Master数据库";
	String ConSlave = "连接Slave数据库";
	String ConDB = "数据库连接";
	String GetTableConfigs = "获取待复制表配置";
	String GetDBConfigs = "获取数据库配置";
	String GetRepStatus = "获取复制起始状态";
	String GetHeartBeatConf = "获取心跳配置";
	String MailRegist = "注册了{}个报警邮件";
	String GetMailConf = "获取注册邮件";
	String OpenConfigFile = "打开配置文件";
	String RedisDataType = "Redis数据类型";
	String TableNotExist = "表{}在master数据库{}中不存在";
	String DoInsert = "插入语句";
	String DoDelete = "删除语句";
	String DoUpdate = "更新语句";
	String Create = "生成";
	String SendErrorInfo = "出错信息包发送。。。";
	String RepFailed = "【复制故障】"; 
	String Row = "行";
	String AutoSetPos = "自动更新位置";
	String FailedEmailTitle = "【下面的语句导致复制停止，请尽快处理，重启复制】";
	String ErrorType = "【错误类型】";
	String Config = "【配置状况】";
	String Type = "类型";
	String CurPos = "【当前复制位置】";
	String CheckValue = "检测值";
	String SetPos = "更新位置";
	String DateParseError = "日期格式：{}解析失败，请在config.xml中添加格式配置";
	String EncodeParseError = "编码转换错误";
}
