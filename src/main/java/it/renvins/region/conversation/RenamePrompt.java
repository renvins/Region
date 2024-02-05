package it.renvins.region.conversation;

import it.renvins.region.service.IConfigService;
import it.renvins.region.service.IRegionsService;
import it.renvins.region.util.Messaging;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class RenamePrompt extends StringPrompt {

    private final String regionName;

    private final IConfigService configService;
    private final IRegionsService regionsService;

    @Override
    public String getPromptText(ConversationContext context) {
        return Messaging.color(configService.getLang().getString("insertNewName"));
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Conversable conversable = context.getForWhom();
        Player player = (Player) conversable;

        regionsService.renameRegion(regionName, input).thenAccept(result -> {
            if (!result) {
                Messaging.sendMessage(player, configService.getLang().getString("cantRenameRegion"));
                return;
            }
            Messaging.sendMessage(player, configService.getLang().getString("regionRenamed"));
        });
        return Prompt.END_OF_CONVERSATION;
    }
}
