import java.util.ArrayList;

public class WeatherManager {
    private ArrayList<Weather> weatherList = new ArrayList<>();

    public boolean isActive(Weather w) {
        if (w.getDuration() <= 0) {
            return false;
        }

        return true;
    }

    public void createWeather(String weather) {
        Weather w = null;

        switch (weather) {
            case "Rain":
                w = new Rain(8, 20, null);
                break;
            case "Heatwave":
                w = new Heatwave(2, 20, null);
                break;
            case "Lightning":
                w = new Lightning(10, 1, null, 3);
                break;
            default:
                break;
        }

        if (w != null) {
            weatherList.add(w);
        }
    }

    public void createWeather(String weather, String selectedDirection) {
        Weather w = null;
        int direction = 0;

        switch (selectedDirection) {
            case "East":
                direction = 1;
                break;
            case "South":
                direction = 2;
                break;
            case "West":
                direction = 3;
                break;
            default:
                break;
        }

        w = new Wind(8, 20, null, direction);

        if (w != null) {
            weatherList.add(w);
        }
    }

    public void update(ForestFireSimulation simulation) {
        weatherList.forEach(Weather::decrementDuration);
        // Iterator<Weather> it = weatherList.iterator();
        // while (it.hasNext()) {
        //     Weather w = it.next();
        //     advanceDecrement(w);

        weatherList.removeIf(w -> w.getDuration() <= 0);
        //     if (!isActive(w)) {
        //         it.remove();
        //     } else {
        weatherList.forEach(w -> w.affectSimulation(simulation));
        //         w.affectSimulation(simulation);
        //     }
        // }
    }
}
