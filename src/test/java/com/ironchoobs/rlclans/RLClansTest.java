package com.ironchoobs.rlclans;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RLClansTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RLClansPlugin.class);
		RuneLite.main(args);
	}
}