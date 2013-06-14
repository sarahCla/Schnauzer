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
package com.sarah.tools.logging;    
import java.io.File;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.PrintWriter;  
  
import javax.xml.parsers.DocumentBuilder;  
import javax.xml.parsers.DocumentBuilderFactory;  
import org.apache.log4j.Logger;  
import org.w3c.dom.Document;  
import org.w3c.dom.Node;  
import org.w3c.dom.NodeList;  
  
import javax.xml.transform.OutputKeys;  
import javax.xml.transform.Transformer;  
import javax.xml.transform.TransformerException;  
import javax.xml.transform.TransformerFactory;  
import javax.xml.transform.dom.DOMSource;  
import javax.xml.transform.stream.StreamResult;  
  
/**
 * 
 * @author SarahCla
 */
public class SetLogUtils {  
    private Logger log = Logger.getLogger(SetLogUtils.class);  
    private String xmlfilename = "log4j.xml";  
  
    public void setlogpath(String logname, String logpath) {  
        Document document = load(xmlfilename);  
        Node root = document.getDocumentElement();  
        NodeList secondNodes = root.getChildNodes();  
        if (secondNodes != null) {  
            for (int i = 0; i < secondNodes.getLength(); i++) {  
                Node secondNode = secondNodes.item(i);  
                if (secondNode.getNodeType() == Node.ELEMENT_NODE && secondNode.getNodeName().equals("appender")) {  
                    String name = secondNode.getAttributes().getNamedItem("name").getNodeValue();  
                    for (Node thirdNode = secondNode.getFirstChild(); thirdNode != null; thirdNode = thirdNode.getNextSibling()) {  
                        if (thirdNode.getNodeType() == Node.ELEMENT_NODE && thirdNode.getNodeName().equals("param")) {  
                            String paramfilename = thirdNode.getAttributes().getNamedItem("name").getNodeValue();  
                            if (paramfilename.equals("File")) {  
                                String value = thirdNode.getAttributes().getNamedItem("value").getNodeValue();  
                                if (logname.equals(name)) {  
                                    thirdNode.getAttributes().getNamedItem("value").setNodeValue("./" + logpath+"/Schnauzer_log_");  
                                }  
                            }  
                        }  
                    }  
                }  
            }  
        }  
        xmlfilename = logpath+".xml";
        doc2XmlFile(document, xmlfilename);
    }  
  
    public Document load(String filename) {  
        System.out.println(filename);  
        Document document = null;  
        try {  
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder builder = factory.newDocumentBuilder();  
            document = builder.parse(new File(filename));  
            document.normalize();  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
        return document;  
    }  
  
    public void doc2XmlFile(Document document, String filename) {  
        try {  
            TransformerFactory tf = TransformerFactory.newInstance();  
            Transformer transformer = tf.newTransformer();  
            DOMSource source = new DOMSource(document);  
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");  
            PrintWriter pw = new PrintWriter(new FileOutputStream(filename));  
            StreamResult result = new StreamResult(pw);  
            transformer.transform(source, result);  
        } catch (TransformerException mye) {  
            log.info("throw exceptio when save " + filename + " " + mye);  
            mye.printStackTrace();  
        } catch (IOException exp) {  
        	log.info("throw exceptio when save " + filename + " " + exp);  
            exp.printStackTrace();  
        }  
  
    }  
}  