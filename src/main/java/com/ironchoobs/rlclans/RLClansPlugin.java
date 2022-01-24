package com.ironchoobs.rlclans;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "RL Clans"
)
public class RLClansPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RLClansConfig config;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private ClientToolbar clientToolbar;

	private RLClansPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");

		panel = new RLClansPanel(this, config, client, skillIconManager);

		// TODO: Make an icon and load it here
		final BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

		navButton = NavigationButton.builder()
				.tooltip("Clan")
				.icon(img)
				.priority(8) 	// position in RHS button list
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");

		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// client logged in
			panel.loggedIn(client);
		}
	}

	@Provides
	RLClansConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RLClansConfig.class);
	}
}
