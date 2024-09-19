
public class TaskResponse {

    private Double elevation;
    private Double latitude;
    private Double longitude;
    private Double generationtime_ms;
    private String timezone;
    private String timezone_abbreviation;
    private Integer utc_offset_seconds;

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getGenerationtime_ms() {
        return generationtime_ms;
    }

    public void setGenerationtime_ms(Double generationtime_ms) {
        this.generationtime_ms = generationtime_ms;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone_abbreviation() {
        return timezone_abbreviation;
    }

    public void setTimezone_abbreviation(String timezone_abbreviation) {
        this.timezone_abbreviation = timezone_abbreviation;
    }

    public Integer getUtc_offset_seconds() {
        return utc_offset_seconds;
    }

    public void setUtc_offset_seconds(Integer utc_offset_seconds) {
        this.utc_offset_seconds = utc_offset_seconds;
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "elevation=" + elevation +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", generationtime_ms=" + generationtime_ms +
                ", timezone='" + timezone + '\'' +
                ", timezone_abbreviation='" + timezone_abbreviation + '\'' +
                ", utc_offset_seconds=" + utc_offset_seconds +
                '}';
    }
}
