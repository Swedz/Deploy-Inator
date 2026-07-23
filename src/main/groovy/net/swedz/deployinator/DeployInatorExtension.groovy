package net.swedz.deployinator

import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

abstract class DeployInatorExtension
{
	abstract Property<Boolean> getAutoGenerateFiles()
	
	abstract Property<String> getArtifactName()
	
	abstract Property<String> getModName()
	
	abstract ListProperty<String> getCompatibleMinecraftVersions()
	
	void compatibleMinecraftVersions(String versions)
	{
		this.getCompatibleMinecraftVersions().addAll(versions.split(","))
	}
	
	@Nested
	abstract ModProject getCurseforge()
	
	void curseforge(Action<? super ModProject> action)
	{
		action.execute(this.getCurseforge())
	}
	
	@Nested
	abstract ModProject getModrinth()
	
	void modrinth(Action<? super ModProject> action)
	{
		action.execute(this.getModrinth())
	}
	
	abstract static class ModProject
	{
		abstract Property<String> getId()
		
		abstract MapProperty<String, String> getRelations()
		
		void relations(String property)
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
			
			this.getRelations().set(projectRelations)
		}
	}
}
