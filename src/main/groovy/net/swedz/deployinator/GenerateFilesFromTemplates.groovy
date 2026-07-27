package net.swedz.deployinator

import org.gradle.api.GradleException
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
		
		boolean shouldPublishToDiscord =
				deployInator.discord.enabled.get() &&
				deployInator.discord.name.isPresent() &&
				deployInator.discord.iconUrl.isPresent()
		templateContent = templateContent.replaceAll(Pattern.quote("%{should_publish_to_discord}%"), shouldPublishToDiscord.toString())
		if(shouldPublishToDiscord)
		{
			templateContent = templateContent.replaceAll(Pattern.quote("%{discord_name}%"), deployInator.discord.name.get())
			templateContent = templateContent.replaceAll(Pattern.quote("%{discord_icon_url}%"), deployInator.discord.iconUrl.get())
		}
		
		boolean includeModMavenRepository =
				deployInator.maven.enabled.get() &&
				deployInator.maven.includeModMavenRepository.get() &&
				deployInator.maven.publishTaskName.isPresent()
		templateContent = templateContent.replaceAll(Pattern.quote("%{should_publish_to_modmaven}%"), includeModMavenRepository.toString())
		if(includeModMavenRepository)
		{
			if(!project.tasks.names.contains(deployInator.maven.publishTaskName.get()))
			{
				throw new GradleException("Cannot find task for name " + deployInator.maven.publishTaskName.get())
			}
			templateContent = templateContent.replaceAll(Pattern.quote("%{publish_task_name}%"), deployInator.maven.publishTaskName.get())
		}
		
		return templateContent
	}
}
