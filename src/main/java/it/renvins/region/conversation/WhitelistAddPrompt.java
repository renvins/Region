package it.renvins.region.conversation;

import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.structure.Region;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class WhitelistAddPrompt extends StringPrompt {

    private final Region region;

    private final IConfigService configService;
    private final IRegionsService regionsService;

    @Override
    public String getPromptText(ConversationContext context) {
        return Messaging.color(configService.getLang().getString("insertUsername"));
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Conversable conversable = context.getForWhom();
        Player player = (Player) conversable;

        regionsService.addWhitelistedPlayer(input, region).thenAccept(result -> {
            if (!result) {
                Messaging.sendMessage(player, configService.getLang().getString("cantAddWhitelist"));
                return;
            }
            Messaging.sendMessage(player, configService.getLang().getString("addedWhitelist"));
        });
        return Prompt.END_OF_CONVERSATION;
    }
}
