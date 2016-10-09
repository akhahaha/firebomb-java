package examples.burn;

import firebomb.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    private String id;
    private String displayName;
    private String email;
    private List<Conversation> conversations = new ArrayList<>();
    private List<Suggestion> suggestions = new ArrayList<>();
    private List<Conversation> conversationUpvotes = new ArrayList<>();
    private List<Suggestion> suggestionUpvotes = new ArrayList<>();

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @OneToMany(foreignFieldName = "owner")
    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    @OneToMany(foreignFieldName = "suggester")
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

    @ManyToMany(foreignIndexName = "upvotingUsers")
    public List<Conversation> getConversationUpvotes() {
        return conversationUpvotes;
    }

    public void setConversationUpvotes(List<Conversation> conversationUpvotes) {
        this.conversationUpvotes = conversationUpvotes;
    }

    @ManyToMany(foreignIndexName = "upvotingUsers")
    public List<Suggestion> getSuggestionUpvotes() {
        return suggestionUpvotes;
    }

    public void setSuggestionUpvotes(List<Suggestion> suggestionUpvotes) {
        this.suggestionUpvotes = suggestionUpvotes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id != null ? id.equals(user.id) : user.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
