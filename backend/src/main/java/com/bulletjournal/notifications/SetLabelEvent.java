package com.bulletjournal.notifications;

import com.bulletjournal.contents.ContentType;

import java.util.List;

public class SetLabelEvent extends Informed {

    private final ContentType contentType;

    public SetLabelEvent(List<Event> events, String originator, ContentType contentType) {
        super(events, originator);
        this.contentType = contentType;
    }

    @Override
    public ContentType getContentType() {
        return this.contentType;
    }

    @Override
    protected String getEventTitle(Event event) {
        return "##" + this.getOriginator() + "## updated labels for " + this.contentType.name() + " ##" + event.getContentName() + "##";
    }

    @Override
    public String getLink(Long contentId) {
        return String.format("/" + this.contentType.name().toLowerCase() + "/%d", contentId);
    }
}
