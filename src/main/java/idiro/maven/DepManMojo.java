package idiro.maven;

import java.io.File;

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
					}
				}
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
}
