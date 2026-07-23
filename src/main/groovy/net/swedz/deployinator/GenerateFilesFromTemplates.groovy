package net.swedz.deployinator

import org.gradle.api.Project

import java.nio.charset.StandardCharsets

class GenerateFilesFromTemplates
{
	static void generate(Project project)
	{
		copyTemplate(project, ".github/workflows/deploy.yml")
		copyTemplate(project, ".github/workflows/discord_message.json")
	}
	
	private static void copyTemplate(Project project, String path)
	{
		var destinationFile = project.file(path)
		var parentDirectory = destinationFile.parentFile
		if(parentDirectory != null && !parentDirectory.exists())
		{
			parentDirectory.mkdirs()
		}
		
		var resourcePath = "/templates/" + path
		var resourceStream = DeployInatorPlugin.getResourceAsStream(resourcePath)
		if(resourceStream == null)
		{
			throw new IllegalStateException("Could not find template file at resource path: ${resourcePath}")
		}
		
		var templateContent = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8)
		if(!destinationFile.exists() || destinationFile.text != templateContent)
		{
			destinationFile.text = templateContent
		}
	}
}
