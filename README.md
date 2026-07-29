# Deploy-Inator

> Behold! My Deploy-Inator!

A simple gradle plugin that simplifies the deployment process for Minecraft mods to ModMaven, CurseForge, Modrinth, and
Discord! Deploy-Inator provides tools that generate GitHub workflows which allows you to automatically deploy your
project to all the aformentioned platforms by creating a release on your GitHub repository!

Internally, Deploy-Inator uses [modmuss50's Mod Publish Plugin](https://github.com/modmuss50/mod-publish-plugin).

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
				includeGroup "net.swedz.deploy-inator"
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

In your `build.gradle`, you will want to choose an environment variable name. By default, Deploy-Inator provides
"MOD_VERSION". Below is an example of how you should configure your project version:

```groovy
version = (System.getenv("MOD_VERSION") ?: "0.0.0-local")
```

The below uses reference to properties that you can define in your `gradle.properties`. I recommend doing it this way,
but you can also write your values out directly in strings.

```groovy
deployInator {
	// The environment variable name to be used for your project's version.
	// This must be the same environment variable that you used to define your project version as explained above.
	// Optional. Defaults to "MOD_VERSION".
	versionEnvironmentVariableName = "MOD_VERSION"
	// If you are using Deploy-Inator for publishing mods, these are required.
	// The display name for the mod. Will be included in the release name on Curseforge and Modrinth.
	// Optional. Must be set if Curseforge or Modrinth publishing is to be used.
	modName = project.mod_name
	// The list of compatible Minecraft versions that are supported by your mod.
	// This should be a list such as: {version},...
	// Optional. Must be set if Curseforge or Modrinth publishing is to be used.
	compatibleMinecraftVersions project.compatible_minecraft_versions
	// The list of mod loaders that your mod supports.
	// This should be a list such as: {loader},...
	// Optional. Must be set if Curseforge or Modrinth publishing is to be used.
	modLoaders "neoforge"
	
	maven {
		// The name of the gradle task to execute when publishing to ModMaven.
		// Optional. When not set, the generated GitHub workflow will not publish anywhere.
		publishTaskName = "publishMavenJavaPublicationToModmavenRepository"
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

## GitHub Workflow

### Generating Files

After making changes to your build scripts or gradle properties that pertains to your Deploy-Inator configuration, if
you do not have `autoGenerateFiles` set to `true`, you should run the `deployInatorGenerateFiles` task. This will
generate your GitHub workflow and related files for you. Otherwise, your workflow configuration may become out of date.

### Secrets

In order for Deploy-Inator to publish your project to ModMaven, Curseforge, Modrinth, and Discord, you must configure
your GitHub repository with your secrets. These can be added in your repository's settings under `Security and quality >
Secrets and variables > Actions > Secrets > Repository secrets`. The below are the secret names needed and what they
are used for.

If a secret pertaining to a given step is not set in your repository, the step will be skipped.

| Secret Name                             | Definition                                           |
|-----------------------------------------|------------------------------------------------------|
| MODMAVEN_USERNAME<br/>MODMAVEN_PASSWORD | The username and password to your ModMaven account.  |
| CURSEFORGE_API_KEY                      | The API key to publish to Curseforge with.           |
| MODRINTH_API_KEY                        | The API key to publish to Modrinth with.             |
| RELEASE_WEBHOOK_URL                     | The webhook URL to use to send a message in Discord. |

## Creating Releases

Once Deploy-Inator is fully configured to your liking, publishing to all your configured platforms is as simple as
creating a release on GitHub. Here are a some caveats to keep in mind:

- Release content format must use the default generated format by GitHub.
	- With the only exception being that the "New Contributors" section it generates is not supported. Whenever that is
	  created, you must remove it. Ensure that there are exactly **two empty lines** between the end of your "What's
      Changed" section and the "Full changelog" line.
        - I do hope to eventually remedy this, but it works for my use case at the moment so I have not bothered yet.
- The version value provided to your environment variable will be the release tag name except the first hyphen and all
  subsequent characters.
    - This means a tag with the name "1.0.0-1.21.1" will result in the version of your mod being "1.0.0".
    - Additionally, including "alpha" or "beta" in your release tag will flag it as such for all configured destination
      platforms.