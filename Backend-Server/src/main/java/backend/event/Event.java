package backend.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class Event extends ApplicationEvent {
    private String message;
    private String type;

    public Event(Object source, String message, String type) {
        super(source);
        this.type = type;
        this.message = message;
    }
}
