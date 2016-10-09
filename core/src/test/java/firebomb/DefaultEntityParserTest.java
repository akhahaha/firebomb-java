package firebomb;

import examples.burn.Conversation;
import examples.burn.User;
import firebomb.database.Data;
import firebomb.definition.EntityDefinition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultEntityParserTest {
    private static DefaultEntityParser entityParser = new DefaultEntityParser();
    private static EntityDefinition userDef = new EntityDefinition(User.class);

    @Test
    public void deserialize() throws Exception {
        Data data = new Data("");
        data.setValue("id", "user1234");
        data.setValue("displayName", "Alan");
        data.setValue("conversations/convo123/id", "convo123");

        User user = entityParser.deserialize(User.class, data);
        assertEquals("user1234", user.getId());
        assertEquals("Alan", user.getDisplayName());
        assertEquals("convo123", user.getConversations().get(0).getId());
    }

    @Test
    public void serialize() throws Exception {
        User user = new User();
        user.setId("user1234");
        user.setDisplayName("Alan");
        Conversation conversation = new Conversation();
        conversation.setId("convo123");
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);
        user.setConversations(conversations);

        Data data = entityParser.serialize(user);
        assertEquals("user1234", data.child("users/user1234/id").getValue());
        assertEquals("Alan", data.child("users/user1234/displayName").getValue());
        assertEquals("user1234", data.child("conversations/convo123/owner/user1234/id").getValue());
    }
}
