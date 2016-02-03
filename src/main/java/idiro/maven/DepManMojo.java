/** 
 *  Copyright 2016 Idiro Analytics Ltd
 *
 *  This file is part of Maven Dynamic Dependency Manager
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package idiro.maven;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * Replace in the install pom variable with running time values.
 * @author etienne
 *
 */
@Mojo(name = "depman")
public class DepManMojo extends AbstractMojo {
	
	
	/**
	 * The directory where the generated flattened POM file will be written to.
	 */
	@Parameter( defaultValue = "${project.build.directory}" )
	private File outputDirectory;
	
	
	/**
	 * The filename of the generated flattened POM file.
	 */
	@Parameter( property = "runTimePomFilename", defaultValue = "runtime-pom.xml" )
	private String runTimePomFilename;
	
	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	@Parameter( defaultValue = "${project}", readonly = true, required = true )
	private MavenProject project;
	 
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Set pom file to runtime");
		File currentProjectFile = project.getFile();
		outputDirectory.mkdir();
		File newProjectFile = changePomFile(currentProjectFile,outputDirectory,runTimePomFilename);
		if(newProjectFile != null){
			project.setFile(newProjectFile);
		}
	}
	
	public File changePomFile(File projectFile, File directory, String filename){
		File ans = new File(directory,filename);
		getLog().debug("Change "+projectFile.toString());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(projectFile);
			NodeList nList = doc.getElementsByTagName("properties").item(0).getChildNodes();
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = nNode.getNodeName();
					getLog().debug("Current element: "+nodeName);
					if(System.getProperty(nodeName) != null){
						getLog().info(nodeName+": change value to "+System.getProperty(nodeName));
						nNode.setTextContent(System.getProperty(nodeName));
						project.getProperties().setProperty(nodeName,System.getProperty(nodeName));
					}
				}
			}
			nList = doc.getElementsByTagName("version");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				nNode.setTextContent(replace(nNode.getTextContent()));
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(ans);
			transformer.transform(source, result);
			
		} catch (Exception e) {
			getLog().error("Error",e);
			return null;
		}
		return ans;
	}
	
	public String replace(String content){
		if(!content.contains("$")){
			return content;
		}
		Iterator<Object> itProj = project.getProperties().keySet().iterator();
		while(itProj.hasNext()){
			Object cur = itProj.next();
			try{
				content = content.replaceAll(Pattern.quote("${"+cur+"}"),replace(project.getProperties().getProperty(cur.toString())));
			}catch(Exception e){
				getLog().error("Fail replacing in "+content+" with "+project.getProperties().getProperty(cur.toString()),e);
			}
		}
		return content;
	}
}
