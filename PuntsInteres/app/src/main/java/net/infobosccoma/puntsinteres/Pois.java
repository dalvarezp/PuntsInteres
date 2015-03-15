package net.infobosccoma.puntsinteres;

/**
 * Created by PC on 10/03/2015.
 * Classe per obtenir les dades del servidor en format Json
 * Els atributs d'aquesta classe han de coincidir exactament amb els del Json
 */
public class Pois {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private String city;

    public Pois(int id, String name, double latitude, double longitude, String city) {
        this.setId(id);
        this.setName(name);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setCity(city);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
