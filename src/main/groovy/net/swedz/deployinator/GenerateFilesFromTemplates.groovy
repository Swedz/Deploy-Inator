package net.swedz.deployinator

import org.gradle.api.Project

import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

class GenerateFilesFromTemplates
{
	static void generate(Project project, DeployInatorExtension deployInator)
	{
		copyTemplate(project, deployInator, ".github/workflows/deploy.yml")
		if(deployInator.discord.enabled.getOrElse(false))
		{
			copyTemplate(project, deployInator, ".github/workflows/discord_message.json")
		}
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
		var fileContent = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8)
		
		var templateContent = formatTemplateContent(project, deployInator, fileContent)
		if(!destinationFile.exists() || destinationFile.text != templateContent)
		{
			destinationFile.text = templateContent
		}
	}
	
	private static String formatTemplateContent(
			Project project,
			DeployInatorExtension deployInator,
			String input
	)
	{
		var templateContent = input
		templateContent = templateContent.replaceAll(Pattern.quote("%{java_version}%"), DeployInatorPlugin.getJavaVersionByProjectToolchain(project).toString())
		if(deployInator.discord.name.isPresent())
		{
			templateContent = templateContent.replaceAll(Pattern.quote("%{discord_name}%"), deployInator.discord.name.get())
		}
		if(deployInator.discord.iconUrl.isPresent())
		{
			templateContent = templateContent.replaceAll(Pattern.quote("%{discord_icon_url}%"), deployInator.discord.iconUrl.get())
		}
		var publicationName = deployInator.maven.publicationName.get()
		templateContent = templateContent.replaceAll(Pattern.quote("%{publish_task_name}%"), "publish${publicationName.charAt(0).toUpperCase().toString() + publicationName.substring(1)}PublicationToModmavenRepository")
		return templateContent
	}
}
