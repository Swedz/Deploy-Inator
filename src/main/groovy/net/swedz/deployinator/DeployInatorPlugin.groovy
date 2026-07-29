package net.swedz.deployinator

import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.Curseforge
import me.modmuss50.mpp.platforms.modrinth.Modrinth
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.jvm.tasks.Jar

class DeployInatorPlugin implements Plugin<Project>
{
	@Override
	void apply(Project project)
	{
		project.plugins.apply("maven-publish")
		project.plugins.apply("me.modmuss50.mod-publish-plugin")
		
		var deployInator = project.extensions.create("deployInator", DeployInatorExtension)
		deployInator.versionEnvironmentVariableName.convention("MOD_VERSION")
		deployInator.maven.enabled.convention(false)
		deployInator.maven.includeModMavenRepository.convention(true)
		deployInator.curseforge.enabled.convention(false)
		deployInator.modrinth.enabled.convention(false)
		deployInator.discord.enabled.convention(false)
		
		project.afterEvaluate {
			applyGenerateFilesTask(it, deployInator)
			applyBuildChangelog(it)
			applyModMaven(it, deployInator)
			applyPublishMods(it, deployInator)
		}
	}
	
	static JavaVersion getJavaVersionByProjectToolchain(Project project)
	{
		return JavaVersion.toVersion(project.extensions.getByType(JavaPluginExtension).toolchain.languageVersion.get().asInt())
	}
	
	private static void applyGenerateFilesTask(Project project, DeployInatorExtension deployInator)
	{
		project.tasks.register("deployInatorGenerateFiles") {
			it.doLast {
				GenerateFilesFromTemplates.generate(project, deployInator)
			}
		}
	}
	
	private static void applyBuildChangelog(Project project)
	{
		project.tasks.register("buildChangelog") {
			it.doLast {
				ChangelogFormatter.appendToGithubEnv()
			}
		}
	}
	
	private static void applyModMaven(Project project, DeployInatorExtension extension)
	{
		if(!extension.maven.enabled.get())
		{
			return
		}
		
		var publishing = project.extensions.getByType(PublishingExtension)
		
		if(extension.maven.includeModMavenRepository.get())
		{
			publishing.repositories {
				it.maven {
					credentials {
						it.username = System.getenv("MODMAVEN_USERNAME")
						it.password = System.getenv("MODMAVEN_PASSWORD")
					}
					name = "modmaven"
					url = "https://modmaven.dev/artifactory/local-releases/"
				}
			}
		}
	}
	
	private static ReleaseType releaseType()
	{
		var type = System.getenv("RELEASE_TYPE") ?: "alpha"
		return switch(type)
		{
			case "alpha" -> ReleaseType.ALPHA
			case "beta" -> ReleaseType.BETA
			default -> ReleaseType.STABLE
		}
	}
	
	private static void applyPublishMods(Project project, DeployInatorExtension extension)
	{
		if(!extension.modName.isPresent() ||
		   !extension.compatibleMinecraftVersions.isPresent() ||
		   !extension.modLoaders.isPresent())
		{
			return
		}
		
		var publishMods = project.extensions.getByType(ModPublishExtension)
		
		publishMods.changelog.set(System.getenv("MOD_CHANGELOG_TRIMMED_PUBLISH"))
		publishMods.type.set(releaseType())
		publishMods.file.set(project.tasks.named("jar", Jar).flatMap {
			it.archiveFile
		})
		publishMods.version.set(project.version.toString())
		publishMods.displayName.set("${extension.modName.get()} ${project.version}")
		publishMods.modLoaders.addAll(extension.modLoaders.get())
		
		if(extension.curseforge.enabled.get())
		{
			publishMods.curseforge { Curseforge curseforge ->
				curseforge.projectId.set(extension.curseforge.id.get())
				curseforge.accessToken.set(System.getenv("CURSEFORGE_API_KEY"))
				curseforge.javaVersions.add(getJavaVersionByProjectToolchain(project))
				curseforge.client.set(extension.curseforge.client.orElse(true))
				curseforge.server.set(extension.curseforge.server.orElse(true))
				curseforge.changelogType.set("markdown")
				
				curseforge.minecraftVersions.addAll(extension.compatibleMinecraftVersions.get())
				
				extension.curseforge.relations.get().each {
					var dependency = it.getKey()
					var requirementType = it.getValue()
					switch(requirementType)
					{
						case "required":
							curseforge.requires(dependency)
							break
						case "optional":
							curseforge.optional(dependency)
							break
						case "incompatibility":
							curseforge.incompatible(dependency)
							break
					}
				}
			}
		}
		
		if(extension.modrinth.enabled.get())
		{
			publishMods.modrinth { Modrinth modrinth ->
				modrinth.projectId.set(extension.modrinth.id.get())
				modrinth.accessToken.set(System.getenv("MODRINTH_API_KEY"))
				
				modrinth.environment.set(extension.modrinth.environment.orElse(ModrinthEnvironment.CLIENT_AND_SERVER))
				
				modrinth.minecraftVersions.addAll(extension.compatibleMinecraftVersions.get())
				
				extension.modrinth.relations.get().each {
					var dependency = it.getKey()
					var requirementType = it.getValue()
					switch(requirementType)
					{
						case "required":
							modrinth.requires(dependency)
							break
						case "optional":
							modrinth.optional(dependency)
							break
						case "incompatibility":
							modrinth.incompatible(dependency)
							break
					}
				}
			}
		}
	}
}
