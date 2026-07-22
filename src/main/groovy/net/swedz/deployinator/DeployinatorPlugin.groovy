package net.swedz.deployinator

import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.Curseforge
import me.modmuss50.mpp.platforms.modrinth.Modrinth
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

class DeployinatorPlugin implements Plugin<Project>
{
	static Map<String, String> gatherProjectRelations(String property)
	{
		Map<String, String> projectRelations = new HashMap<>()

		def relations = property.split(",")
		relations.each {
			def split = it.split(":")
			if(split.length == 2)
			{
				def dependency = split[0]
				def requirementType = split[1]
				projectRelations.put(dependency, requirementType)
			}
		}

		return projectRelations
	}

	static ReleaseType releaseType()
	{
		def type = System.getenv("RELEASE_TYPE") ?: "alpha"
		if(type == "alpha")
		{
			return ReleaseType.ALPHA
		}
		else if(type == "beta")
		{
			return ReleaseType.BETA
		}
		else
		{
			return ReleaseType.STABLE
		}
	}

	@Override
	void apply(Project project)
	{
		project.plugins.apply("maven-publish")
		project.plugins.apply("me.modmuss50.mod-publish-plugin")

		project.afterEvaluate {
			var properties = DeployinatorProperties.getOrThrow(it)

			GenerateFilesFromTemplates.generate(project)
			applyBuildChangelog(it)
			applyModMaven(it, properties)
			applyPublishMods(it, properties)
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

	private static void applyModMaven(Project project, DeployinatorProperties properties)
	{
		var publishing = project.extensions.getByType(PublishingExtension)
		publishing.publications {
			it.register('mavenJava', MavenPublication) {
				it.from project.components.java
				it.artifactId = properties.artifact_name()
				it.artifact(project.tasks.named("sourceJar")) {
					it.setClassifier("sources")
				}
			}
		}
		publishing.repositories {
			it.mavenLocal()
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

	private static void applyPublishMods(Project project, DeployinatorProperties properties)
	{
		var publishMods = project.extensions.getByType(ModPublishExtension)

		publishMods.changelog.set(System.getenv("MOD_CHANGELOG_TRIMMED_PUBLISH"))
		publishMods.type.set(releaseType())
		publishMods.file.set(project.tasks.named("jar", Jar).flatMap {
			it.archiveFile
		})
		publishMods.version.set(project.version.toString())
		publishMods.displayName.set("${properties.mod_name()} ${project.version}")
		publishMods.modLoaders.set(["neoforge"])

		publishMods.curseforge { Curseforge curseforge ->
			curseforge.projectId.set(properties.curseforge_project())
			curseforge.accessToken.set(System.getenv("CURSEFORGE_API_KEY"))
			curseforge.javaVersions.add(JavaVersion.toVersion(project.extensions.getByType(JavaPluginExtension).toolchain.languageVersion.get().asInt()))
			curseforge.client.set(true)
			curseforge.server.set(true)
			curseforge.changelogType.set("markdown")

			def compatible_minecraft_versions = properties.compatible_minecraft_versions().split(",")
			compatible_minecraft_versions.each {
				curseforge.minecraftVersions.add(it)
			}

			gatherProjectRelations(properties.curseforge_project_relations()).each {
				def dependency = it.getKey()
				def requirementType = it.getValue()
				if(requirementType == "required")
				{
					curseforge.requires(dependency)
				}
				else if(requirementType == "optional")
				{
					curseforge.optional(dependency)
				}
				else if(requirementType == "incompatibility")
				{
					curseforge.incompatible(dependency)
				}
			}
		}

		publishMods.modrinth { Modrinth modrinth ->
			modrinth.projectId.set(properties.modrinth_project())
			modrinth.accessToken.set(System.getenv("MODRINTH_API_KEY"))

			def compatible_minecraft_versions = properties.compatible_minecraft_versions().split(",")
			compatible_minecraft_versions.each {
				modrinth.minecraftVersions.add(it)
			}

			gatherProjectRelations(properties.modrinth_project_relations()).each {
				def dependency = it.getKey()
				def requirementType = it.getValue()
				if(requirementType == "required")
				{
					modrinth.requires(dependency)
				}
				else if(requirementType == "optional")
				{
					modrinth.optional(dependency)
				}
				else if(requirementType == "incompatibility")
				{
					modrinth.incompatible(dependency)
				}
			}
		}
	}
}
