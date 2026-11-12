package Simulation;

import java.util.Random;

import Terrain.Grass;
import Terrain.River;
import Terrain.Terrain;
import Terrain.Tree;
import Terrain.Vegetation;
import Weather.WeatherManager;

public class ForestFireSimulation {
    private Grid<Cell> grid;
    private Grid<Float> heatMap;
    private float ignitionThreshold;
    private WeatherManager weatherManager = new WeatherManager();

    public ForestFireSimulation(int rows, int columns, Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }

        grid = new Grid<Cell>(rows, columns);
        initializeGrid(random);

        heatMap = new Grid<Float>(rows, columns);
        heatMap.fill(0f);

        ignitionThreshold = 100f;
    }

    private void initializeGrid(Random random) {
        grid.stream().forEachOrdered(c -> {
            int terrainNumber = random.nextInt(100);
            Terrain terrain;

            if (terrainNumber < 60) {
                int age = random.nextInt(11);
                int height = 3 + random.nextInt(8);
                int fuel = 40 + random.nextInt(30);
                int burnRate = 2 + random.nextInt(4);
                float moisture = 0.3f + random.nextFloat() * 0.3f;

                terrain = new Tree(age, fuel, burnRate, moisture, height);

            } else if (terrainNumber < 90) {
                int age = random.nextInt(11);
                int density = 1 + random.nextInt(4);
                int fuel = 15 + random.nextInt(15);
                int burnRate = 4 + random.nextInt(3);
                float moisture = 0.1f + random.nextFloat() * 0.2f;

                terrain = new Grass(age, fuel, burnRate, moisture, density);

            } else {
                float coolingStrength = 0.1f + random.nextFloat() * 0.2f;
                terrain = new River(coolingStrength);
            }

            grid.setCell(c.x, c.y, new Cell(terrain));
        });
    }

    public Grid<Cell> getGrid() {
        return grid;
    }

    public WeatherManager getWeatherManager() {
        return this.weatherManager;
    }

    public float getIgnitionThreshold() {
        return ignitionThreshold;
    }

    public void igniteCell(int row, int column, int intensity) {
        if (intensity <= 0) {
            throw new IllegalArgumentException(
                    "Fire intensity must be greater than zero");
        }

        Cell cell = grid.getCell(row, column);

        if (cell == null) {
            throw new IllegalStateException(
                    "The selected Grid position has no this Cell coordinate");
        }

        cell.ignite(intensity);
    }

    public void addHeat(int row, int column, float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Heat amount cannot be negative");
        }

        float currentHeat = heatMap.getCell(row, column);

        heatMap.setCell(row, column, currentHeat + amount);
    }

    public void removeHeat(int row, int column, float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Heat amount cannot be negative");
        }

        float currentHeat = heatMap.getCell(row, column);
        float newHeat = Math.max(0f, currentHeat - amount);

        heatMap.setCell(row, column, newHeat);
    }

    public boolean hasReachedIgnitionThreshold(int row, int column) {
        return heatMap.getCell(row, column) >= ignitionThreshold;
    }

    public void update() {
        weatherManager.update(this);
        spreadFires();
        applyRiverCooling();
        updateCells();
        igniteHeatedCells();
        evolveTerrain();
    }

    public void addWind(int direction, int strength) {
        grid.stream().parallel().forEach(c -> {
            if (c.value.isBurning()) {
                switch (direction) {
                    // East
                    case 1:
                        if (!(c.y + 1 > grid.getColumns()))
                            addHeat(c.x, c.y + 1, strength);
                        break;

                    // South
                    case 2:
                        if (!(c.x + 1 > grid.getRows()))
                            addHeat(c.x + 1, c.y, strength);
                        break;

                    // West
                    case 3:
                        if (!(c.y - 1 < 0))
                            addHeat(c.x, c.y - 1, strength);
                        break;

                    // North
                    default:
                        if (!(c.x - 1 < 0))
                            addHeat(c.x - 1, c.y, strength);
                        break;
                }
            }
        });
    }

    private void igniteHeatedCells() {
        grid.stream().parallel().filter(f -> ((hasReachedIgnitionThreshold(f.x, f.y)) &&
                (!f.value.isBurning()))).forEach(c -> {
                    if (!c.value.canBurn()) {
                        heatMap.setCell(c.x, c.y, 0f);
                    } else {
                        // System.out.println("Ignition logic fired");
                        int intensity = (int) Math.ceil(ignitionThreshold / 10f);

                        c.value.ignite(intensity);

                        if (c.value.isBurning()) {
                            removeHeat(c.x, c.y, ignitionThreshold);
                        }
                    }
                });
    }

    private void updateCells() {
        grid.stream().parallel().forEach(c -> {
            c.value.updateBurningState();
        });
    }

    private void spreadFires() {
        grid.stream().parallel().filter(f -> f.value.isBurning()).forEach(c -> {
            Cell sourceCell = c.value;

            try {
                int fireIntensity = sourceCell.getFire().getIntensity();

                float spreadHeat = fireIntensity;
                Terrain terrain = sourceCell.getTerrain();

                if (terrain instanceof Vegetation) {
                    Vegetation vegetation = (Vegetation) terrain;

                    spreadHeat = vegetation.calculateSpreadHeat(fireIntensity);
                }

                for (Grid.Position neighbour : this.grid.neighbourPositions(c.x, c.y)) {

                    addHeat(neighbour.getRow(), neighbour.getColumn(), spreadHeat);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Can't burn on cell (" + c.x + ", " + c.y + ")");
            }
        });
    }

    private void applyRiverCooling() {
        grid.stream().parallel().filter(f -> f.value.getTerrain() instanceof River).forEach(c -> {
            float cooling = ((River) c.value.getTerrain()).getCoolingStrength();

            removeHeat(c.x, c.y, cooling);
            for (Grid.Position neighbour : grid.neighbourPositions(c.x, c.y)) {
                removeHeat(neighbour.getRow(), neighbour.getColumn(), cooling / 2.0f);
            }
        });
    }

    private void evolveTerrain() {
        grid.stream().filter(f -> (f.value != null) &&
                !f.value.isBurning() &&
                (f.value.getTerrain() != null))
                .forEach(c -> {
                    c.value.getTerrain().update();
                });
    }

    public int[][] getRandomPositions() {
        int[][] positions = new int[(int) (Math.random()
                * (grid.getRows() > grid.getColumns() ? grid.getRows() : grid.getColumns())) + 1][2];
        for (int i = 0; i < positions.length; i++) {
            positions[i][0] = ((int) (Math.random() * grid.getRows()));
            positions[i][1] = ((int) (Math.random() * grid.getColumns()));
        }
        return positions;
    }
}
