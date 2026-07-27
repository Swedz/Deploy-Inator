package net.swedz.deployinator

import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

abstract class DeployInatorExtension
{
	abstract Property<String> getModName()
	
	abstract ListProperty<String> getCompatibleMinecraftVersions()
	
	void compatibleMinecraftVersions(String versions)
	{
		this.getCompatibleMinecraftVersions().addAll(versions.split(","))
	}
	
	abstract ListProperty<String> getModLoaders()
	
	void modLoaders(String modLoaders)
	{
		this.getModLoaders().addAll(modLoaders.split(","))
	}
	
	@Nested
	abstract Maven getMaven()
	
	void maven(Action<? super Maven> action)
	{
		this.getMaven().enabled.set(true)
		action.execute(this.getMaven())
	}
	
	abstract static class Maven
	{
		abstract Property<Boolean> getEnabled()
		
		abstract Property<String> getPublishTaskName()
		
		abstract Property<Boolean> getIncludeModMavenRepository()
	}
	
	@Nested
	abstract CurseforgeModProject getCurseforge()
	
	void curseforge(Action<? super CurseforgeModProject> action)
	{
		this.getCurseforge().enabled.set(true)
		action.execute(this.getCurseforge())
	}
	
	@Nested
	abstract ModrinthModProject getModrinth()
	
	void modrinth(Action<? super ModrinthModProject> action)
	{
		this.getModrinth().enabled.set(true)
		action.execute(this.getModrinth())
	}
	
	abstract static class ModProject
	{
		abstract Property<Boolean> getEnabled()
		
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
	
	abstract static class CurseforgeModProject extends ModProject
	{
		abstract Property<Boolean> getClient()
		
		abstract Property<Boolean> getServer()
	}
	
	abstract static class ModrinthModProject extends ModProject
	{
		abstract Property<ModrinthEnvironment> getEnvironment()
	}
	
	@Nested
	abstract Discord getDiscord()
	
	void discord(Action<? super Discord> action)
	{
		this.getDiscord().enabled.set(true)
		action.execute(this.getDiscord())
	}
	
	abstract static class Discord
	{
		abstract Property<Boolean> getEnabled()
		
		abstract Property<String> getName()
		
		abstract Property<String> getIconUrl()
	}
}
