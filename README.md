# Deploy-Inator

> Behold! My Deploy-Inator!

A simple gradle plugin that simplifies the deployment process for Minecraft mods.

## Setup

First, you must included ModMaven in your plugin management repositories in your `settings.gradle`:

```groovy
pluginManagement {
	repositories {
		maven {
			name = "Modmaven"
			url "https://modmaven.dev"
			content {
				includeGroup "net.swedz"
			}
		}
	}
}
```

Now you can apply the plugin in your `build.gradle`:

```groovy
plugins {
	// For the latest version, see the releases on the GitHub repository.
	id 'net.swedz.deploy-inator' version 'VERSION'
}
```

The below uses reference to properties that you can define in your `gradle.properties`. I recommend doing it this way,
but you can also write your values out directly in strings.

```groovy
deployInator {
	// Whether the template files should be automatically generated on gradle reload.
	// This effectively runs the `deployInatorGenerateFiles` gradle task.
	// Optional. Defaults to false.
	autoGenerateFiles = false
	
	// If you are using Deploy-Inator for publishing mods, these are required.
	// The display name for the mod. Will be included in the release name on Curseforge and Modrinth.
	// Optional. Must be set if Curseforge or Modrinth publishing is to be used.
	modName = project.mod_name
	// The list of compatible Minecraft versions that are supported by your mod.
	// This should be a list such as: {version},...
	// Optional. Must be set if Curseforge or Modrinth publishing is to be used.
	compatibleMinecraftVersions project.compatible_minecraft_versions
	
	maven {
		// The publication name to use when publishing, as defined by your registered publication in your build.gradle.
		// Optional. When not set, the generated GitHub workflow will not publish to ModMaven.
		publicationName = "java"
		// Whether the publication to the ModMaven repository should be registered.
		// Optional. Defaults to true. If false, the generated GitHub workflow will not publish to ModMaven.
		includeModMavenRepository = true
	}

	curseforge {
		// The Curseforge project ID.
		// Required.
		id = project.curseforge_project
		// The project relations for the Curseforge releases to include.
		// This should be a list such as: {project_slug}:{required|optional|incompatibility},...
		// Optional. When not set, no relations will be used.
		relations project.curseforge_project_relations
		// Whether the mod runs on the server.
		// Optional. Defaults to true.
		server = true
		// Whether the mod runs on the client.
		// Optional. Defaults to true.
		client = true
	}

	modrinth {
		// The Modrinth project ID.
		// Required.
		id = project.modrinth_project
		// The project relations for the Modrinth releases to include.
		// This should be a list such as: {project_slug}:{required|optional|incompatibility},...
		// Optional. When not set, no relations will be used.
		relations project.modrinth_project_relations
		// The environment that the mod is expected to run on.
		// Optional. Defaults to ModrinthEnvironment.CLIENT_AND_SERVER
		environment = ModrinthEnvironment.CLIENT_AND_SERVER
	}
	
	discord {
		// The name to display in Discord update message publications.
		// Required.
		name = project.deploy_name
		// The URL to the image to display for the Discord update message publication.
		// I recommend using a direct static link to an image. Such as one NOT hosted by Discord.
		// Required.
		iconUrl = project.deploy_discord_icon
	}
}
```

## Generating GitHub Workflow

After making changes to your build scripts or gradle properties that pertains to your Deploy-Inator configuration, if
you do not have `autoGenerateFiles` set to `true`, you should run the `deployInatorGenerateFiles` task. This will
generate your GitHub workflow and related files for you. Otherwise, your workflow configuration may become out of date.