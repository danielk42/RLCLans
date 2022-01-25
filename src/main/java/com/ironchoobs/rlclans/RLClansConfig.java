package com.ironchoobs.rlclans;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("RLClans")
public interface
RLClansConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
		keyName = "lazyLoad",
		name = "Enable lazy loading",
		description = "Whether to load data as it is needed or on startup"
	)
	default boolean lazyLoad() { return true; }
}
