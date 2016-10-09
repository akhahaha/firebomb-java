package examples.burn;

import firebomb.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Message {
    private String id;
    private Conversation conversation;
    private int index;
    private boolean isContext = true;
    private boolean isPrimary = true;
    private String text;
    private String imgUrl;
    private String selectedSuggestionId;
    private List<Suggestion> suggestions = new ArrayList<>();

    @Id
    @GeneratedValue
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(foreignIndexName = "messages")
    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    @NonNull
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @NonNull
    public boolean isContext() {
        return isContext;
    }

    public void setContext(boolean context) {
        isContext = context;
    }

    @NonNull
    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSelectedSuggestionId() {
        return selectedSuggestionId;
    }

    public void setSelectedSuggestionId(String selectedSuggestionId) {
        this.selectedSuggestionId = selectedSuggestionId;
    }

    @OneToMany(foreignFieldName = "suggestions")
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }
}
