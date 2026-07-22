package net.swedz.deployinator

class ChangelogFormatter
{
	private static String trim(String changelog, int maxLength, boolean discordFormat)
	{
		changelog = changelog.replaceAll(/## What's Changed\r?\n/, '')

		if(discordFormat)
		{
			changelog = changelog.replaceAll(/## New Contributors/, '**New Contributors**')
		}

		changelog = changelog.replaceAll(/@([a-zA-Z0-9_]+)/, '`@$1`')

		changelog = changelog.replaceAll(/https:\/\/github.com\/([^ ]+)\/pull\/([0-9]+)/, '[#\$2](https://github.com/\$1/pull/\$2)')

		changelog = changelog.replaceAll(/\*\*Full Changelog\*\*: (https:\/\/github.com\/\S+)/, '[Full Changelog](\$1)')

		if(discordFormat)
		{
			changelog = changelog.replaceAll(/\r?\n/, '\\\\n')
			def previous = null
			while(changelog != previous)
			{
				previous = changelog
				changelog = changelog.replaceAll(/\\n\\n/, '\\\\n')
			}
		}

		if(maxLength > 0)
		{
			def changelogLines = changelog.split('\\\\n')
			def lastLine = changelogLines[-1]
			def trimmedChangelog = []
			def currentLength = lastLine.length()

			for(int i = 0; i < changelogLines.size() - 1; i++)
			{
				def line = changelogLines[i]
				if(currentLength + line.length() + 2 + 5 > maxLength)
				{
					trimmedChangelog << '...'
					break
				}
				trimmedChangelog << line
				currentLength += line.length() + 2
			}

			trimmedChangelog << lastLine

			return trimmedChangelog.join('\\n')
		}
		else
		{
			return changelog
		}
	}

	static void appendToGithubEnv()
	{
		def githubEnvFile = new File(System.getenv("GITHUB_ENV"))

		def changelog = System.getenv("MOD_CHANGELOG")

		def changelogTrimmedDiscord = trim(changelog, 1024, true)
		githubEnvFile.append("MOD_CHANGELOG_TRIMMED_DISCORD<<EOF\n${changelogTrimmedDiscord}\nEOF\n")

		def changelogTrimmedPublish = trim(changelog, 0, false)
		githubEnvFile.append("MOD_CHANGELOG_TRIMMED_PUBLISH<<EOF\n${changelogTrimmedPublish}\nEOF\n")
	}
}
