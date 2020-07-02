
package com.ibm.energyoptimizer.PojoClasses;


public class List {

    private Integer dt;
    private Main main;
    private java.util.List<Weather_> weather = null;
    private Clouds clouds;
    private Wind wind;
    private Sys sys;
    private String dtTxt;
    private Rain rain;

    /**
     * No args constructor for use in serialization
     * 
     */
    public List() {
    }

    /**
     * 
     * @param dt
     * @param rain
     * @param dtTxt
     * @param weather
     * @param main
     * @param clouds
     * @param sys
     * @param wind
     */
    public List(Integer dt, Main main, java.util.List<Weather_> weather, Clouds clouds, Wind wind, Sys sys, String dtTxt, Rain rain) {
        super();
        this.dt = dt;
        this.main = main;
        this.weather = weather;
        this.clouds = clouds;
        this.wind = wind;
        this.sys = sys;
        this.dtTxt = dtTxt;
        this.rain = rain;
    }

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public java.util.List<Weather_> getWeather() {
        return weather;
    }

    public void setWeather(java.util.List<Weather_> weather) {
        this.weather = weather;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public String getDtTxt() {
        return dtTxt;
    }

    public void setDtTxt(String dtTxt) {
        this.dtTxt = dtTxt;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

}
