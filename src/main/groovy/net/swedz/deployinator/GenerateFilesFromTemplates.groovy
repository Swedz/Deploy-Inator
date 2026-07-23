package net.swedz.deployinator

import org.gradle.api.Project

import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

class GenerateFilesFromTemplates
{
	static void generate(Project project, DeployInatorExtension deployInator)
	{
		copyTemplate(project, deployInator, ".github/workflows/deploy.yml")
		copyTemplate(project, deployInator, ".github/workflows/discord_message.json")
	}
	
	private static void copyTemplate(
			Project project,
			DeployInatorExtension deployInator,
			String path
	)
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
		templateContent = templateContent.replaceAll(Pattern.quote("%{java_version}%"), DeployInatorPlugin.getJavaVersionByProjectToolchain(project).toString())
		templateContent = templateContent.replaceAll(Pattern.quote("%{discord_name}%"), deployInator.discord.name.get())
		templateContent = templateContent.replaceAll(Pattern.quote("%{discord_icon_url}%"), deployInator.discord.iconUrl.get())
		if(!destinationFile.exists() || destinationFile.text != templateContent)
		{
			destinationFile.text = templateContent
		}
	}
}
