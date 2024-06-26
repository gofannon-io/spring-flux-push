package xyz.gofannon.springfluxpush;


import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class Message {

    @XmlValue
    private String content;

    public Message() {}

    public Message(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
