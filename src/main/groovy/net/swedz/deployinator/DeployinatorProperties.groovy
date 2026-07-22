package net.swedz.deployinator

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

record DeployinatorProperties(
		String artifact_name,
		String mod_name,
		String compatible_minecraft_versions,
		String curseforge_project,
		String curseforge_project_relations,
		String modrinth_project,
		String modrinth_project_relations
)
{
	static DeployinatorProperties getOrThrow(Project project)
	{
		var artifact_name = project.findProperty("artifact_name")?.toString()
		if(!artifact_name)
		{
			throw new InvalidUserDataException("Missing 'artifact_name' property in gradle.properties")
		}
		var mod_name = project.findProperty("mod_name")?.toString()
		if(!mod_name)
		{
			throw new InvalidUserDataException("Missing 'mod_name' property in gradle.properties")
		}
		var compatible_minecraft_versions = project.findProperty("compatible_minecraft_versions")?.toString()
		if(!compatible_minecraft_versions)
		{
			throw new InvalidUserDataException("Missing 'compatible_minecraft_versions' property in gradle.properties")
		}
		var curseforge_project = project.findProperty("curseforge_project")?.toString()
		if(!curseforge_project)
		{
			throw new InvalidUserDataException("Missing 'curseforge_project' property in gradle.properties")
		}
		var curseforge_project_relations = project.findProperty("curseforge_project_relations")?.toString()
		if(!curseforge_project_relations)
		{
			throw new InvalidUserDataException("Missing 'curseforge_project_relations' property in gradle.properties")
		}
		var modrinth_project = project.findProperty("modrinth_project")?.toString()
		if(!modrinth_project)
		{
			throw new InvalidUserDataException("Missing 'modrinth_project' property in gradle.properties")
		}
		var modrinth_project_relations = project.findProperty("modrinth_project_relations")?.toString()
		if(!modrinth_project_relations)
		{
			throw new InvalidUserDataException("Missing 'modrinth_project_relations' property in gradle.properties")
		}
		return new DeployinatorProperties(
				artifact_name,
				mod_name,
				compatible_minecraft_versions,
				curseforge_project,
				curseforge_project_relations,
				modrinth_project,
				modrinth_project_relations
		)
	}
}