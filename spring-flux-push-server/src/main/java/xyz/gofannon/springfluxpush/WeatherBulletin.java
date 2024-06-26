package xyz.gofannon.springfluxpush;


import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "weather-bulletin")
@XmlAccessorType(XmlAccessType.FIELD)
public class WeatherBulletin {

    @XmlAttribute(name = "id")
    private long id;

    @XmlValue
    private String overview;

    public WeatherBulletin() {}

    public WeatherBulletin(long id, String overview) {
        this.id = id;
        this.overview = overview;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
