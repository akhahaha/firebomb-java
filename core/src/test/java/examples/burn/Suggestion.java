package examples.burn;

import firebomb.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity
public class Suggestion {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm zzz";
    private static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    private String id;
    private Message message;
    private User suggester;
    private String text;
    private Date created;
    private List<User> upvotingUsers = new ArrayList<>();

    @Id
    @GeneratedValue
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(foreignIndexName = "suggestions")
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @ManyToOne(foreignIndexName = "suggestions")
    public User getSuggester() {
        return suggester;
    }

    public void setSuggester(User suggester) {
        this.suggester = suggester;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Ignore
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Property("created")
    @NonNull
    public String getCreatedString() {
        if (created == null) {
            return null;
        }
        return SDF.format(created);
    }

    public void setCreatedString(String createdString) {
        try {
            this.created = SDF.parse(createdString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public int getScore() {
        return upvotingUsers.size();
    }

    public boolean hasVote(User user) {
        return upvotingUsers.contains(user);
    }

    public void setVote(User user, boolean isUpVote) {
        if (isUpVote) {
            upvotingUsers.add(user);
        } else {
            upvotingUsers.remove(user);
        }
    }

    @ManyToMany(foreignIndexName = "suggestionUpvotes")
    public List<User> getUpvotingUsers() {
        return upvotingUsers;
    }

    public void setUpvotingUsers(List<User> upvotingUsers) {
        this.upvotingUsers = upvotingUsers;
    }
}
